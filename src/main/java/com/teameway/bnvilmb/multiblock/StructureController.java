package com.teameway.bnvilmb.multiblock;

import com.teameway.bnvilmb.multiblock.controller.ControllerBlockEntity;
import com.teameway.bnvilmb.multiblock.structure.StructureMatch;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;

/**
 * 多方块控制器
 *
 * <p>只负责控制器方块、结构 ID 与成型/解散回调；结构判定由 {@code StructureManager} 完成。</p>
 */
public interface StructureController {
    Block block();

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

    default void onFormed(ServerLevel level, ControllerBlockEntity controller, StructureMatch match) {
    }

    default void onUnformed(ServerLevel level, ControllerBlockEntity controller) {
    }
}
