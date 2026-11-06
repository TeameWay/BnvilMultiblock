package com.teameway.bnvilmb.ui.button;

import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.teameway.bnvilmb.ui.UiContext;
import com.teameway.bnvilmb.ui.UiMetrics;
import com.teameway.bnvilmb.ui.panel.FloatingPanel;
import com.teameway.bnvilmb.ui.panel.PanelManager;
import dev.vfyjxf.taffy.style.FlexDirection;
import dev.vfyjxf.taffy.style.TaffyPosition;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * main 左侧的按钮栏
 *
 * <p>按钮按 {@link SideButton#priority()} 降序排列，同优先级保持添加顺序。添加新按钮只需要
 * 调用 {@link #add(SideButton)}，不需要改这里。</p>
 */
public final class SideButtonBar extends UIElement {
    private final UiContext context;
    private final PanelManager panels;
    private final List<SideButton> buttons = new ArrayList<>();

    public SideButtonBar(UiContext context, PanelManager panels) {
        this.context = context;
        this.panels = panels;
        setId("bnvil-side-buttons");
        layout(layout -> layout
            .positionType(TaffyPosition.ABSOLUTE)
            .left(UiMetrics.SIDE_BUTTON_X)
            .top(UiMetrics.SIDE_BUTTON_Y)
            .width(UiMetrics.SIDE_BUTTON)
            .flexDirection(FlexDirection.COLUMN)
            .gapAll(UiMetrics.SIDE_BUTTON_GAP));
    }

    /**
     * 添加一个按钮
     *
     * @param button 按钮
     * @return 当前栏，便于链式调用
     */
    public SideButtonBar add(SideButton button) {
        if (!button.isAvailable(this.context)) {
            return this;
        }
        this.buttons.add(button);
        button.setOnClick(event -> {
            FloatingPanel panel = button.panel(this.context);
            this.panels.toggle(panel, button, this.context);
        });
        this.resort();
        return this;
    }

    public SideButtonBar addAll(SideButton... buttons) {
        for (SideButton button : buttons) {
            this.add(button);
        }
        return this;
    }

    /**
     * 重新按优先级排序
     */
    public void resort() {
        this.buttons.sort(Comparator.comparingInt(SideButton::priority).reversed());
        clearAllChildren();
        for (SideButton button : this.buttons) {
            addChild(button);
        }
    }
}
