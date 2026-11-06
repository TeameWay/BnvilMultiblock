package com.teameway.bnvilmb.ui.panel;

import com.lowdragmc.lowdraglib2.LDLib2;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Label;
import com.lowdragmc.lowdraglib2.gui.ui.event.UIEvents;
import com.teameway.bnvilmb.ui.UiContext;
import com.teameway.bnvilmb.ui.UiMetrics;
import com.teameway.bnvilmb.ui.nav.UiNavigation;
import com.teameway.bnvilmb.ui.widget.PortCard;
import dev.vfyjxf.taffy.style.FlexDirection;
import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.function.Supplier;

/**
 * 端口或控制器列表浮窗
 */
public final class PortListPanel extends FloatingPanel {
    private final Supplier<List<PortCard.Entry>> entries;
    private final UiNavigation navigation;
    private UIElement list;
    private String signature;

    public PortListPanel(String panelId, Component title, Supplier<List<PortCard.Entry>> entries, UiNavigation navigation) {
        super(panelId, title, UiMetrics.PORT_PANEL_WIDTH, UiMetrics.PORT_PANEL_MAX_HEIGHT);
        this.entries = entries;
        this.navigation = navigation;
    }

    @Override
    protected UIElement buildBody(UiContext context) {
        this.list = new UIElement().layout(layout -> layout
            .widthPercent(100)
            .flexDirection(FlexDirection.COLUMN)
            .gapAll(2));
        this.rebuildIfChanged();
        addEventListener(UIEvents.TICK, event -> {
            if (!LDLib2.isServer()) {
                this.rebuildIfChanged();
            }
        });
        return this.list;
    }

    @Override
    protected void onOpened() {
        this.rebuildIfChanged();
    }

    private void rebuildIfChanged() {
        List<PortCard.Entry> current = this.entries.get();
        String next = signature(current);
        if (next.equals(this.signature)) {
            return;
        }
        this.signature = next;
        this.list.clearAllChildren();
        if (current.isEmpty()) {
            Label hint = new Label();
            hint.setText(Component.translatable("gui.bnvil_multiblock.panel.empty"));
            hint.addClass("bnvil-hint");
            hint.layout(layout -> layout.widthPercent(100).height(UiMetrics.LINE));
            this.list.addChild(hint);
            return;
        }
        for (PortCard.Entry entry : current) {
            this.list.addChild(new PortCard(entry, this.navigation));
        }
    }

    private static String signature(List<PortCard.Entry> entries) {
        StringBuilder builder = new StringBuilder();
        for (PortCard.Entry entry : entries) {
            builder.append(entry.pos().asLong())
                .append('|')
                .append(entry.title().getString())
                .append(';');
        }
        return builder.toString();
    }
}
