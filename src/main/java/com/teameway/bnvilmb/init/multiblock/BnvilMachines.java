package com.teameway.bnvilmb.init.multiblock;

import com.teameway.bnvilmb.BnvilMultiBlock;
import com.teameway.bnvilmb.multiblock.MultiblockMachine;
import dev.anvilcraft.lib.v2.registrum.util.entry.RegistryEntry;
import net.neoforged.fml.common.EventBusSubscriber;

import static com.teameway.bnvilmb.BnvilMultiBlock.REGISTRUM;

/**
 * 多方块机器与控制器的注册
 *
 * <p>控制器进入 {@code bnvil_multiblock:multiblock_controllers}，机器声明进入 {@code bnvil_multiblock:multiblocks}，
 * datagen 直接遍历后者输出结构模板 JSON；模板用 supplier 传入，没到求值时机就不构建。</p>
 */
@EventBusSubscriber(modid = BnvilMultiBlock.MODID)
public class BnvilMachines {

    /**
     * 简易处理器，结构模板取自 {@code BnvilMultiblockDefinitions}
     */
    public static final RegistryEntry<MultiblockMachine, MultiblockMachine> SIMPLE_PROCESSOR = REGISTRUM.multiblock(
        "simple_processor",
        () -> MultiblockMachine.builder(BnvilControllers.SIMPLE_PROCESSOR_CONTROLLER)
            .pattern(() -> BnvilMultiblockDefinitions.SIMPLE_PROCESSOR)
            .build()
    ).register();

    /**
     * 触发类加载，条目由 {@code REGISTRUM} 统一注册
     */
    public static void register() {
    }
}
