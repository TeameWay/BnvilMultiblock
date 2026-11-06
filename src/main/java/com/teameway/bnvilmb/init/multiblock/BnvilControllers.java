package com.teameway.bnvilmb.init.multiblock;

import com.teameway.bnvilmb.multiblock.StructureController;
import com.teameway.bnvilmb.multiblock.controller.SimpleProcessorController;
import dev.anvilcraft.lib.v2.registrum.util.entry.RegistryEntry;

import static com.teameway.bnvilmb.BnvilMultiBlock.REGISTRUM;

/**
 * 控制器实例注册
 *
 * <p>入口 {@code simple_processor} 同时是结构定义 ID 的路径，datagen 按它输出
 * {@code structure_patterns/simple_processor.json}。</p>
 */
public class BnvilControllers {
    public static final RegistryEntry<StructureController, SimpleProcessorController> SIMPLE_PROCESSOR_CONTROLLER = REGISTRUM.multiblockController(
        "simple_processor",
        BnvilControllerBlocks.SIMPLE_CONTROLLER,
        SimpleProcessorController::new
    ).register();

    /**
     * 触发类加载，条目由 {@code REGISTRUM} 统一注册
     */
    public static void register() {
    }
}
