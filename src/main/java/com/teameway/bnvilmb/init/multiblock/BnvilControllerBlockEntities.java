package com.teameway.bnvilmb.init.multiblock;

import com.teameway.bnvilmb.multiblock.controller.ControllerBlockEntity;
import dev.anvilcraft.lib.v2.registrum.util.entry.BlockEntityEntry;

import static com.teameway.bnvilmb.BnvilMultiBlock.REGISTRUM;

public class BnvilControllerBlockEntities {

    public static final BlockEntityEntry<ControllerBlockEntity> SIMPLE_CONTROLLER = REGISTRUM
        .blockEntity("simple_controller", ControllerBlockEntity::new)
        .validBlock(BnvilControllerBlocks.SIMPLE_CONTROLLER)
        .register();

    public static void register() {
    }
}