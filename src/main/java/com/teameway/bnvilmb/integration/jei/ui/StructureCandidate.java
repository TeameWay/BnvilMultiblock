package com.teameway.bnvilmb.integration.jei.ui;

import net.minecraft.world.level.block.Block;

/**
 * 某个位置的一个候选方块
 *
 * <p>附带该方块在结构里的数量条件：槽位里的端口与替代物取所在角色的全局 {@code min}/{@code max}；
 * 固定格取它在结构里占的格数，此时最少与最多相等。</p>
 *
 * @param block 方块
 * @param min   最少数量，负数表示无法给出条件
 * @param max   最多数量，负数表示不限
 */
public record StructureCandidate(Block block, int min, int max) {

    /**
     * 固定格：数量既是最少也是最多
     *
     * @param block 方块
     * @param count 该谓词在结构里占的格数
     * @return 候选方块
     */
    public static StructureCandidate exact(Block block, int count) {
        return new StructureCandidate(block, count, count);
    }

    /**
     * 条件给不出数量时用的占位值
     *
     * @param block 方块
     * @return 候选方块，不带数量条件
     */
    public static StructureCandidate unknown(Block block) {
        return new StructureCandidate(block, -1, -1);
    }
}
