package com.teameway.bnvilmb.integration.jei.categories;

import com.lowdragmc.lowdraglib2.integration.xei.jei.ModularUIRecipeCategory;
import com.teameway.bnvilmb.BnvilMultiBlock;
import com.teameway.bnvilmb.integration.jei.layout.StructureLayout;
import com.teameway.bnvilmb.integration.jei.ui.MultiblockInfoUI;
import com.teameway.bnvilmb.multiblock.MultiblockType;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.helpers.IJeiHelpers;
import mezz.jei.api.recipe.types.IRecipeType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;

import java.util.List;

/**
 * 结构信息分类
 *
 * <p>每个机器类型一条记录，只描述结构本身。预览、点击选框与所需方块清单都放在同一页里。</p>
 */
public class MultiblockInfoRecipeCategory extends ModularUIRecipeCategory<MultiblockInfoRecipeCategory.MultiblockInfo> {
    public static final IRecipeType<MultiblockInfo> RECIPE_TYPE =
        IRecipeType.create(BnvilMultiBlock.id("multiblock_info"), MultiblockInfo.class);

    private final @Nullable IDrawable icon;

    /**
     * 创建结构信息分类
     *
     * <p>分类图标取传入列表里第一个机器类型的控制器方块，列表为空时没有图标。</p>
     *
     * @param helpers JEI 助手
     * @param types 全局注册表里的机器类型
     */
    public MultiblockInfoRecipeCategory(IJeiHelpers helpers, List<MultiblockType> types) {
        super(info -> MultiblockInfoUI.create(info.type()));
        this.icon = types.stream()
            .findFirst()
            .map(type -> helpers.getGuiHelper()
                .createDrawableItemLike(
                    type.controller()
                        .get().getBlock()
                        .asItem()
                )
            )
            .orElse(null);
    }

    /**
     * @return 该分类的配方类型
     */
    @Override
    public IRecipeType<MultiblockInfo> getRecipeType() {
        return RECIPE_TYPE;
    }

    /**
     * @return 分类标题的翻译键 {@code gui.bnvil_multiblock.jei.multiblock_info}
     */
    @Override
    public Component getTitle() {
        return Component.translatable("gui.bnvil_multiblock.jei.multiblock_info");
    }

    /**
     * @return 分类图标，注册表为空时为 {@code null}
     */
    @Override
    public @Nullable IDrawable getIcon() {
        return this.icon;
    }

    /**
     * @param recipe 结构信息记录
     * @return 记录里的结构定义 ID
     */
    @Override
    public Identifier getIdentifier(MultiblockInfo recipe) {
        return recipe.id();
    }

    /**
     * @return 页面宽度，取结构页的面板宽度
     */
    @Override
    public int getWidth() {
        return StructureLayout.PANEL_WIDTH;
    }

    /**
     * @return 页面高度，取结构页排版的高度
     */
    @Override
    public int getHeight() {
        return StructureLayout.INSTANCE.height();
    }

    /**
     * 结构信息页的一条记录
     *
     * <p>{@code type} 用来构建页面，{@code id} 作为 JEI 记录标识。</p>
     *
     * @param id 结构定义 ID
     * @param type 机器类型
     */
    public record MultiblockInfo(Identifier id, MultiblockType type) {
    }
}