package com.teameway.bnvilmb.ui.panel;

import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.data.ScrollDisplay;
import com.lowdragmc.lowdraglib2.gui.ui.data.ScrollerMode;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Button;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Label;
import com.lowdragmc.lowdraglib2.gui.ui.elements.ScrollerView;
import com.lowdragmc.lowdraglib2.gui.ui.event.UIEvents;
import com.lowdragmc.lowdraglib2.gui.util.WindowDragHelper;
import com.teameway.bnvilmb.ui.UiContext;
import com.teameway.bnvilmb.ui.UiMetrics;
import dev.vfyjxf.taffy.style.AlignItems;
import dev.vfyjxf.taffy.style.FlexDirection;
import dev.vfyjxf.taffy.style.TaffyPosition;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

import java.util.ArrayList;
import java.util.List;

/**
 * 可拖动浮窗
 *
 * <p>标题栏负责拖动，正文放进纵向滚动视图。浮窗默认隐藏，打开时按锚点放到按钮左侧，
 * 越界后夹进屏幕。关闭父浮窗会一起关闭子浮窗。</p>
 */
public abstract class FloatingPanel extends UIElement {
    private final String panelId;
    private final int panelWidth;
    private final int maxHeight;
    private final List<FloatingPanel> children = new ArrayList<>();
    private FloatingPanel parent;
    private PanelManager manager;
    private Label titleLabel;
    private boolean built;

    protected FloatingPanel(String panelId, Component title, int panelWidth, int maxHeight) {
        this.panelId = panelId;
        this.panelWidth = panelWidth;
        this.maxHeight = maxHeight;
        setId(panelId);
        addClass("bnvil-floating-panel");
        layout(layout -> layout
            .positionType(TaffyPosition.ABSOLUTE)
            .width(panelWidth)
            .height(maxHeight)
            .flexDirection(FlexDirection.COLUMN)
            .paddingAll(UiMetrics.PANEL_PADDING)
            .gapAll(0));
        setVisible(false);
        style(style -> style.zIndex(UiMetrics.PANEL_Z));
        this.titleLabel = new Label();
        this.titleLabel.setText(title);
    }

    /**
     * @param context 界面上下文
     * @return 正文内容
     */
    protected abstract UIElement buildBody(UiContext context);

    final void attach(PanelManager manager, UiContext context) {
        this.manager = manager;
        if (this.built) {
            return;
        }
        this.built = true;
        UIElement titleBar = new UIElement().layout(layout -> layout
            .widthPercent(100)
            .height(UiMetrics.TITLE_BAR_HEIGHT)
            .flexDirection(FlexDirection.ROW)
            .alignItems(AlignItems.CENTER)
            .gapAll(2))
            .addClass("bnvil-floating-title");
        this.titleLabel.addClass("bnvil-title");
        this.titleLabel.textStyle(style -> style.adaptiveWidth(false));
        this.titleLabel.layout(layout -> layout.flex(1).height(UiMetrics.LINE));
        Button close = new Button();
        close.setText(Component.literal("x"));
        close.addClass("bnvil-floating-close");
        close.textStyle(style -> style.fontSize(8));
        close.layout(layout -> layout.width(12).height(12));
        close.setOnClick(event -> close());
        close.addEventListener(UIEvents.MOUSE_DOWN, event -> event.stopPropagation());
        titleBar.addChildren(this.titleLabel, close);
        // 关闭按钮自己处理点击，不在它身上启动拖动
        WindowDragHelper.setDragMove(titleBar, this, event -> !(event.target instanceof Button), null);
        titleBar.addEventListener(UIEvents.DRAG_SOURCE_UPDATE, event -> clampToScreen());

        ScrollerView scroll = new ScrollerView();
        scroll.scrollerStyle(style -> style
            .mode(ScrollerMode.VERTICAL)
            .verticalScrollDisplay(ScrollDisplay.AUTO)
            .horizontalScrollDisplay(ScrollDisplay.NEVER));
        UIElement body = buildBody(context);
        body.layout(layout -> layout.widthPercent(100));
        scroll.addScrollViewChild(body);
        scroll.layout(layout -> layout
            .widthPercent(100)
            .flex(1)
            .minHeight(0)
            .marginTop(2));
        addChildren(titleBar, scroll);
        addEventListener(UIEvents.MOUSE_DOWN, event -> this.manager.bringToFront(this), true);
        addEventListener(UIEvents.LAYOUT_CHANGED, event -> {
            if (isOpen()) {
                clampToScreen();
            }
        });
        for (FloatingPanel child : this.children) {
            this.manager.attach(child);
        }
    }

    protected final PanelManager manager() {
        return this.manager;
    }

    public final boolean isOpen() {
        return isVisible();
    }

    public final void openAt(UIElement anchor) {
        if (this.manager == null) {
            return;
        }
        setVisible(true);
        this.manager.bringToFront(this);
        positionAt(anchor);
        onOpened();
    }

    public final void toggleAt(UIElement anchor) {
        if (isOpen()) {
            close();
        } else {
            openAt(anchor);
        }
    }

    public final void close() {
        for (FloatingPanel child : new ArrayList<>(this.children)) {
            child.close();
        }
        setVisible(false);
        if (this.manager != null) {
            this.manager.onClosed(this);
        }
        onClosed();
    }

    /**
     * 打开后调用，子类可在这里刷新列表
     */
    protected void onOpened() {
    }

    /**
     * 关闭后调用
     */
    protected void onClosed() {
    }

    public final void addChildPanel(FloatingPanel child) {
        child.parent = this;
        this.children.add(child);
        if (this.manager != null) {
            this.manager.attach(child);
        }
    }

    public final void setTitle(Component title) {
        this.titleLabel.setText(title);
    }

    private void positionAt(UIElement anchor) {
        var mui = getModularUI();
        int x;
        int y;
        if (mui != null) {
            // 锚点可能嵌在按钮栏或父浮窗里，先换算成相对根元素的位置
            float anchorX = anchor.getPositionX() - mui.getLeftPos();
            float anchorY = anchor.getPositionY() - mui.getTopPos();
            x = Math.round(anchorX - this.panelWidth - UiMetrics.PANEL_GAP);
            y = Math.round(anchorY);
            float screenX = mui.getLeftPos() + x;
            float screenY = mui.getTopPos() + y;
            screenX = Mth.clamp(screenX, 0, Math.max(0, mui.getScreenWidth() - this.panelWidth));
            screenY = Mth.clamp(screenY, 0, Math.max(0, mui.getScreenHeight() - getSizeHeight()));
            x = Math.round(screenX - mui.getLeftPos());
            y = Math.round(screenY - mui.getTopPos());
        } else {
            x = Math.round(anchor.getLayoutX() - this.panelWidth - UiMetrics.PANEL_GAP);
            y = Math.round(anchor.getLayoutY());
        }
        int targetX = x;
        int targetY = y;
        layout(layout -> layout.left(targetX).top(targetY));
    }

    private void clampToScreen() {
        var mui = getModularUI();
        if (mui == null) {
            return;
        }
        float x = getLayoutX();
        float y = getLayoutY();
        float screenX = mui.getLeftPos() + x;
        float screenY = mui.getTopPos() + y;
        screenX = Mth.clamp(screenX, 0, Math.max(0, mui.getScreenWidth() - getSizeWidth()));
        screenY = Mth.clamp(screenY, 0, Math.max(0, mui.getScreenHeight() - getSizeHeight()));
        float nextX = screenX - mui.getLeftPos();
        float nextY = screenY - mui.getTopPos();
        if (nextX != x || nextY != y) {
            layout(layout -> layout.left(nextX).top(nextY));
        }
    }
}
