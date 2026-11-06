package com.teameway.bnvilmb.ui.port;

import com.lowdragmc.lowdraglib2.gui.sync.bindings.impl.SupplierDataSource;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.data.TextWrap;
import com.lowdragmc.lowdraglib2.gui.ui.elements.FluidSlot;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Label;
import com.lowdragmc.lowdraglib2.utils.FluidHelper;
import com.teameway.bnvilmb.ui.UiContext;
import com.teameway.bnvilmb.ui.UiMetrics;
import com.teameway.bnvilmb.ui.shell.MainSection;
import com.teameway.bnvilmb.util.NumberFormatUtil;
import dev.vfyjxf.taffy.style.FlexDirection;
import dev.vfyjxf.taffy.style.TaffyPosition;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.fluid.FluidResource;

/**
 * 流体仓的 main 上半部分
 *
 * <p>128×96 的方框居中，左侧三行是“流体总量:”、数量和流体名称，右下角是 18×18 流体槽。
 * 数量直接显示 mB 原值并做千位分隔，不写单位。</p>
 */
public final class FluidHatchSection extends MainSection {
    private final PortUiData data;

    public FluidHatchSection(PortUiData data) {
        this.data = data;
    }

    @Override
    public boolean centerContent() {
        return true;
    }

    @Override
    public UIElement create(UiContext context) {
        ResourceHandler<FluidResource> handler = this.data.fluidHandler();
        if (handler == null || handler.size() == 0) {
            Label label = new Label();
            label.setText(Component.translatable("gui.bnvil_multiblock.port.fluid_empty"));
            label.addClass("bnvil-hint");
            return label;
        }

        UIElement box = new UIElement().addClass("bnvil-fluid-box");
        box.layout(layout -> layout
            .width(UiMetrics.FLUID_BOX_WIDTH)
            .height(UiMetrics.FLUID_BOX_HEIGHT)
            .positionType(TaffyPosition.RELATIVE));

        UIElement text = new UIElement().layout(layout -> layout
            .positionType(TaffyPosition.ABSOLUTE)
            .left(4)
            .top(4)
            .width(92)
            .flexDirection(FlexDirection.COLUMN)
            .gapAll(1));
        Label total = new Label();
        total.setText(Component.translatable("gui.bnvil_multiblock.port.fluid_total"));
        total.textStyle(style -> style.adaptiveWidth(false).textWrap(TextWrap.HIDE));
        total.setOverflowVisible(false);
        total.layout(layout -> layout.widthPercent(100).height(UiMetrics.LINE));
        Label amount = new Label();
        amount.textStyle(style -> style.adaptiveWidth(false).textWrap(TextWrap.HIDE));
        amount.setOverflowVisible(false);
        amount.layout(layout -> layout.widthPercent(100).height(UiMetrics.LINE));
        Label name = new Label();
        name.layout(layout -> layout.widthPercent(100).height(UiMetrics.LINE));
        name.textStyle(style -> style
            .fontSize(8)
            .adaptiveWidth(false)
            .textWrap(TextWrap.HIDE));
        name.setOverflowVisible(false);
        text.addChildren(total, amount, name);

        FluidSlot slot = new FluidSlot();
        slot.bind(handler, 0);
        slot.layout(layout -> layout
            .positionType(TaffyPosition.ABSOLUTE)
            .right(4)
            .bottom(4)
            .width(UiMetrics.SLOT)
            .height(UiMetrics.SLOT));
        slot.amountLabel.setDisplay(false);

        amount.bindDataSource(SupplierDataSource.of(
            () -> Component.literal(NumberFormatUtil.grouped(slot.getFluid().getAmount()))
        ));
        name.bindDataSource(SupplierDataSource.of(
            () -> slot.getFluid().isEmpty()
                  ? Component.translatable("gui.bnvil_multiblock.port.fluid_empty")
                  : FluidHelper.getDisplayName(slot.getFluid())
        ));

        box.addChildren(text, slot);
        return box;
    }
}
