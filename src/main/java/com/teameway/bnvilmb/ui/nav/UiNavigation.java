package com.teameway.bnvilmb.ui.nav;

import com.lowdragmc.lowdraglib2.LDLib2;
import com.lowdragmc.lowdraglib2.gui.factory.BlockUIMenuType;
import com.lowdragmc.lowdraglib2.networking.rpc.RPCPacket;
import com.lowdragmc.lowdraglib2.networking.rpc.RPCPacketDistributor;
import com.lowdragmc.lowdraglib2.syncdata.rpc.RPCSender;
import com.teameway.bnvilmb.BnvilMultiBlock;
import com.teameway.bnvilmb.multiblock.port.ControlPort;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;

/**
 * 界面跳转与跨端操作
 *
 * <p>客户端只发 RPC，服务端负责打开方块界面和修改控制端口。所有入口集中在这里，
 * 浮窗和按钮不直接碰网络层。</p>
 */
public final class UiNavigation {

    /**
     * 打开指定位置的方块界面
     *
     * @param pos 方块位置
     */
    public void openBlockUi(BlockPos pos) {
        if (LDLib2.isServer()) {
            return;
        }
        RPCPacketDistributor.rpcToServer("open_block_ui", pos);
    }

    /**
     * 修改控制端口的提供量
     *
     * @param pos 端口位置
     * @param value 目标值
     */
    public void setControlPort(BlockPos pos, int value) {
        if (LDLib2.isServer()) {
            return;
        }
        RPCPacketDistributor.rpcToServer("set_control_port", pos, value);
    }

    @RPCPacket(modId = BnvilMultiBlock.MODID, value = "open_block_ui")
    private static void handleOpenBlockUi(RPCSender sender, BlockPos pos) {
        if (sender.asPlayer() instanceof ServerPlayer player) {
            BlockUIMenuType.openUI(player, pos);
        }
    }

    @RPCPacket(modId = BnvilMultiBlock.MODID, value = "set_control_port")
    private static void handleSetControlPort(RPCSender sender, BlockPos pos, int value) {
        if (!(sender.asPlayer() instanceof ServerPlayer player)) {
            return;
        }
        double reach = 64.0;
        if (player.distanceToSqr(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5) > reach) {
            return;
        }
        if (player.level().getBlockEntity(pos) instanceof ControlPort port) {
            port.setProvided(value);
        }
    }
}
