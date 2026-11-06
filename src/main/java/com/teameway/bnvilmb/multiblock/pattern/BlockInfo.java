package com.teameway.bnvilmb.multiblock.pattern;

import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

/**
 * 一个结构位置的方块快照。
 *
 * <p>只保存方块状态与方块实体引用，用于缓存快速校验；不复制方块实体数据。</p>
 *
 * @param state 方块状态
 * @param blockEntity 方块实体，没有时为 {@code null}
 */
public record BlockInfo(BlockState state, @Nullable BlockEntity blockEntity) {
}
