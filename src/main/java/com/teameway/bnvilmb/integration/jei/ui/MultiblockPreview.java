package com.teameway.bnvilmb.integration.jei.ui;

import com.lowdragmc.lowdraglib2.gui.ui.elements.Scene;
import com.lowdragmc.lowdraglib2.utils.virtuallevel.TrackedDummyWorld;
import com.teameway.bnvilmb.multiblock.MultiblockType;
import com.teameway.bnvilmb.multiblock.pattern.BlockInfo;
import com.teameway.bnvilmb.multiblock.pattern.BlockPattern;
import com.teameway.bnvilmb.multiblock.pattern.MultiPredicate;
import com.teameway.bnvilmb.multiblock.port.PortRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import org.joml.Vector3f;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * 多方块结构预览。
 *
 * <p>从代码定义的 {@link BlockPattern} 直接取格子和候选方块，不再查询数据包注册表。
 * 固定格显示谓词的第一个候选，槽位显示端口或替代方块的第一个候选。</p>
 */
public class MultiblockPreview {
    private static final int BASE_Y = 64;
    private static final float CAMERA_RANGE = 1.35F;
    private static final Map<BlockPattern, Structure> STRUCTURES = new LinkedHashMap<>();

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

    public static List<RequiredBlock> requiredBlocks(MultiblockType machine) {
        Structure structure = structureOf(machine);
        return structure == null ? List.of() : structure.demands();
    }

    public static List<StructureCandidate> candidatesAt(MultiblockType machine, BlockPos pos) {
        Structure structure = structureOf(machine);
        return structure == null ? List.of() : structure.candidatesAt().getOrDefault(pos, List.of());
    }

    public static int layerCount(MultiblockType machine) {
        Structure structure = structureOf(machine);
        return structure == null ? 0 : structure.levels().size();
    }

    public static List<BlockPos> layerPositions(MultiblockType machine, int index) {
        Structure structure = structureOf(machine);
        if (structure == null) {
            return List.of();
        }
        List<Integer> levels = structure.levels();
        if (index < 0 || index >= levels.size()) {
            return structure.positions();
        }
        int level = levels.get(index);
        List<BlockPos> visible = new ArrayList<>();
        for (BlockPos pos : structure.positions()) {
            if (pos.getY() == level) {
                visible.add(pos);
            }
        }
        return visible;
    }

    public static void showLayer(Scene scene, MultiblockType machine, int index) {
        Structure structure = structureOf(machine);
        if (structure == null) {
            return;
        }
        scene.setRenderedCore(layerPositions(machine, index), null, false);
        scene.setCenter(new Vector3f(0.5F, BASE_Y + 0.5F, 0.5F));
        scene.setZoom(structure.zoom());
    }

    public static Set<BlockPos> replaceablePositions(MultiblockType machine) {
        Structure structure = structureOf(machine);
        return structure == null ? Set.of() : structure.replaceable();
    }

    private static @Nullable Structure structureOf(MultiblockType machine) {
        BlockPattern pattern = machine.controller().get().pattern();
        if (pattern == null) {
            return null;
        }
        Structure cached = STRUCTURES.get(pattern);
        if (cached != null) {
            return cached;
        }
        Structure built = build(pattern);
        if (built != null) {
            STRUCTURES.put(pattern, built);
        }
        return built;
    }

    private static @Nullable Structure build(BlockPattern pattern) {
        List<BlockPattern.Cell> cells = pattern.cells(BlockPos.ZERO, Direction.NORTH, Direction.UP, false);
        TrackedDummyWorld world = new TrackedDummyWorld();
        List<BlockPos> positions = new ArrayList<>();
        Map<BlockPos, List<StructureCandidate>> candidatesAt = new LinkedHashMap<>();
        Map<Block, Integer> counts = new LinkedHashMap<>();
        int maxX = 1;
        int maxY = 1;
        int maxZ = 1;
        Block controller = null;

        for (BlockPattern.Cell cell : cells) {
            List<Block> candidates = candidateBlocks(cell.predicate());
            if (candidates.isEmpty()) {
                continue;
            }
            Block block = candidates.getFirst();
            if (block == Blocks.AIR) {
                continue;
            }
            BlockPos pos = new BlockPos(cell.pos().getX(), BASE_Y + cell.pos().getY(), cell.pos().getZ());
            world.setBlockAndUpdate(pos, block.defaultBlockState());
            positions.add(pos);
            int min = Math.max(0, cell.predicate().minCount());
            int max = cell.predicate().maxCount();
            candidatesAt.put(pos, candidates.stream()
                .map(candidate -> new StructureCandidate(candidate, min, max))
                .toList());
            counts.merge(block, 1, Integer::sum);
            if (cell.predicate().isController()) {
                controller = block;
            }
            maxX = Math.max(maxX, Math.abs(cell.pos().getX()) + 1);
            maxY = Math.max(maxY, Math.abs(cell.pos().getY()) + 1);
            maxZ = Math.max(maxZ, Math.abs(cell.pos().getZ()) + 1);
        }

        if (positions.isEmpty()) {
            return null;
        }
        TreeSet<Integer> levels = new TreeSet<>();
        Set<BlockPos> replaceable = new LinkedHashSet<>();
        for (BlockPos pos : positions) {
            levels.add(pos.getY());
            List<StructureCandidate> cell = candidatesAt.get(pos);
            if (cell != null && cell.size() > 1) {
                replaceable.add(pos);
            }
        }
        float span = Math.max(maxX, Math.max(maxY, maxZ)) * 2.0F;
        return new Structure(
            world,
            List.copyOf(positions),
            Collections.unmodifiableMap(candidatesAt),
            Collections.unmodifiableSet(replaceable),
            orderBlocks(counts, controller),
            List.copyOf(levels),
            span * CAMERA_RANGE
        );
    }

    private static List<Block> candidateBlocks(MultiPredicate predicate) {
        List<Block> blocks = new ArrayList<>();
        for (BlockInfo info : predicate.candidates()) {
            Block block = info.state().getBlock();
            if (block != Blocks.AIR && !blocks.contains(block)) {
                blocks.add(block);
            }
        }
        return blocks;
    }

    private static List<RequiredBlock> orderBlocks(Map<Block, Integer> counts, @Nullable Block controller) {
        List<RequiredBlock> ordered = new ArrayList<>(counts.size());
        counts.forEach((block, count) -> ordered.add(new RequiredBlock(block, count)));
        ordered.sort(Comparator
            .comparingInt((RequiredBlock required) -> rank(required.block(), controller))
            .thenComparingInt(required -> portKindOrder(required.block()))
            .thenComparing(Comparator.comparingInt(RequiredBlock::count).reversed())
            .thenComparing(required -> blockId(required.block())));
        return List.copyOf(ordered);
    }

    private static int rank(Block block, @Nullable Block controller) {
        if (block == controller) {
            return 0;
        }
        return PortRegistry.isPort(block.defaultBlockState()) ? 1 : 2;
    }

    private static int portKindOrder(Block block) {
        PortRegistry.Descriptor descriptor = PortRegistry.descriptor(block.defaultBlockState());
        return descriptor == null ? 0 : descriptor.kind().ordinal();
    }

    private static String blockId(Block block) {
        Identifier id = BuiltInRegistries.BLOCK.getKey(block);
        return id == null ? "" : id.toString();
    }

    private record Structure(
        TrackedDummyWorld world,
        List<BlockPos> positions,
        Map<BlockPos, List<StructureCandidate>> candidatesAt,
        Set<BlockPos> replaceable,
        List<RequiredBlock> demands,
        List<Integer> levels,
        float zoom
    ) {
    }
}
