package com.teameway.bnvilmb.ui.port;

import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Label;
import com.teameway.bnvilmb.ui.UiContext;
import com.teameway.bnvilmb.ui.UiMetrics;
import com.teameway.bnvilmb.ui.shell.MainSection;
import com.teameway.bnvilmb.ui.widget.SlotGrid;
import dev.vfyjxf.taffy.style.FlexDirection;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;

/**
 * 物品总线的 main 上半部分
 *
 * <p>整体套一层与物品栏等宽的方框，框内 9 格一行，从上到下排列；槽位多时由
 * MainPanel 的滚动区承接。</p>
 */
public final class ItemBusSection extends MainSection {
    private final PortUiData data;

    public ItemBusSection(PortUiData data) {
        this.data = data;
    }

    @Override
    public boolean scrollable() {
        return true;
    }

    @Override
    public UIElement create(UiContext context) {
        ResourceHandler<ItemResource> handler = this.data.itemHandler();
        if (handler == null || handler.size() == 0) {
            Label label = new Label();
            label.setText(Component.translatable("gui.bnvil_multiblock.port.slots", 0));
            label.addClass("bnvil-hint");
            return label;
        }
        UIElement box = new UIElement().addClass("bnvil-item-box");
        box.layout(layout -> layout
            .width(UiMetrics.INVENTORY_WIDTH)
            .flexDirection(FlexDirection.COLUMN));
        box.addChild(new SlotGrid(handler, this.data.slotCount(), UiMetrics.ITEM_COLUMNS));
        return box;
    }
}
