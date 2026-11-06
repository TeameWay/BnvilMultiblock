package com.teameway.bnvilmb.ui.controller;

import com.lowdragmc.lowdraglib2.LDLib2;
import com.lowdragmc.lowdraglib2.gui.sync.bindings.impl.SupplierDataSource;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.data.TextWrap;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Label;
import com.lowdragmc.lowdraglib2.gui.ui.event.UIEvents;
import com.teameway.bnvilmb.ui.UiContext;
import com.teameway.bnvilmb.ui.UiMetrics;
import com.teameway.bnvilmb.ui.shell.MainSection;
import com.teameway.bnvilmb.ui.widget.RecipeViewElement;
import dev.vfyjxf.taffy.style.FlexDirection;
import net.minecraft.network.chat.Component;

import java.util.function.Supplier;

/**
 * 控制器 main 的上半部分
 *
 * <p>标题之外的信息分成两块，都套与物品栏等宽的框：上面是状态、形态、并行、线程，
 * 下面是 0 号线程的配方。两块之间用一条分割线隔开。整段放进滚动区，配方再长也不撑高 main。</p>
 */
public final class ControllerMainSection extends MainSection {
    private final ControllerUiData data;
    private String currentClass = "";

    public ControllerMainSection(ControllerUiData data) {
        this.data = data;
    }

    @Override
    public boolean scrollable() {
        return true;
    }

    @Override
    public UIElement create(UiContext context) {
        UIElement column = new UIElement().layout(layout -> layout
            .widthPercent(100)
            .flexDirection(FlexDirection.COLUMN)
            .gapAll(0));

        Label state = line(() -> this.data.stateText());
        this.currentClass = stateClass();
        state.addClass(this.currentClass);

        UIElement infoBox = new UIElement().addClass("bnvil-info-box");
        infoBox.layout(layout -> layout
            .widthPercent(100)
            .flexDirection(FlexDirection.COLUMN)
            .paddingAll(UiMetrics.INFO_BOX_PADDING)
            .gapAll(UiMetrics.LINE_GAP));
        infoBox.addChildren(
            state,
            hint(() -> this.data.variantsText()),
            hint(() -> Component.translatable(
                "gui.bnvil_multiblock.controller.info.parallel", this.data.parallelSlots()
            )),
            hint(() -> Component.translatable(
                "gui.bnvil_multiblock.controller.info.threads", this.data.threadSlots()
            ))
        );
        column.addChild(infoBox);

        UIElement divider = new UIElement().addClass("bnvil-divider");
        divider.layout(layout -> layout
            .widthPercent(100)
            .height(UiMetrics.DIVIDER_HEIGHT)
            .marginTop(UiMetrics.DIVIDER_GAP)
            .marginBottom(UiMetrics.DIVIDER_GAP));
        column.addChild(divider);

        UIElement recipeBox = new UIElement().addClass("bnvil-info-box");
        recipeBox.layout(layout -> layout
            .widthPercent(100)
            .flexDirection(FlexDirection.COLUMN)
            .paddingAll(UiMetrics.INFO_BOX_PADDING));
        recipeBox.addChild(new RecipeViewElement(this.data, 0));
        column.addChild(recipeBox);

        column.addEventListener(UIEvents.TICK, event -> {
            if (LDLib2.isServer()) {
                return;
            }
            String next = stateClass();
            if (!next.equals(this.currentClass)) {
                state.removeClasses("bnvil-state-idle", "bnvil-state-working", "bnvil-state-error");
                state.addClass(next);
                this.currentClass = next;
            }
        });
        return column;
    }

    private String stateClass() {
        if (!this.data.formed()) {
            return this.data.structureError().isEmpty() ? "bnvil-state-idle" : "bnvil-state-error";
        }
        return this.data.working() ? "bnvil-state-working" : "bnvil-state-idle";
    }

    private static Label line(Supplier<Component> supplier) {
        Label label = new Label();
        label.bindDataSource(SupplierDataSource.of(supplier));
        label.textStyle(style -> style.adaptiveWidth(false).textWrap(TextWrap.HIDE));
        label.setOverflowVisible(false);
        label.layout(layout -> layout.widthPercent(100).height(UiMetrics.LINE));
        return label;
    }

    private static Label hint(Supplier<Component> supplier) {
        Label label = line(supplier);
        label.addClass("bnvil-hint");
        return label;
    }
}
