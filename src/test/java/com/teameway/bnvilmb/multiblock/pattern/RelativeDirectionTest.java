package com.teameway.bnvilmb.multiblock.pattern;

import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class RelativeDirectionTest {
    @Test
    void frontFollowsAllSixDirections() {
        assertEquals(Direction.NORTH, RelativeDirection.FRONT.getRelativeFacing(Direction.NORTH, Direction.UP, false));
        assertEquals(Direction.SOUTH, RelativeDirection.FRONT.getRelativeFacing(Direction.SOUTH, Direction.UP, false));
        assertEquals(Direction.WEST, RelativeDirection.FRONT.getRelativeFacing(Direction.WEST, Direction.UP, false));
        assertEquals(Direction.EAST, RelativeDirection.FRONT.getRelativeFacing(Direction.EAST, Direction.UP, false));
        assertEquals(Direction.UP, RelativeDirection.FRONT.getRelativeFacing(Direction.UP, Direction.NORTH, false));
        assertEquals(Direction.DOWN, RelativeDirection.FRONT.getRelativeFacing(Direction.DOWN, Direction.NORTH, false));
    }

    @Test
    void upAndRightFollowTheFrame() {
        assertEquals(Direction.UP, RelativeDirection.UP.getRelativeFacing(Direction.NORTH, Direction.UP, false));
        assertEquals(Direction.EAST, RelativeDirection.RIGHT.getRelativeFacing(Direction.NORTH, Direction.UP, false));
        assertEquals(Direction.WEST, RelativeDirection.LEFT.getRelativeFacing(Direction.NORTH, Direction.UP, false));
    }

    @Test
    void flipMirrorsOnlyTheRightAxis() {
        assertEquals(Direction.WEST, RelativeDirection.RIGHT.getRelativeFacing(Direction.NORTH, Direction.UP, true));
        assertEquals(Direction.EAST, RelativeDirection.LEFT.getRelativeFacing(Direction.NORTH, Direction.UP, true));
        assertEquals(Direction.NORTH, RelativeDirection.FRONT.getRelativeFacing(Direction.NORTH, Direction.UP, true));
        assertEquals(Direction.UP, RelativeDirection.UP.getRelativeFacing(Direction.NORTH, Direction.UP, true));
    }

    @Test
    void rejectsNonPerpendicularFrame() {
        assertThrows(IllegalArgumentException.class, () ->
            RelativeDirection.FRONT.getRelativeFacing(Direction.NORTH, Direction.SOUTH, false)
        );
    }

    @Test
    void localOffsetsAreOrthogonal() {
        assertEquals(new Vec3i(0, 0, -1), RelativeDirection.FRONT.localOffset());
        assertEquals(new Vec3i(1, 0, 0), RelativeDirection.RIGHT.localOffset());
        assertEquals(0, dot(RelativeDirection.FRONT.localOffset(), RelativeDirection.RIGHT.localOffset()));
    }

    private static int dot(Vec3i a, Vec3i b) {
        return a.getX() * b.getX() + a.getY() * b.getY() + a.getZ() * b.getZ();
    }
}
