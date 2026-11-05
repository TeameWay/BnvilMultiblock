package com.teameway.bnvilmb.multiblock.port.block.entity;

import com.teameway.bnvilmb.multiblock.port.PortKind;
import com.teameway.bnvilmb.multiblock.port.PortTier;
import com.teameway.bnvilmb.multiblock.port.block.AbstractPortBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

/**
 * 控制端口方块实体（并行仓 / 线程仓），无资源，只持久化端口归属
 */
public class ControlPortBlockEntity extends AbstractPortBlockEntity {
    private final PortTier tier;

    public ControlPortBlockEntity(BlockEntityType<ControlPortBlockEntity> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        this.tier = ((AbstractPortBlock) state.getBlock()).tier();
    }

    @Override
    public PortKind portKind() {
        return ((AbstractPortBlock) this.getBlockState().getBlock()).portKind();
    }

    @Override
    public PortTier tier() {
        return this.tier;
    }
}
