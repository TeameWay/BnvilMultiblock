package com.teameway.bnvilmb.multiblock.port.block.entity;

import com.teameway.bnvilmb.multiblock.port.MultiblockPort;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * 端口方块实体基类。
 *
 * <p>端口可以共享，因此 NBT 保存控制器位置列表；旧存档的单个 {@code controller_pos}
 * 会在读取时迁移成只含一个元素的集合。</p>
 */
public abstract class AbstractPortBlockEntity extends BlockEntity implements MultiblockPort {
    private final Set<BlockPos> controllerPositions = new LinkedHashSet<>();

    protected AbstractPortBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public Set<BlockPos> controllerPositions() {
        return Set.copyOf(this.controllerPositions);
    }

    @Override
    public void addController(BlockPos controllerPos) {
        if (this.controllerPositions.add(controllerPos.immutable())) {
            this.setChanged();
        }
    }

    @Override
    public void removeController(BlockPos controllerPos) {
        if (this.controllerPositions.remove(controllerPos)) {
            this.setChanged();
        }
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.controllerPositions.clear();
        for (BlockPos pos : input.listOrEmpty("controllers", BlockPos.CODEC)) {
            this.controllerPositions.add(pos.immutable());
        }
        if (this.controllerPositions.isEmpty()) {
            input.read("controller_pos", BlockPos.CODEC).ifPresent(pos -> this.controllerPositions.add(pos.immutable()));
        }
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        ValueOutput.TypedOutputList<BlockPos> list = output.list("controllers", BlockPos.CODEC);
        for (BlockPos pos : this.controllerPositions) {
            list.add(pos);
        }
    }
}
