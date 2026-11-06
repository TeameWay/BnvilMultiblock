package com.teameway.bnvilmb.ui.widget;

import com.lowdragmc.lowdraglib2.LDLib2;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Label;
import com.lowdragmc.lowdraglib2.gui.ui.event.UIEvents;
import com.teameway.bnvilmb.recipe.view.RecipeView;
import com.teameway.bnvilmb.ui.UiMetrics;
import com.teameway.bnvilmb.ui.controller.ControllerUiData;
import com.teameway.bnvilmb.util.NumberFormatUtil;
import dev.vfyjxf.taffy.style.FlexDirection;
import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.Locale;

/**
 * 配方信息
 *
 * <p>按线程槽位读取配方 ID，列出输入、产出、耗时与能耗。配方或并行倍数变化时重建内容，
 * main 和配方浮窗共用同一个组件。</p>
 */
public final class RecipeViewElement extends UIElement {
    private final ControllerUiData data;
    private int slot;
    private String signature = "";

    public RecipeViewElement(ControllerUiData data, int slot) {
        this.data = data;
        this.slot = slot;
        layout(layout -> layout
            .widthPercent(100)
            .flexDirection(FlexDirection.COLUMN)
            .gapAll(UiMetrics.LINE_GAP));
        addEventListener(UIEvents.TICK, event -> {
            if (!LDLib2.isServer()) {
                refresh();
            }
        });
        refresh();
    }

    public void setSlot(int slot) {
        this.slot = slot;
        this.signature = "";
        refresh();
    }

    private void refresh() {
        String next = this.data.machineType() + '|' + this.data.recipeIdAt(this.slot) + '|' + this.data.batchAt(this.slot);
        if (next.equals(this.signature)) {
            return;
        }
        this.signature = next;
        clearAllChildren();
        if (this.data.machineType().isEmpty()) {
            addChild(hint("gui.bnvil_multiblock.controller.recipe.hint"));
            return;
        }
        String recipeId = this.data.recipeIdAt(this.slot);
        if (recipeId.isEmpty()) {
            addChild(hint("gui.bnvil_multiblock.controller.state.idle"));
            return;
        }
        RecipeView recipe = RecipeView.find(this.data.machineType(), recipeId);
        if (recipe == null) {
            addChild(hint("gui.bnvil_multiblock.controller.recipe.unavailable"));
            return;
        }
        addSection("gui.bnvil_multiblock.controller.recipe.inputs", "bnvil-input");
        addItems(recipe.itemInputs(), recipe.time(), "bnvil-input");
        addFluids(recipe.fluidInputs(), recipe.time(), "bnvil-input");
        addSection("gui.bnvil_multiblock.controller.recipe.outputs", "bnvil-output");
        addItems(recipe.itemOutputs(), recipe.time(), "bnvil-output");
        addFluids(recipe.fluidOutputs(), recipe.time(), "bnvil-output");
        addChild(text("gui.bnvil_multiblock.jei.time", seconds(recipe.time())));
        addChild(text("gui.bnvil_multiblock.jei.energy_total", recipe.energy()));
        addChild(text("gui.bnvil_multiblock.jei.energy_rate", power(recipe)));
    }

    private void addSection(String key, String styleClass) {
        Label label = text(key);
        label.addClass(styleClass);
        addChild(label);
    }

    private void addItems(List<RecipeView.ItemEntry> entries, int time, String styleClass) {
        for (RecipeView.ItemEntry entry : entries) {
            long total = entry.amount() * (long) this.batch();
            Label label = text(
                "gui.bnvil_multiblock.controller.recipe.line_item",
                NumberFormatUtil.compact(total), entry.name()
            );
            label.addClass(styleClass);
            addChild(label);
            addChild(rate(total, time, false));
        }
    }

    private void addFluids(List<RecipeView.FluidEntry> entries, int time, String styleClass) {
        for (RecipeView.FluidEntry entry : entries) {
            long total = entry.amount() * (long) this.batch();
            Label label = text(
                "gui.bnvil_multiblock.controller.recipe.line_fluid",
                NumberFormatUtil.fluidVolume(total), entry.name()
            );
            label.addClass(styleClass);
            addChild(label);
            addChild(rate(total, time, true));
        }
    }

    private Label rate(long amount, int time, boolean fluid) {
        int ticks = Math.max(1, time);
        float perTick = amount / (float) ticks;
        float perSecond = perTick * 20.0F;
        String key = fluid
                     ? "gui.bnvil_multiblock.controller.recipe.rate_fluid"
                     : "gui.bnvil_multiblock.controller.recipe.rate_item";
        Label label = text(key, NumberFormatUtil.decimal(perTick), NumberFormatUtil.decimal(perSecond));
        label.addClass("bnvil-hint");
        label.layout(layout -> layout
            .height(UiMetrics.LINE)
            .paddingLeft(6));
        return label;
    }

    private int batch() {
        return Math.max(1, this.data.batchAt(this.slot));
    }

    private Label hint(String key) {
        Label label = text(key);
        label.addClass("bnvil-hint");
        return label;
    }

    private Label text(String key, Object... args) {
        Label label = new Label();
        label.setText(Component.translatable(key, args));
        label.layout(layout -> layout.widthPercent(100).height(UiMetrics.LINE));
        return label;
    }

    private static long power(RecipeView recipe) {
        return recipe.energy() <= 0L
               ? 0L
               : Math.ceilDiv(recipe.energy(), Math.max(1, recipe.time()));
    }

    private static String seconds(int ticks) {
        return String.format(Locale.ROOT, "%.1f", ticks / 20.0F);
    }
}
