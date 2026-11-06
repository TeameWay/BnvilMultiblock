package com.teameway.bnvilmb.init.multiblock;

import com.teameway.bnvilmb.multiblock.port.PortKind;
import com.teameway.bnvilmb.multiblock.structure.StructureOrientation;
import com.teameway.bnvilmb.multiblock.structure.StructurePattern;
import com.teameway.bnvilmb.multiblock.structure.StructureRole;
import com.teameway.bnvilmb.multiblock.structure.Substitute;
import dev.anvilcraft.lib.v2.util.predicate.BlockStatePredicate;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Blocks;

import java.util.List;

/**
 * 模组内置结构模板
 *
 * <p>{@link #SIMPLE_PROCESSOR} 分两层，每层 3×3：第一层全是铁块，第二层四角是槽位 {@code $}，
 * 其余为金块，正中的 {@code 0} 是控制器。结构随控制器水平朝向旋转，主朝向为北。</p>
 *
 * <p>槽位接受 {@code item_input}、{@code item_output}、{@code fluid_input} 与 {@code parallel}
 * 四种角色；{@code item_output} 与 {@code fluid_input} 至少各一个，{@code item_input} 最多两个，
 * {@code parallel} 不限数量，也可以用金块或铁块顶替。</p>
 */
public class BnvilMultiblockDefinitions {
    /*
     * 并行槽位可用的替代方块：金块与铁块，并行与线程加成都传 0，只参与结构判定。
     */
    private static final Substitute GOLD = new Substitute(
        BlockStatePredicate.builder().of(Blocks.GOLD_BLOCK).build(), 0, 0
    );
    private static final Substitute IRON = new Substitute(
        BlockStatePredicate.builder().of(Blocks.IRON_BLOCK).build(), 0, 0
    );

    public static final StructurePattern SIMPLE_PROCESSOR = StructurePattern.builder()
        .orientation(StructureOrientation.HORIZONTAL)
        .canonicalFront(Direction.NORTH)
        .layer("AAA", "AAA", "AAA")
        .layer("$B$", "B0B", "$B$")
        .where('A', Blocks.IRON_BLOCK)
        .where('B', Blocks.GOLD_BLOCK)
        .controller('0', BnvilControllerBlocks.SIMPLE_CONTROLLER.get())
        .slot('$', List.of("item_input", "item_output", "fluid_input", "parallel"), false)
        .role("item_input", new StructureRole(List.of(PortKind.ITEM_INPUT), List.of(), 0, 2))
        .role("item_output", new StructureRole(List.of(PortKind.ITEM_OUTPUT), List.of(), 1, -1))
        .role("fluid_input", new StructureRole(List.of(PortKind.FLUID_INPUT), List.of(), 1, 2))
        .role("parallel", new StructureRole(List.of(PortKind.PARALLEL), List.of(GOLD, IRON), 0, -1))
        .build();

    private BnvilMultiblockDefinitions() {
    }
}
