package com.teameway.bnvilmb.multiblock.structure;

import com.teameway.bnvilmb.BnvilMultiBlock;
import com.teameway.bnvilmb.init.BnvilRegistries;
import com.teameway.bnvilmb.multiblock.StructureController;
import com.teameway.bnvilmb.multiblock.StructureControllerRegistry;
import com.teameway.bnvilmb.multiblock.controller.ControllerBlockEntity;
import com.teameway.bnvilmb.multiblock.port.MultiblockPort;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

/**
 * 每个服务端世界的结构管理器
 *
 * <p>维护控制器到 {@link StructureInstance} 的映射，以及方块位置、区块到控制器的反向索引。
 * 事件只负责标脏，控制器方块实体在自己的 tick 里调用 {@link #tick(StructureInstance)}，
 * 避免在破坏事件中读到尚未被移除的方块。</p>
 */
public final class StructureManager {
    /** 没有脏标记时强制重新判定的 tick 周期 */
    private static final int PERIODIC_TICKS = 20;
    /** 世界到管理器的缓存，世界卸载时由 {@link #remove(ServerLevel)} 清理 */
    private static final Map<ServerLevel, StructureManager> MANAGERS = new WeakHashMap<>();

    private final ServerLevel level;
    /** 控制器位置到结构实例 */
    private final Map<BlockPos, StructureInstance> instances = new HashMap<>();
    /** 方块位置压缩值到覆盖它的控制器位置集合 */
    private final Map<Long, Set<BlockPos>> positionIndex = new HashMap<>();
    /** 区块到覆盖它的控制器位置集合 */
    private final Map<ChunkPos, Set<BlockPos>> chunkIndex = new HashMap<>();

    private StructureManager(ServerLevel level) {
        this.level = level;
    }

    /**
     * 取得世界的管理器，不存在时创建
     *
     * <p>按世界缓存，访问时对缓存加锁。世界卸载时由 {@link #remove(ServerLevel)} 丢弃。</p>
     *
     * @param level 服务端世界
     * @return 该世界的管理器
     */
    public static StructureManager get(ServerLevel level) {
        synchronized (MANAGERS) {
            return MANAGERS.computeIfAbsent(level, StructureManager::new);
        }
    }

    /**
     * 丢弃世界的管理器
     *
     * @param level 服务端世界
     */
    public static void remove(ServerLevel level) {
        synchronized (MANAGERS) {
            MANAGERS.remove(level);
        }
    }

    /**
     * 为控制器登记结构实例
     *
     * <p>同一位置已有实例时直接返回。控制器方块未在注册表登记，或数据包中缺少对应模板时，
     * 记一条警告并返回 {@code null}。</p>
     *
     * @param controller 控制器方块实体
     * @return 结构实例，失败时为 {@code null}
     */
    public @Nullable StructureInstance register(ControllerBlockEntity controller) {
        BlockPos pos = controller.getBlockPos();
        StructureInstance existing = this.instances.get(pos);
        if (existing != null) {
            return existing;
        }
        StructureController structureController = StructureControllerRegistry.get(controller.getBlockState().getBlock());
        if (structureController == null) {
            BnvilMultiBlock.LOGGER.warn("No structure controller registered for block {}", controller.getBlockState().getBlock());
            return null;
        }
        ResourceKey<StructurePattern> key = StructureInstance.key(structureController);
        Holder.Reference<StructurePattern> holder = this.level.registryAccess()
            .lookupOrThrow(BnvilRegistries.STRUCTURE_PATTERNS)
            .get(key)
            .orElse(null);
        if (holder == null) {
            BnvilMultiBlock.LOGGER.warn("Missing structure pattern {}", key.identifier());
            return null;
        }
        StructureInstance instance = new StructureInstance(controller, key, holder, controller.structureFacing());
        this.instances.put(pos, instance);
        this.index(instance);
        return instance;
    }

    /**
     * 注销结构实例并从索引中移除
     *
     * <p>{@code unform} 为 {@code true} 且实例仍处于成型状态时，先清空端口认领，再把最近一次
     * 匹配结果交给 {@code ControllerBlockEntity#onStructureUnformed}。区块卸载时传 {@code false}，
     * 只移除索引与实例，不触发解散回调。</p>
     *
     * @param instance 结构实例，{@code null} 时直接返回
     * @param unform 是否按解散处理
     */
    public void unregister(@Nullable StructureInstance instance, boolean unform) {
        if (instance == null) {
            return;
        }
        this.unindex(instance);
        this.instances.remove(instance.controllerPos(), instance);
        if (unform && instance.formed()) {
            instance.setFormed(false);
            this.releaseClaims(instance);
            instance.controller().onStructureUnformed(instance.lastMatch());
        }
    }

    /**
     * 更新控制器朝向并重建索引
     *
     * <p>朝向未变化时不做任何事。变化后先移除旧索引，重算世界坐标，再按新坐标重新索引，
     * 并把实例标脏。</p>
     *
     * @param instance 结构实例
     * @param facing 新的控制器正面
     */
    public void updateFacing(StructureInstance instance, Direction facing) {
        if (instance.facing() == facing) {
            return;
        }
        this.unindex(instance);
        instance.setFacing(facing);
        this.index(instance);
        instance.setDirty(true);
    }

    /**
     * 标脏覆盖该位置的控制器
     *
     * <p>没有控制器覆盖该位置时直接返回。</p>
     *
     * @param pos 发生方块变化的位置
     */
    public void markDirty(BlockPos pos) {
        Set<BlockPos> controllers = this.positionIndex.get(pos.asLong());
        if (controllers == null) {
            return;
        }
        for (BlockPos controllerPos : controllers) {
            StructureInstance instance = this.instances.get(controllerPos);
            if (instance != null) {
                instance.setDirty(true);
            }
        }
    }

    /**
     * 标脏覆盖该区块的控制器
     *
     * @param pos 区块坐标
     */
    public void markDirtyChunk(ChunkPos pos) {
        Set<BlockPos> controllers = this.chunkIndex.get(pos);
        if (controllers == null) {
            return;
        }
        for (BlockPos controllerPos : controllers) {
            StructureInstance instance = this.instances.get(controllerPos);
            if (instance != null) {
                instance.setDirty(true);
            }
        }
    }

    /**
     * 推进一个结构实例的判定
     *
     * <p>控制器被移除时注销并按解散处理。实例不脏且空闲未满 {@code PERIODIC_TICKS} 时跳过；
     * 结构范围有区块未加载时挂起并清掉脏标记。其余情况调用 {@link StructureMatcher#match}：
     * 成型时同步端口认领，首次成型回调 {@code onStructureFormed}，已成型则回调
     * {@code onStructureUpdated}；未成型时若此前已成型则解散，随后报告失败结果。结束后清除
     * 暂停、脏标记与空闲计数，并保存本次匹配结果。</p>
     *
     * @param instance 控制器对应的结构实例
     */
    public void tick(StructureInstance instance) {
        if (instance.controller().isRemoved()) {
            this.unregister(instance, true);
            return;
        }
        instance.setIdleTicks(instance.idleTicks() + 1);
        if (!instance.dirty() && instance.idleTicks() < PERIODIC_TICKS) {
            return;
        }
        if (!this.allLoaded(instance)) {
            instance.setSuspended(true);
            instance.setDirty(false);
            instance.setIdleTicks(0);
            return;
        }

        StructureMatch match = StructureMatcher.match(
            this.level, instance.controllerPos(), instance.facing(), instance.pattern()
        );
        if (match.unloaded()) {
            instance.setSuspended(true);
            instance.setDirty(false);
            instance.setIdleTicks(0);
            return;
        }
        if (match.formed()) {
            this.syncClaims(instance, match);
            if (!instance.formed()) {
                instance.setFormed(true);
                instance.controller().onStructureFormed(match);
            } else {
                instance.controller().onStructureUpdated(match);
            }
        } else {
            if (instance.formed()) {
                instance.setFormed(false);
                this.releaseClaims(instance);
                instance.controller().onStructureUnformed(instance.lastMatch());
            }
            instance.controller().onStructureInvalid(match);
        }
        instance.setSuspended(false);
        instance.setLastMatch(match);
        instance.setDirty(false);
        instance.setIdleTicks(0);
    }

    private boolean allLoaded(StructureInstance instance) {
        for (BlockPos pos : instance.positions()) {
            if (!this.level.isLoaded(pos)) {
                return false;
            }
        }
        return true;
    }

    /**
     * 让实例的端口认领与本次匹配识别出的端口一致
     *
     * <p>先释放本次匹配里不再出现的端口，再尝试认领新端口；认领失败的端口不会记入实例。</p>
     */
    private void syncClaims(StructureInstance instance, StructureMatch match) {
        Set<BlockPos> newPorts = new LinkedHashSet<>(match.portPositions());
        for (BlockPos pos : new ArrayList<>(instance.claimedPorts())) {
            if (!newPorts.contains(pos)) {
                this.setClaim(pos, null);
                instance.claimedPorts().remove(pos);
            }
        }
        for (BlockPos pos : newPorts) {
            if (this.setClaim(pos, instance.controllerPos())) {
                instance.claimedPorts().add(pos);
            }
        }
    }

    /** 释放实例认领的全部端口 */
    private void releaseClaims(StructureInstance instance) {
        for (BlockPos pos : instance.claimedPorts()) {
            this.setClaim(pos, null);
        }
        instance.claimedPorts().clear();
    }

    /**
     * 尝试把端口归属写入方块实体
     *
     * <p>方块实体必须实现 {@link MultiblockPort}；已被其它控制器认领的端口不会被改写。</p>
     *
     * @param pos 端口位置
     * @param controllerPos 控制器位置，{@code null} 表示释放
     * @return 是否写入成功
     */
    private boolean setClaim(BlockPos pos, @Nullable BlockPos controllerPos) {
        BlockEntity be = this.level.getBlockEntity(pos);
        if (!(be instanceof MultiblockPort port)) {
            return false;
        }
        if (port.controllerPos() != null && !port.controllerPos().equals(controllerPos)) {
            return false;
        }
        port.setControllerPos(controllerPos);
        return true;
    }

    /** 把实例覆盖的位置与区块登记到反向索引 */
    private void index(StructureInstance instance) {
        for (BlockPos pos : instance.positions()) {
            this.positionIndex.computeIfAbsent(pos.asLong(), ignored -> new LinkedHashSet<>())
                .add(instance.controllerPos());
        }
        for (ChunkPos chunk : this.chunks(instance)) {
            this.chunkIndex.computeIfAbsent(chunk, ignored -> new LinkedHashSet<>())
                .add(instance.controllerPos());
        }
    }

    /** 从反向索引移除实例，空集合一并删除 */
    private void unindex(StructureInstance instance) {
        for (BlockPos pos : instance.positions()) {
            Set<BlockPos> controllers = this.positionIndex.get(pos.asLong());
            if (controllers == null) {
                continue;
            }
            controllers.remove(instance.controllerPos());
            if (controllers.isEmpty()) {
                this.positionIndex.remove(pos.asLong());
            }
        }
        for (ChunkPos chunk : this.chunks(instance)) {
            Set<BlockPos> controllers = this.chunkIndex.get(chunk);
            if (controllers == null) {
                continue;
            }
            controllers.remove(instance.controllerPos());
            if (controllers.isEmpty()) {
                this.chunkIndex.remove(chunk);
            }
        }
    }

    private List<ChunkPos> chunks(StructureInstance instance) {
        List<ChunkPos> chunks = new ArrayList<>();
        for (BlockPos pos : instance.positions()) {
            ChunkPos chunk = ChunkPos.containing(pos);
            if (!chunks.contains(chunk)) {
                chunks.add(chunk);
            }
        }
        return chunks;
    }
}
