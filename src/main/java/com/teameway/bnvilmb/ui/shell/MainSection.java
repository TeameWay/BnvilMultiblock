package com.teameway.bnvilmb.ui.shell;

import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.teameway.bnvilmb.ui.UiContext;

/**
 * main 上半部分的内容
 *
 * <p>滚动与居中由 {@link MainPanel} 统一处理，子类只负责生成内容元素。
 * 新增一种信息区时继承这个类，不需要改 MainPanel。</p>
 */
public abstract class MainSection {
    /**
     * @param context 界面上下文
     * @return 上半部分内容
     */
    public abstract UIElement create(UiContext context);

    /**
     * @return 内容是否放进纵向滚动视图
     */
    public boolean scrollable() {
        return false;
    }

    /**
     * @return 内容是否在剩余空间里居中
     */
    public boolean centerContent() {
        return false;
    }
}
