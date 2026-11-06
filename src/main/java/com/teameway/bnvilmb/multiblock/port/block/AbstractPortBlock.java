package com.teameway.bnvilmb.multiblock.port.block;

import com.lowdragmc.lowdraglib2.gui.factory.BlockUIMenuType;
import com.lowdragmc.lowdraglib2.gui.ui.ModularUI;
import com.lowdragmc.lowdraglib2.gui.ui.UI;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.teameway.bnvilmb.multiblock.port.MultiblockPort;
import com.teameway.bnvilmb.multiblock.port.PortKind;
import com.teameway.bnvilmb.multiblock.port.PortTier;
import com.teameway.bnvilmb.ui.BnvilUi;
import dev.anvilcraft.lib.v2.registrum.util.entry.BlockEntityEntry;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

/**
 * 端口方块基类
 *
 * <p>只保存种类与等级，方块实体在构造时按等级创建容量。</p>
 */
public abstract class AbstractPortBlock extends Block implements EntityBlock, BlockUIMenuType.BlockUI {
    private final PortKind portKind;
    private final PortTier tier;

    protected AbstractPortBlock(Properties properties, PortKind portKind, PortTier tier) {
        super(properties);
        this.portKind = portKind;
        this.tier = tier;
    }

    /**
     * @return 该方块的端口种类
     */
    public PortKind portKind() {
        return this.portKind;
    }

    /**
     * @return 该方块的端口等级
     */
    public PortTier tier() {
        return this.tier;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return this.blockEntityEntry().create(pos, state);
    }

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

    @Override
    public ModularUI createUI(BlockUIMenuType.BlockUIHolder holder) {
        if (holder.player.level().getBlockEntity(holder.pos) instanceof MultiblockPort port) {
            return BnvilUi.port(port, holder);
        }
        return new ModularUI(UI.of(new UIElement()), holder.player);
    }

    /**
     * 对应的方块实体注册条目
     *
     * @return 该方块使用的方块实体注册条目
     */
    protected abstract BlockEntityEntry<? extends BlockEntity> blockEntityEntry();
}
