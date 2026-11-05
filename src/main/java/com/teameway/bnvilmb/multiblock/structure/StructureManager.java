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
 * <p>事件只负责标脏，控制器方块实体在自己的 tick 里调用 {@link #tick(StructureInstance)}，
 * 避免在破坏事件中读到尚未被移除的方块。</p>
 */
public final class StructureManager {
    private static final int PERIODIC_TICKS = 20;
    private static final Map<ServerLevel, StructureManager> MANAGERS = new WeakHashMap<>();

    private final ServerLevel level;
    private final Map<BlockPos, StructureInstance> instances = new HashMap<>();
    private final Map<Long, Set<BlockPos>> positionIndex = new HashMap<>();
    private final Map<ChunkPos, Set<BlockPos>> chunkIndex = new HashMap<>();

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

    public void updateFacing(StructureInstance instance, Direction facing) {
        if (instance.facing() == facing) {
            return;
        }
        this.unindex(instance);
        instance.setFacing(facing);
        this.index(instance);
        instance.setDirty(true);
    }

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

    private void releaseClaims(StructureInstance instance) {
        for (BlockPos pos : instance.claimedPorts()) {
            this.setClaim(pos, null);
        }
        instance.claimedPorts().clear();
    }

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
