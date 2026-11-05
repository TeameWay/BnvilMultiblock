package com.teameway.bnvilmb.init.multiblock;

import com.teameway.bnvilmb.common.multiblock.controller.ControllerBlock;
import com.teameway.bnvilmb.init.BnvilCreativeTabs;
import dev.anvilcraft.lib.v2.registrum.util.entry.BlockEntry;

import static com.teameway.bnvilmb.BnvilMultiBlock.REGISTRUM;

public class BnvilControllerBlocks {

    public static final BlockEntry<ControllerBlock> SIMPLE_CONTROLLER = REGISTRUM
        .block("simple_controller", ControllerBlock::new)
        .properties(properties -> properties.strength(3.5F).requiresCorrectToolForDrops())
        .simpleItem()
        .register();

    static {
        REGISTRUM.defaultCreativeTab(BnvilCreativeTabs.MULTIBLOCK);
    }

    public static void register() {
    }
}