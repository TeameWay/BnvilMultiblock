package com.teameway.bnvilmb.dev.controller;

import com.teameway.bnvilmb.dev.init.multiblock.BnvilControllerBlockEntities;
import com.teameway.bnvilmb.multiblock.controller.MultiblockControllerBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * 开发测试用的简易控制器方块。
 *
 * <p>开箱行为由 {@link MultiblockControllerBlock} 提供，这里只绑定示例控制器方块实体。</p>
 */
public class ControllerBlock extends MultiblockControllerBlock {
    /**
     * @param properties 方块属性
     */
    public ControllerBlock(Properties properties) {
        super(properties);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return BnvilControllerBlockEntities.SIMPLE_CONTROLLER.create(pos, state);
    }
}
