package com.teameway.bnvilmb.multiblock;

import com.teameway.bnvilmb.multiblock.controller.ControllerBlockEntity;
import com.teameway.bnvilmb.multiblock.structure.StructureMatch;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;

/**
 * 多方块控制器
 *
 * <p>只负责控制器方块、结构 ID 与成型/解散回调；结构判定由 {@code StructureManager} 完成。
 * 实现类经 {@code StructureControllerRegistry} 登记，一个控制器方块对应一个实例。</p>
 */
public interface StructureController {
    /**
     * @return 控制器方块
     */
    Block block();

    /**
     * @return 结构模板 ID
     */
    Identifier patternId();

    /**
     * 兼容旧调用点的别名
     *
     * @return 控制器方块
     */
    default Block getBlock() {
        return this.block();
    }

    /**
     * 兼容旧调用点的别名
     *
     * @return 结构模板 ID
     */
    default Identifier getDefinitionId() {
        return this.patternId();
    }

    /**
     * 结构成型时调用，默认实现为空
     *
     * @param level 服务端世界
     * @param controller 控制器方块实体
     * @param match 本次结构匹配的结果
     */
    default void onFormed(ServerLevel level, ControllerBlockEntity controller, StructureMatch match) {
    }

    /**
     * 结构解散时调用，默认实现为空
     *
     * @param level 服务端世界
     * @param controller 控制器方块实体
     */
    default void onUnformed(ServerLevel level, ControllerBlockEntity controller) {
    }
}
