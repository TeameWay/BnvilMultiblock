package com.teameway.bnvilmb.integration.jei.ui;

import net.minecraft.world.level.block.Block;

/**
 * 结构所需方块及其数量
 *
 * <p>清单按“控制器、端口、其余”分组，组内再按数量从多到少排列，界面上直接按列表顺序铺格，
 * 不再二次排序。</p>
 *
 * @param block 方块
 * @param count 该方块在结构中出现的次数
 */
public record RequiredBlock(Block block, int count) {
}
