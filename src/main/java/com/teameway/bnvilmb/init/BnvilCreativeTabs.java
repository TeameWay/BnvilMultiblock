package com.teameway.bnvilmb.init;

import com.teameway.bnvilmb.init.multiblock.BnvilControllerBlocks;
import dev.anvilcraft.lib.v2.registrum.util.entry.RegistryEntry;
import net.minecraft.world.item.CreativeModeTab;

import static com.teameway.bnvilmb.BnvilMultiBlock.REGISTRUM;

/**
 * 创造模式物品栏
 *
 * <p>两个物品栏的图标都是控制器方块，延迟到注册阶段才求值，
 * 避免与 {@link BnvilControllerBlocks} 的静态初始化互相等待。</p>
 */
public class BnvilCreativeTabs {
    /**
     * 主物品栏，模组入口会把它设为 Registrum 的默认创造页
     */
    public static final RegistryEntry<CreativeModeTab, CreativeModeTab> MULTIBLOCK = REGISTRUM
        .creativeTab("multiblock", BnvilControllerBlocks.SIMPLE_CONTROLLER)
        .lang(_ -> "itemGroup.bnvil_multiblock.multiblock", "BnvilMultiblock")
        .register();

    public static final RegistryEntry<CreativeModeTab, CreativeModeTab> MULTIBLOCK_CONTROLLER = REGISTRUM
        .creativeTab("multiblock_controller", BnvilControllerBlocks.SIMPLE_CONTROLLER)
        .lang(_ -> "itemGroup.bnvil_multiblock.multiblock_controller", "BnvilMultiblock - Controller")
        .register();

    /**
     * 触发类加载，条目由 {@code REGISTRUM} 统一注册
     */
    public static void register() {
    }
}
