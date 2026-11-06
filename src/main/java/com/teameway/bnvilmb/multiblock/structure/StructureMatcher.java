package com.teameway.bnvilmb.multiblock.structure;

import com.teameway.bnvilmb.multiblock.port.MultiblockPort;
import com.teameway.bnvilmb.multiblock.port.PortKind;
import com.teameway.bnvilmb.multiblock.port.PortRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 结构匹配器
 *
 * <p>端口按 {@link PortRegistry} 的固有种类计数；非端口方块只作为替代方块参与小规模回溯。
 * 槽位本身不绑定角色，因此同一组角色可以在任意槽位互换。匹配过程中同时累计替代方块提供的
 * 并行与线程加成。</p>
 */
public final class StructureMatcher {
    private StructureMatcher() {
    }

    /**
     * 按模板扫描一次结构
     *
     * <p>任一位置未加载时立即返回 {@link StructureMatch.State#UNLOADED}，不再继续扫描。
     * 固定方块与端口直接判定；可接受替代方块的槽位先收集候选，再按候选数从少到多回溯分配
     * 角色，分配失败时补充角色数量问题。</p>
     *
     * @param level 服务端世界
     * @param controllerPos 控制器方块坐标
     * @param facing 控制器当前正面
     * @param pattern 结构模板
     * @return 本次判定结果
     */
    public static StructureMatch match(ServerLevel level, BlockPos controllerPos, Direction facing,
                                       StructurePattern pattern) {
        List<StructureProblem> problems = new ArrayList<>();
        List<BlockPos> positions = new ArrayList<>();
        List<SlotMatch> slots = new ArrayList<>();
        Map<String, Integer> fixedCounts = new HashMap<>();
        Set<String> reportedMax = new HashSet<>();
        List<Flexible> flexible = new ArrayList<>();
        int parallelBonus = 0;
        int threadBonus = 0;

        for (Vec3i offset : pattern.positions()) {
            Vec3i rotated = StructureRotation.rotate(offset, pattern.canonicalFront(), facing, pattern.orientation());
            BlockPos pos = controllerPos.offset(rotated);
            positions.add(pos);
            if (!level.isLoaded(pos)) {
                problems.add(new StructureProblem(pos, StructureProblem.Type.UNLOADED));
                return new StructureMatch(controllerPos, facing, pattern, StructureMatch.State.UNLOADED,
                    problems, positions, slots, 0, 0);
            }
            BlockState state = level.getBlockState(pos);
            if (!pattern.isSlot(offset)) {
                BlockEntity be = level.getBlockEntity(pos);
                if (!pattern.fixedAt(offset).test(level, state, be)) {
                    problems.add(new StructureProblem(pos, StructureProblem.Type.FIXED));
                }
                continue;
            }

            StructureSlot slot = pattern.slotAt(offset);
            if (slot == null) {
                problems.add(new StructureProblem(pos, StructureProblem.Type.SLOT));
                continue;
            }
            if (state.isAir()) {
                if (slot.allowAir()) {
                    slots.add(new SlotMatch(pos, pattern.keyAt(offset), state, null, null));
                } else {
                    problems.add(new StructureProblem(pos, StructureProblem.Type.SLOT));
                }
                continue;
            }

            PortRegistry.Descriptor descriptor = PortRegistry.descriptor(state);
            if (descriptor != null) {
                String roleId = roleForPort(slot, pattern, descriptor.kind());
                if (roleId == null) {
                    problems.add(new StructureProblem(pos, StructureProblem.Type.PORT_NOT_ALLOWED));
                    continue;
                }
                StructureRole role = pattern.roles().get(roleId);
                int count = fixedCounts.merge(roleId, 1, Integer::sum);
                if (count > role.maxCount() && reportedMax.add(roleId)) {
                    problems.add(new StructureProblem(pos, StructureProblem.Type.ROLE_MAX, roleId, count, role.min(), role.max()));
                }
                BlockEntity be = level.getBlockEntity(pos);
                if (be instanceof MultiblockPort port) {
                    BlockPos claimed = port.controllerPos();
                    if (claimed != null && !claimed.equals(controllerPos)) {
                        problems.add(new StructureProblem(pos, StructureProblem.Type.PORT_CLAIMED));
                    }
                }
                slots.add(new SlotMatch(pos, pattern.keyAt(offset), state, descriptor.kind(), roleId));
                continue;
            }

            List<String> candidates = candidates(slot, pattern, state);
            if (candidates.isEmpty()) {
                problems.add(new StructureProblem(pos, StructureProblem.Type.SLOT));
                continue;
            }
            flexible.add(new Flexible(pos, pattern.keyAt(offset), state, candidates));
            Bonus bonus = bestBonus(candidates, pattern, state);
            parallelBonus += bonus.parallels;
            threadBonus += bonus.threads;
        }

        flexible.sort(Comparator.comparingInt(f -> f.candidates.size()));
        Map<String, Integer> counts = new HashMap<>(fixedCounts);
        int[] assignment = new int[flexible.size()];
        boolean solved = solve(0, flexible, pattern, counts, assignment);
        if (!solved) {
            appendCountProblems(problems, pattern, fixedCounts, flexible);
        } else {
            for (int i = 0; i < flexible.size(); i++) {
                Flexible item = flexible.get(i);
                String role = item.candidates.get(assignment[i]);
                slots.add(new SlotMatch(item.pos, item.key, item.state, null, role));
            }
        }

        StructureMatch.State state = problems.isEmpty() && solved
            ? StructureMatch.State.FORMED
            : StructureMatch.State.INVALID;
        return new StructureMatch(controllerPos, facing, pattern, state, problems, positions, slots,
            parallelBonus, threadBonus);
    }

    /** 按槽位角色顺序返回第一个接受该端口种类的角色 */
    private static String roleForPort(StructureSlot slot, StructurePattern pattern, PortKind kind) {
        for (String roleId : slot.roles()) {
            StructureRole role = pattern.roles().get(roleId);
            if (role != null && role.accepts(kind)) {
                return roleId;
            }
        }
        return null;
    }

    /** 列出槽位里通过替代方块匹配该方块状态的角色，保持槽位角色顺序 */
    private static List<String> candidates(StructureSlot slot, StructurePattern pattern, BlockState state) {
        List<String> result = new ArrayList<>();
        for (String roleId : slot.roles()) {
            StructureRole role = pattern.roles().get(roleId);
            if (role == null) {
                continue;
            }
            for (Substitute substitute : role.substitutes()) {
                if (substitute.matches(state)) {
                    result.add(roleId);
                    break;
                }
            }
        }
        return result;
    }

    /** 取候选角色中匹配替代方块的最大并行与线程加成 */
    private static Bonus bestBonus(List<String> candidates, StructurePattern pattern, BlockState state) {
        int parallels = 0;
        int threads = 0;
        for (String roleId : candidates) {
            StructureRole role = pattern.roles().get(roleId);
            if (role == null) {
                continue;
            }
            for (Substitute substitute : role.substitutes()) {
                if (!substitute.matches(state)) {
                    continue;
                }
                parallels = Math.max(parallels, substitute.parallels());
                threads = Math.max(threads, substitute.threads());
            }
        }
        return new Bonus(parallels, threads);
    }

    /** 按候选顺序回溯分配角色，并用上限与下限剪枝 */
    private static boolean solve(int index, List<Flexible> flexible, StructurePattern pattern,
                                 Map<String, Integer> counts, int[] assignment) {
        if (index == flexible.size()) {
            return minsMet(pattern, counts);
        }
        Flexible item = flexible.get(index);
        for (int candidate = 0; candidate < item.candidates.size(); candidate++) {
            String roleId = item.candidates.get(candidate);
            StructureRole role = pattern.roles().get(roleId);
            if (role == null) {
                continue;
            }
            int count = counts.getOrDefault(roleId, 0);
            if (count >= role.maxCount()) {
                continue;
            }
            counts.put(roleId, count + 1);
            assignment[index] = candidate;
            if (canStillMeetMins(index + 1, flexible, pattern, counts)
                && solve(index + 1, flexible, pattern, counts, assignment)) {
                return true;
            }
            counts.put(roleId, count);
        }
        return false;
    }

    /** 用剩余槽位全部让给该角色的上界判断下限是否还够 */
    private static boolean canStillMeetMins(int from, List<Flexible> flexible, StructurePattern pattern,
                                            Map<String, Integer> counts) {
        for (Map.Entry<String, StructureRole> entry : pattern.roles().entrySet()) {
            String roleId = entry.getKey();
            int min = entry.getValue().min();
            int count = counts.getOrDefault(roleId, 0);
            if (count >= min) {
                continue;
            }
            for (int i = from; i < flexible.size(); i++) {
                if (flexible.get(i).candidates.contains(roleId)) {
                    count++;
                }
            }
            if (count < min) {
                return false;
            }
        }
        return true;
    }

    /** 检查所有角色是否达到下限 */
    private static boolean minsMet(StructurePattern pattern, Map<String, Integer> counts) {
        for (Map.Entry<String, StructureRole> entry : pattern.roles().entrySet()) {
            if (counts.getOrDefault(entry.getKey(), 0) < entry.getValue().min()) {
                return false;
            }
        }
        return true;
    }

    /** 回溯失败后为无法达到下限的角色生成问题，若仍无问题则补一条 {@code PATTERN} */
    private static void appendCountProblems(List<StructureProblem> problems, StructurePattern pattern,
                                            Map<String, Integer> fixedCounts, List<Flexible> flexible) {
        for (Map.Entry<String, StructureRole> entry : pattern.roles().entrySet()) {
            String roleId = entry.getKey();
            StructureRole role = entry.getValue();
            int possible = fixedCounts.getOrDefault(roleId, 0);
            for (Flexible item : flexible) {
                if (item.candidates.contains(roleId)) {
                    possible++;
                }
            }
            if (possible < role.min()) {
                problems.add(new StructureProblem(BlockPos.ZERO, StructureProblem.Type.ROLE_MIN,
                    roleId, possible, role.min(), role.max()));
            }
        }
        if (problems.isEmpty()) {
            problems.add(new StructureProblem(BlockPos.ZERO, StructureProblem.Type.PATTERN));
        }
    }

    private record Flexible(BlockPos pos, char key, BlockState state, List<String> candidates) {
    }

    private record Bonus(int parallels, int threads) {
    }
}
