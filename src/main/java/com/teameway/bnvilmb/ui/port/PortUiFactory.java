package com.teameway.bnvilmb.ui.port;

import com.lowdragmc.lowdraglib2.gui.factory.BlockUIMenuType;
import com.lowdragmc.lowdraglib2.gui.ui.ModularUI;
import com.lowdragmc.lowdraglib2.syncdata.holder.blockentity.ISyncPersistRPCBlockEntity;
import com.teameway.bnvilmb.multiblock.port.MultiblockPort;
import com.teameway.bnvilmb.ui.UiContext;
import com.teameway.bnvilmb.ui.UiMetrics;
import com.teameway.bnvilmb.ui.button.PortsButton;
import com.teameway.bnvilmb.ui.nav.UiNavigation;
import com.teameway.bnvilmb.ui.shell.MainPanel;
import com.teameway.bnvilmb.ui.shell.UiShell;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.entity.BlockEntity;

/**
 * 端口界面入口
 */
public final class PortUiFactory {
    private PortUiFactory() {
    }

    public static ModularUI create(MultiblockPort port, BlockUIMenuType.BlockUIHolder holder) {
        Component name = holder.blockState.getBlock().getName();
        // 打开界面时强制同步一次，确保客户端拿到控制器归属列表
        if (!holder.player.level().isClientSide() && port instanceof ISyncPersistRPCBlockEntity syncable) {
            syncable.markDirty("controllerList");
            syncable.sync(true);
        }
        PortUiData data = new PortBlockEntityData(port, name);
        UiContext context = new UiContext(
            holder.player,
            port instanceof BlockEntity entity ? entity : null,
            new UiNavigation()
        );
        UiShell shell = UiShell.create(context, UiMetrics.MAIN_WIDTH, UiMetrics.MAIN_HEIGHT);
        shell.main(new MainPanel(
            context,
            name,
            PortSections.create(data),
            UiMetrics.MAIN_WIDTH,
            UiMetrics.MAIN_HEIGHT
        ));
        shell.buttons(bar -> bar.add(new PortsButton()));
        return shell.toModularUI();
    }
}
