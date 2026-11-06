package com.teameway.bnvilmb.multiblock.pattern;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Blocks;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MultiblockPatternBuilderTest {
    private static BlockPattern simplePattern() {
        return MultiblockPatternBuilder.start(RelativeDirection.UP, RelativeDirection.FRONT, RelativeDirection.RIGHT)
            .slice("0G")
            .where('0', Predicates.controller(Blocks.IRON_BLOCK))
            .where('G', Predicates.blocks(Blocks.GOLD_BLOCK))
            .allowFlip(true)
            .build();
    }

    @Test
    void controllerOffsetAndSixWayPositions() {
        BlockPattern pattern = simplePattern();
        List<BlockPos> north = pattern.positions(BlockPos.ZERO, Direction.NORTH, Direction.UP, false);
        assertTrue(north.contains(BlockPos.ZERO), "controller should be at origin");
        assertTrue(north.contains(new BlockPos(1, 0, 0)), "north front should put G east");

        List<BlockPos> east = pattern.positions(BlockPos.ZERO, Direction.EAST, Direction.UP, false);
        assertTrue(east.contains(new BlockPos(0, 0, 1)), "east front should put G south");
    }

    @Test
    void flipMirrorsThePattern() {
        BlockPattern pattern = simplePattern();
        List<BlockPos> mirrored = pattern.positions(BlockPos.ZERO, Direction.NORTH, Direction.UP, true);
        assertTrue(mirrored.contains(new BlockPos(-1, 0, 0)), "flip should put G west");
    }

    @Test
    void cellsExposePredicates() {
        BlockPattern pattern = simplePattern();
        List<BlockPattern.Cell> cells = pattern.cells(BlockPos.ZERO, Direction.NORTH, Direction.UP, false);
        assertEquals(2, cells.size());
        assertTrue(cells.stream().anyMatch(cell -> cell.predicate().isController()));
    }
}
