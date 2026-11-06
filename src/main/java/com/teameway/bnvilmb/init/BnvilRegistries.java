package com.teameway.bnvilmb.init;

import com.teameway.bnvilmb.BnvilMultiBlock;
import com.teameway.bnvilmb.multiblock.MultiblockMachine;
import com.teameway.bnvilmb.multiblock.MultiblockType;
import com.teameway.bnvilmb.multiblock.StructureController;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.registries.NewRegistryEvent;
import net.neoforged.neoforge.registries.RegistryBuilder;

/**
 * 模组自建的注册表。
 *
 * <p>结构模板改为代码定义，不再有 {@code structure_patterns} 数据包注册表。</p>
 */
@EventBusSubscriber(modid = BnvilMultiBlock.MODID)
public class BnvilRegistries {
    public static final ResourceKey<Registry<StructureController>> MULTIBLOCK_CONTROLLERS =
        ResourceKey.createRegistryKey(BnvilMultiBlock.id("multiblock_controllers"));

    public static final ResourceKey<Registry<MultiblockType>> MULTIBLOCK_TYPES =
        ResourceKey.createRegistryKey(BnvilMultiBlock.id("multiblock_types"));

    public static final ResourceKey<Registry<MultiblockMachine>> MULTIBLOCKS =
        ResourceKey.createRegistryKey(BnvilMultiBlock.id("multiblocks"));

    private BnvilRegistries() {
    }

    public static void register() {
    }

    /**
     * 取全局控制器注册表。
     *
     * <p>{@link BuiltInRegistries#REGISTRY} 包含所有模组通过 {@code NewRegistryEvent} 创建的静态注册表，
     * 因此这里能看到其他模组用自己的 {@code BnvilRegistrum} 注册的条目。</p>
     *
     * @return 控制器注册表
     */
    public static Registry<StructureController> controllers() {
        return RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY)
            .lookupOrThrow(MULTIBLOCK_CONTROLLERS);
    }

    /**
     * 取全局机器类型注册表。
     *
     * @return 机器类型注册表
     */
    public static Registry<MultiblockType> types() {
        return RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY)
            .lookupOrThrow(MULTIBLOCK_TYPES);
    }

    @SubscribeEvent
    public static void onNewRegistry(NewRegistryEvent event) {
        event.create(new RegistryBuilder<>(MULTIBLOCK_CONTROLLERS));
        event.create(new RegistryBuilder<>(MULTIBLOCKS));
        event.create(new RegistryBuilder<>(MULTIBLOCK_TYPES));
    }
}