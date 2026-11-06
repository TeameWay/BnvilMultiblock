package com.teameway.bnvilmb;

import com.mojang.logging.LogUtils;
import com.teameway.bnvilmb.datagen.BnvilDatagen;
import com.teameway.bnvilmb.init.BnvilRecipeSerializers;
import com.teameway.bnvilmb.init.BnvilRecipeTypes;
import com.teameway.bnvilmb.init.BnvilRegistries;
import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import org.slf4j.Logger;

/**
 * 模组主入口
 *
 * <p>构造时只挂载库自身的注册表、配方类型与序列化器，再配置数据生成。示例机器、控制器、
 * 端口与结构由开发源集单独注册，不会进入正式模组。</p>
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
     * 挂载库自身的注册入口
     *
     * <p>这里只登记注册表与配方基础设施；示例多方块内容由开发源集的 {@code DevContent} 加载。</p>
     *
     * @param modEventBus 模组事件总线，配方类型与序列化器的 DeferredRegister 挂到它上面
     * @param modContainer 模组容器，由 NeoForge 注入
     */
    public BnvilMultiBlock(IEventBus modEventBus, ModContainer modContainer) {
        BnvilRegistries.register();
        BnvilRecipeTypes.register(modEventBus);
        BnvilRecipeSerializers.register(modEventBus);

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
}
