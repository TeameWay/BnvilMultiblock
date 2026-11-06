package com.teameway.bnvilmb.dev.init;

import com.teameway.bnvilmb.dev.init.multiblock.BnvilControllerBlocks;
import dev.anvilcraft.lib.v2.registrum.util.entry.RegistryEntry;
import net.minecraft.world.item.CreativeModeTab;

import static com.teameway.bnvilmb.BnvilMultiBlock.REGISTRUM;

/**
 * 创造模式物品栏
 *
 */
public class BnvilCreativeTabs {
    /**
     * 主物品栏，开发入口会把它设为 Registrum 的默认创造页
     */
    public static final RegistryEntry<CreativeModeTab, CreativeModeTab> MULTIBLOCK = REGISTRUM
        .creativeTab("multiblock", BnvilControllerBlocks.SIMPLE_CONTROLLER)
        .lang(_ -> "itemGroup.bnvil_multiblock.multiblock", "BnvilMultiblock")
        .register();

    /**
     * 触发类加载，条目由 {@code REGISTRUM} 统一注册
     */
    public static void register() {
    }
}
