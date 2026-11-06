package com.teameway.bnvilmb.multiblock;

import com.teameway.bnvilmb.init.BnvilRegistries;
import net.minecraft.world.level.block.Block;
import org.jspecify.annotations.Nullable;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 控制器方块到控制器实例的登记表
 *
 * <p>一个控制器方块当前只对应一个代码 pattern；以后需要一方块多结构时，把键扩展成
 * {@code (block, structureKey)} 即可。</p>
 */
public final class StructureControllerRegistry {
    private static final Map<Block, StructureController> BY_BLOCK = new ConcurrentHashMap<>();

    private StructureControllerRegistry() {
    }

    /**
     * 登记控制器
     *
     * <p>以 {@link StructureController#block()} 为键。重复登记时映射先指向新实例，再抛出异常，
     * 旧实例之后查不到。</p>
     *
     * @param controller 控制器实例
     * @throws IllegalStateException 该方块已经登记过控制器
     */
    public static void register(StructureController controller) {
        StructureController previous = BY_BLOCK.put(controller.block(), controller);
        if (previous != null) {
            throw new IllegalStateException("Controller block already registered: " + controller.block());
        }
    }

    /**
     * 从全局控制器注册表重建方块映射。
     *
     * <p>其他模组用自己的 {@code BnvilRegistrum} 注册的控制器也在这个注册表里，因此不能再只遍历
     * 某一个 Registrum 实例。</p>
     */
    public static void reload() {
        BY_BLOCK.clear();
        BnvilRegistries.controllers().stream().forEach(StructureControllerRegistry::register);
    }

    /**
     * 按控制器方块查找
     *
     * @param block 控制器方块
     * @return 控制器实例，未登记返回 {@code null}
     */
    public static @Nullable StructureController get(Block block) {
        return BY_BLOCK.get(block);
    }
}