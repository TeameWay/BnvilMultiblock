package com.teameway.bnvilmb.ui.controller;

import com.lowdragmc.lowdraglib2.gui.ui.ModularUI;
import com.teameway.bnvilmb.multiblock.controller.ControllerBlockEntity;
import com.teameway.bnvilmb.ui.UiContext;
import com.teameway.bnvilmb.ui.UiMetrics;
import com.teameway.bnvilmb.ui.button.PortsButton;
import com.teameway.bnvilmb.ui.button.ThreadsButton;
import com.teameway.bnvilmb.ui.nav.UiNavigation;
import com.teameway.bnvilmb.ui.shell.MainPanel;
import com.teameway.bnvilmb.ui.shell.UiShell;
import net.minecraft.world.entity.player.Player;

/**
 * 控制器界面入口
 */
public final class ControllerUiFactory {
    private ControllerUiFactory() {
    }

    public static ModularUI create(ControllerBlockEntity controller, Player player) {
        ControllerUiData data = new ControllerBlockEntityData(controller);
        UiContext context = new UiContext(player, controller, new UiNavigation());
        UiShell shell = UiShell.create(context, UiMetrics.MAIN_WIDTH, UiMetrics.CONTROLLER_HEIGHT);
        shell.main(new MainPanel(
            context,
            data.blockName(),
            new ControllerMainSection(data),
            UiMetrics.MAIN_WIDTH,
            UiMetrics.CONTROLLER_HEIGHT
        ));
        shell.buttons(bar -> bar.addAll(new ThreadsButton(), new PortsButton()));
        return shell.toModularUI();
    }
}
