package com.teameway.bnvilmb.multiblock.pattern;

import com.teameway.bnvilmb.multiblock.controller.ControllerBlockEntity;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 一个控制器的 pattern 运行状态。
 *
 * <p>对应 GTM 的 {@code PatternState}：保存当前方块快照、成型状态、错误、朝向和端口认领。
 * 缓存命中时不需要重新跑 predicate。</p>
 */
public final class PatternState {
    private final ControllerBlockEntity controller;
    private final BlockPos controllerPos;
    private final BlockPattern pattern;
    private final Long2ObjectMap<BlockInfo> cache = new Long2ObjectOpenHashMap<>();
    private final Set<BlockPos> claimedPorts = new HashSet<>();

    private Direction frontFacing;
    private Direction upwardsFacing;
    private boolean flipped;
    private boolean formed;
    private boolean dirty = true;
    private boolean suspended;
    private long readyAtTick;
    private List<PatternError> errors = List.of();
    private int parallelBonus;
    private int threadBonus;
    private boolean cacheHit;
    private @Nullable BlockPos cachedControllerPos;

    public PatternState(ControllerBlockEntity controller, BlockPattern pattern) {
        this.controller = controller;
        this.controllerPos = controller.getBlockPos();
        this.pattern = pattern;
        this.frontFacing = controller.frontFacing();
        this.upwardsFacing = controller.upwardsFacing();
        this.flipped = controller.flipped();
    }

    public ControllerBlockEntity controller() {
        return this.controller;
    }

    public BlockPos controllerPos() {
        return this.controllerPos;
    }

    public BlockPattern pattern() {
        return this.pattern;
    }

    public Long2ObjectMap<BlockInfo> cache() {
        return this.cache;
    }

    public void clearCache() {
        this.cache.clear();
        this.cachedControllerPos = null;
    }

    public void cache(BlockPos pos, BlockInfo info) {
        this.cache.put(pos.asLong(), info);
        this.cachedControllerPos = this.controllerPos;
    }

    public @Nullable BlockPos cachedControllerPos() {
        return this.cachedControllerPos;
    }

    public boolean formed() {
        return this.formed;
    }

    public void setFormed(boolean formed) {
        this.formed = formed;
    }

    public List<PatternError> errors() {
        return this.errors;
    }

    public void setErrors(List<PatternError> errors) {
        this.errors = List.copyOf(errors);
    }

    public boolean hasErrors() {
        return !this.errors.isEmpty();
    }

    public boolean dirty() {
        return this.dirty;
    }

    public void setDirty(boolean dirty) {
        this.dirty = dirty;
    }

    public boolean suspended() {
        return this.suspended;
    }

    public void setSuspended(boolean suspended) {
        this.suspended = suspended;
    }

    public long readyAtTick() {
        return this.readyAtTick;
    }

    public void setReadyAtTick(long readyAtTick) {
        this.readyAtTick = readyAtTick;
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

    /**
     * 更新朝向；调用方负责清空缓存和标脏。
     */
    public void setFacing(Direction frontFacing, Direction upwardsFacing, boolean flipped) {
        this.frontFacing = frontFacing;
        this.upwardsFacing = upwardsFacing;
        this.flipped = flipped;
    }

    public Set<BlockPos> claimedPorts() {
        return this.claimedPorts;
    }

    public int parallelBonus() {
        return this.parallelBonus;
    }

    public int threadBonus() {
        return this.threadBonus;
    }

    public void setBonuses(int parallelBonus, int threadBonus) {
        this.parallelBonus = parallelBonus;
        this.threadBonus = threadBonus;
    }

    public boolean cacheHit() {
        return this.cacheHit;
    }

    public void setCacheHit(boolean cacheHit) {
        this.cacheHit = cacheHit;
    }

    /**
     * @return 当前缓存中识别为端口的位置
     */
    public List<BlockPos> portPositions() {
        List<BlockPos> ports = new ArrayList<>();
        for (Long2ObjectMap.Entry<BlockInfo> entry : this.cache.long2ObjectEntrySet()) {
            if (com.teameway.bnvilmb.multiblock.port.PortRegistry.descriptor(entry.getValue().state()) != null) {
                ports.add(BlockPos.of(entry.getLongKey()));
            }
        }
        return List.copyOf(ports);
    }

    /**
     * @return 结构当前覆盖的世界坐标
     */
    public List<BlockPos> positions() {
        return this.pattern.positions(this.controllerPos, this.frontFacing, this.upwardsFacing, this.flipped);
    }
}
