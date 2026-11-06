package com.teameway.bnvilmb.datagen;

import com.teameway.bnvilmb.BnvilMultiBlock;
import com.teameway.bnvilmb.datagen.lang.BnvilLangGenerator;
import com.teameway.bnvilmb.datagen.multiblock.BnvilMultiblockDefinitionProvider;
import dev.anvilcraft.lib.v2.registrum.providers.ProviderType;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import net.neoforged.neoforge.data.loading.DatagenModLoader;

import java.util.concurrent.CompletableFuture;

import static com.teameway.bnvilmb.BnvilMultiBlock.REGISTRUM;

/**
 * 数据生成入口
 *
 * <p>方块状态、方块模型、物品模型、掉落表与语言里的方块/物品名由 Registrum 自动生成，
 * 这里只额外挂上多方块结构定义与界面文本。</p>
 *
 * <p>{@link #configureDataGen()} 在模组构造时调用，非 datagen 进程里直接返回；
 * {@link #gatherData} 只在客户端 datagen 时收到事件。</p>
 */
@EventBusSubscriber(modid = BnvilMultiBlock.MODID)
public class BnvilDatagen {
    /**
     * 注册语言 provider
     *
     * <p>{@code DatagenModLoader.isRunningDataGen()} 为 false 时直接返回，不注册任何 provider；
     * 键值对由 {@link BnvilLangGenerator} 提供。</p>
     */
    public static void configureDataGen() {
        if (!DatagenModLoader.isRunningDataGen()) {
            return;
        }
        REGISTRUM.addDataGenerator(ProviderType.LANG, BnvilLangGenerator::accept);
    }

    /**
     * 注册结构模板生成器
     *
     * <p>路径、注册表查询与输出目录都取自事件。</p>
     *
     * @param event 数据生成事件，提供 {@link DataGenerator}、{@link PackOutput} 与注册表查询
     */
    @SubscribeEvent
    public static void gatherData(GatherDataEvent.Client event) {
        DataGenerator generator = event.getGenerator();
        PackOutput packOutput = generator.getPackOutput();
        CompletableFuture<HolderLookup.Provider> lookupProvider = event.getWorldLookupProvider();

        event.addProvider(new BnvilMultiblockDefinitionProvider(packOutput, lookupProvider));
    }
}
