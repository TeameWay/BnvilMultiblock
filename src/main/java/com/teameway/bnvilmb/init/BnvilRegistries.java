package com.teameway.bnvilmb.init;

import com.teameway.bnvilmb.BnvilMultiBlock;
import com.teameway.bnvilmb.multiblock.MultiblockMachine;
import com.teameway.bnvilmb.multiblock.MultiblockType;
import com.teameway.bnvilmb.multiblock.StructureController;
import com.teameway.bnvilmb.multiblock.structure.StructurePattern;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.registries.NewDatapackRegistryEvent;
import net.neoforged.neoforge.registries.NewRegistryEvent;
import net.neoforged.neoforge.registries.RegistryBuilder;

/**
 * 模组自建的注册表
 *
 * <p>结构模板由数据包注册表 {@code bnvil_multiblock:structure_patterns} 承载，
 * 控制器、机器声明与机器类型仍在代码侧注册，三者的主键都在这个类里声明。</p>
 */
@EventBusSubscriber(modid = BnvilMultiBlock.MODID)
public class BnvilRegistries {
    /**
     * 多方块控制器，条目值为 {@link StructureController} 实例
     */
    public static final ResourceKey<Registry<StructureController>> MULTIBLOCK_CONTROLLERS =
        ResourceKey.createRegistryKey(BnvilMultiBlock.id("multiblock_controllers"));

    /**
     * 机器类型，条目值为 {@link MultiblockType} 实例
     */
    public static final ResourceKey<Registry<MultiblockType>> MULTIBLOCK_TYPES =
        ResourceKey.createRegistryKey(BnvilMultiBlock.id("multiblock_types"));

    /**
     * 机器声明，条目值为 {@link MultiblockMachine} 实例，数据生成按它输出结构模板
     */
    public static final ResourceKey<Registry<MultiblockMachine>> MULTIBLOCKS =
        ResourceKey.createRegistryKey(BnvilMultiBlock.id("multiblocks"));

    /**
     * 数据包结构模板注册表
     */
    public static final ResourceKey<Registry<StructurePattern>> STRUCTURE_PATTERNS =
        ResourceKey.createRegistryKey(BnvilMultiBlock.id("structure_patterns"));

    private BnvilRegistries() {
    }

    /**
     * 触发类加载
     *
     * <p>三个代码侧注册表在 {@link #onNewRegistry} 里创建，条目由 {@code REGISTRUM} 随后写入。</p>
     */
    public static void register() {
    }

    /**
     * 注册自定义注册表
     *
     * <p>NeoForge 在 {@code RegisterEvent} 之前触发本事件，注册表随后即可接收条目。
     * 结构模板走数据包注册表，不在这里创建。</p>
     *
     * @param event 新建注册表事件
     */
    @SubscribeEvent
    public static void onNewRegistry(NewRegistryEvent event) {
        event.create(new RegistryBuilder<>(MULTIBLOCK_CONTROLLERS));
        event.create(new RegistryBuilder<>(MULTIBLOCKS));
        event.create(new RegistryBuilder<>(MULTIBLOCK_TYPES));
    }

    /**
     * 注册结构模板数据包注册表
     *
     * <p>服务端加载，客户端通过同一个 codec 同步，JEI 才能读到结构预览。</p>
     *
     * @param event 新建数据包注册表事件
     */
    @SubscribeEvent
    public static void onNewDatapackRegistry(NewDatapackRegistryEvent event) {
        event.worldRegistry(STRUCTURE_PATTERNS, StructurePattern.CODEC, StructurePattern.CODEC);
    }
}
