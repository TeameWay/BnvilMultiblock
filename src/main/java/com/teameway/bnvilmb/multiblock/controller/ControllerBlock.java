package com.teameway.bnvilmb.multiblock.controller;

import com.lowdragmc.lowdraglib2.gui.factory.BlockUIMenuType;
import com.lowdragmc.lowdraglib2.gui.ui.ModularUI;
import com.lowdragmc.lowdraglib2.gui.ui.UI;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.teameway.bnvilmb.init.multiblock.BnvilControllerBlockEntities;
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
 * 多方块控制器方块
 *
 * <p>方块本身只负责创建方块实体、在服务端注册 ticker 并打开 LDLib2 界面；结构扫描与配方执行
 * 由 {@link ControllerBlockEntity} 承担。</p>
 */
public class ControllerBlock extends Block implements EntityBlock, BlockUIMenuType.BlockUI {
    /**
     * 属性全部来自构造参数，方块不保存额外状态
     *
     * @param properties 方块属性
     */
    public ControllerBlock(Properties properties) {
        super(properties);
    }

    /**
     * 放置时把结构朝向设为放置者朝向的反方向
     *
     * <p>只接受水平方向；竖直方向回退到 {@link Direction#NORTH}，客户端与放置者为空时直接返回。</p>
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
            controller.setStructureFacing(facing.getAxis().isHorizontal() ? facing : Direction.NORTH);
        }
    }

    /**
     * 创建控制器方块实体
     *
     * @param pos 方块位置
     * @param state 方块状态
     * @return 注册项 {@code SIMPLE_CONTROLLER} 创建的方块实体
     */
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return BnvilControllerBlockEntities.SIMPLE_CONTROLLER.create(pos, state);
    }

    /**
     * 只在服务端提供 ticker
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
     * 打开控制器界面
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
     * 为打开界面的玩家构建控制器界面
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
