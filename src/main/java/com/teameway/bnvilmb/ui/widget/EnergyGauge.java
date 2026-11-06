package com.teameway.bnvilmb.ui.widget;

import com.lowdragmc.lowdraglib2.gui.sync.bindings.impl.SupplierDataSource;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.data.TextWrap;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Label;
import com.lowdragmc.lowdraglib2.gui.ui.elements.ProgressBar;
import com.lowdragmc.lowdraglib2.gui.ui.event.HoverTooltips;
import com.lowdragmc.lowdraglib2.gui.ui.event.UIEvents;
import com.teameway.bnvilmb.ui.UiMetrics;
import com.teameway.bnvilmb.util.NumberFormatUtil;
import dev.vfyjxf.taffy.style.AlignItems;
import dev.vfyjxf.taffy.style.FlexDirection;
import net.minecraft.network.chat.Component;

import java.util.function.Supplier;

/**
 * 能源仓读数
 *
 * <p>三行文本加一条进度条。数值按千位缩写，进度条固定在 192×18。</p>
 */
public final class EnergyGauge extends UIElement {
    public EnergyGauge(Supplier<Long> stored, Supplier<Long> capacity) {
        layout(layout -> layout
            .width(UiMetrics.ENERGY_BAR_WIDTH)
            .flexDirection(FlexDirection.COLUMN)
            .gapAll(2)
            .alignItems(AlignItems.FLEX_START));
        addChild(line(() -> Component.translatable(
            "gui.bnvil_multiblock.energy.current", NumberFormatUtil.compact(stored.get())
        )));
        addChild(line(() -> Component.translatable(
            "gui.bnvil_multiblock.energy.capacity", NumberFormatUtil.compact(capacity.get())
        )));
        addChild(line(() -> Component.translatable(
            "gui.bnvil_multiblock.energy.percent", percent(stored.get(), capacity.get())
        )));

        ProgressBar bar = new ProgressBar();
        bar.setRange(0.0F, 1.0F);
        bar.bindDataSource(SupplierDataSource.of(() -> {
            long max = capacity.get();
            return max <= 0L ? 0.0F : Math.min(1.0F, (float) ((double) stored.get() / (double) max));
        }));
        bar.layout(layout -> layout
            .width(UiMetrics.ENERGY_BAR_WIDTH)
            .height(UiMetrics.ENERGY_BAR_HEIGHT)
            .alignSelf(AlignItems.CENTER));
        // 进度条默认会显示占位文字 Label，这里隐藏；悬浮时给出不换算单位的当前 FE
        bar.label(label -> label.setDisplay(false));
        bar.addEventListener(UIEvents.HOVER_TOOLTIPS, event -> event.hoverTooltips =
            HoverTooltips.create(Component.translatable(
                "gui.bnvil_multiblock.energy.current.raw",
                NumberFormatUtil.grouped(stored.get())
            )));
        addChild(bar);
    }

    private static Label line(Supplier<Component> supplier) {
        Label label = new Label();
        label.bindDataSource(SupplierDataSource.of(supplier));
        label.textStyle(style -> style.adaptiveWidth(false).textWrap(TextWrap.HIDE));
        label.setOverflowVisible(false);
        label.layout(layout -> layout.widthPercent(100).height(UiMetrics.LINE));
        return label;
    }

    private static String percent(long stored, long capacity) {
        if (capacity <= 0L) {
            return "0";
        }
        long value = (long) Math.min(100.0, Math.max(0.0, (double) stored * 100.0 / (double) capacity));
        return Long.toString(value);
    }
}
