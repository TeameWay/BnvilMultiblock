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
import com.teameway.bnvilmb.multiblock.pattern.BlockPattern;
import com.teameway.bnvilmb.multiblock.pattern.PatternError;
import com.teameway.bnvilmb.multiblock.pattern.PatternState;
import com.teameway.bnvilmb.multiblock.port.MultiblockPort;
import com.teameway.bnvilmb.multiblock.port.MultiblockPortTable;
import com.teameway.bnvilmb.multiblock.port.PortKind;
import com.teameway.bnvilmb.multiblock.port.PortRef;
import com.teameway.bnvilmb.multiblock.structure.StructureManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 控制器方块实体。
 *
 * <p>对应 GTM 的 {@code MultiblockControllerMachine}。结构生命周期由 {@link StructureManager}
 * 驱动，这里负责结构回调、端口表、运行时和同步字段。结构模板是代码定义的 {@link BlockPattern}，
 * 没有数据包注册表。</p>
 */
public class ControllerBlockEntity extends BlockEntity implements ISyncPersistRPCBlockEntity {
    private static final int SAVE_INTERVAL = 20;
    private static final char SEPARATOR = '|';
    private static final String SEPARATOR_REGEX = "\\|";

    private final FieldManagedStorage syncStorage = new FieldManagedStorage(this);
    private final List<MachineJob> pendingJobs = new ArrayList<>();
    private @Nullable MachineRuntime runtime;
    private @Nullable PatternState structure;
    private Direction frontFacing = Direction.NORTH;
    private Direction upwardsFacing = Direction.UP;
    private boolean flipped;
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
    @DescSynced
    private String[] ports = new String[0];

    public ControllerBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public IManagedStorage getSyncStorage() {
        return this.syncStorage;
    }

    /**
     * @return 控制器方块对应的代码 pattern，未注册时为 {@code null}
     */
    public @Nullable BlockPattern pattern() {
        StructureController controller = StructureControllerRegistry.get(this.getBlockState().getBlock());
        return controller == null ? null : controller.pattern();
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
     * 服务端每 tick 调用。
     *
     * <p>结构检查由 {@link StructureManager} 的待检查队列驱动，这里只推进机器运行时。</p>
     *
     * @param level 服务端世界
     */
    public void serverTick(ServerLevel level) {
        if (this.structure == null) {
            this.structure = StructureManager.get(level).register(this);
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
     * 结构成型。
     *
     * @param state 结构状态
     */
    public void onStructureFormed(PatternState state) {
        if (!(this.level instanceof ServerLevel serverLevel)) {
            return;
        }
        this.form(serverLevel, state);
        StructureController controller = StructureControllerRegistry.get(this.getBlockState().getBlock());
        if (controller != null) {
            controller.onFormed(serverLevel, this, state);
        }
    }

    /**
     * 结构仍然合法但方块发生变化。
     *
     * @param state 结构状态
     */
    public void onStructureUpdated(PatternState state) {
        if (!(this.level instanceof ServerLevel serverLevel)) {
            return;
        }
        if (this.runtime == null) {
            this.form(serverLevel, state);
            return;
        }
        this.refreshPorts(this.portTable(serverLevel, state));
        this.clearStructureError();
    }

    /**
     * 端口提供量变化后重建端口表并重调运行时
     *
     * <p>线程仓或并行仓改数字时由端口方块实体调用。缩容丢弃的任务槽会按当前配方扣除一次输入，
     * 进度与输入都不返还。</p>
     */
    public void onPortSettingsChanged() {
        if (!(this.level instanceof ServerLevel serverLevel) || this.structure == null || this.runtime == null) {
            return;
        }
        MultiblockPortTable table = this.portTable(serverLevel, this.structure);
        this.refreshPorts(table);
        this.runtime.retune(table);
        this.refreshStatus();
        this.setChanged();
        this.sync(true);
    }

    /**
     * 结构解散。
     *
     * @param state 结构状态
     */
    public void onStructureUnformed(PatternState state) {
        if (this.level instanceof ServerLevel serverLevel) {
            StructureController controller = StructureControllerRegistry.get(this.getBlockState().getBlock());
            if (controller != null) {
                controller.onUnformed(serverLevel, this, state);
            }
        }
        this.unform();
    }

    /**
     * 结构不合法时记录首个问题。
     *
     * @param state 失败状态
     */
    public void onStructureInvalid(PatternState state) {
        String error = state.errors().stream()
            .findFirst()
            .map(PatternError::describe)
            .orElse("");
        if (!error.equals(this.structureError)) {
            BnvilMultiBlock.LOGGER.debug("Structure invalid at {}: {}", this.worldPosition, state.errors());
            this.structureError = error;
            this.setChanged();
            this.sync(true);
        }
    }

    /**
     * 建立机器运行时。
     *
     * @param level 服务端世界
     * @param state 结构状态
     */
    private void form(ServerLevel level, PatternState state) {
        BlockPattern pattern = state.pattern();
        Identifier typeId = this.resolveType(pattern);
        if (typeId == null) {
            this.structureError = "No matching multiblock type variant";
            BnvilMultiBlock.LOGGER.warn("Multiblock at {} has no matching type variant; controller stays idle", this.worldPosition);
            this.sync(true);
            return;
        }
        MultiblockPortTable table = this.portTable(level, state);
        this.refreshPorts(table);
        MachineRuntime created = new MachineRuntime(typeId, pattern);
        created.formed(table);
        if (!this.pendingJobs.isEmpty()) {
            created.restore(this.pendingJobs);
            this.pendingJobs.clear();
        }
        this.runtime = created;
        this.machineType = typeId.toString();
        this.clearStructureError();
        this.refreshStatus();
        this.setChanged();
        this.sync(true);
    }

    private @Nullable Identifier resolveType(BlockPattern pattern) {
        Registry<MultiblockType> types = BnvilRegistries.types();
        for (MultiblockType type : types) {
            Identifier id = types.getKey(type);
            if (id != null && pattern.variants().contains(id)) {
                return id;
            }
        }
        return null;
    }

    private MultiblockPortTable portTable(ServerLevel level, PatternState state) {
        List<MultiblockPort> ports = new ArrayList<>();
        for (BlockPos pos : state.portPositions()) {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity instanceof MultiblockPort port) {
                ports.add(port);
            }
        }
        return MultiblockPortTable.of(ports, state.parallelBonus(), state.threadBonus());
    }

    private void clearStructureError() {
        if (!this.structureError.isEmpty()) {
            this.structureError = "";
            this.setChanged();
            this.sync(true);
        }
    }

    /**
     * 清掉运行时并回到未成型状态。
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

    public @Nullable MachineRuntime runtime() {
        return this.runtime;
    }

    /**
     * 重写端口摘要。
     */
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
        if (Arrays.equals(this.ports, next)) {
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

    /**
     * 读取一个端口的界面摘要
     *
     * @param index 端口序号
     * @return 端口摘要
     */
    public PortRef portRefAt(int index) {
        PortEntry entry = this.portAt(index);
        return new PortRef(PortKind.byName(entry.kind()), entry.tier(), entry.pos());
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

    public Direction frontFacing() {
        return this.frontFacing;
    }

    public Direction upwardsFacing() {
        return this.upwardsFacing;
    }

    public boolean flipped() {
        return this.flipped;
    }

    public Direction structureFacing() {
        return this.frontFacing;
    }

    public void setFrontFacing(Direction frontFacing) {
        this.frontFacing = frontFacing;
        this.upwardsFacing = normalizeUp(frontFacing, this.upwardsFacing);
        this.updateStructureOrientation();
    }

    public void setUpwardsFacing(Direction upwardsFacing) {
        this.upwardsFacing = normalizeUp(this.frontFacing, upwardsFacing);
        this.updateStructureOrientation();
    }

    public void setFlipped(boolean flipped) {
        this.flipped = flipped;
        this.updateStructureOrientation();
    }

    public void setStructureFacing(Direction facing) {
        this.setFrontFacing(facing);
    }

    private static Direction normalizeUp(Direction front, Direction up) {
        if (up.getAxis() != front.getAxis()) {
            return up;
        }
        return front.getAxis() == Direction.Axis.Y ? Direction.NORTH : Direction.UP;
    }

    private void updateStructureOrientation() {
        if (this.level instanceof ServerLevel serverLevel && this.structure != null) {
            StructureManager.get(serverLevel).updateOrientation(this.structure);
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
        String front = input.getStringOr("front_facing", input.getStringOr("facing", Direction.NORTH.getSerializedName()));
        Direction savedFront = Direction.byName(front);
        this.frontFacing = savedFront == null ? Direction.NORTH : savedFront;
        String up = input.getStringOr("upwards_facing", Direction.UP.getSerializedName());
        Direction savedUp = Direction.byName(up);
        this.upwardsFacing = normalizeUp(this.frontFacing, savedUp == null ? Direction.UP : savedUp);
        this.flipped = input.getBooleanOr("flipped", false);

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
        output.putString("front_facing", this.frontFacing.getSerializedName());
        output.putString("upwards_facing", this.upwardsFacing.getSerializedName());
        output.putBoolean("flipped", this.flipped);
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