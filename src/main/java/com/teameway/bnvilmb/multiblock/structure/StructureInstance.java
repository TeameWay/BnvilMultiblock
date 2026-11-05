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
 */
public final class StructureInstance {
    private final ControllerBlockEntity controller;
    private final ResourceKey<StructurePattern> patternKey;
    private final Holder.Reference<StructurePattern> patternHolder;
    private final StructurePattern pattern;
    private final Set<BlockPos> claimedPorts = new HashSet<>();
    private Direction facing;
    private List<BlockPos> positions = List.of();
    private List<BlockPos> slotPositions = List.of();
    private boolean formed;
    private boolean dirty = true;
    private boolean suspended;
    private int idleTicks;
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

    public ControllerBlockEntity controller() {
        return this.controller;
    }

    public ResourceKey<StructurePattern> patternKey() {
        return this.patternKey;
    }

    public Holder.Reference<StructurePattern> patternHolder() {
        return this.patternHolder;
    }

    public StructurePattern pattern() {
        return this.pattern;
    }

    public Direction facing() {
        return this.facing;
    }

    void setFacing(Direction facing) {
        this.facing = facing;
        this.rebuildPositions();
    }

    public List<BlockPos> positions() {
        return this.positions;
    }

    public List<BlockPos> slotPositions() {
        return this.slotPositions;
    }

    public Set<BlockPos> claimedPorts() {
        return this.claimedPorts;
    }

    public boolean formed() {
        return this.formed;
    }

    void setFormed(boolean formed) {
        this.formed = formed;
    }

    public boolean dirty() {
        return this.dirty;
    }

    void setDirty(boolean dirty) {
        this.dirty = dirty;
    }

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

    public @Nullable StructureMatch lastMatch() {
        return this.lastMatch;
    }

    void setLastMatch(@Nullable StructureMatch lastMatch) {
        this.lastMatch = lastMatch;
    }

    public BlockPos controllerPos() {
        return this.controller.getBlockPos();
    }

    static ResourceKey<StructurePattern> key(StructureController controller) {
        return ResourceKey.create(BnvilRegistries.STRUCTURE_PATTERNS, controller.patternId());
    }
}
