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
 * <p>成型判定由 {@link StructureManager} 驱动，这里承担结构生命周期、端口扫描与机器运行时。
 * 线程数组只同步进度、耗时、倍数与配方 ID，任务对象留在服务端。</p>
 *
 * <p>区块加载后向 {@link StructureManager} 注册，方块移除或区块卸载时注销；匹配结果通过
 * {@code onStructure*} 回调进入，运行时、端口摘要与结构错误都在这里落地。</p>
 */
public class ControllerBlockEntity extends BlockEntity implements ISyncPersistRPCBlockEntity {
    /**
     * 运行时存在时每隔多少 tick 调用一次 {@code setChanged()}
     */
    private static final int SAVE_INTERVAL = 20;
    /**
     * 端口摘要各段之间的分隔符
     *
     * <p>坐标内部用逗号分成三段，这里必须换成竖线，否则解析无法按位次拆分。</p>
     */
    private static final char SEPARATOR = '|';
    /**
     * 解析端口摘要用的分隔正则
     *
     * <p>{@link String#split(String)} 接收正则，竖线在正则里表示分支，直接传
     * {@link #SEPARATOR} 会按单字符切分。</p>
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
     * <p>格式为 {@code 种类|等级|x,y,z}；成型与结构更新时整体重写，解散时清空。</p>
     */
    @DescSynced
    private String[] ports = new String[0];

    /**
     * @param type 方块实体类型
     * @param pos 方块位置
     * @param state 方块状态
     */
    public ControllerBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    /**
     * @return LDLib2 同步与持久化共用的字段存储
     */
    @Override
    public IManagedStorage getSyncStorage() {
        return this.syncStorage;
    }

    /**
     * 在服务端向 {@link StructureManager} 注册，区块重新加载后也由这里补回结构引用
     */
    @Override
    public void onLoad() {
        super.onLoad();
        if (this.level instanceof ServerLevel serverLevel) {
            this.structure = StructureManager.get(serverLevel).register(this);
        }
    }

    /**
     * 方块被移除时注销结构并清空引用
     */
    @Override
    public void setRemoved() {
        if (this.level instanceof ServerLevel serverLevel && this.structure != null) {
            StructureManager.get(serverLevel).unregister(this.structure, true);
            this.structure = null;
        }
        super.setRemoved();
    }

    /**
     * 区块卸载时注销结构并清空引用
     */
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
     * <p>结构引用为空时先补注册；运行时存在时推进运行时、刷新同步字段，并每
     * {@value #SAVE_INTERVAL} tick 标记一次存档。</p>
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
     * <p>先由 {@link #form} 建立 {@link MachineRuntime}，再回调
     * {@link StructureController#onFormed}。</p>
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
     * <p>运行时尚未建立时按成型处理；已建立时只重扫端口并清掉结构错误，不重建运行时。</p>
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
     * <p>先回调控制器的 {@link StructureController#onUnformed}，再清掉运行时与端口摘要。</p>
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
     * 结构不合法时记录首个问题
     *
     * <p>只有错误文本变化时才写字段、标记存档并同步，相同的错误不会反复刷同步。</p>
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

    /**
     * 按结构模板找到多方块类型，扫描端口并建立运行时
     *
     * <p>类型由 {@link #resolveType} 按结构模板上的标签反查；找不到时只记录错误并保持空闲。
     * 存档里待恢复的任务会在运行时建好后补回去。</p>
     */
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

    /**
     * 按结构模板上的标签反查多方块类型，标签由注册项 ID 生成
     */
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
     * 清掉运行时并回到未成型状态
     *
     * <p>运行时存在时才执行清理，包括机器类型、端口摘要与同步状态。</p>
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
     * @return 当前机器运行时，未成型时为 {@code null}
     */
    public @Nullable MachineRuntime runtime() {
        return this.runtime;
    }

    /**
     * 重写端口摘要
     *
     * <p>顺序按 {@code PortKind.values()} 分组，组内保持端口表的顺序；数组内容不变时不写字段。</p>
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

    /**
     * @param index 端口序号
     * @return 端口方块位置
     * @throws ArrayIndexOutOfBoundsException 序号越界
     */
    public BlockPos portPosition(int index) {
        return this.portAt(index).pos();
    }

    /**
     * @param index 端口序号
     * @return 端口种类
     * @throws ArrayIndexOutOfBoundsException 序号越界
     */
    public PortKind portKind(int index) {
        return PortKind.byName(this.portAt(index).kind());
    }

    /**
     * @param index 端口序号
     * @return 摘要中记录的端口等级文本
     * @throws ArrayIndexOutOfBoundsException 序号越界
     */
    public String portTier(int index) {
        return this.portAt(index).tier();
    }

    /**
     * @return 端口数量
     */
    public int portCount() {
        return this.ports.length;
    }

    /**
     * 解析一条端口摘要；任一段缺失或数字解析失败时用空坐标兜底
     */
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

    /**
     * 把运行时状态写进同步字段
     *
     * <p>数组长度变化时整体重建；空槽位的进度记为 -1，其余字段清零。</p>
     */
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

    /**
     * @return 当前同步的线程槽位数
     */
    public int threadSlots() {
        return this.threadSlots;
    }

    /**
     * @return 当前并行数，未成型时为 0
     */
    public int parallelSlots() {
        return this.parallelSlots;
    }

    /**
     * @return 机器类型的注册 ID 文本，未成型时为空串
     */
    public String machineType() {
        return this.machineType;
    }

    /**
     * @return 形态 ID 列表，以逗号加空格分隔，未成型时为空串
     */
    public String variantText() {
        return this.variantText;
    }

    /**
     * @return 最近一次结构错误文本，没有错误时为空串
     */
    public String structureError() {
        return this.structureError;
    }

    /**
     * @return 结构朝向，始终为水平方向
     */
    public Direction structureFacing() {
        return this.structureFacing;
    }

    /**
     * 设置控制器朝向；由方块放置或调试命令调用
     *
     * <p>传入非水平方向时退回 {@link Direction#NORTH}，已有结构会同步更新朝向。</p>
     *
     * @param facing 新的水平朝向
     */
    public void setStructureFacing(Direction facing) {
        Direction horizontal = facing.getAxis().isHorizontal() ? facing : Direction.NORTH;
        this.structureFacing = horizontal;
        if (this.level instanceof ServerLevel serverLevel && this.structure != null) {
            StructureManager.get(serverLevel).updateFacing(this.structure, horizontal);
        }
        this.setChanged();
    }

    /**
     * @param slot 线程序号
     * @return 0 到 1 的进度；序号越界或周期尚未开始时为 0
     */
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

    /**
     * @param slot 线程序号
     * @return 配方 ID 文本；序号越界或没有配方时为空串
     */
    public String recipeIdAt(int slot) {
        if (slot < 0 || slot >= this.threadRecipe.length) {
            return "";
        }
        String recipe = this.threadRecipe[slot];
        return recipe == null ? "" : recipe;
    }

    /**
     * @param slot 线程序号
     * @return 并行批次；序号越界时为 0
     */
    public int batchAt(int slot) {
        return slot < 0 || slot >= this.threadBatch.length ? 0 : this.threadBatch[slot];
    }

    /**
     * @param slot 线程序号
     * @return 周期长度，单位 tick；序号越界时为 0
     */
    public int durationAt(int slot) {
        return slot < 0 || slot >= this.threadDuration.length ? 0 : this.threadDuration[slot];
    }

    /**
     * @return 是否有线程正在跑配方
     */
    public boolean working() {
        for (String recipe : this.threadRecipe) {
            if (recipe != null && !recipe.isEmpty()) {
                return true;
            }
        }
        return false;
    }

    /**
     * 读取朝向与任务列表；任务先放进待恢复列表，等结构成型后再交给运行时
     */
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

    /**
     * 写入朝向与任务列表；运行时存在时存运行时任务，否则存待恢复任务
     */
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
