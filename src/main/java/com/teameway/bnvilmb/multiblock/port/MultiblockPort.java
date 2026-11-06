package com.teameway.bnvilmb.multiblock.port;

import net.minecraft.core.BlockPos;
import org.jspecify.annotations.Nullable;

/**
 * 多方块端口
 *
 * <p>由端口方块实体实现，结构成型时被扫描收集；资源端口再实现 {@link ItemPort}、{@link FluidPort}、
 * {@link EnergyPort}。</p>
 */
public interface MultiblockPort {
    /**
     * @return 端口种类
     */
    PortKind portKind();

    /**
     * @return 端口等级
     */
    PortTier tier();

    /**
     * @return 当前认领该端口的控制器位置，未认领时为 {@code null}
     */
    default @Nullable BlockPos controllerPos() {
        return null;
    }

    /**
     * 设置端口归属，由结构管理器在成型与解散时调用
     *
     * @param controllerPos 控制器位置，{@code null} 表示解除认领
     */
    default void setControllerPos(@Nullable BlockPos controllerPos) {
    }

    /**
     * @return 提供的线程增量，非线程仓返回 0
     */
    default int threadsDelta() {
        return this.tier().threads();
    }

    /**
     * @return 提供的并行增量，非并行仓返回 0
     */
    default int parallelsDelta() {
        return this.tier().parallels();
    }
}
