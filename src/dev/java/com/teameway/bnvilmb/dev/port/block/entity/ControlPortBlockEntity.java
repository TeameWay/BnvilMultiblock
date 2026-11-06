package com.teameway.bnvilmb.dev.port.block.entity;

import com.teameway.bnvilmb.multiblock.port.PortKind;
import com.teameway.bnvilmb.multiblock.port.PortTier;
import com.teameway.bnvilmb.multiblock.port.block.AbstractPortBlock;
import com.teameway.bnvilmb.multiblock.port.block.entity.AbstractPortBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

/**
 * 控制端口方块实体，对应并行仓与线程仓
 *
 * <p>不创建资源处理器，持久化的状态只有基类里的端口归属。</p>
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
