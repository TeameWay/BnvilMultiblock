package com.teameway.bnvilmb.integration.jei.categories;

import com.lowdragmc.lowdraglib2.integration.xei.jei.ModularUIRecipeCategory;
import com.teameway.bnvilmb.BnvilMultiBlock;
import com.teameway.bnvilmb.multiblock.MultiblockType;
import com.teameway.bnvilmb.init.BnvilRegistries;
import com.teameway.bnvilmb.integration.jei.layout.JeiLayout;
import com.teameway.bnvilmb.integration.jei.layout.StructureLayout;
import com.teameway.bnvilmb.integration.jei.ui.MultiblockInfoUI;
import com.teameway.bnvilmb.integration.jei.ui.MultiblockPreview;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.helpers.IJeiHelpers;
import mezz.jei.api.recipe.types.IRecipeType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import org.jspecify.annotations.Nullable;

import java.util.Map;

/**
 * 独立的多方块结构信息分类
 *
 * <p>每个机器类型一条记录，只描述结构本身；预览、点击选框与所需方块清单在同一页里。</p>
 */
public class MultiblockInfoRecipeCategory extends ModularUIRecipeCategory<MultiblockInfoRecipeCategory.MultiblockInfo> {
    public static final IRecipeType<MultiblockInfo> RECIPE_TYPE =
        IRecipeType.create(BnvilMultiBlock.id("multiblock_info"), MultiblockInfo.class);

    private final @Nullable IDrawable icon;

    public MultiblockInfoRecipeCategory(IJeiHelpers helpers) {
        super(info -> MultiblockInfoUI.create(info.type(), info.blocks()));
        this.icon = BnvilMultiBlock.REGISTRUM.getAll(BnvilRegistries.MULTIBLOCK_TYPES)
            .stream()
            .findFirst()
            .map(entry -> helpers.getGuiHelper()
                .createDrawableItemLike(
                    entry.get()
                        .controller()
                        .get().getBlock()
                        .asItem()
                )
            )
            .orElse(null);
    }

    @Override
    public IRecipeType<MultiblockInfo> getRecipeType() {
        return RECIPE_TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("gui.bnvil_multiblock.jei.multiblock_info");
    }

    @Override
    public @Nullable IDrawable getIcon() {
        return this.icon;
    }

    @Override
    public Identifier getIdentifier(MultiblockInfo recipe) {
        return recipe.id();
    }

    @Override
    public int getWidth() {
        return JeiLayout.PANEL_WIDTH;
    }

    @Override
    public int getHeight() {
        return StructureLayout.INSTANCE.height();
    }

    /**
     * @param id     结构定义 ID
     * @param type   机器类型
     * @param blocks 结构所需方块及数量
     */
    public record MultiblockInfo(Identifier id, MultiblockType type, Map<Block, Integer> blocks) {
        /**
         * 按机器类型统计结构所需方块
         *
         * @param id   结构定义 ID
         * @param type 机器类型
         * @return 结构信息
         */
        public static MultiblockInfo of(Identifier id, MultiblockType type) {
            return new MultiblockInfo(id, type, MultiblockPreview.requiredBlocks(type));
        }
    }
}
