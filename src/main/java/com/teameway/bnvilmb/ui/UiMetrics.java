package com.teameway.bnvilmb.ui;

/**
 * 界面尺寸
 *
 * <p>main 宽 170，内边距 4，正文可用宽度正好等于玩家物品栏的 162 像素。
 * 控制器 main 高 224，其余 main 高 192；正文高度由 main 高度反推。</p>
 */
public final class UiMetrics {
    public static final int MAIN_WIDTH = 170;
    public static final int MAIN_HEIGHT = 192;
    public static final int CONTROLLER_HEIGHT = 224;
    public static final int PADDING = 2;
    public static final int TITLE_HEIGHT = 12;
    public static final int TITLE_GAP = 2;
    public static final int INVENTORY_WIDTH = 162;
    public static final int INVENTORY_HEIGHT = 77;
    public static final int INVENTORY_GAP = 4;
    public static final int LINE = 10;
    public static final int LINE_GAP = 1;
    public static final int SLOT = 18;
    public static final int SLOT_GAP = 0;
    public static final int ITEM_COLUMNS = 9;
    public static final int SIDE_BUTTON = 16;
    public static final int SIDE_BUTTON_GAP = 2;
    public static final int SIDE_BUTTON_X = -18;
    public static final int SIDE_BUTTON_Y = 0;
    public static final int PANEL_GAP = 4;
    public static final int THREAD_PANEL_WIDTH = 128;
    public static final int THREAD_PANEL_MAX_HEIGHT = 192;
    public static final int RECIPE_PANEL_WIDTH = 128;
    public static final int RECIPE_PANEL_MAX_HEIGHT = 192;
    public static final int PORT_PANEL_WIDTH = 128;
    public static final int PORT_PANEL_MAX_HEIGHT = 192;
    public static final int TITLE_BAR_HEIGHT = 12;
    public static final int PANEL_PADDING = 2;
    public static final int PANEL_Z = 100;
    public static final int ENERGY_BAR_WIDTH = 156;
    public static final int ENERGY_BAR_HEIGHT = 18;
    public static final int STEPPER_WIDTH = 88;
    public static final int STEPPER_HEIGHT = 18;
    public static final int STEPPER_BUTTON = 18;
    public static final int FLUID_BOX_WIDTH = 128;
    public static final int FLUID_BOX_HEIGHT = 64;
    public static final int INFO_BOX_PADDING = 3;
    public static final int DIVIDER_HEIGHT = 1;
    public static final int DIVIDER_GAP = 3;
    public static final int CARD_HEIGHT = 45;
    public static final int CARD_SPACING = 1;
    public static final int CARD_PADDING = 1;
    public static final int CARD_RADIUS = 3;
    public static final int BODY_HEIGHT_PORT = bodyHeight(MAIN_HEIGHT);
    public static final int BODY_HEIGHT_CONTROLLER = bodyHeight(CONTROLLER_HEIGHT);

    private UiMetrics() {
    }

    /**
     * 由 main 高度反推上半部分正文高度
     *
     * @param mainHeight main 高度
     * @return 标题与物品栏之外可用于内容的像素高度
     */
    public static int bodyHeight(int mainHeight) {
        return mainHeight - PADDING * 2 - INVENTORY_HEIGHT - INVENTORY_GAP - TITLE_HEIGHT - TITLE_GAP;
    }
}
