package com.teameway.bnvilmb.ui.widget;

import com.lowdragmc.lowdraglib2.LDLib2;
import com.lowdragmc.lowdraglib2.gui.sync.bindings.impl.SupplierDataSource;
import com.lowdragmc.lowdraglib2.gui.texture.SDFRectTexture;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.data.TextWrap;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Label;
import com.lowdragmc.lowdraglib2.gui.ui.event.UIEvents;
import com.teameway.bnvilmb.ui.UiMetrics;
import com.teameway.bnvilmb.ui.controller.ControllerUiData;
import com.teameway.bnvilmb.util.NumberFormatUtil;
import dev.vfyjxf.taffy.style.FlexDirection;
import dev.vfyjxf.taffy.style.TaffyPosition;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

import java.util.Locale;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * 线程卡片
 *
 * <p>四行信息：线程号、并行、进度、已用与总时长。进度底衬是绝对定位的一层，
 * 不参与文字排版，颜色只改 alpha，不触发重新测量。</p>
 */
public final class ThreadCard extends UIElement {
    private static final int TINT_COLOR = 0x3E6B4A;
    private static final int TINT_MAX_ALPHA = 0x99;

    private final ControllerUiData data;
    private final int slot;
    private final SDFRectTexture tint = SDFRectTexture.of(0x00000000).setRadius(UiMetrics.CARD_RADIUS);

    public ThreadCard(ControllerUiData data, int slot, Consumer<ThreadCard> onClick) {
        this.data = data;
        this.slot = slot;
        addClass("bnvil-thread-card");
        layout(layout -> layout
            .height(UiMetrics.CARD_HEIGHT)
            .flexDirection(FlexDirection.COLUMN)
            .paddingAll(UiMetrics.CARD_PADDING)
            .gapAll(1));

        UIElement tintLayer = new UIElement()
            .layout(layout -> layout
                .positionType(TaffyPosition.ABSOLUTE)
                .left(1)
                .top(1)
                .right(1)
                .bottom(1))
            .setAllowHitTest(false);
        tintLayer.getStyle().background(this.tint);

        Label title = bound(() -> Component.translatable(
            "gui.bnvil_multiblock.controller.thread.title", this.slot
        ));
        title.addClass("bnvil-title");
        Label parallel = bound(() -> Component.translatable(
            "gui.bnvil_multiblock.controller.thread.parallel", this.data.batchAt(this.slot)
        ));
        Label progress = bound(() -> Component.translatable(
            "gui.bnvil_multiblock.controller.thread.progress", percent(this.data.progressAt(this.slot))
        ));
        Label time = bound(this::timeText);
        clip(title);
        clip(parallel);
        clip(progress);
        clip(time);

        addChildren(tintLayer, title, parallel, progress, time);
        addEventListener(UIEvents.CLICK, event -> onClick.accept(this));
        addEventListener(UIEvents.TICK, event -> {
            if (LDLib2.isServer()) {
                return;
            }
            float progressValue = Mth.clamp(this.data.progressAt(this.slot), 0.0F, 1.0F);
            int alpha = Math.round(progressValue * TINT_MAX_ALPHA);
            this.tint.setColor(alpha << 24 | TINT_COLOR);
        });
    }

    public void setSelected(boolean selected) {
        if (selected) {
            addClass("bnvil-thread-selected");
        } else {
            removeClass("bnvil-thread-selected");
        }
    }

    private static Label bound(Supplier<Component> supplier) {
        Label label = new Label();
        label.bindDataSource(SupplierDataSource.of(supplier));
        return label;
    }

    private static void clip(Label label) {
        label.layout(layout -> layout.widthPercent(100).height(UiMetrics.LINE));
        label.textStyle(style -> style.adaptiveWidth(false).textWrap(TextWrap.HIDE));
        label.setOverflowVisible(false);
    }

    private static String percent(float progress) {
        return String.format(Locale.ROOT, "%.0f", progress * 100.0F);
    }

    private Component timeText() {
        long total = Math.max(0L, this.data.durationAt(this.slot));
        long elapsed = Math.max(0L, Math.round(this.data.progressAt(this.slot) * total));
        if (total < 20L) {
            return Component.translatable(
                "gui.bnvil_multiblock.controller.thread.time.ticks",
                NumberFormatUtil.compact(elapsed), NumberFormatUtil.compact(total)
            );
        }
        return Component.translatable(
            "gui.bnvil_multiblock.controller.thread.time.seconds",
            String.format(Locale.ROOT, "%.2f", elapsed / 20.0F),
            String.format(Locale.ROOT, "%.2f", total / 20.0F)
        );
    }
}
