package com.teameway.bnvilmb.integration.jei.ui;

import com.lowdragmc.lowdraglib2.gui.ui.elements.Scene;
import com.lowdragmc.lowdraglib2.utils.virtuallevel.TrackedDummyWorld;
import com.teameway.bnvilmb.init.BnvilRegistries;
import com.teameway.bnvilmb.multiblock.MultiblockType;
import com.teameway.bnvilmb.multiblock.port.PortKind;
import com.teameway.bnvilmb.multiblock.port.PortRegistry;
import com.teameway.bnvilmb.multiblock.structure.StructurePattern;
import com.teameway.bnvilmb.multiblock.structure.StructureRole;
import com.teameway.bnvilmb.multiblock.structure.StructureSlot;
import com.teameway.bnvilmb.multiblock.structure.Substitute;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Vec3i;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Vector3f;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 多方块结构预览
 *
 * <p>把结构模板映射成具体方块状态：固定格取谓词的第一个可渲染方块，槽位按角色顺序轮换取一个
 * 合法示例，保证四个角不会都显示成输入总线。写入虚拟世界时整体抬升 {@value #BASE_Y} 格。</p>
 */
public class MultiblockPreview {
    private static final int BASE_Y = 64;
    private static final float CAMERA_RANGE = 1.35F;
    private static final Map<Identifier, Structure> STRUCTURES = new LinkedHashMap<>();

    private MultiblockPreview() {
    }

    public static @Nullable Scene create(MultiblockType machine) {
        Structure structure = structureOf(machine);
        if (structure == null) {
            return null;
        }
        Scene scene = new Scene();
        scene.createScene(structure.world())
            .setTickWorld(true)
            .useCacheBuffer()
            .setClipContext(ClipContext.Block.VISUAL, ClipContext.Fluid.SOURCE_ONLY);
        scene.setRenderedCore(structure.positions(), null, false);
        scene.setCenter(new Vector3f(0.5F, BASE_Y + 0.5F, 0.5F));
        scene.setZoom(structure.zoom());
        return scene;
    }

    public static Map<Block, Integer> requiredBlocks(MultiblockType machine) {
        Structure structure = structureOf(machine);
        return structure == null ? Map.of() : structure.blocks();
    }

    public static @Nullable Block blockAt(MultiblockType machine, BlockPos pos) {
        Structure structure = structureOf(machine);
        return structure == null ? null : structure.blockAt().get(pos);
    }

    private static @Nullable Structure structureOf(MultiblockType machine) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null) {
            return null;
        }
        Identifier patternId = machine.controller().get().patternId();
        Structure cached = STRUCTURES.get(patternId);
        if (cached != null) {
            return cached;
        }
        Structure built = build(patternId, minecraft);
        if (built != null) {
            STRUCTURES.put(patternId, built);
        }
        return built;
    }

    private static @Nullable Structure build(Identifier patternId, Minecraft minecraft) {
        ResourceKey<StructurePattern> key = ResourceKey.create(BnvilRegistries.STRUCTURE_PATTERNS, patternId);
        StructurePattern pattern = minecraft.level.registryAccess()
            .lookupOrThrow(BnvilRegistries.STRUCTURE_PATTERNS)
            .get(key)
            .map(Holder.Reference::value)
            .orElse(null);
        if (pattern == null) {
            return null;
        }

        List<Vec3i> entries = new ArrayList<>(pattern.positions());
        entries.sort(Comparator
            .comparingInt(Vec3i::getY)
            .thenComparingInt(Vec3i::getZ)
            .thenComparingInt(Vec3i::getX));

        TrackedDummyWorld world = new TrackedDummyWorld();
        List<BlockPos> positions = new ArrayList<>(entries.size());
        Map<BlockPos, Block> blockAt = new LinkedHashMap<>();
        Map<Block, Integer> counts = new LinkedHashMap<>();
        int maxX = 1;
        int maxY = 1;
        int maxZ = 1;
        int slotCursor = 0;
        for (Vec3i offset : entries) {
            Block block = pattern.isSlot(offset)
                ? sampleSlotBlock(pattern, offset, slotCursor++)
                : sampleFixedBlock(pattern.fixedAt(offset));
            if (block == Blocks.AIR) {
                continue;
            }
            BlockPos pos = new BlockPos(offset.getX(), BASE_Y + offset.getY(), offset.getZ());
            BlockState state = block.defaultBlockState();
            world.setBlockAndUpdate(pos, state);
            positions.add(pos);
            blockAt.put(pos, block);
            counts.merge(block, 1, Integer::sum);
            maxX = Math.max(maxX, Math.abs(offset.getX()) + 1);
            maxY = Math.max(maxY, Math.abs(offset.getY()) + 1);
            maxZ = Math.max(maxZ, Math.abs(offset.getZ()) + 1);
        }
        if (positions.isEmpty()) {
            return null;
        }
        float span = Math.max(maxX, Math.max(maxY, maxZ)) * 2.0F;
        float zoom = span * CAMERA_RANGE;
        return new Structure(
            world,
            List.copyOf(positions),
            Collections.unmodifiableMap(blockAt),
            Collections.unmodifiableMap(counts),
            zoom
        );
    }

    private static Block sampleFixedBlock(dev.anvilcraft.lib.v2.util.predicate.BlockStatePredicate predicate) {
        if (predicate == null) {
            return Blocks.AIR;
        }
        List<BlockState> states = predicate.getStatesCache();
        return states.isEmpty() ? Blocks.AIR : states.getFirst().getBlock();
    }

    private static Block sampleSlotBlock(StructurePattern pattern, Vec3i offset, int slotIndex) {
        StructureSlot slot = pattern.slotAt(offset);
        if (slot == null || slot.roles().isEmpty()) {
            return Blocks.AIR;
        }
        String roleId = slot.roles().get(slotIndex % slot.roles().size());
        StructureRole role = pattern.roles().get(roleId);
        if (role == null) {
            return Blocks.AIR;
        }
        for (PortKind kind : role.portKinds()) {
            Set<Block> blocks = PortRegistry.blocks(kind);
            if (!blocks.isEmpty()) {
                return blocks.iterator().next();
            }
        }
        for (Substitute substitute : role.substitutes()) {
            List<BlockState> states = substitute.predicate().getStatesCache();
            if (!states.isEmpty()) {
                return states.getFirst().getBlock();
            }
        }
        return Blocks.AIR;
    }

    private record Structure(
        TrackedDummyWorld world,
        List<BlockPos> positions,
        Map<BlockPos, Block> blockAt,
        Map<Block, Integer> blocks,
        float zoom
    ) {
    }
}
