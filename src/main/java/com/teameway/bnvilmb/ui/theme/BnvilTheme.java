package com.teameway.bnvilmb.ui.theme;

import com.lowdragmc.lowdraglib2.gui.ui.UI;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.style.StylesheetManager;
import com.teameway.bnvilmb.BnvilMultiBlock;
import net.minecraft.resources.Identifier;

/**
 * 界面主题
 *
 * <p>{@code lss/bnvil.lss}：上半部分是基底控件外观，下半部分是本模组的界面规则</p>
 */
public final class BnvilTheme {
    private static final Identifier THEME = BnvilMultiBlock.id("lss/bnvil.lss");

    private BnvilTheme() {
    }

    /**
     * 给界面套上主题
     *
     * @param root 界面根元素
     * @return 套用 {@code lss/bnvil.lss} 的界面
     */
    public static UI of(UIElement root) {
        return UI.of(root, StylesheetManager.INSTANCE.getStylesheetSafe(THEME));
    }
}
