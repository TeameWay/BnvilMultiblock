package com.teameway.bnvilmb.dev.init.multiblock;

import com.teameway.bnvilmb.dev.controller.SimpleProcessorController;
import com.teameway.bnvilmb.multiblock.StructureController;
import dev.anvilcraft.lib.v2.registrum.util.entry.RegistryEntry;

import static com.teameway.bnvilmb.BnvilMultiBlock.REGISTRUM;

/**
 * 控制器实例注册。
 */
public class BnvilControllers {
    public static final RegistryEntry<StructureController, SimpleProcessorController> SIMPLE_PROCESSOR_CONTROLLER = REGISTRUM.multiblockController(
        "simple_processor",
        BnvilControllerBlocks.SIMPLE_CONTROLLER,
        block -> new SimpleProcessorController(block, () -> BnvilMultiblockDefinitions.SIMPLE_PROCESSOR)
    ).register();

    /**
     * 触发类加载，条目由 {@code REGISTRUM} 统一注册
     */
    public static void register() {
    }
}
