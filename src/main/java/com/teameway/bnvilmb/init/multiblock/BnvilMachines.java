package com.teameway.bnvilmb.init.multiblock;

import com.teameway.bnvilmb.BnvilMultiBlock;
import com.teameway.bnvilmb.common.multiblock.MultiblockMachine;
import dev.anvilcraft.lib.v2.registrum.util.entry.RegistryEntry;
import net.neoforged.fml.common.EventBusSubscriber;

import static com.teameway.bnvilmb.BnvilMultiBlock.REGISTRUM;

/**
 * 多方块机器与控制器的注册
 *
 * <p>控制器进入 {@code bnvil_multiblock:multiblock_controllers}，机器声明进入 {@code bnvil_multiblock:multiblocks}，
 * datagen 直接遍历后者输出定义 JSON。</p>
 */
@EventBusSubscriber(modid = BnvilMultiBlock.MODID)
public class BnvilMachines {

    public static final RegistryEntry<MultiblockMachine, MultiblockMachine> SIMPLE_PROCESSOR = REGISTRUM.multiblock(
        "simple_processor",
        () -> MultiblockMachine.builder(BnvilControllers.SIMPLE_PROCESSOR_CONTROLLER)
            .definition(() -> BnvilMultiblockDefinitions.SIMPLE_PROCESSOR)
            .build()
    ).register();

    public static void register() {
    }
}