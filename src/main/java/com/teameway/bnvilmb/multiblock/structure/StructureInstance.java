package com.teameway.bnvilmb.multiblock.structure;

import com.teameway.bnvilmb.init.BnvilRegistries;
import com.teameway.bnvilmb.multiblock.StructureController;
import com.teameway.bnvilmb.multiblock.controller.ControllerBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.Vec3i;
import net.minecraft.resources.ResourceKey;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 单个控制器的结构运行状态
 *
 * <p>保存模板引用、朝向、世界坐标缓存、端口认领与最近一次匹配结果。世界坐标在构造时以及
 * 朝向变化后重建；区块未加载时由管理器把实例挂起，避免在数据不完整时判定。</p>
 */
public final class StructureInstance {
    private final ControllerBlockEntity controller;
    private final ResourceKey<StructurePattern> patternKey;
    private final Holder.Reference<StructurePattern> patternHolder;
    private final StructurePattern pattern;
    /** 已由本结构认领的端口位置 */
    private final Set<BlockPos> claimedPorts = new HashSet<>();
    private Direction facing;
    private List<BlockPos> positions = List.of();
    private List<BlockPos> slotPositions = List.of();
    private boolean formed;
    private boolean dirty = true;
    private boolean suspended;
    /** 距上次判定的 tick 数，供管理器的周期重算使用 */
    private int idleTicks;
    /** 最近一次匹配结果，尚未判定时为 {@code null} */
    private @Nullable StructureMatch lastMatch;

    StructureInstance(
        ControllerBlockEntity controller,
        ResourceKey<StructurePattern> patternKey,
        Holder.Reference<StructurePattern> patternHolder,
        Direction facing
    ) {
        this.controller = controller;
        this.patternKey = patternKey;
        this.patternHolder = patternHolder;
        this.pattern = patternHolder.value();
        this.facing = facing;
        this.rebuildPositions();
    }

    private void rebuildPositions() {
        List<BlockPos> all = new ArrayList<>(this.pattern.positions().size());
        for (Vec3i offset : this.pattern.positions()) {
            Vec3i rotated = StructureRotation.rotate(
                offset, this.pattern.canonicalFront(), this.facing,
                this.pattern.orientation()
            );
            all.add(this.controller.getBlockPos().offset(rotated));
        }
        this.positions = List.copyOf(all);
        List<BlockPos> slots = new ArrayList<>(this.pattern.slotOffsets().size());
        for (Vec3i offset : this.pattern.rotatedSlotOffsets(this.facing)) {
            slots.add(this.controller.getBlockPos().offset(offset));
        }
        this.slotPositions = List.copyOf(slots);
    }

    /**
     * @return 结构所属的控制器方块实体
     */
    public ControllerBlockEntity controller() {
        return this.controller;
    }

    /**
     * @return 结构模板注册键
     */
    public ResourceKey<StructurePattern> patternKey() {
        return this.patternKey;
    }

    /**
     * @return 结构模板的注册表 holder
     */
    public Holder.Reference<StructurePattern> patternHolder() {
        return this.patternHolder;
    }

    /**
     * @return 解析后的结构模板
     */
    public StructurePattern pattern() {
        return this.pattern;
    }

    /**
     * @return 当前控制器正面
     */
    public Direction facing() {
        return this.facing;
    }

    /** 更新朝向并重建世界坐标缓存 */
    void setFacing(Direction facing) {
        this.facing = facing;
        this.rebuildPositions();
    }

    /**
     * @return 结构覆盖的全部世界坐标，顺序与模板 positions 一致
     */
    public List<BlockPos> positions() {
        return this.positions;
    }

    /**
     * @return 槽位世界坐标，顺序与模板 slotOffsets 一致
     */
    public List<BlockPos> slotPositions() {
        return this.slotPositions;
    }

    /**
     * @return 内部集合本身，结构管理器同步认领时直接增删
     */
    public Set<BlockPos> claimedPorts() {
        return this.claimedPorts;
    }

    /**
     * @return 当前是否成型
     */
    public boolean formed() {
        return this.formed;
    }

    void setFormed(boolean formed) {
        this.formed = formed;
    }

    /**
     * @return 是否需要重新判定
     */
    public boolean dirty() {
        return this.dirty;
    }

    void setDirty(boolean dirty) {
        this.dirty = dirty;
    }

    /**
     * @return 是否因区块未加载而暂停
     */
    public boolean suspended() {
        return this.suspended;
    }

    void setSuspended(boolean suspended) {
        this.suspended = suspended;
    }

    int idleTicks() {
        return this.idleTicks;
    }

    void setIdleTicks(int idleTicks) {
        this.idleTicks = idleTicks;
    }

    /**
     * @return 最近一次匹配结果，尚未判定时为 {@code null}
     */
    public @Nullable StructureMatch lastMatch() {
        return this.lastMatch;
    }

    void setLastMatch(@Nullable StructureMatch lastMatch) {
        this.lastMatch = lastMatch;
    }

    /**
     * @return 控制器方块坐标
     */
    public BlockPos controllerPos() {
        return this.controller.getBlockPos();
    }

    static ResourceKey<StructurePattern> key(StructureController controller) {
        return ResourceKey.create(BnvilRegistries.STRUCTURE_PATTERNS, controller.patternId());
    }
}
