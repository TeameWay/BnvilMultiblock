package com.teameway.bnvilmb.init;

import com.teameway.bnvilmb.BnvilMultiBlock;
import com.teameway.bnvilmb.recipe.MultiblockRecipe;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * 多方块配方序列化器
 *
 * <p>JSON 读取与网络同步共用 {@link MultiblockRecipe#MAP_CODEC} 与 {@link MultiblockRecipe#STREAM_CODEC}。</p>
 */
public class BnvilRecipeSerializers {
    private static final DeferredRegister<RecipeSerializer<?>> SERIALIZER = DeferredRegister.create(
        Registries.RECIPE_SERIALIZER,
        BnvilMultiBlock.MODID
    );

    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<MultiblockRecipe>> MULTIBLOCK =
        SERIALIZER.register(
            "multiblock",
            () -> new RecipeSerializer<>(MultiblockRecipe.MAP_CODEC, MultiblockRecipe.STREAM_CODEC)
        );

    /**
     * 把序列化器挂到模组事件总线
     *
     * @param bus 模组事件总线
     */
    public static void register(IEventBus bus) {
        SERIALIZER.register(bus);
    }
}
