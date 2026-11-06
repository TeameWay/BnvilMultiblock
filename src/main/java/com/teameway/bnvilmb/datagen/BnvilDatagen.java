package com.teameway.bnvilmb.datagen;

import com.teameway.bnvilmb.BnvilMultiBlock;
import com.teameway.bnvilmb.datagen.lang.BnvilLangGenerator;
import dev.anvilcraft.lib.v2.registrum.providers.ProviderType;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.data.loading.DatagenModLoader;

import static com.teameway.bnvilmb.BnvilMultiBlock.REGISTRUM;

/**
 * 数据生成入口。
 */
@EventBusSubscriber(modid = BnvilMultiBlock.MODID)
public class BnvilDatagen {
    public static void configureDataGen() {
        if (!DatagenModLoader.isRunningDataGen()) {
            return;
        }
        REGISTRUM.addDataGenerator(ProviderType.LANG, BnvilLangGenerator::accept);
    }
}
