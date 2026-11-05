package com.teameway.bnvilmb.event;

import com.teameway.bnvilmb.BnvilMultiBlock;
import com.teameway.bnvilmb.init.BnvilRegistries;
import com.teameway.bnvilmb.multiblock.StructureControllerRegistry;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;

import static com.teameway.bnvilmb.BnvilMultiBlock.REGISTRUM;

@EventBusSubscriber(modid = BnvilMultiBlock.MODID)
public class MultiblockEvent {
    /**
     * 把注册表里的控制器交给 {@link StructureControllerRegistry}
     *
     * <p>控制器在注册表注册阶段已构造完成，这里只做登记；查询会同时发生在服务端与客户端，因此必须双端执行。</p>
     *
     * @param event 公共初始化事件
     */
    @SubscribeEvent
    public static void onCommonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> REGISTRUM.getAll(BnvilRegistries.MULTIBLOCK_CONTROLLERS)
            .forEach(entry -> StructureControllerRegistry.register(entry.get())));
    }
}
