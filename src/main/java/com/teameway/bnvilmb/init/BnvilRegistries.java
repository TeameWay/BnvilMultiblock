package com.teameway.bnvilmb.init;

import com.teameway.bnvilmb.BnvilMultiBlock;
import com.teameway.bnvilmb.common.multiblock.MultiblockMachine;
import com.teameway.bnvilmb.common.multiblock.MultiblockType;
import dev.anvilcraft.lib.v2.multiblock.dynamic.controller.IController;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.registries.NewRegistryEvent;
import net.neoforged.neoforge.registries.RegistryBuilder;

/**
 * 模组自建的注册表
 *
 * <p>多方块结构定义仍由 anvillib 的数据包注册表 {@code anvillib:definitions} 承载，
 * 这里只登记代码侧的控制器、机器声明与机器类型。</p>
 */
@EventBusSubscriber(modid = BnvilMultiBlock.MODID)
public class BnvilRegistries {
    /**
     * 多方块控制器，条目值为 {@link IController} 实例
     */
    public static final ResourceKey<Registry<IController>> MULTIBLOCK_CONTROLLERS =
        ResourceKey.createRegistryKey(BnvilMultiBlock.id("multiblock_controllers"));

    public static final ResourceKey<Registry<MultiblockType>> MULTIBLOCK_TYPES =
        ResourceKey.createRegistryKey(BnvilMultiBlock.id("multiblock_types"));

    public static final ResourceKey<Registry<MultiblockMachine>> MULTIBLOCKS =
        ResourceKey.createRegistryKey(BnvilMultiBlock.id("multiblocks"));

    private BnvilRegistries() {
    }

    /**
     * 触发类加载
     */
    public static void register() {
    }

    /**
     * 注册自定义注册表
     *
     * <p>NeoForge 在 {@code RegisterEvent} 之前触发本事件，注册表随后即可接收条目。</p>
     *
     * @param event 新建注册表事件
     */
    @SubscribeEvent
    public static void onNewRegistry(NewRegistryEvent event) {
        event.create(new RegistryBuilder<>(MULTIBLOCK_CONTROLLERS));
        event.create(new RegistryBuilder<>(MULTIBLOCKS));
        event.create(new RegistryBuilder<>(MULTIBLOCK_TYPES));
    }
}
