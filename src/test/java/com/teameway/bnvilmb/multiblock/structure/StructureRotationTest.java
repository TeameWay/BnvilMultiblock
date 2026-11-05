package com.teameway.bnvilmb.multiblock.structure;

import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class StructureRotationTest {
    @Test
    void rotatesNorthToEast() {
        assertEquals(new Vec3i(1, 0, 0), StructureRotation.rotate(
            new Vec3i(0, 0, -1), Direction.NORTH, Direction.EAST, StructureOrientation.HORIZONTAL
        ));
    }

    @Test
    void rotatesNorthToSouth() {
        assertEquals(new Vec3i(0, 0, 1), StructureRotation.rotate(
            new Vec3i(0, 0, -1), Direction.NORTH, Direction.SOUTH, StructureOrientation.HORIZONTAL
        ));
    }

    @Test
    void rotatesNorthToWest() {
        assertEquals(new Vec3i(-1, 0, 0), StructureRotation.rotate(
            new Vec3i(0, 0, -1), Direction.NORTH, Direction.WEST, StructureOrientation.HORIZONTAL
        ));
    }

    @Test
    void noneKeepsLocalCoordinates() {
        assertEquals(new Vec3i(2, 3, -4), StructureRotation.rotate(
            new Vec3i(2, 3, -4), Direction.NORTH, Direction.EAST, StructureOrientation.NONE
        ));
    }
}
