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
 */
public class BnvilMultiblockDefinitions {
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
