package com.teameway.bnvilmb.init.multiblock;

import com.teameway.bnvilmb.common.multiblock.controller.SimpleProcessorController;
import dev.anvilcraft.lib.v2.multiblock.dynamic.controller.IController;
import dev.anvilcraft.lib.v2.registrum.util.entry.RegistryEntry;

import static com.teameway.bnvilmb.BnvilMultiBlock.REGISTRUM;

public class BnvilControllers {
    public static final RegistryEntry<IController, SimpleProcessorController> SIMPLE_PROCESSOR_CONTROLLER = REGISTRUM.multiblockController(
        "simple_processor",
        BnvilControllerBlocks.SIMPLE_CONTROLLER,
        SimpleProcessorController::new
    ).register();

    public static void register() {
    }
}