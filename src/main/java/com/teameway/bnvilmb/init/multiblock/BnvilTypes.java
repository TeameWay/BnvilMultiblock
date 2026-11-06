package com.teameway.bnvilmb.init.multiblock;

import com.teameway.bnvilmb.multiblock.MultiblockType;
import dev.anvilcraft.lib.v2.registrum.util.entry.RegistryEntry;

import static com.teameway.bnvilmb.BnvilMultiBlock.REGISTRUM;

/**
 * 多方块机器类型注册
 *
 * <p>类型只有 {@code simple_processor} 一项，与控制器入口同名；条目值只持有控制器入口。</p>
 */
public class BnvilTypes {
    public static final RegistryEntry<MultiblockType, MultiblockType> SIMPLE_PROCESSOR =
        REGISTRUM.multiblockType(
            "simple_processor",
            () -> new MultiblockType(BnvilControllers.SIMPLE_PROCESSOR_CONTROLLER)
        ).register();

    /**
     * 触发类加载，条目由 {@code REGISTRUM} 统一注册
     */
    public static void register() {
    }
}