package com.teameway.bnvilmb.multiblock.structure;

import com.teameway.bnvilmb.multiblock.port.PortKind;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

/**
 * 单个槽位在匹配时的实际内容
 *
 * <p>空气槽位的 {@code portKind} 与 {@code role} 都为 {@code null}；非端口替代方块只填 {@code role}。</p>
 *
 * @param pos 世界坐标
 * @param key 网格字符
 * @param state 实际方块状态，空气也照原状态记录
 * @param portKind 实际端口种类，非端口时为 {@code null}
 * @param role 分配到的角色 ID，空气时为 {@code null}
 */
public record SlotMatch(
    BlockPos pos,
    char key,
    BlockState state,
    @Nullable PortKind portKind,
    @Nullable String role
) {
}
