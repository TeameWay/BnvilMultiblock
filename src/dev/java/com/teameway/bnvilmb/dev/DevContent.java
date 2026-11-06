package com.teameway.bnvilmb.dev;

import com.teameway.bnvilmb.BnvilMultiBlock;
import com.teameway.bnvilmb.dev.init.BnvilCreativeTabs;
import com.teameway.bnvilmb.dev.init.multiblock.BnvilControllerBlockEntities;
import com.teameway.bnvilmb.dev.init.multiblock.BnvilControllerBlocks;
import com.teameway.bnvilmb.dev.init.multiblock.BnvilControllers;
import com.teameway.bnvilmb.dev.init.multiblock.BnvilMachines;
import com.teameway.bnvilmb.dev.init.multiblock.BnvilPorts;
import com.teameway.bnvilmb.dev.init.multiblock.BnvilTypes;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLConstructModEvent;

/**
 * 开发源集的内容入口。
 *
 * <p>示例控制器、机器、端口与创造页只在这里注册。正式构建不包含这个类，库本体因此不会向玩家
 * 提供任何现成多方块内容。</p>
 */
@EventBusSubscriber(modid = BnvilMultiBlock.MODID)
public final class DevContent {
    private DevContent() {
    }

    /**
     * 在模组构造完成后、注册事件之前装入示例内容。
     *
     * @param event 模组构造事件
     */
    @SubscribeEvent
    public static void onConstruct(FMLConstructModEvent event) {
        BnvilControllerBlocks.register();
        BnvilControllerBlockEntities.register();
        BnvilControllers.register();
        BnvilMachines.register();
        BnvilPorts.register();
        BnvilTypes.register();
        BnvilCreativeTabs.register();
        BnvilMultiBlock.REGISTRUM.defaultCreativeTab(BnvilCreativeTabs.MULTIBLOCK);
    }
}
