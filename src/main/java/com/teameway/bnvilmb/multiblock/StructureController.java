package com.teameway.bnvilmb.multiblock;

import com.teameway.bnvilmb.multiblock.controller.ControllerBlockEntity;
import com.teameway.bnvilmb.multiblock.pattern.BlockPattern;
import com.teameway.bnvilmb.multiblock.pattern.PatternState;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;

/**
 * 多方块控制器。
 *
 * <p>结构模板由代码定义，控制器直接持有 {@link BlockPattern}；结构判定由
 * {@code StructureManager} 驱动。实现类经 {@code StructureControllerRegistry} 登记。</p>
 */
public interface StructureController {
    /**
     * @return 控制器方块
     */
    Block block();

    /**
     * @return 代码定义的结构模板
     */
    BlockPattern pattern();

    /**
     * 兼容旧调用点的别名
     *
     * @return 控制器方块
     */
    default Block getBlock() {
        return this.block();
    }

    /**
     * 结构成型时调用，默认实现为空
     *
     * @param level 服务端世界
     * @param controller 控制器方块实体
     * @param state 本次结构状态
     */
    default void onFormed(ServerLevel level, ControllerBlockEntity controller, PatternState state) {
    }

    /**
     * 结构解散时调用，默认实现为空
     *
     * @param level 服务端世界
     * @param controller 控制器方块实体
     * @param state 解散前的结构状态
     */
    default void onUnformed(ServerLevel level, ControllerBlockEntity controller, PatternState state) {
    }
}
