package com.teameway.bnvilmb.dev.init.multiblock;

import com.teameway.bnvilmb.dev.controller.ControllerBlock;
import dev.anvilcraft.lib.v2.registrum.util.entry.BlockEntry;

import static com.teameway.bnvilmb.BnvilMultiBlock.REGISTRUM;

/**
 * 控制器方块的注册
 */
public class BnvilControllerBlocks {

    public static final BlockEntry<ControllerBlock> SIMPLE_CONTROLLER = REGISTRUM
        .block("simple_controller", ControllerBlock::new)
        .properties(properties -> properties.strength(3.5F).requiresCorrectToolForDrops())
        .simpleItem()
        .register();

    /**
     * 触发类加载，条目由 {@code REGISTRUM} 统一注册
     */
    public static void register() {
    }
}