package com.teameway.bnvilmb.ui.button;

import com.lowdragmc.lowdraglib2.gui.ui.elements.Button;
import com.teameway.bnvilmb.ui.UiContext;
import com.teameway.bnvilmb.ui.UiMetrics;
import com.teameway.bnvilmb.ui.panel.FloatingPanel;
import net.minecraft.network.chat.Component;

/**
 * main 左侧的按钮
 *
 * <p>按钮只负责自己的外观、优先级和浮窗工厂。排序、点击与浮窗生命周期交给
 * {@link SideButtonBar} 和 {@code PanelManager}。</p>
 */
public abstract class SideButton extends Button {
    public static final int PRIORITY_THREAD = 300;
    public static final int PRIORITY_PORTS = 200;
    public static final int PRIORITY_DEFAULT = 100;

    private final String id;
    private final int priority;
    private FloatingPanel panel;

    protected SideButton(String id, int priority, String label) {
        this.id = id;
        this.priority = priority;
        setText(Component.literal(label));
        textStyle(style -> style.fontSize(8));
        layout(layout -> layout.width(UiMetrics.SIDE_BUTTON).height(UiMetrics.SIDE_BUTTON));
        addClass("bnvil-side-button");
    }

    public final String id() {
        return this.id;
    }

    /**
     * @return 排序权重，数值越大越靠上
     */
    public final int priority() {
        return this.priority;
    }

    /**
     * @param context 界面上下文
     * @return 当前界面是否显示这个按钮
     */
    public abstract boolean isAvailable(UiContext context);

    /**
     * @param context 界面上下文
     * @return 按钮对应的浮窗
     */
    protected abstract FloatingPanel createPanel(UiContext context);

    final FloatingPanel panel(UiContext context) {
        if (this.panel == null) {
            this.panel = createPanel(context);
        }
        return this.panel;
    }
}
