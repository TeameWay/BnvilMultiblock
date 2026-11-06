package com.teameway.bnvilmb.integration.jei.layout;

/**
 * 结构页排版
 *
 * <p>三维场景铺满整个内容区，左侧一列与底部清单直接盖在它上面。左侧一列最上面是场景里选中的方块，
 * 下面是同一位置可以换用的其余方块；底部横排清单列出全部所需方块，种类多时横向滚动。预览右上角
 * 两个按钮也盖在场景上。场景不会被侧栏挤小，也不会偏到一侧。</p>
 *
 * <p>宽度比配方页宽，多出的部分留给左侧一列；高度固定。</p>
 */
public record StructureLayout() {
    /**
     * 结构页排版不含可变量，用单例即可
     */
    public static final StructureLayout INSTANCE = new StructureLayout();

    /**
     * 结构页比 {@link JeiLayout#PANEL_WIDTH} 宽，多出的部分留给左侧一列
     */
    public static final int PANEL_WIDTH = 200;
    /**
     * 内容区宽度，面板去掉左右内边距
     */
    public static final int CONTENT_WIDTH = PANEL_WIDTH - JeiLayout.PANEL_PADDING * 2;
    /**
     * 场景高度，等于内容区高度：场景铺满整页，侧栏叠在它上面
     */
    public static final int PREVIEW_HEIGHT = 200;
    /**
     * 底部清单区域高度，含横向滚动条
     */
    public static final int STRIP_HEIGHT = 24;

    /**
     * LDLib2 竖直滚动条固定 5 像素宽
     */
    private static final int SCROLLER_WIDTH = 5;
    /**
     * 左侧一列的宽度：一列槽位加一条竖直滚动条
     */
    public static final int COLUMN_WIDTH = JeiLayout.SLOT + SCROLLER_WIDTH;
    /**
     * 左侧可替代方块列表的高度：从选中格下方一直排到底部清单上沿
     */
    public static final int ALTERNATIVE_HEIGHT = PREVIEW_HEIGHT - STRIP_HEIGHT - JeiLayout.SLOT;
    /**
     * 画面用的场景尺寸，与内容区同宽同高
     */
    public static final int SCENE_WIDTH = CONTENT_WIDTH;
    public static final int SCENE_HEIGHT = PREVIEW_HEIGHT;
    /**
     * 预览区右上角按钮的尺寸与间距，按钮盖在场景上沿
     */
    public static final int BUTTON_WIDTH = 40;
    public static final int BUTTON_HEIGHT = 12;
    public static final int BUTTON_GAP = 1;

    /**
     * 页面总高度
     *
     * @return 上下内边距加场景高度
     */
    public int height() {
        return JeiLayout.PANEL_PADDING * 2 + PREVIEW_HEIGHT;
    }

    /**
     * 底部清单区域在内容区内的纵向位置
     *
     * <p>贴着内容区下沿。</p>
     *
     * @return 纵向位置
     */
    public int stripTop() {
        return PREVIEW_HEIGHT - STRIP_HEIGHT;
    }
}
