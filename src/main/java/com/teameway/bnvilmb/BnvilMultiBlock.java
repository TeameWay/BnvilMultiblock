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
 * 模组主入口
 *
 * <p>构造时依次挂载注册表、配方类型与序列化器、控制器、机器、端口与机器类型，最后配置数据生成；
 * 默认创造模式物品栏在创造页注册之后设为 {@code BnvilCreativeTabs.MULTIBLOCK}。</p>
 *
 * <p>{@link #id(String)} 给出本模组命名空间下的 ID，注册表键、JEI 类型与测试 ID 都由它拼出。</p>
 */
@Mod(BnvilMultiBlock.MODID)
public class BnvilMultiBlock {
    public static final String MODID = "bnvil_multiblock";
    public static final String MOD_NAME = "BnvilMultiblock";
    public static final Logger LOGGER = LogUtils.getLogger();

    /**
     * 模组自己的 Registrum，各注册类通过它创建条目
     */
    public static BnvilRegistrum REGISTRUM = BnvilRegistrum.create(MODID);

    /**
     * 挂载各注册入口
     *
     * <p>注册表、配方与多方块相关的条目都在这里登记，条目本身由各自的注册类定义。</p>
     *
     * @param modEventBus 模组事件总线，配方类型与序列化器的 DeferredRegister 挂到它上面
     * @param modContainer 模组容器，由 NeoForge 注入
     */
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
     * @param path 路径，不带命名空间
     * @return {@code bnvil_multiblock:<path>}
     */
    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(MODID, path);
    }

    private static void addTab() {
        REGISTRUM.defaultCreativeTab(BnvilCreativeTabs.MULTIBLOCK);
    }
}
