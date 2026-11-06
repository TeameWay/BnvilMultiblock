package com.teameway.bnvilmb.ui.panel;

import com.lowdragmc.lowdraglib2.LDLib2;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Label;
import com.lowdragmc.lowdraglib2.gui.ui.event.UIEvents;
import com.teameway.bnvilmb.ui.UiContext;
import com.teameway.bnvilmb.ui.UiMetrics;
import com.teameway.bnvilmb.ui.controller.ControllerUiData;
import com.teameway.bnvilmb.ui.widget.ThreadCard;
import dev.vfyjxf.taffy.style.FlexDirection;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 线程列表浮窗
 *
 * <p>点击卡片选中线程并打开配方浮窗；再次点击同一张卡片取消选中。关闭线程浮窗时配方浮窗一起关闭。</p>
 */
public final class ThreadListPanel extends FloatingPanel {
    private final ControllerUiData data;
    private final UiContext context;
    private final List<ThreadCard> cards = new ArrayList<>();
    private UIElement list;
    private RecipePanel recipePanel;
    private int selected = -1;
    private int count = -1;

    public ThreadListPanel(ControllerUiData data, UiContext context) {
        super("threads", Component.translatable("gui.bnvil_multiblock.panel.threads"),
            UiMetrics.THREAD_PANEL_WIDTH, UiMetrics.THREAD_PANEL_MAX_HEIGHT);
        this.data = data;
        this.context = context;
    }

    @Override
    protected UIElement buildBody(UiContext context) {
        this.list = new UIElement().layout(layout -> layout
            .widthPercent(100)
            .flexDirection(FlexDirection.COLUMN)
            .gapAll(UiMetrics.CARD_SPACING));
        this.rebuild();
        addEventListener(UIEvents.TICK, event -> {
            if (!LDLib2.isServer()) {
                this.refresh();
            }
        });
        return this.list;
    }

    @Override
    protected void onOpened() {
        this.refresh();
    }

    private void refresh() {
        if (this.data.threadSlots() != this.count) {
            this.rebuild();
            return;
        }
        if (this.selected >= this.count) {
            this.selected = -1;
            this.closeRecipe();
            this.highlight();
        }
    }

    private void rebuild() {
        this.count = this.data.threadSlots();
        this.list.clearAllChildren();
        this.cards.clear();
        if (this.count == 0) {
            this.selected = -1;
            this.closeRecipe();
            Label hint = new Label();
            hint.setText(Component.translatable("gui.bnvil_multiblock.controller.thread.none"));
            hint.addClass("bnvil-hint");
            hint.layout(layout -> layout.widthPercent(100).height(UiMetrics.LINE));
            this.list.addChild(hint);
            return;
        }
        for (int slot = 0; slot < this.count; slot++) {
            ThreadCard card = new ThreadCard(this.data, slot, this::select);
            this.cards.add(card);
            this.list.addChild(card);
        }
        if (this.selected >= this.count) {
            this.selected = -1;
            this.closeRecipe();
        }
        this.highlight();
    }

    private void select(ThreadCard card) {
        int slot = this.cards.indexOf(card);
        if (slot < 0) {
            return;
        }
        if (this.selected == slot) {
            this.selected = -1;
            this.closeRecipe();
        } else {
            this.selected = slot;
            this.openRecipe();
        }
        this.highlight();
    }

    private void highlight() {
        for (int index = 0; index < this.cards.size(); index++) {
            this.cards.get(index).setSelected(index == this.selected);
        }
    }

    private void openRecipe() {
        if (this.selected < 0) {
            this.closeRecipe();
            return;
        }
        if (this.recipePanel == null) {
            this.recipePanel = new RecipePanel(this.data, this.context, this.selected);
            addChildPanel(this.recipePanel);
        } else {
            this.recipePanel.setSlot(this.selected);
        }
        if (!this.recipePanel.isOpen()) {
            manager().open(this.recipePanel, this, this.context);
        } else {
            manager().bringToFront(this.recipePanel);
        }
    }

    private void closeRecipe() {
        if (this.recipePanel != null) {
            this.recipePanel.close();
        }
    }

    @Override
    protected void onClosed() {
        this.closeRecipe();
    }
}
