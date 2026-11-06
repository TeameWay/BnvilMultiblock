package com.teameway.bnvilmb.dev.init.multiblock;

import com.teameway.bnvilmb.multiblock.controller.ControllerBlockEntity;
import dev.anvilcraft.lib.v2.registrum.util.entry.BlockEntityEntry;

import static com.teameway.bnvilmb.BnvilMultiBlock.REGISTRUM;

/**
 * 控制器方块实体的注册
 */
public class BnvilControllerBlockEntities {

    public static final BlockEntityEntry<ControllerBlockEntity> SIMPLE_CONTROLLER = REGISTRUM
        .blockEntity("simple_controller", ControllerBlockEntity::new)
        .validBlock(BnvilControllerBlocks.SIMPLE_CONTROLLER)
        .register();

    /**
     * 触发类加载，条目由 {@code REGISTRUM} 统一注册
     */
    public static void register() {
    }
}