package com.teameway.bnvilmb.init.multiblock;

import com.teameway.bnvilmb.multiblock.controller.ControllerBlockEntity;
import dev.anvilcraft.lib.v2.registrum.util.entry.BlockEntityEntry;

import static com.teameway.bnvilmb.BnvilMultiBlock.REGISTRUM;

/**
 * 控制器方块实体的注册
 *
 * <p>方块实体绑定到 {@code BnvilControllerBlocks.SIMPLE_CONTROLLER}，控制器回调直接以它为参数。</p>
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