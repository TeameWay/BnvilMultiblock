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
import dev.anvilcraft.lib.v2.util.predicate.BlockStatePredicate;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Vec3i;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import org.joml.Vector3f;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * 多方块结构预览
 *
 * <p>把结构模板映射成具体方块状态：固定格取谓词的第一个方块，槽位按角色顺序轮换取一个合法示例，
 * 保证四个角不会都显示成输入总线。写入虚拟世界时整体抬升 {@value #BASE_Y} 格。</p>
 *
 * <p>每个位置同时记下该处允许的全部方块及各自的数量条件，场景里点中方块后可以据此列出可替代项；
 * 需求方块清单按“控制器、端口、其余按数量从多到少”排好序，界面直接照单铺格。</p>
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

    /**
     * 结构所需方块，已按展示顺序排好
     *
     * @param machine 机器类型
     * @return 控制器在最前，其后是端口，最后是其余方块（数量从多到少）
     */
    public static List<RequiredBlock> requiredBlocks(MultiblockType machine) {
        Structure structure = structureOf(machine);
        return structure == null ? List.of() : structure.demands();
    }

    /**
     * 某个位置允许放的全部方块
     *
     * @param machine 机器类型
     * @param pos     预览里的方块坐标
     * @return 候选方块，第一个是预览里实际显示的那个；位置未知时为空
     */
    public static List<StructureCandidate> candidatesAt(MultiblockType machine, BlockPos pos) {
        Structure structure = structureOf(machine);
        return structure == null ? List.of() : structure.candidatesAt().getOrDefault(pos, List.of());
    }

    /**
     * 结构一共有多少层（按 y 值的不同取值计数）
     *
     * @param machine 机器类型
     * @return 层数，结构不存在时为 0
     */
    public static int layerCount(MultiblockType machine) {
        Structure structure = structureOf(machine);
        return structure == null ? 0 : structure.levels().size();
    }

    /**
     * 单层的方块坐标
     *
     * @param machine 机器类型
     * @param index   层序号，自下往上从 {@code 0} 开始；负数或越界表示全部
     * @return 该层的坐标，越界时返回全部坐标
     */
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

    /**
     * 只渲染某一层
     *
     * <p>重新登记渲染核心后把镜头拉回原位，切换层数时视角不会跟着跳。</p>
     *
     * @param scene   预览场景
     * @param machine 机器类型
     * @param index   层序号，自下往上从 {@code 0} 开始；负数或越界表示全部
     */
    public static void showLayer(Scene scene, MultiblockType machine, int index) {
        Structure structure = structureOf(machine);
        if (structure == null) {
            return;
        }
        scene.setRenderedCore(layerPositions(machine, index), null, false);
        scene.setCenter(new Vector3f(0.5F, BASE_Y + 0.5F, 0.5F));
        scene.setZoom(structure.zoom());
    }

    /**
     * 可以换成别的方块的位置
     *
     * @param machine 机器类型
     * @return 候选多于一个的坐标
     */
    public static Set<BlockPos> replaceablePositions(MultiblockType machine) {
        Structure structure = structureOf(machine);
        return structure == null ? Set.of() : structure.replaceable();
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

        Map<BlockStatePredicate, Integer> fixedCounts = countFixedPredicates(pattern, entries);
        TrackedDummyWorld world = new TrackedDummyWorld();
        List<BlockPos> positions = new ArrayList<>(entries.size());
        Map<BlockPos, List<StructureCandidate>> candidatesAt = new LinkedHashMap<>();
        Map<Block, Integer> counts = new LinkedHashMap<>();
        int maxX = 1;
        int maxY = 1;
        int maxZ = 1;
        int slotCursor = 0;
        for (Vec3i offset : entries) {
            List<StructureCandidate> candidates = candidatesOf(pattern, offset, fixedCounts);
            if (candidates.isEmpty()) {
                continue;
            }
            Block block = pattern.isSlot(offset) ? sampleSlotBlock(pattern, offset, slotCursor++) : null;
            if (block == null) {
                block = candidates.getFirst().block();
            }
            BlockPos pos = new BlockPos(offset.getX(), BASE_Y + offset.getY(), offset.getZ());
            world.setBlockAndUpdate(pos, block.defaultBlockState());
            positions.add(pos);
            candidatesAt.put(pos, withFirst(candidates, block));
            counts.merge(block, 1, Integer::sum);
            maxX = Math.max(maxX, Math.abs(offset.getX()) + 1);
            maxY = Math.max(maxY, Math.abs(offset.getY()) + 1);
            maxZ = Math.max(maxZ, Math.abs(offset.getZ()) + 1);
        }
        if (positions.isEmpty()) {
            return null;
        }
        List<StructureCandidate> controllerCell = candidatesAt.get(new BlockPos(0, BASE_Y, 0));
        Block controller = controllerCell == null ? null : controllerCell.getFirst().block();
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
        float zoom = span * CAMERA_RANGE;
        return new Structure(
            world,
            List.copyOf(positions),
            Collections.unmodifiableMap(candidatesAt),
            Collections.unmodifiableSet(replaceable),
            orderBlocks(counts, controller),
            List.copyOf(levels),
            zoom
        );
    }

    /**
     * 统计每个固定格谓词在结构里占的格数
     *
     * <p>同一映射字符下的格子共用同一个谓词实例，按实例计数即可得到“这一类固定方块要几个”。</p>
     *
     * @param pattern 结构模板
     * @param entries 全部坐标
     * @return 谓词到格数的映射
     */
    private static Map<BlockStatePredicate, Integer> countFixedPredicates(StructurePattern pattern, List<Vec3i> entries) {
        Map<BlockStatePredicate, Integer> counts = new IdentityHashMap<>();
        for (Vec3i offset : entries) {
            BlockStatePredicate predicate = pattern.fixedAt(offset);
            if (predicate != null) {
                counts.merge(predicate, 1, Integer::sum);
            }
        }
        return counts;
    }

    /**
     * 该位置允许的全部方块
     *
     * <p>固定格取谓词里声明的方块；槽位按角色的声明顺序展开，每个角色先列端口、再列替代物。</p>
     *
     * @param pattern     结构模板
     * @param offset      相对控制器的坐标
     * @param fixedCounts 各固定格谓词在结构里的格数
     * @return 候选方块，位置不存在时为空
     */
    private static List<StructureCandidate> candidatesOf(
        StructurePattern pattern,
        Vec3i offset,
        Map<BlockStatePredicate, Integer> fixedCounts
    ) {
        StructureSlot slot = pattern.slotAt(offset);
        if (slot == null) {
            BlockStatePredicate predicate = pattern.fixedAt(offset);
            return predicateCandidates(predicate, predicate == null ? -1 : fixedCounts.getOrDefault(predicate, -1));
        }
        List<StructureCandidate> candidates = new ArrayList<>();
        for (String roleId : slot.roles()) {
            StructureRole role = pattern.roles().get(roleId);
            if (role == null) {
                continue;
            }
            for (StructureCandidate candidate : roleCandidates(role)) {
                addIfAbsent(candidates, candidate);
            }
        }
        return candidates;
    }

    /**
     * 一个角色认可的方块：先端口，后替代物，条件取角色的全局数量范围
     *
     * <p>端口按方块注册 ID 排序，{@link PortRegistry#blocks(PortKind)} 本身不保证顺序。</p>
     *
     * @param role 角色
     * @return 候选方块
     */
    private static List<StructureCandidate> roleCandidates(StructureRole role) {
        List<StructureCandidate> candidates = new ArrayList<>();
        for (PortKind kind : role.portKinds()) {
            List<Block> ports = new ArrayList<>(PortRegistry.blocks(kind));
            ports.sort(Comparator.comparing(MultiblockPreview::blockId));
            for (Block port : ports) {
                addIfAbsent(candidates, new StructureCandidate(port, role.min(), role.max()));
            }
        }
        for (Substitute substitute : role.substitutes()) {
            for (Block block : predicateBlocks(substitute.predicate())) {
                addIfAbsent(candidates, new StructureCandidate(block, role.min(), role.max()));
            }
        }
        return candidates;
    }

    /**
     * 谓词里声明的方块；按标签声明时展开标签内容
     *
     * @param predicate 方块谓词，可为 {@code null}
     * @return 方块列表，跳过空气
     */
    private static List<Block> predicateBlocks(@Nullable BlockStatePredicate predicate) {
        if (predicate == null) {
            return List.of();
        }
        List<Block> blocks = new ArrayList<>();
        for (Holder<Block> holder : predicate.getBlocks()) {
            Block block = holder.value();
            if (block != Blocks.AIR) {
                addIfAbsent(blocks, block);
            }
        }
        return blocks;
    }

    /**
     * 固定格的候选方块
     *
     * @param predicate  方块谓词，可为 {@code null}
     * @param exactCount 这类方块在结构里占的格数，负数表示给不出数量
     * @return 候选方块
     */
    private static List<StructureCandidate> predicateCandidates(@Nullable BlockStatePredicate predicate, int exactCount) {
        List<Block> blocks = predicateBlocks(predicate);
        // 一格只认一种方块时格数就是精确数量；一格认多种方块时总数摊不开，退化成不给条件
        boolean exact = exactCount >= 0 && blocks.size() == 1;
        List<StructureCandidate> candidates = new ArrayList<>(blocks.size());
        for (Block block : blocks) {
            candidates.add(exact ? StructureCandidate.exact(block, exactCount) : StructureCandidate.unknown(block));
        }
        return candidates;
    }

    /**
     * 按槽位序号轮换角色，取该角色的第一个候选方块
     *
     * @param pattern   结构模板
     * @param offset    相对控制器的坐标
     * @param slotIndex 该槽位在全部槽位里的序号，用于错开各槽位的角色
     * @return 示例方块，角色取不到方块时为 {@code null}
     */
    private static @Nullable Block sampleSlotBlock(StructurePattern pattern, Vec3i offset, int slotIndex) {
        StructureSlot slot = pattern.slotAt(offset);
        if (slot == null || slot.roles().isEmpty()) {
            return null;
        }
        String roleId = slot.roles().get(slotIndex % slot.roles().size());
        StructureRole role = pattern.roles().get(roleId);
        if (role == null) {
            return null;
        }
        List<StructureCandidate> candidates = roleCandidates(role);
        return candidates.isEmpty() ? null : candidates.getFirst().block();
    }

    /**
     * 整理需求方块清单的顺序：控制器、端口、其余，后两组都按数量从多到少
     *
     * @param counts     各方块出现次数
     * @param controller 控制器方块，可为 {@code null}
     * @return 已排序的清单
     */
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

    /**
     * @param block      方块
     * @param controller 控制器方块，可为 {@code null}
     * @return 分组序号：控制器 0、端口 1、其余 2
     */
    private static int rank(Block block, @Nullable Block controller) {
        if (block == controller) {
            return 0;
        }
        return PortRegistry.isPort(block.defaultBlockState()) ? 1 : 2;
    }

    /**
     * @param block 方块
     * @return 端口种类在 {@link PortKind} 里的序号，不是端口时为 0
     */
    private static int portKindOrder(Block block) {
        PortRegistry.Descriptor descriptor = PortRegistry.descriptor(block.defaultBlockState());
        return descriptor == null ? 0 : descriptor.kind().ordinal();
    }

    /**
     * @param block 方块
     * @return 注册 ID，用于同序时的稳定排序
     */
    private static String blockId(Block block) {
        Identifier id = BuiltInRegistries.BLOCK.getKey(block);
        return id == null ? "" : id.toString();
    }

    private static void addIfAbsent(List<Block> blocks, Block block) {
        if (!blocks.contains(block)) {
            blocks.add(block);
        }
    }

    private static void addIfAbsent(List<StructureCandidate> candidates, StructureCandidate candidate) {
        for (StructureCandidate existing : candidates) {
            if (existing.block() == candidate.block()) {
                return;
            }
        }
        candidates.add(candidate);
    }

    /**
     * 把预览里显示的那个方块挪到最前
     *
     * @param candidates 候选方块
     * @param block      当前显示的方块
     * @return 显示方块在最前的列表
     */
    private static List<StructureCandidate> withFirst(List<StructureCandidate> candidates, Block block) {
        LinkedHashSet<StructureCandidate> ordered = new LinkedHashSet<>(candidates.size());
        for (StructureCandidate candidate : candidates) {
            if (candidate.block() == block) {
                ordered.add(candidate);
                break;
            }
        }
        ordered.addAll(candidates);
        return List.copyOf(ordered);
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
