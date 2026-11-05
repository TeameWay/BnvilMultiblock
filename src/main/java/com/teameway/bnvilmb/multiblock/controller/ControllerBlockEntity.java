package com.teameway.bnvilmb.multiblock.controller;

import com.lowdragmc.lowdraglib2.syncdata.annotation.DescSynced;
import com.lowdragmc.lowdraglib2.syncdata.holder.blockentity.ISyncPersistRPCBlockEntity;
import com.lowdragmc.lowdraglib2.syncdata.storage.FieldManagedStorage;
import com.lowdragmc.lowdraglib2.syncdata.storage.IManagedStorage;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.teameway.bnvilmb.BnvilMultiBlock;
import com.teameway.bnvilmb.init.BnvilRegistries;
import com.teameway.bnvilmb.multiblock.MultiblockType;
import com.teameway.bnvilmb.multiblock.StructureController;
import com.teameway.bnvilmb.multiblock.StructureControllerRegistry;
import com.teameway.bnvilmb.multiblock.machine.MachineJob;
import com.teameway.bnvilmb.multiblock.machine.MachineRuntime;
import com.teameway.bnvilmb.multiblock.port.MultiblockPort;
import com.teameway.bnvilmb.multiblock.port.MultiblockPortScanner;
import com.teameway.bnvilmb.multiblock.port.MultiblockPortTable;
import com.teameway.bnvilmb.multiblock.port.PortKind;
import com.teameway.bnvilmb.multiblock.structure.StructureInstance;
import com.teameway.bnvilmb.multiblock.structure.StructureManager;
import com.teameway.bnvilmb.multiblock.structure.StructureMatch;
import com.teameway.bnvilmb.multiblock.structure.StructurePattern;
import com.teameway.bnvilmb.multiblock.structure.StructureProblem;
import dev.anvilcraft.lib.v2.registrum.util.entry.RegistryEntry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 控制器方块实体
 *
 * <p>成型判定由 {@link StructureManager} 驱动；这里只负责结构生命周期、端口扫描与机器运行时。
 * 线程状态只同步进度、耗时、倍数与配方 ID，不传输任务对象。</p>
 */
public class ControllerBlockEntity extends BlockEntity implements ISyncPersistRPCBlockEntity {
    private static final int SAVE_INTERVAL = 20;
    /**
     * 端口摘要各段之间的分隔符
     *
     * <p>坐标内部已用逗号分三段，这里必须换成竖线，否则解析无法按位次拆分。</p>
     */
    private static final char SEPARATOR = '|';
    /**
     * 解析端口摘要用的分隔正则
     *
     * <p>{@link String#split(String)} 接收正则，竖线在正则中表示分支，直接传 {@link #SEPARATOR} 会按单字符切分。</p>
     */
    private static final String SEPARATOR_REGEX = "\\|";

    private final FieldManagedStorage syncStorage = new FieldManagedStorage(this);
    private final List<MachineJob> pendingJobs = new ArrayList<>();
    private @Nullable MachineRuntime runtime;
    private @Nullable StructureInstance structure;
    private Direction structureFacing = Direction.NORTH;
    private int saveCounter;

    @DescSynced
    private int threadSlots;
    @DescSynced
    private int parallelSlots;
    @DescSynced
    private int[] threadProgress = new int[0];
    @DescSynced
    private int[] threadDuration = new int[0];
    @DescSynced
    private int[] threadBatch = new int[0];
    @DescSynced
    private String[] threadRecipe = new String[0];
    @DescSynced
    private String machineType = "";
    @DescSynced
    private String variantText = "";
    @DescSynced
    private String structureError = "";
    /**
     * 结构里所有端口的摘要，一项一个端口
     *
     * <p>格式为 {@code 种类|等级|x,y,z}，只在成型与解散时更新。</p>
     */
    @DescSynced
    private String[] ports = new String[0];

    public ControllerBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public IManagedStorage getSyncStorage() {
        return this.syncStorage;
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (this.level instanceof ServerLevel serverLevel) {
            this.structure = StructureManager.get(serverLevel).register(this);
        }
    }

    @Override
    public void setRemoved() {
        if (this.level instanceof ServerLevel serverLevel && this.structure != null) {
            StructureManager.get(serverLevel).unregister(this.structure, true);
            this.structure = null;
        }
        super.setRemoved();
    }

    @Override
    public void onChunkUnloaded() {
        if (this.level instanceof ServerLevel serverLevel && this.structure != null) {
            StructureManager.get(serverLevel).unregister(this.structure, false);
            this.structure = null;
        }
        super.onChunkUnloaded();
    }

    /**
     * 服务端每 tick 调用
     *
     * @param level 服务端世界
     */
    public void serverTick(ServerLevel level) {
        if (this.structure == null) {
            this.structure = StructureManager.get(level).register(this);
        }
        if (this.structure != null) {
            StructureManager.get(level).tick(this.structure);
        }
        MachineRuntime current = this.runtime;
        if (current != null) {
            current.tick(level);
            this.refreshStatus();
            if (++this.saveCounter >= SAVE_INTERVAL) {
                this.saveCounter = 0;
                this.setChanged();
            }
        }
    }

    /**
     * 结构成型时建立运行时
     *
     * @param match 结构匹配结果
     */
    public void onStructureFormed(StructureMatch match) {
        if (!(this.level instanceof ServerLevel serverLevel) || this.structure == null) {
            return;
        }
        this.form(serverLevel, match);
        StructureController controller = StructureControllerRegistry.get(this.getBlockState().getBlock());
        if (controller != null) {
            controller.onFormed(serverLevel, this, match);
        }
    }

    /**
     * 结构仍然合法但方块发生变化
     *
     * @param match 结构匹配结果
     */
    public void onStructureUpdated(StructureMatch match) {
        if (!(this.level instanceof ServerLevel serverLevel) || this.structure == null) {
            return;
        }
        if (this.runtime == null) {
            this.form(serverLevel, match);
            return;
        }
        this.refreshPorts(MultiblockPortScanner.scan(serverLevel, match));
        this.clearStructureError();
    }

    /**
     * 结构解散
     *
     * @param match 最后一次匹配结果，可能为 {@code null}
     */
    public void onStructureUnformed(@Nullable StructureMatch match) {
        if (this.level instanceof ServerLevel serverLevel) {
            StructureController controller = StructureControllerRegistry.get(this.getBlockState().getBlock());
            if (controller != null) {
                controller.onUnformed(serverLevel, this);
            }
        }
        this.unform();
    }

    /**
     * 结构不合法
     *
     * @param match 失败结果
     */
    public void onStructureInvalid(StructureMatch match) {
        String error = match.firstProblem()
            .map(StructureProblem::describe)
            .orElse("");
        if (!error.equals(this.structureError)) {
            BnvilMultiBlock.LOGGER.debug("Structure invalid at {}: {}", this.worldPosition, match.problems());
            this.structureError = error;
            this.setChanged();
            this.sync(true);
        }
    }

    private void form(ServerLevel level, StructureMatch match) {
        StructureInstance instance = this.structure;
        if (instance == null) {
            return;
        }
        Holder.Reference<StructurePattern> holder = instance.patternHolder();
        RegistryEntry<MultiblockType, MultiblockType> type = this.resolveType(holder);
        if (type == null) {
            this.structureError = "No matching multiblock type tag";
            BnvilMultiBlock.LOGGER.warn("Multiblock at {} has no matching type tag; controller stays idle", this.worldPosition);
            this.sync(true);
            return;
        }
        MultiblockPortTable table = MultiblockPortScanner.scan(level, match);
        this.refreshPorts(table);
        MachineRuntime created = new MachineRuntime(type.getId(), holder);
        created.formed(table);
        if (!this.pendingJobs.isEmpty()) {
            created.restore(this.pendingJobs);
            this.pendingJobs.clear();
        }
        this.runtime = created;
        this.machineType = type.getId().toString();
        this.clearStructureError();
        this.refreshStatus();
        this.setChanged();
        this.sync(true);
    }

    private @Nullable RegistryEntry<MultiblockType, MultiblockType> resolveType(
        Holder.Reference<StructurePattern> pattern
    ) {
        for (RegistryEntry<MultiblockType, MultiblockType> candidate : BnvilMultiBlock.REGISTRUM.getAll(BnvilRegistries.MULTIBLOCK_TYPES)) {
            if (pattern.is(TagKey.create(BnvilRegistries.STRUCTURE_PATTERNS, candidate.getId()))) {
                return candidate;
            }
        }
        return null;
    }

    private void clearStructureError() {
        if (!this.structureError.isEmpty()) {
            this.structureError = "";
            this.setChanged();
            this.sync(true);
        }
    }

    /**
     * 结构解散时清理运行时
     */
    public void unform() {
        MachineRuntime current = this.runtime;
        if (current != null) {
            current.unformed();
            this.runtime = null;
            this.machineType = "";
            this.clearPorts();
            this.refreshStatus();
            this.setChanged();
            this.sync(true);
        }
    }

    /**
     * @return 机器运行时，未成型返回 {@code null}
     */
    public @Nullable MachineRuntime runtime() {
        return this.runtime;
    }

    private void refreshPorts(MultiblockPortTable table) {
        List<String> entries = new ArrayList<>();
        for (PortKind kind : PortKind.values()) {
            for (MultiblockPort port : table.ports(kind)) {
                if (port instanceof BlockEntity entity) {
                    BlockPos pos = entity.getBlockPos();
                    entries.add(kind.getSerializedName() + SEPARATOR + port.tier().name()
                                + SEPARATOR + pos.getX() + "," + pos.getY() + "," + pos.getZ());
                }
            }
        }
        String[] next = entries.toArray(String[]::new);
        if (java.util.Arrays.equals(this.ports, next)) {
            return;
        }
        this.ports = next;
        this.setChanged();
        this.sync(true);
    }

    private void clearPorts() {
        this.ports = new String[0];
    }

    public BlockPos portPosition(int index) {
        return this.portAt(index).pos();
    }

    public PortKind portKind(int index) {
        return PortKind.byName(this.portAt(index).kind());
    }

    public String portTier(int index) {
        return this.portAt(index).tier();
    }

    public int portCount() {
        return this.ports.length;
    }

    private PortEntry portAt(int index) {
        String raw = this.ports[index];
        String[] parts = raw.split(SEPARATOR_REGEX, 3);
        if (parts.length != 3) {
            return new PortEntry(PortKind.ITEM_INPUT.getSerializedName(), "", BlockPos.ZERO);
        }
        String[] coordinates = parts[2].split(",");
        if (coordinates.length != 3) {
            return new PortEntry(parts[0], parts[1], BlockPos.ZERO);
        }
        try {
            return new PortEntry(
                parts[0], parts[1], new BlockPos(
                Integer.parseInt(coordinates[0]),
                Integer.parseInt(coordinates[1]),
                Integer.parseInt(coordinates[2])
            )
            );
        } catch (NumberFormatException ignored) {
            return new PortEntry(parts[0], parts[1], BlockPos.ZERO);
        }
    }

    private record PortEntry(String kind, String tier, BlockPos pos) {
    }

    private void refreshStatus() {
        MachineRuntime current = this.runtime;
        List<MachineJob> jobs = current != null ? current.jobs() : List.of();
        int slots = jobs.size();
        if (this.threadProgress.length != slots) {
            this.threadProgress = new int[slots];
            this.threadDuration = new int[slots];
            this.threadBatch = new int[slots];
            this.threadRecipe = new String[slots];
        }
        for (int slot = 0; slot < slots; slot++) {
            MachineJob job = jobs.get(slot);
            if (job == null) {
                this.threadProgress[slot] = -1;
                this.threadDuration[slot] = 0;
                this.threadBatch[slot] = 0;
                this.threadRecipe[slot] = "";
            } else {
                this.threadProgress[slot] = job.progress();
                this.threadDuration[slot] = job.duration();
                this.threadBatch[slot] = job.batch();
                this.threadRecipe[slot] = job.recipeId().toString();
            }
        }
        this.threadSlots = slots;
        this.parallelSlots = current != null ? current.parallels() : 0;
        this.variantText = current == null
                           ? ""
                           : current.variants().stream().map(Identifier::toString).sorted().collect(Collectors.joining(", "));
    }

    public int threadSlots() {
        return this.threadSlots;
    }

    public int parallelSlots() {
        return this.parallelSlots;
    }

    public String machineType() {
        return this.machineType;
    }

    public String variantText() {
        return this.variantText;
    }

    public String structureError() {
        return this.structureError;
    }

    public Direction structureFacing() {
        return this.structureFacing;
    }

    /**
     * 设置控制器朝向；由方块放置或调试命令调用
     *
     * @param facing 新的水平正面
     */
    public void setStructureFacing(Direction facing) {
        Direction horizontal = facing.getAxis().isHorizontal() ? facing : Direction.NORTH;
        this.structureFacing = horizontal;
        if (this.level instanceof ServerLevel serverLevel && this.structure != null) {
            StructureManager.get(serverLevel).updateFacing(this.structure, horizontal);
        }
        this.setChanged();
    }

    public float progressAt(int slot) {
        if (slot < 0 || slot >= this.threadProgress.length) {
            return 0.0F;
        }
        int duration = this.threadDuration[slot];
        int progress = this.threadProgress[slot];
        if (duration <= 0 || progress <= 0) {
            return 0.0F;
        }
        return Math.min(1.0F, progress / (float) duration);
    }

    public String recipeIdAt(int slot) {
        if (slot < 0 || slot >= this.threadRecipe.length) {
            return "";
        }
        String recipe = this.threadRecipe[slot];
        return recipe == null ? "" : recipe;
    }

    public int batchAt(int slot) {
        return slot < 0 || slot >= this.threadBatch.length ? 0 : this.threadBatch[slot];
    }

    public int durationAt(int slot) {
        return slot < 0 || slot >= this.threadDuration.length ? 0 : this.threadDuration[slot];
    }

    public boolean working() {
        for (String recipe : this.threadRecipe) {
            if (recipe != null && !recipe.isEmpty()) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        Direction savedFacing = Direction.byName(input.getStringOr("facing", Direction.NORTH.getSerializedName()));
        this.structureFacing = savedFacing != null && savedFacing.getAxis().isHorizontal() ? savedFacing : Direction.NORTH;
        int maxSlot = -1;
        List<SavedJob> saved = new ArrayList<>();
        for (SavedJob entry : input.listOrEmpty("jobs", SavedJob.CODEC)) {
            saved.add(entry);
            maxSlot = Math.max(maxSlot, entry.slot());
        }
        if (maxSlot < 0) {
            return;
        }
        this.pendingJobs.clear();
        for (int slot = 0; slot <= maxSlot; slot++) {
            this.pendingJobs.add(null);
        }
        for (SavedJob entry : saved) {
            this.pendingJobs.set(entry.slot(), new MachineJob(entry.recipe(), entry.batch(), entry.progress()));
        }
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putString("facing", this.structureFacing.getSerializedName());
        List<MachineJob> jobs = this.runtime != null ? this.runtime.jobs() : this.pendingJobs;
        ValueOutput.TypedOutputList<SavedJob> list = output.list("jobs", SavedJob.CODEC);
        for (int slot = 0; slot < jobs.size(); slot++) {
            MachineJob job = jobs.get(slot);
            if (job != null) {
                list.add(new SavedJob(slot, job.recipeId(), job.batch(), job.progress()));
            }
        }
    }

    private record SavedJob(int slot, Identifier recipe, int batch, int progress) {
        private static final Codec<SavedJob> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.fieldOf("slot").forGetter(SavedJob::slot),
            Identifier.CODEC.fieldOf("recipe").forGetter(SavedJob::recipe),
            Codec.INT.optionalFieldOf("batch", 1).forGetter(SavedJob::batch),
            Codec.INT.optionalFieldOf("progress", 0).forGetter(SavedJob::progress)
        ).apply(instance, SavedJob::new));
    }
}
