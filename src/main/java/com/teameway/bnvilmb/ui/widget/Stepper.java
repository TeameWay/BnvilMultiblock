package com.teameway.bnvilmb.ui.widget;

import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Button;
import com.lowdragmc.lowdraglib2.gui.ui.elements.TextField;
import com.lowdragmc.lowdraglib2.gui.ui.event.HoverTooltips;
import com.lowdragmc.lowdraglib2.gui.ui.event.UIEvent;
import com.lowdragmc.lowdraglib2.gui.ui.event.UIEvents;
import com.teameway.bnvilmb.ui.UiMetrics;
import dev.vfyjxf.taffy.style.AlignItems;
import dev.vfyjxf.taffy.style.FlexDirection;
import net.minecraft.network.chat.Component;

import java.util.function.IntConsumer;
import java.util.function.IntSupplier;

/**
 * 88×18 的加减控件
 *
 * <p>左侧减号、中间可输入数字、右侧加号。普通点击增减 1；按住 Ctrl 点击翻倍或减半；
 * 按住 Shift 点击直接到上限或下限。数字框与按钮在边界上自动禁用或夹取。</p>
 */
public final class Stepper extends UIElement {
    private static final int DIGIT_WIDTH = 6;

    private final IntSupplier value;
    private final int max;
    private final String currentKey;
    private final IntConsumer onChange;
    private final Button minus;
    private final Button plus;
    private final TextField field;
    private int lastPadding = -1;

    public Stepper(IntSupplier value, int max, String currentKey, IntConsumer onChange) {
        this.value = value;
        this.max = Math.max(0, max);
        this.currentKey = currentKey;
        this.onChange = onChange;
        addClass("bnvil-stepper");
        layout(layout -> layout
            .width(UiMetrics.STEPPER_WIDTH)
            .height(UiMetrics.STEPPER_HEIGHT)
            .flexDirection(FlexDirection.ROW)
            .alignItems(AlignItems.CENTER));

        this.minus = new Button();
        this.minus.setText(Component.literal("-"));
        this.minus.layout(layout -> layout
            .width(UiMetrics.STEPPER_BUTTON)
            .height(UiMetrics.STEPPER_BUTTON));
        this.minus.setOnClick(this::decrease);
        this.minus.addEventListener(UIEvents.HOVER_TOOLTIPS,
            event -> event.hoverTooltips = tooltip(false));

        this.field = new TextField();
        this.field.addClass("bnvil-stepper-field");
        this.field.setNumbersOnlyInt(0, this.max);
        this.field.setValue(Integer.toString(this.value.getAsInt()), false);
        this.field.layout(layout -> layout
            .width(UiMetrics.STEPPER_WIDTH - UiMetrics.STEPPER_BUTTON * 2)
            .height(UiMetrics.STEPPER_HEIGHT)
            .paddingAll(1));
        this.centerText();
        this.field.bindObserver(text -> {
            if (text.isEmpty()) {
                return;
            }
            try {
                int parsed = Integer.parseInt(text);
                this.onChange.accept(Math.max(0, Math.min(this.max, parsed)));
                this.centerText();
            } catch (NumberFormatException ignored) {
                // 输入框本身只接受数字，这里只防极端文本
            }
        });
        this.field.addEventListener(UIEvents.HOVER_TOOLTIPS, event -> event.hoverTooltips =
            HoverTooltips.create(
                Component.translatable(this.currentKey, this.value.getAsInt()),
                Component.translatable("gui.bnvil_multiblock.control.max", this.max)
            ));

        this.plus = new Button();
        this.plus.setText(Component.literal("+"));
        this.plus.layout(layout -> layout
            .width(UiMetrics.STEPPER_BUTTON)
            .height(UiMetrics.STEPPER_BUTTON));
        this.plus.setOnClick(this::increase);
        this.plus.addEventListener(UIEvents.HOVER_TOOLTIPS,
            event -> event.hoverTooltips = tooltip(true));

        addChildren(this.minus, this.field, this.plus);
        addEventListener(UIEvents.TICK, event -> {
            int current = this.value.getAsInt();
            this.minus.setActive(current > 0);
            this.plus.setActive(current < this.max);
            if (!this.field.isFocused()) {
                String text = Integer.toString(current);
                if (!text.equals(this.field.getValue())) {
                    this.field.setValue(text, false);
                }
                this.centerText();
            }
        });
    }

    /**
     * 按当前数字长度调整左内边距，让数字在输入框里居中
     */
    private void centerText() {
        int width = (int) this.field.getSizeWidth();
        if (width <= 0) {
            width = UiMetrics.STEPPER_WIDTH - UiMetrics.STEPPER_BUTTON * 2;
        }
        String text = this.field.getValue();
        int textWidth = Math.min(width - 2, text.length() * DIGIT_WIDTH);
        int padding = Math.max(1, (width - textWidth) / 2);
        if (padding == this.lastPadding) {
            return;
        }
        this.lastPadding = padding;
        this.field.layout(layout -> layout.paddingLeft(padding));
    }

    private void decrease(UIEvent event) {
        int current = this.value.getAsInt();
        if (event.isCtrlDown()) {
            this.onChange.accept(Math.max(0, current / 2));
        } else if (event.isShiftDown()) {
            this.onChange.accept(0);
        } else {
            this.onChange.accept(Math.max(0, current - 1));
        }
    }

    private void increase(UIEvent event) {
        int current = this.value.getAsInt();
        if (event.isCtrlDown()) {
            this.onChange.accept((int) Math.min((long) this.max, (long) current * 2L));
        } else if (event.isShiftDown()) {
            this.onChange.accept(this.max);
        } else {
            this.onChange.accept(Math.min(this.max, current + 1));
        }
    }

    private HoverTooltips tooltip(boolean plus) {
        if (UIElement.isCtrlOrCmdDown()) {
            return HoverTooltips.create(Component.translatable(
                plus
                ? "gui.bnvil_multiblock.control.double"
                : "gui.bnvil_multiblock.control.half"
            ));
        }
        if (UIElement.isShiftDown()) {
            return HoverTooltips.create(Component.translatable(
                plus
                ? "gui.bnvil_multiblock.control.to_max"
                : "gui.bnvil_multiblock.control.to_min"
            ));
        }
        return HoverTooltips.create(Component.literal(plus ? "+1" : "-1"));
    }
}
