package com.teameway.bnvilmb.init.multiblock;

import com.teameway.bnvilmb.multiblock.StructureController;
import com.teameway.bnvilmb.multiblock.controller.SimpleProcessorController;
import dev.anvilcraft.lib.v2.registrum.util.entry.RegistryEntry;

import static com.teameway.bnvilmb.BnvilMultiBlock.REGISTRUM;

public class BnvilControllers {
    public static final RegistryEntry<StructureController, SimpleProcessorController> SIMPLE_PROCESSOR_CONTROLLER = REGISTRUM.multiblockController(
        "simple_processor",
        BnvilControllerBlocks.SIMPLE_CONTROLLER,
        SimpleProcessorController::new
    ).register();

    public static void register() {
    }
}
