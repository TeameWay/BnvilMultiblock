package com.teameway.bnvilmb.multiblock.structure;

import com.teameway.bnvilmb.BnvilMultiBlock;
import com.teameway.bnvilmb.multiblock.StructureController;
import com.teameway.bnvilmb.multiblock.StructureControllerRegistry;
import com.teameway.bnvilmb.multiblock.controller.ControllerBlockEntity;
import com.teameway.bnvilmb.multiblock.pattern.BlockPattern;
import com.teameway.bnvilmb.multiblock.pattern.PatternError;
import com.teameway.bnvilmb.multiblock.pattern.PatternState;
import com.teameway.bnvilmb.multiblock.port.MultiblockPort;
import net.minecraft.core.BlockPos;
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
 * 每个服务端世界的多方块结构管理器。
 *
 * <p>对应 GTM 的 {@code MultiblockWorldSavedData} 加控制器生命周期：维护控制器到
 * {@link PatternState} 的映射、方块位置/区块反向索引，以及待检查队列。方块变化只标脏，
 * 由 {@link #tick(ServerLevel)} 在服务端 tick 中处理。缓存命中时不会重新跑 predicate。</p>
 */
public final class StructureManager {
    private static final Map<ServerLevel, StructureManager> MANAGERS = new WeakHashMap<>();

    private final ServerLevel level;
    private final Map<BlockPos, PatternState> instances = new HashMap<>();
    private final Map<Long, Set<BlockPos>> positionIndex = new HashMap<>();
    private final Map<ChunkPos, Set<BlockPos>> chunkIndex = new HashMap<>();
    private final Set<BlockPos> dirty = new LinkedHashSet<>();

    private StructureManager(ServerLevel level) {
        this.level = level;
    }

    public static StructureManager get(ServerLevel level) {
        synchronized (MANAGERS) {
            return MANAGERS.computeIfAbsent(level, StructureManager::new);
        }
    }

    public static void remove(ServerLevel level) {
        synchronized (MANAGERS) {
            MANAGERS.remove(level);
        }
    }

    /**
     * 为控制器登记结构状态，延迟两个 tick 做首次检查。
     *
     * @param controller 控制器方块实体
     * @return 状态；控制器没有注册 pattern 时为 {@code null}
     */
    public @Nullable PatternState register(ControllerBlockEntity controller) {
        BlockPos pos = controller.getBlockPos();
        PatternState existing = this.instances.get(pos);
        if (existing != null) {
            return existing;
        }
        StructureController structureController = StructureControllerRegistry.get(controller.getBlockState().getBlock());
        if (structureController == null) {
            BnvilMultiBlock.LOGGER.warn("No structure controller registered for block {}", controller.getBlockState().getBlock());
            return null;
        }
        BlockPattern pattern = structureController.pattern();
        if (pattern == null) {
            BnvilMultiBlock.LOGGER.warn("Controller {} has no pattern", controller.getBlockState().getBlock());
            return null;
        }
        PatternState state = new PatternState(controller, pattern);
        state.setReadyAtTick(this.level.getGameTime() + 2);
        this.instances.put(pos, state);
        this.index(state);
        this.dirty.add(pos);
        return state;
    }

    /**
     * 注销结构状态。
     *
     * @param state 状态，可为 {@code null}
     * @param unform 是否按解散处理
     */
    public void unregister(@Nullable PatternState state, boolean unform) {
        if (state == null) {
            return;
        }
        this.unindex(state);
        this.instances.remove(state.controllerPos(), state);
        this.dirty.remove(state.controllerPos());
        if (unform && state.formed()) {
            state.setFormed(false);
            this.releaseClaims(state);
            state.controller().onStructureUnformed(state);
        }
    }

    /**
     * 更新控制器朝向并重建索引。
     *
     * @param state 结构状态
     */
    public void updateOrientation(PatternState state) {
        this.unindex(state);
        state.setFacing(
            state.controller().frontFacing(),
            state.controller().upwardsFacing(),
            state.controller().flipped()
        );
        state.clearCache();
        state.setDirty(true);
        this.index(state);
        this.dirty.add(state.controllerPos());
    }

    /**
     * 标脏指定位置。
     *
     * @param pos 方块位置
     */
    public void markDirty(BlockPos pos) {
        Set<BlockPos> controllers = this.positionIndex.get(pos.asLong());
        if (controllers == null || controllers.isEmpty()) {
            return;
        }
        this.dirty.addAll(controllers);
    }

    /**
     * 标脏覆盖指定区块的控制器。
     *
     * @param pos 区块坐标
     */
    public void markDirtyChunk(ChunkPos pos) {
        Set<BlockPos> controllers = this.chunkIndex.get(pos);
        if (controllers != null) {
            this.dirty.addAll(controllers);
        }
    }

    /**
     * 区块卸载时清掉相关缓存，避免保留失效方块实体引用。
     *
     * @param pos 区块坐标
     */
    public void invalidateChunk(ChunkPos pos) {
        Set<BlockPos> controllers = this.chunkIndex.get(pos);
        if (controllers == null) {
            return;
        }
        for (BlockPos controllerPos : controllers) {
            PatternState state = this.instances.get(controllerPos);
            if (state != null) {
                state.clearCache();
                state.setDirty(true);
                this.dirty.add(controllerPos);
            }
        }
    }

    /**
     * 处理待检查结构。
     *
     * @param level 服务端世界
     */
    public void tick(ServerLevel level) {
        if (this.dirty.isEmpty()) {
            return;
        }
        long gameTime = level.getGameTime();
        List<PatternState> due = new ArrayList<>();
        for (BlockPos pos : List.copyOf(this.dirty)) {
            PatternState state = this.instances.get(pos);
            if (state == null) {
                this.dirty.remove(pos);
                continue;
            }
            if (gameTime < state.readyAtTick()) {
                continue;
            }
            this.dirty.remove(pos);
            due.add(state);
        }
        for (PatternState state : due) {
            this.check(state);
        }
    }

    private void check(PatternState state) {
        ControllerBlockEntity controller = state.controller();
        if (controller.isRemoved()) {
            this.unregister(state, true);
            return;
        }
        if (!this.allLoaded(state)) {
            state.setSuspended(true);
            state.setDirty(false);
            return;
        }

        state.setSuspended(false);
        boolean previousFlipped = state.flipped();
        boolean valid = state.pattern().checkPatternFastAt(
            this.level,
            state,
            state.controllerPos(),
            controller.frontFacing(),
            controller.upwardsFacing(),
            state.pattern().allowFlip()
        );

        if (valid) {
            valid = this.syncClaims(state);
        }
        if (valid) {
            if (!state.formed()) {
                state.setFormed(true);
                controller.onStructureFormed(state);
            } else if (!state.cacheHit()) {
                controller.onStructureUpdated(state);
            }
        } else {
            if (state.formed()) {
                state.setFormed(false);
                this.releaseClaims(state);
                controller.onStructureUnformed(state);
            }
            controller.onStructureInvalid(state);
        }
        if (previousFlipped != state.flipped()) {
            this.rebuildIndex();
        }
        state.setDirty(false);
    }

    private boolean allLoaded(PatternState state) {
        for (BlockPos pos : state.positions()) {
            if (!this.level.isLoaded(pos)) {
                return false;
            }
        }
        return true;
    }

    private boolean syncClaims(PatternState state) {
        boolean success = true;
        Set<BlockPos> newPorts = new LinkedHashSet<>(state.portPositions());
        for (BlockPos pos : new ArrayList<>(state.claimedPorts())) {
            if (!newPorts.contains(pos)) {
                this.removeClaim(pos, state.controllerPos());
                state.claimedPorts().remove(pos);
            }
        }
        for (BlockPos pos : newPorts) {
            if (this.addClaim(pos, state.controllerPos())) {
                state.claimedPorts().add(pos);
            } else {
                state.setErrors(List.of(PatternError.shared(pos)));
                success = false;
            }
        }
        return success;
    }

    private void releaseClaims(PatternState state) {
        for (BlockPos pos : state.claimedPorts()) {
            this.removeClaim(pos, state.controllerPos());
        }
        state.claimedPorts().clear();
    }

    private boolean addClaim(BlockPos pos, BlockPos controllerPos) {
        BlockEntity be = this.level.getBlockEntity(pos);
        if (!(be instanceof MultiblockPort port)) {
            return false;
        }
        if (!port.canShared()) {
            for (BlockPos owner : port.controllerPositions()) {
                if (!owner.equals(controllerPos)) {
                    return false;
                }
            }
        }
        port.addController(controllerPos);
        return true;
    }

    private void removeClaim(BlockPos pos, BlockPos controllerPos) {
        BlockEntity be = this.level.getBlockEntity(pos);
        if (be instanceof MultiblockPort port) {
            port.removeController(controllerPos);
        }
    }

    private void rebuildIndex() {
        this.positionIndex.clear();
        this.chunkIndex.clear();
        for (PatternState state : this.instances.values()) {
            this.index(state);
        }
    }

    private void index(PatternState state) {
        for (BlockPos pos : state.positions()) {
            this.positionIndex.computeIfAbsent(pos.asLong(), ignored -> new LinkedHashSet<>())
                .add(state.controllerPos());
        }
        for (ChunkPos chunk : this.chunks(state)) {
            this.chunkIndex.computeIfAbsent(chunk, ignored -> new LinkedHashSet<>())
                .add(state.controllerPos());
        }
    }

    private void unindex(PatternState state) {
        for (BlockPos pos : state.positions()) {
            Set<BlockPos> controllers = this.positionIndex.get(pos.asLong());
            if (controllers == null) {
                continue;
            }
            controllers.remove(state.controllerPos());
            if (controllers.isEmpty()) {
                this.positionIndex.remove(pos.asLong());
            }
        }
        for (ChunkPos chunk : this.chunks(state)) {
            Set<BlockPos> controllers = this.chunkIndex.get(chunk);
            if (controllers == null) {
                continue;
            }
            controllers.remove(state.controllerPos());
            if (controllers.isEmpty()) {
                this.chunkIndex.remove(chunk);
            }
        }
    }

    private List<ChunkPos> chunks(PatternState state) {
        List<ChunkPos> chunks = new ArrayList<>();
        for (BlockPos pos : state.positions()) {
            ChunkPos chunk = ChunkPos.containing(pos);
            if (!chunks.contains(chunk)) {
                chunks.add(chunk);
            }
        }
        return chunks;
    }
}
