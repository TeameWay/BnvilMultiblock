package com.teameway.bnvilmb.event;

import com.teameway.bnvilmb.BnvilMultiBlock;
import com.teameway.bnvilmb.multiblock.StructureControllerRegistry;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;

/**
 * 公共初始化阶段的控制器登记
 *
 * <p>没有限定 {@code Dist}，服务端与客户端都会执行。</p>
 */
@EventBusSubscriber(modid = BnvilMultiBlock.MODID)
public class MultiblockEvent {
    /**
     * 从全局控制器注册表重建 {@link StructureControllerRegistry}
     *
     * <p>控制器在注册表注册阶段已构造完成，这里只做登记；查询会同时发生在服务端与客户端，因此必须双端执行。</p>
     *
     * @param event 公共初始化事件
     */
    @SubscribeEvent
    public static void onCommonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(StructureControllerRegistry::reload);
    }
}