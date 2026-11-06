package com.teameway.bnvilmb.ui.widget;

import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.elements.ItemSlot;
import com.teameway.bnvilmb.ui.UiMetrics;
import dev.vfyjxf.taffy.style.FlexDirection;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;

/**
 * 物品槽网格
 *
 * <p>从左到右、从上到下逐个绑定处理器槽位。列数由调用方决定，物品总线用 9 列。</p>
 */
public final class SlotGrid extends UIElement {
    public SlotGrid(ResourceHandler<ItemResource> handler, int size, int columns) {
        layout(layout -> layout
            .flexDirection(FlexDirection.COLUMN)
            .gapAll(UiMetrics.SLOT_GAP));
        int count = Math.min(size, handler.size());
        for (int start = 0; start < count; start += columns) {
            UIElement row = new UIElement().layout(layout -> layout
                .flexDirection(FlexDirection.ROW)
                .height(UiMetrics.SLOT)
                .gapAll(UiMetrics.SLOT_GAP));
            for (int index = start; index < Math.min(start + columns, count); index++) {
                ItemSlot slot = new ItemSlot();
                slot.bind(handler, index);
                slot.layout(layout -> layout.width(UiMetrics.SLOT).height(UiMetrics.SLOT));
                row.addChild(slot);
            }
            addChild(row);
        }
    }
}
