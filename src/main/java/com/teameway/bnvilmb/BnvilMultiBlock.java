package com.teameway.bnvilmb;

import com.mojang.logging.LogUtils;
import com.teameway.bnvilmb.datagen.BnvilDatagen;
import com.teameway.bnvilmb.init.BnvilCreativeTabs;
import com.teameway.bnvilmb.init.BnvilRecipeSerializers;
import com.teameway.bnvilmb.init.BnvilRecipeTypes;
import com.teameway.bnvilmb.init.BnvilRegistries;
import com.teameway.bnvilmb.init.multiblock.BnvilControllerBlockEntities;
import com.teameway.bnvilmb.init.multiblock.BnvilControllerBlocks;
import com.teameway.bnvilmb.init.multiblock.BnvilControllers;
import com.teameway.bnvilmb.init.multiblock.BnvilMachines;
import com.teameway.bnvilmb.init.multiblock.BnvilPorts;
import com.teameway.bnvilmb.init.multiblock.BnvilTypes;
import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import org.slf4j.Logger;

/**
 * BnvilMultiblock 主入口
 *
 * <p>负责挂载各注册入口，并提供全模组统一的 ID 构造方法。</p>
 */
@Mod(BnvilMultiBlock.MODID)
public class BnvilMultiBlock {
    public static final String MODID = "bnvil_multiblock";
    public static final String MOD_NAME = "BnvilMultiblock";
    public static final Logger LOGGER = LogUtils.getLogger();

    public static BnvilRegistrum REGISTRUM = BnvilRegistrum.create(MODID);

    public BnvilMultiBlock(IEventBus modEventBus, ModContainer modContainer) {
        BnvilRegistries.register();
        BnvilRecipeTypes.register(modEventBus);
        BnvilRecipeSerializers.register(modEventBus);

        BnvilCreativeTabs.register();
        addTab();
        
        BnvilControllerBlocks.register();
        BnvilControllerBlockEntities.register();
        BnvilControllers.register();
        BnvilMachines.register();
        BnvilPorts.register();
        BnvilTypes.register();

        BnvilDatagen.configureDataGen();
    }

    /**
     * 构造本模组命名空间下的 ID
     *
     * @param path 路径
     * @return 完整 ID
     */
    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(MODID, path);
    }

    private static void addTab() {
        REGISTRUM.defaultCreativeTab(BnvilCreativeTabs.MULTIBLOCK);
    }
}
