package com.teameway.bnvilmb.multiblock.structure;

import com.teameway.bnvilmb.multiblock.port.PortKind;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

/**
 * 单个槽位的匹配结果
 *
 * @param pos      世界坐标
 * @param key      网格字符
 * @param state    实际方块状态
 * @param portKind 实际端口种类，非端口为 {@code null}
 * @param role     分配到的角色，空气为 {@code null}
 */
public record SlotMatch(
    BlockPos pos,
    char key,
    BlockState state,
    @Nullable PortKind portKind,
    @Nullable String role
) {
}
