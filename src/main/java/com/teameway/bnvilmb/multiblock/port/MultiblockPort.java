package com.teameway.bnvilmb.multiblock.port;

import net.minecraft.core.BlockPos;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Set;

/**
 * 多方块端口。
 *
 * <p>端口可以被多个控制器共享，和 GTM 的 {@code MultiblockPartMachine} 一样保存
 * 控制器位置集合；{@link #canShared()} 为 {@code false} 的端口仍然只允许一个控制器。</p>
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
     * @return 当前认领该端口的控制器位置，只读
     */
    Set<BlockPos> controllerPositions();

    /**
     * 返回客户端界面可用的控制器位置
     *
     * <p>方块实体在客户端不持有服务端的归属集合，具体实现应返回同步字段里的列表；
     * 没有同步数据的实现退回 {@link #controllerPositions()}。</p>
     *
     * @return 控制器位置列表
     */
    default List<BlockPos> visibleControllers() {
        return List.copyOf(this.controllerPositions());
    }

    /**
     * @return 是否允许被多个控制器共享，默认允许
     */
    default boolean canShared() {
        return true;
    }

    /**
     * @param controllerPos 控制器位置
     * @return 是否已经属于该控制器
     */
    default boolean hasController(BlockPos controllerPos) {
        return this.controllerPositions().contains(controllerPos);
    }

    /**
     * 加入一个控制器。
     *
     * @param controllerPos 控制器位置
     */
    default void addController(BlockPos controllerPos) {
    }

    /**
     * 移除一个控制器。
     *
     * @param controllerPos 控制器位置
     */
    default void removeController(BlockPos controllerPos) {
    }

    /**
     * 兼容旧代码：返回第一个控制器位置。
     *
     * @return 第一个控制器位置，没有时为 {@code null}
     */
    default @Nullable BlockPos controllerPos() {
        return this.controllerPositions().stream().findFirst().orElse(null);
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
