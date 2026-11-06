package com.teameway.bnvilmb.multiblock;

import dev.anvilcraft.lib.v2.registrum.util.entry.RegistryEntry;

/**
 * 多方块机器类型
 *
 * <p>类型在 Java 注册表里声明，决定用哪个控制器、哪套配方；具体形态通过 pattern 的
 * {@code variants} 集合关联。</p>
 *
 * @param controller 控制器入口
 */
public record MultiblockType(RegistryEntry<StructureController, ? extends StructureController> controller) {
}
