package com.teameway.bnvilmb.ui.panel;

import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.teameway.bnvilmb.ui.UiContext;
import com.teameway.bnvilmb.ui.UiMetrics;

import java.util.ArrayList;
import java.util.List;

/**
 * 浮窗管理
 *
 * <p>负责把浮窗挂到浮层、打开、关闭、置顶和父子联动。按钮只调用 {@link #toggle}。</p>
 */
public final class PanelManager {
    private final UIElement layer;
    private final List<FloatingPanel> open = new ArrayList<>();
    private int z = UiMetrics.PANEL_Z;

    public PanelManager(UIElement layer) {
        this.layer = layer;
    }

    public void attach(FloatingPanel panel) {
        if (panel.getParent() == null) {
            this.layer.addChild(panel);
        }
    }

    public void open(FloatingPanel panel, UIElement anchor, UiContext context) {
        attach(panel);
        panel.attach(this, context);
        panel.openAt(anchor);
        if (!this.open.contains(panel)) {
            this.open.add(panel);
        }
        bringToFront(panel);
    }

    public void toggle(FloatingPanel panel, UIElement anchor, UiContext context) {
        if (panel.isOpen()) {
            close(panel);
        } else {
            open(panel, anchor, context);
        }
    }

    public void close(FloatingPanel panel) {
        panel.close();
    }

    public void onClosed(FloatingPanel panel) {
        this.open.remove(panel);
    }

    public void bringToFront(FloatingPanel panel) {
        panel.style(style -> style.zIndex(++this.z));
    }

    public void closeAll() {
        for (FloatingPanel panel : new ArrayList<>(this.open)) {
            panel.close();
        }
        this.open.clear();
    }
}
