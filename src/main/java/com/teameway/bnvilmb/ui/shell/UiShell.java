package com.teameway.bnvilmb.ui.shell;

import com.lowdragmc.lowdraglib2.gui.ui.ModularUI;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.teameway.bnvilmb.ui.UiContext;
import com.teameway.bnvilmb.ui.button.SideButtonBar;
import com.teameway.bnvilmb.ui.panel.PanelManager;
import com.teameway.bnvilmb.ui.theme.BnvilTheme;
import dev.vfyjxf.taffy.style.TaffyPosition;

import java.util.function.Consumer;

/**
 * 界面外壳
 *
 * <p>根元素与 main 同尺寸，main 占满根元素，按钮栏和浮层用绝对定位放在根元素之外。
 * LDLib2 会把固定尺寸的根元素垂直居中，main 因此不会被按钮或浮窗挤压。</p>
 */
public final class UiShell {
    private final UIElement root;
    private final PanelManager panels;
    private final SideButtonBar buttons;
    private final UiContext context;

    private UiShell(UiContext context, int width, int height) {
        this.context = context;
        this.root = new UIElement().setId("bnvil-ui")
            .layout(layout -> layout.width(width).height(height));
        UIElement panelLayer = new UIElement().setId("bnvil-panel-layer")
            .setAllowHitTest(false)
            .layout(layout -> layout
                .positionType(TaffyPosition.ABSOLUTE)
                .left(0)
                .top(0)
                .widthPercent(100)
                .heightPercent(100));
        this.panels = new PanelManager(panelLayer);
        this.buttons = new SideButtonBar(context, this.panels);
        this.root.addChildren(this.buttons, panelLayer);
    }

    public static UiShell create(UiContext context, int width, int height) {
        return new UiShell(context, width, height);
    }

    public UiShell main(MainPanel main) {
        this.root.addChildAt(main, 0);
        return this;
    }

    public UiShell buttons(Consumer<SideButtonBar> config) {
        config.accept(this.buttons);
        return this;
    }

    public UIElement root() {
        return this.root;
    }

    public PanelManager panels() {
        return this.panels;
    }

    public ModularUI toModularUI() {
        return new ModularUI(BnvilTheme.of(this.root), this.context.player());
    }
}
