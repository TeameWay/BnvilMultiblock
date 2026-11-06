package com.teameway.bnvilmb.dev.init.multiblock;

import com.teameway.bnvilmb.multiblock.pattern.BlockPattern;
import com.teameway.bnvilmb.multiblock.pattern.MultiPredicate;
import com.teameway.bnvilmb.multiblock.pattern.MultiblockPatternBuilder;
import com.teameway.bnvilmb.multiblock.pattern.Predicates;
import com.teameway.bnvilmb.multiblock.pattern.RelativeDirection;
import com.teameway.bnvilmb.multiblock.port.PortKind;
import dev.anvilcraft.lib.v2.util.predicate.BlockStatePredicate;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Blocks;

/**
 * 模组内置结构模板。
 *
 * <p>结构使用 GTM 风格的代码 builder 定义：{@code slice(...)} 写层，{@code where(...)} 绑定
 * 方块或端口谓词。{@code $} 槽位同时接受物品输入、物品输出、流体输入、并行仓和替代方块，
 * 数量约束挂在 predicate 上。</p>
 */
public final class BnvilMultiblockDefinitions {
    private static final Identifier SIMPLE_PROCESSOR_ID = Identifier.fromNamespaceAndPath("bnvil_multiblock", "simple_processor");
    private static final Identifier COOLED_ID = Identifier.fromNamespaceAndPath("bnvil_multiblock", "cooled");

    public static final BlockPattern SIMPLE_PROCESSOR = MultiblockPatternBuilder
        .start(RelativeDirection.UP, RelativeDirection.FRONT, RelativeDirection.RIGHT)
        .slice("AAA", "AAA", "AAA")
        .slice("$B$", "B0B", "$B$")
        .where('A', Predicates.blocks(Blocks.IRON_BLOCK))
        .where('B', Predicates.blocks(Blocks.GOLD_BLOCK))
        .where('0', Predicates.controller(BnvilControllerBlocks.SIMPLE_CONTROLLER.get()))
        .where('$', slotPredicate())
        .allowFlip(true)
        .variant(SIMPLE_PROCESSOR_ID)
        .variant(COOLED_ID)
        .build();

    private BnvilMultiblockDefinitions() {
    }

    /**
     * 默认端口配置
     *
     * <p>输入总线(0~2)，输出总线(至少 1)，输入仓(1~2)，并行仓与线程仓按放置数量。</p>
     */
    public static MultiPredicate slotPredicate() {
        return Predicates
            .port(PortKind.ITEM_INPUT).setMaxGlobalLimited(2)
            .and(Predicates.port(PortKind.ITEM_OUTPUT).setMinGlobalLimited(1))
            .and(Predicates.port(PortKind.FLUID_INPUT).setGlobalMinMax(1, 2))
            .and(Predicates.port(PortKind.PARALLEL))
            .and(Predicates.port(PortKind.THREAD))
            .and(Predicates.substitute(BlockStatePredicate.builder().of(Blocks.GOLD_BLOCK).build(), 2, 1))
            .and(Predicates.substitute(BlockStatePredicate.builder().of(Blocks.IRON_BLOCK).build(), 1, 1));
    }
}
