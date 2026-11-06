package com.teameway.bnvilmb.integration.jei;

import com.teameway.bnvilmb.BnvilMultiBlock;
import com.teameway.bnvilmb.init.BnvilRegistries;
import com.teameway.bnvilmb.integration.jei.categories.MultiblockInfoRecipeCategory;
import com.teameway.bnvilmb.integration.jei.categories.MultiblockRecipeCategory;
import com.teameway.bnvilmb.multiblock.MultiblockType;
import com.teameway.bnvilmb.recipe.CompiledRecipe;
import com.teameway.bnvilmb.recipe.MultiblockRecipeIndex;
import com.teameway.bnvilmb.recipe.TypeIndex;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.helpers.IJeiHelpers;
import mezz.jei.api.recipe.types.IRecipeType;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.resources.Identifier;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * JEI 集成入口
 *
 * <p>分类按机器类型注册。配方取客户端配方索引里的编译结果，与服务端同源，排序与机器选择配方一致。
 * 结构信息页单独占一个分类，所有机器类型共用。</p>
 */
@JeiPlugin
public class BnvilJEIPlugin implements IModPlugin {
    private static final Map<Identifier, IRecipeType<CompiledRecipe>> TYPES = new HashMap<>();

    private static IRecipeType<CompiledRecipe> typeOf(Identifier machine) {
        return TYPES.computeIfAbsent(machine, id -> IRecipeType.create(id, CompiledRecipe.class));
    }

    /**
     * 取全局注册表里的机器类型。
     *
     * <p>其他模组通过自己的 {@code BnvilRegistrum} 注册的类型也会出现在这里。</p>
     *
     * @return 机器类型条目列表
     */
    private static List<RegisteredType> types() {
        return BnvilRegistries.types()
            .entrySet()
            .stream()
            .map(entry -> new RegisteredType(entry.getKey().identifier(), entry.getValue()))
            .toList();
    }

    private static List<CompiledRecipe> recipesOf(Identifier machine) {
        MultiblockRecipeIndex index = MultiblockRecipeIndex.current();
        if (index == null) {
            return List.of();
        }
        TypeIndex type = index.type(machine);
        return type == null ? List.of() : type.recipes();
    }

    /**
     * @return 固定为 {@code bnvil_multiblock:jei}
     */
    @Override
    public Identifier getPluginUid() {
        return BnvilMultiBlock.id("jei");
    }

    /**
     * 注册结构信息分类，再为每个机器类型注册一个配方分类
     *
     * <p>配方分类构造时接收该类型当前的配方列表，用来确定整页的统一排版。</p>
     *
     * @param registration 分类注册器
     */
    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        List<RegisteredType> types = types();
        if (types.isEmpty()) {
            return;
        }
        IJeiHelpers helpers = registration.getJeiHelpers();
        List<MultiblockType> multiblockTypes = types.stream().map(RegisteredType::type).toList();
        registration.addRecipeCategories(new MultiblockInfoRecipeCategory(helpers, multiblockTypes));
        for (RegisteredType entry : types) {
            List<CompiledRecipe> recipes = recipesOf(entry.id());
            MultiblockRecipeCategory category = new MultiblockRecipeCategory(
                helpers, typeOf(entry.id()), entry.type(), recipes
            );
            registration.addRecipeCategories(category);
        }
    }

    /**
     * 注册结构信息记录与各机器类型的配方
     *
     * <p>没有配方的机器类型只注册分类，不注册配方内容。</p>
     *
     * @param registration 配方注册器
     */
    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        List<RegisteredType> types = types();
        if (types.isEmpty()) {
            return;
        }
        registration.addRecipes(
            MultiblockInfoRecipeCategory.RECIPE_TYPE,
            types.stream()
                .map(entry -> new MultiblockInfoRecipeCategory.MultiblockInfo(entry.id(), entry.type()))
                .toList()
        );
        for (RegisteredType entry : types) {
            List<CompiledRecipe> recipes = recipesOf(entry.id());
            if (recipes.isEmpty()) {
                continue;
            }
            registration.addRecipes(typeOf(entry.id()), recipes);
        }
    }

    /**
     * 把各机器类型的控制器方块登记为配方分类与结构信息分类的合成站
     *
     * @param registration 合成站注册器
     */
    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        for (RegisteredType entry : types()) {
            var stack = entry.type().controller().get().getBlock().asItem().getDefaultInstance();
            registration.addCraftingStation(typeOf(entry.id()), stack);
            registration.addCraftingStation(MultiblockInfoRecipeCategory.RECIPE_TYPE, stack);
        }
    }

    private record RegisteredType(Identifier id, MultiblockType type) {
    }
}
