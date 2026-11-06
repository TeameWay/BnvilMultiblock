package com.teameway.bnvilmb.ui.shell;

import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.data.Horizontal;
import com.lowdragmc.lowdraglib2.gui.ui.data.ScrollDisplay;
import com.lowdragmc.lowdraglib2.gui.ui.data.ScrollerMode;
import com.lowdragmc.lowdraglib2.gui.ui.data.TextWrap;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Label;
import com.lowdragmc.lowdraglib2.gui.ui.elements.ScrollerView;
import com.lowdragmc.lowdraglib2.gui.ui.elements.inventory.InventorySlots;
import com.teameway.bnvilmb.ui.UiContext;
import com.teameway.bnvilmb.ui.UiMetrics;
import dev.vfyjxf.taffy.style.AlignContent;
import dev.vfyjxf.taffy.style.AlignItems;
import dev.vfyjxf.taffy.style.FlexDirection;
import net.minecraft.network.chat.Component;

/**
 * main 面板
 *
 * <p>固定尺寸，自上而下是标题、上半部分、玩家物品栏。标题不参与滚动，物品栏固定在底部，
 * 中间内容再多也只会在自己的滚动区里移动。</p>
 */
public final class MainPanel extends UIElement {

    public MainPanel(UiContext context, Component title, MainSection section, int width, int height) {
        setId("bnvil-main");
        addClass("panel_bg");
        layout(layout -> layout
            .width(width)
            .height(height)
            .flexDirection(FlexDirection.COLUMN)
            .paddingAll(UiMetrics.PADDING)
            .gapAll(0));

        Label titleLabel = new Label();
        titleLabel.setText(title);
        titleLabel.addClass("bnvil-title");
        titleLabel.textStyle(style -> style
            .adaptiveWidth(false)
            .textWrap(TextWrap.HIDE)
            .textAlignHorizontal(Horizontal.CENTER));
        titleLabel.setOverflowVisible(false);
        titleLabel.layout(layout -> layout
            .widthPercent(100)
            .height(UiMetrics.TITLE_HEIGHT));

        UIElement body = section.scrollable() ? scrollBody(context, section) : plainBody(context, section);

        InventorySlots inventory = new InventorySlots();
        inventory.layout(layout -> layout
            .width(UiMetrics.INVENTORY_WIDTH)
            .height(UiMetrics.INVENTORY_HEIGHT)
            .alignSelf(AlignItems.CENTER)
            .marginTop(UiMetrics.INVENTORY_GAP));

        addChildren(titleLabel, body, inventory);
    }

    private static UIElement plainBody(UiContext context, MainSection section) {
        UIElement body = new UIElement().layout(layout -> layout
            .widthPercent(100)
            .flex(1)
            .minHeight(0)
            .marginTop(UiMetrics.TITLE_GAP)
            .flexDirection(FlexDirection.COLUMN)
            .justifyContent(section.centerContent() ? AlignContent.CENTER : AlignContent.FLEX_START)
            .alignItems(section.centerContent() ? AlignItems.CENTER : AlignItems.FLEX_START));
        body.addChild(section.create(context));
        return body;
    }

    private static UIElement scrollBody(UiContext context, MainSection section) {
        ScrollerView scroll = new ScrollerView();
        scroll.scrollerStyle(style -> style
            .mode(ScrollerMode.VERTICAL)
            .verticalScrollDisplay(ScrollDisplay.AUTO));
        UIElement content = section.create(context);
        content.layout(layout -> layout.widthPercent(100));
        scroll.addScrollViewChild(content);
        scroll.layout(layout -> layout
            .widthPercent(100)
            .flex(1)
            .minHeight(0)
            .marginTop(UiMetrics.TITLE_GAP));
        return scroll;
    }
}
