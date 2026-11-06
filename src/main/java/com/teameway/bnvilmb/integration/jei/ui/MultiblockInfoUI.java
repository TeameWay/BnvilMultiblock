package com.teameway.bnvilmb.integration.jei.ui;

import com.lowdragmc.lowdraglib2.client.scene.SceneRenderContext;
import com.lowdragmc.lowdraglib2.client.utils.RenderUtils;
import com.lowdragmc.lowdraglib2.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib2.gui.texture.ItemStackTexture;
import com.lowdragmc.lowdraglib2.gui.ui.ModularUI;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.data.ScrollDisplay;
import com.lowdragmc.lowdraglib2.gui.ui.data.ScrollerMode;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Button;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Label;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Menu;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Scene;
import com.lowdragmc.lowdraglib2.gui.ui.elements.ScrollerView;
import com.lowdragmc.lowdraglib2.gui.ui.event.UIEvent;
import com.lowdragmc.lowdraglib2.gui.ui.event.UIEvents;
import com.lowdragmc.lowdraglib2.gui.util.TreeBuilder;
import com.lowdragmc.lowdraglib2.integration.xei.IngredientIO;
import com.mojang.blaze3d.vertex.PoseStack;
import com.teameway.bnvilmb.gui.BnvilTheme;
import com.teameway.bnvilmb.integration.jei.layout.JeiLayout;
import com.teameway.bnvilmb.integration.jei.layout.StructureLayout;
import com.teameway.bnvilmb.ldlib.slot.CompactItemSlot;
import com.teameway.bnvilmb.multiblock.MultiblockType;
import dev.vfyjxf.taffy.style.FlexDirection;
import dev.vfyjxf.taffy.style.TaffyPosition;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.joml.Vector2f;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * 结构信息页
 *
 * <p>左侧一列上下排开：最上面是被点中的方块，下面依次列出同一位置可以换用的其余端口或方块；
 * 每格的悬浮提示带上结构对它的数量要求（最多、精确、最少）。在选中格上右键会弹出候选菜单，
 * 菜单项只是普通条目，不注册成 JEI 原料。右侧是结构预览，以控制器为中心；预览右上角两个按钮
 * 分别切换显示层数与高亮可替换方块。底部横排清单列出全部所需方块与数量，种类多时横向滚动。</p>
 */
public class MultiblockInfoUI {
    /**
     * 高亮颜色，取偏黄绿以便与场景里的方块区分。
     */
    private static final float HIGHLIGHT_RED = 0.35F;
    private static final float HIGHLIGHT_GREEN = 1.0F;
    private static final float HIGHLIGHT_BLUE = 0.35F;
    private static final float HIGHLIGHT_SCALE = 1.01F;

    private MultiblockInfoUI() {
    }

    /**
     * 构建结构信息界面
     *
     * @param type 机器类型
     * @return 供 JEI 显示的界面
     */
    public static ModularUI create(MultiblockType type) {
        List<RequiredBlock> blocks = MultiblockPreview.requiredBlocks(type);
        StructureLayout layout = StructureLayout.INSTANCE;
        int height = layout.height();
        int contentHeight = height - JeiLayout.PANEL_PADDING * 2;

        UIElement root = new UIElement()
            .layout(l -> l
                .width(StructureLayout.PANEL_WIDTH)
                .height(height)
                .paddingAll(JeiLayout.PANEL_PADDING))
            .addClass("panel_bg");

        UIElement content = new UIElement()
            .layout(l -> l.width(StructureLayout.CONTENT_WIDTH).height(contentHeight));
        root.addChild(content);

        StructureHintSlot selected = createSelectedSlot();
        ScrollerView alternatives = createAlternatives();

        PreviewPage page = new PreviewPage(type, content, selected, alternatives);
        page.install();

        content.addChild(selected);
        content.addChild(alternatives);
        addStrip(content, blocks, layout.stripTop());

        ModularUI ui = ModularUI.of(BnvilTheme.of(root));
        ui.setDrawTooltips(true);
        return ui;
    }

    /**
     * 左上角的单格：显示场景里点中的方块
     *
     * @return 槽位
     */
    private static StructureHintSlot createSelectedSlot() {
        StructureHintSlot slot = new StructureHintSlot();
        slot.layout(l -> l
            .positionType(TaffyPosition.ABSOLUTE)
            .left(0)
            .top(0)
            .width(JeiLayout.SLOT)
            .height(JeiLayout.SLOT));
        slot.xeiRecipeSlot(IngredientIO.INPUT, 1.0F);
        return slot;
    }

    /**
     * 左侧竖直列表：选中格下方依次列出同一位置可换用的其余方块
     *
     * @return 可滚动列表
     */
    private static ScrollerView createAlternatives() {
        ScrollerView scroller = new ScrollerView();
        scroller.viewPort(viewPort -> viewPort
                .style(style -> style.backgroundTexture(IGuiTexture.EMPTY))
                .layout(l -> l.paddingAll(0)))
            .viewContainer(viewContainer -> viewContainer.layout(l -> l.flexDirection(FlexDirection.COLUMN)))
            .scrollerStyle(style -> style
                .mode(ScrollerMode.VERTICAL)
                .verticalScrollDisplay(ScrollDisplay.AUTO))
            .layout(l -> l
                .positionType(TaffyPosition.ABSOLUTE)
                .left(0)
                .top(JeiLayout.SLOT)
                .width(StructureLayout.COLUMN_WIDTH)
                .height(StructureLayout.ALTERNATIVE_HEIGHT));
        return scroller;
    }

    /**
     * 重填可替代方块列表
     *
     * @param scroller   列表容器
     * @param candidates 该位置允许的全部方块，第一个就是上方格子里已经显示的那个
     */
    private static void fillAlternatives(ScrollerView scroller, List<StructureCandidate> candidates) {
        UIElement container = scroller.viewContainer;
        container.clearAllChildren();
        for (int i = 1; i < candidates.size(); i++) {
            StructureCandidate candidate = candidates.get(i);
            StructureHintSlot slot = new StructureHintSlot();
            slot.hint(requirement(candidate));
            slot.setItem(new ItemStack(candidate.block().asItem()));
            slot.layout(l -> l.width(JeiLayout.SLOT).height(JeiLayout.SLOT));
            container.addChild(slot);
        }
    }

    /**
     * 把候选方块的数量条件翻成一行提示
     *
     * @param candidate 候选方块
     * @return 提示文本；给不出条件时为 {@code null}
     */
    private static @Nullable Component requirement(StructureCandidate candidate) {
        int min = candidate.min();
        int max = candidate.max();
        if (min < 0) {
            return null;
        }
        if (max < 0) {
            return min > 0 ? Component.translatable("gui.bnvil_multiblock.jei.required.min", min) : null;
        }
        if (min == max) {
            return Component.translatable("gui.bnvil_multiblock.jei.required.exact", min);
        }
        if (min == 0) {
            return Component.translatable("gui.bnvil_multiblock.jei.required.max", max);
        }
        return Component.translatable("gui.bnvil_multiblock.jei.required.range", min, max);
    }

    /**
     * 菜单与提示里用的方块名：方块名后面接数量条件
     *
     * @param candidate 候选方块
     * @return 名称
     */
    private static Component nameOf(StructureCandidate candidate) {
        Component name = new ItemStack(candidate.block().asItem()).getHoverName();
        Component condition = requirement(candidate);
        return condition == null ? name : name.copy().append(" ").append(condition);
    }

    /**
     * 底部横排清单，横向滚动
     *
     * @param content 内容区
     * @param blocks  已排好序的需求方块
     * @param top     清单上边缘
     */
    private static void addStrip(UIElement content, List<RequiredBlock> blocks, int top) {
        ScrollerView strip = new ScrollerView();
        UIElement row = new UIElement().layout(l -> l.flexDirection(FlexDirection.ROW).gapAll(JeiLayout.SLOT_GAP));
        for (RequiredBlock required : blocks) {
            CompactItemSlot slot = new CompactItemSlot();
            slot.setItem(new ItemStack(required.block().asItem()));
            slot.setAmount(required.count());
            slot.xeiRecipeSlot(IngredientIO.INPUT, 1.0F);
            slot.layout(l -> l.width(JeiLayout.SLOT).height(JeiLayout.SLOT));
            row.addChild(slot);
        }
        strip.addScrollViewChild(row);
        strip.viewPort(viewPort -> viewPort
                .style(style -> style.backgroundTexture(IGuiTexture.EMPTY))
                .layout(l -> l.paddingAll(0)))
            .viewContainer(viewContainer -> viewContainer.layout(l -> l.flexDirection(FlexDirection.ROW)))
            .scrollerStyle(style -> style
                .mode(ScrollerMode.HORIZONTAL)
                .horizontalScrollDisplay(ScrollDisplay.AUTO))
            .layout(l -> l
                .positionType(TaffyPosition.ABSOLUTE)
                .left(0)
                .top(top)
                .width(StructureLayout.CONTENT_WIDTH)
                .height(StructureLayout.STRIP_HEIGHT));
        content.addChild(strip);
    }

    /**
     * 预览区的交互状态
     *
     * <p>场景、右上角按钮与左侧选中格共享同一份状态：当前显示的候选、显示层数与高亮开关。
     * 每次打开配方页都会新建一份，不会串到别的机器上。</p>
     */
    private static final class PreviewPage {
        private final MultiblockType type;
        private final UIElement content;
        private final StructureHintSlot selected;
        private final ScrollerView alternatives;
        private final @Nullable Scene scene;
        private final int layerCount;
        private final Set<BlockPos> replaceable;
        private final Set<BlockPos> highlighted = new LinkedHashSet<>();
        /**
         * 当前显示的层序号，自下往上从 {@code 0} 开始；{@code -1} 表示完整结构。
         */
        private int slice = -1;
        private boolean highlight;
        private List<StructureCandidate> candidates = List.of();
        private @Nullable Button layerButton;
        private @Nullable Button highlightButton;

        private PreviewPage(MultiblockType type, UIElement content, StructureHintSlot selected, ScrollerView alternatives) {
            this.type = type;
            this.content = content;
            this.selected = selected;
            this.alternatives = alternatives;
            this.scene = MultiblockPreview.create(type);
            this.layerCount = MultiblockPreview.layerCount(type);
            this.replaceable = MultiblockPreview.replaceablePositions(type);
        }

        /**
         * 把场景与两个按钮挂到页面上
         */
        private void install() {
            if (this.scene == null) {
                Label missing = JeiLayout.label(Component.translatable("gui.bnvil_multiblock.jei.no_structure"));
                missing.layout(l -> l
                    .positionType(TaffyPosition.ABSOLUTE)
                    .left(0)
                    .top(StructureLayout.PREVIEW_HEIGHT / 2)
                    .width(StructureLayout.CONTENT_WIDTH));
                content.addChild(missing);
                return;
            }
            this.scene.setOnSelected((pos, facing) -> this.select(pos));
            this.scene.setOverlay(this::renderHighlight);
            this.scene.layout(l -> l
                .positionType(TaffyPosition.ABSOLUTE)
                .left(0)
                .top(0)
                .width(StructureLayout.SCENE_WIDTH)
                .height(StructureLayout.SCENE_HEIGHT));
            content.addChild(this.scene);

            this.layerButton = createButton(0);
            this.highlightButton = createButton(StructureLayout.BUTTON_HEIGHT + StructureLayout.BUTTON_GAP);
            this.layerButton.addEventListener(UIEvents.MOUSE_UP, this::onLayerClick);
            this.highlightButton.addEventListener(UIEvents.MOUSE_UP, event -> {
                if (event.button == 0) {
                    this.toggleHighlight();
                }
            });
            this.selected.addEventListener(UIEvents.MOUSE_DOWN, this::onSelectedClick);
            content.addChild(this.layerButton);
            content.addChild(this.highlightButton);

            this.refreshButtons();
            this.applyLayer();
        }

        private Button createButton(int top) {
            Button button = new Button();
            button.textStyle(style -> style.fontSize(7));
            button.layout(l -> l
                .positionType(TaffyPosition.ABSOLUTE)
                .left(StructureLayout.CONTENT_WIDTH - StructureLayout.BUTTON_WIDTH)
                .top(top)
                .width(StructureLayout.BUTTON_WIDTH)
                .height(StructureLayout.BUTTON_HEIGHT));
            return button;
        }

        /**
         * 场景里点中方块：刷新选中格与可替代方块列表
         *
         * @param pos 方块坐标
         */
        private void select(BlockPos pos) {
            List<StructureCandidate> found = MultiblockPreview.candidatesAt(this.type, pos);
            if (found.isEmpty()) {
                return;
            }
            this.candidates = found;
            this.pick(found.getFirst());
        }

        /**
         * 把某个候选放到选中格，并以它为首重排下方的可替代列表
         *
         * @param candidate 候选方块
         */
        private void pick(StructureCandidate candidate) {
            this.selected.setItem(new ItemStack(candidate.block().asItem()));
            this.selected.hint(requirement(candidate));
            List<StructureCandidate> ordered = new ArrayList<>(this.candidates.size());
            ordered.add(candidate);
            for (StructureCandidate other : this.candidates) {
                if (other != candidate) {
                    ordered.add(other);
                }
            }
            this.candidates = ordered;
            fillAlternatives(this.alternatives, ordered);
        }

        /**
         * 选中格上的右键：弹出该位置的候选菜单
         *
         * @param event 鼠标事件
         */
        private void onSelectedClick(UIEvent event) {
            if (event.button != 1 || this.candidates.size() < 2) {
                return;
            }
            ModularUI mui = this.selected.getModularUI();
            if (mui == null) {
                return;
            }
            TreeBuilder.Menu menu = TreeBuilder.Menu.start();
            for (StructureCandidate candidate : this.candidates) {
                menu.leaf(new ItemStackTexture(new ItemStack(candidate.block().asItem())), nameOf(candidate),
                    () -> this.pick(candidate));
            }
            UIElement root = mui.ui.rootElement;
            Vector2f offset = root.worldToLocalLayoutOffset(new Vector2f(event.x, event.y));
            root.addChild(new Menu<>(menu.build(), TreeBuilder.Menu::uiProvider)
                .setHoverTextureProvider(TreeBuilder.Menu::hoverTextureProvider)
                .setOnNodeClicked(TreeBuilder.Menu::handle)
                .layout(l -> {
                    l.left(offset.x);
                    l.top(offset.y);
                }));
            event.stopPropagation();
        }

        /**
         * 层数按钮：左键切下一层、右键切上一层、中键回到完整结构
         *
         * @param event 鼠标事件
         */
        private void onLayerClick(UIEvent event) {
            boolean changed = switch (event.button) {
                // 左键切到下一层；切过最后一层回到完整结构
                case 0 -> this.setSlice(this.slice >= this.layerCount - 1 ? -1 : this.slice + 1);
                // 右键切到上一层；完整结构时从最上面一层开始
                case 1 -> this.setSlice(this.slice < 0 ? this.layerCount - 1 : this.slice - 1);
                // 中键回到完整结构
                case 2 -> this.setSlice(-1);
                default -> false;
            };
            if (changed) {
                event.stopPropagation();
            }
        }

        /**
         * 切换层序号
         *
         * @param slice 层序号，{@code -1} 表示完整结构
         * @return 是否发生变化
         */
        private boolean setSlice(int slice) {
            int clamped = slice < 0 ? -1 : Math.min(slice, this.layerCount - 1);
            if (clamped == this.slice) {
                return false;
            }
            this.slice = clamped;
            this.applyLayer();
            this.refreshButtons();
            return true;
        }

        private void toggleHighlight() {
            this.highlight = !this.highlight;
            this.refreshButtons();
        }

        /**
         * 按当前层序号重设场景渲染核心与高亮集合
         */
        private void applyLayer() {
            if (this.scene == null) {
                return;
            }
            MultiblockPreview.showLayer(this.scene, this.type, this.slice);
            this.highlighted.clear();
            for (BlockPos pos : MultiblockPreview.layerPositions(this.type, this.slice)) {
                if (this.replaceable.contains(pos)) {
                    this.highlighted.add(pos);
                }
            }
        }

        private void refreshButtons() {
            if (this.layerButton != null) {
                this.layerButton.setText(this.slice < 0
                    ? Component.translatable("gui.bnvil_multiblock.jei.layer.all")
                    : Component.translatable("gui.bnvil_multiblock.jei.layer.count", this.slice));
            }
            if (this.highlightButton != null) {
                this.highlightButton.setText(Component.translatable(this.highlight
                    ? "gui.bnvil_multiblock.jei.highlight.on"
                    : "gui.bnvil_multiblock.jei.highlight.off"));
            }
        }

        /**
         * 场景覆盖层：高亮开着时给可替换方块描一层色块
         *
         * @param ctx 场景渲染上下文
         */
        private void renderHighlight(SceneRenderContext ctx) {
            if (!this.highlight) {
                return;
            }
            for (BlockPos pos : this.highlighted) {
                RenderUtils.renderBlockOverLay(ctx.renderPass(), new PoseStack(), pos,
                    HIGHLIGHT_RED, HIGHLIGHT_GREEN, HIGHLIGHT_BLUE, HIGHLIGHT_SCALE);
            }
        }
    }
}
