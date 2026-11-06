package com.teameway.bnvilmb.multiblock.controller;

import com.lowdragmc.lowdraglib2.gui.factory.BlockUIMenuType;
import com.lowdragmc.lowdraglib2.gui.ui.ModularUI;
import com.lowdragmc.lowdraglib2.gui.ui.UI;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jspecify.annotations.Nullable;

/**
 * 多方块控制器方块基类。
 *
 * <p>控制器方块的开箱行为集中在这里：放置时写入六向朝向、只在服务端提供 ticker、右键打开
 * {@link ControllerUI}。具体方块只需实现 {@link EntityBlock#newBlockEntity(BlockPos, BlockState)}
 * 返回自己的控制器方块实体。</p>
 */
public abstract class MultiblockControllerBlock extends Block implements EntityBlock, BlockUIMenuType.BlockUI {
    /**
     * @param properties 方块属性
     */
    protected MultiblockControllerBlock(Properties properties) {
        super(properties);
    }

    /**
     * 放置时把结构朝向设为放置者朝向的反方向。
     *
     * <p>正面可以是六向；正面竖直时，上方取放置者的水平朝向，否则取 {@link Direction#UP}。</p>
     *
     * @param level 所在世界
     * @param pos 方块位置
     * @param state 方块状态
     * @param placer 放置者，可能为 {@code null}
     * @param stack 放置时使用的物品
     */
    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (level.isClientSide() || placer == null) {
            return;
        }
        if (level.getBlockEntity(pos) instanceof ControllerBlockEntity controller) {
            Direction facing = placer.getDirection().getOpposite();
            Direction up = facing.getAxis().isVertical() ? Direction.fromYRot(placer.getYRot()) : Direction.UP;
            controller.setFrontFacing(facing);
            controller.setUpwardsFacing(up);
        }
    }

    /**
     * 只在服务端提供 ticker。
     *
     * @param <T> 方块实体类型参数
     * @param level 所在世界
     * @param state 方块状态
     * @param type 方块实体类型
     * @return 服务端 ticker，客户端为 {@code null}
     */
    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(
        Level level,
        BlockState state,
        BlockEntityType<T> type
    ) {
        if (level.isClientSide()) {
            return null;
        }
        return (tickLevel, pos, tickState, blockEntity) -> {
            if (tickLevel instanceof ServerLevel serverLevel && blockEntity instanceof ControllerBlockEntity controller) {
                controller.serverTick(serverLevel);
            }
        };
    }

    /**
     * 打开控制器界面。
     *
     * <p>客户端返回 {@link InteractionResult#SUCCESS}；服务端通过 {@link BlockUIMenuType#openUI}
     * 打开界面并返回 {@link InteractionResult#CONSUME}，非 {@link ServerPlayer} 返回
     * {@link InteractionResult#PASS}。</p>
     *
     * @param state 方块状态
     * @param level 所在世界
     * @param pos 方块位置
     * @param player 使用的玩家
     * @param hit 命中结果
     * @return 交互结果
     */
    @Override
    public InteractionResult useWithoutItem(
        BlockState state,
        Level level,
        BlockPos pos,
        Player player,
        BlockHitResult hit
    ) {
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        if (player instanceof ServerPlayer serverPlayer) {
            BlockUIMenuType.openUI(serverPlayer, pos);
            return InteractionResult.CONSUME;
        }
        return InteractionResult.PASS;
    }

    /**
     * 为打开界面的玩家构建控制器界面。
     *
     * <p>方块实体不是 {@link ControllerBlockEntity} 时返回只含空根元素的界面。</p>
     *
     * @param holder 界面持有者，提供玩家与方块位置
     * @return 控制器界面
     */
    @Override
    public ModularUI createUI(BlockUIMenuType.BlockUIHolder holder) {
        if (holder.player.level().getBlockEntity(holder.pos) instanceof ControllerBlockEntity controller) {
            return ControllerUI.create(controller, holder.player);
        }
        return new ModularUI(UI.of(new UIElement()), holder.player);
    }
}
