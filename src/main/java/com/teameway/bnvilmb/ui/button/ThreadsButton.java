package com.teameway.bnvilmb.ui.button;

import com.teameway.bnvilmb.multiblock.controller.ControllerBlockEntity;
import com.teameway.bnvilmb.ui.UiContext;
import com.teameway.bnvilmb.ui.controller.ControllerBlockEntityData;
import com.teameway.bnvilmb.ui.panel.FloatingPanel;
import com.teameway.bnvilmb.ui.panel.ThreadListPanel;
import net.minecraft.network.chat.Component;

/**
 * 线程按钮，只在控制器界面出现
 */
public final class ThreadsButton extends SideButton {
    public ThreadsButton() {
        super("threads", PRIORITY_THREAD, "T");
        style(style -> style.tooltips(Component.translatable("gui.bnvil_multiblock.button.threads")));
    }

    @Override
    public boolean isAvailable(UiContext context) {
        return context.host() instanceof ControllerBlockEntity;
    }

    @Override
    protected FloatingPanel createPanel(UiContext context) {
        ControllerBlockEntity controller = (ControllerBlockEntity) context.host();
        return new ThreadListPanel(new ControllerBlockEntityData(controller), context);
    }
}
