package com.teameway.bnvilmb.multiblock;

import net.minecraft.world.level.block.Block;
import org.jspecify.annotations.Nullable;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 控制器方块到控制器实例的登记表
 *
 * <p>一个控制器方块当前只对应一个结构；以后需要一方块多结构时，把键扩展成
 * {@code (block, patternId)} 即可。</p>
 */
public final class StructureControllerRegistry {
    private static final Map<Block, StructureController> BY_BLOCK = new ConcurrentHashMap<>();

    private StructureControllerRegistry() {
    }

    public static void register(StructureController controller) {
        StructureController previous = BY_BLOCK.put(controller.block(), controller);
        if (previous != null) {
            throw new IllegalStateException("Controller block already registered: " + controller.block());
        }
    }

    public static @Nullable StructureController get(Block block) {
        return BY_BLOCK.get(block);
    }
}
