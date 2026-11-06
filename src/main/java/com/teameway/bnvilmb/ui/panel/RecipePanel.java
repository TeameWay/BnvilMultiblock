package com.teameway.bnvilmb.ui.panel;

import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.teameway.bnvilmb.ui.UiContext;
import com.teameway.bnvilmb.ui.UiMetrics;
import com.teameway.bnvilmb.ui.controller.ControllerUiData;
import com.teameway.bnvilmb.ui.widget.RecipeViewElement;
import net.minecraft.network.chat.Component;

/**
 * 配方信息浮窗
 */
public final class RecipePanel extends FloatingPanel {
    private final ControllerUiData data;
    private int slot;
    private RecipeViewElement content;

    public RecipePanel(ControllerUiData data, UiContext context, int slot) {
        super("recipe", title(slot), UiMetrics.RECIPE_PANEL_WIDTH, UiMetrics.RECIPE_PANEL_MAX_HEIGHT);
        this.data = data;
        this.slot = slot;
    }

    @Override
    protected UIElement buildBody(UiContext context) {
        this.content = new RecipeViewElement(this.data, this.slot);
        return this.content;
    }

    public void setSlot(int slot) {
        this.slot = slot;
        setTitle(title(slot));
        if (this.content != null) {
            this.content.setSlot(slot);
        }
    }

    private static Component title(int slot) {
        return Component.translatable("gui.bnvil_multiblock.controller.thread.title", slot);
    }
}
