package com.teameway.bnvilmb.gui;

import com.lowdragmc.lowdraglib2.gui.ui.UI;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.style.StylesheetManager;
import com.teameway.bnvilmb.BnvilMultiBlock;
import net.minecraft.resources.Identifier;

/**
 * 界面主题
 *
 * <p>全模组共用一份 {@code lss/bnvil.lss}：文件上半部分是基底控件外观，
 * 下半部分是本模组的界面规则，合在一起是为了让配色只有一处可改。</p>
 *
 * <p>样式由 {@link StylesheetManager} 在资源重载时注册，改完按 {@code F3 + T} 生效。
 * 覆盖已有规则时要靠 id 或类权重，不要靠书写顺序。</p>
 */
public class BnvilTheme {

    private static final Identifier THEME = BnvilMultiBlock.id("lss/bnvil.lss");

    private BnvilTheme() {
    }

    /**
     * 给界面套上主题
     *
     * @param root 界面根元素
     * @return 带主题的界面
     */
    public static UI of(UIElement root) {
        return UI.of(root, StylesheetManager.INSTANCE.getStylesheetSafe(THEME));
    }
}
