package com.teameway.bnvilmb.init;

import com.teameway.bnvilmb.BnvilMultiBlock;
import com.teameway.bnvilmb.recipe.MultiblockRecipe;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * 多方块配方类型
 *
 * <p>类型本身不带数据，配方内容全部由 {@link MultiblockRecipe} 承载。</p>
 */
public class BnvilRecipeTypes {
    private static final DeferredRegister<RecipeType<?>> TYPE = DeferredRegister.create(Registries.RECIPE_TYPE, BnvilMultiBlock.MODID);

    public static final DeferredHolder<RecipeType<?>, RecipeType<MultiblockRecipe>> MULTIBLOCK =
        registerType("multiblock");

    private static <T extends Recipe<?>> DeferredHolder<RecipeType<?>, RecipeType<T>> registerType(String name) {
        return TYPE.register(name, RecipeType::simple);
    }

    /**
     * 把配方类型挂到模组事件总线
     *
     * @param bus 模组事件总线
     */
    public static void register(IEventBus bus) {
        TYPE.register(bus);
    }
}
