package com.teameway.bnvilmb.ui.widget;

import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.data.TextWrap;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Label;
import com.lowdragmc.lowdraglib2.gui.ui.event.UIEvents;
import com.teameway.bnvilmb.ui.UiMetrics;
import com.teameway.bnvilmb.ui.nav.UiNavigation;
import dev.vfyjxf.taffy.style.FlexDirection;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;

import java.util.List;

/**
 * 端口或控制器的列表卡片
 *
 * <p>第一行是名称，第二行是坐标。按住 shift 点击会打开对应方块界面。</p>
 */
public final class PortCard extends UIElement {
    public record Entry(Component title, Component subtitle, BlockPos pos, List<Component> tooltips) {
    }

    public PortCard(Entry entry, UiNavigation navigation) {
        addClass("bnvil-port-card");
        layout(layout -> layout
            .height(24)
            .flexDirection(FlexDirection.COLUMN)
            .paddingAll(2)
            .gapAll(0));
        Label title = label(entry.title());
        title.addClass("bnvil-title");
        Label subtitle = label(entry.subtitle());
        subtitle.addClass("bnvil-hint");
        addChildren(title, subtitle);
        if (!entry.tooltips().isEmpty()) {
            style(style -> style.tooltips(entry.tooltips().toArray(Component[]::new)));
        }
        addEventListener(UIEvents.CLICK, event -> {
            if (event.isShiftDown()) {
                navigation.openBlockUi(entry.pos());
                event.stopPropagation();
            }
        });
    }

    private static Label label(Component text) {
        Label label = new Label();
        label.setText(text);
        label.layout(layout -> layout.widthPercent(100).height(UiMetrics.LINE));
        label.textStyle(style -> style.adaptiveWidth(false).textWrap(TextWrap.HIDE));
        label.setOverflowVisible(false);
        return label;
    }
}
