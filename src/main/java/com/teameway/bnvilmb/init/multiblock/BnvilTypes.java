package com.teameway.bnvilmb.init.multiblock;

import com.teameway.bnvilmb.common.multiblock.MultiblockType;
import dev.anvilcraft.lib.v2.registrum.util.entry.RegistryEntry;

import static com.teameway.bnvilmb.BnvilMultiBlock.REGISTRUM;

/**
 * 多方块机器类型注册
 */
public class BnvilTypes {
    public static final RegistryEntry<MultiblockType, MultiblockType> SIMPLE_PROCESSOR =
        REGISTRUM.multiblockType(
            "simple_processor",
            () -> new MultiblockType(BnvilControllers.SIMPLE_PROCESSOR_CONTROLLER)
        ).register();

    public static void register() {
    }
}