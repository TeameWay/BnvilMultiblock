package com.teameway.bnvilmb.multiblock.structure;

import com.teameway.bnvilmb.multiblock.port.PortKind;
import dev.anvilcraft.lib.v2.util.predicate.BlockStatePredicate;
import net.minecraft.world.level.block.Blocks;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class StructureRoleTest {
    @Test
    void missingMaxMeansUnlimited() {
        StructureRole role = new StructureRole(List.of(PortKind.ITEM_INPUT), List.of(), 0, -1);
        assertEquals(Integer.MAX_VALUE, role.maxCount());
    }

    @Test
    void rejectsInvertedRange() {
        assertThrows(IllegalArgumentException.class,
            () -> new StructureRole(List.of(PortKind.ITEM_INPUT), List.of(), 2, 1));
    }

    @Test
    void rejectsNegativeSubstituteBonus() {
        assertThrows(IllegalArgumentException.class,
            () -> new Substitute(BlockStatePredicate.builder().of(Blocks.GOLD_BLOCK).build(), -1, 0));
    }
}
