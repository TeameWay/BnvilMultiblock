package com.teameway.bnvilmb.multiblock.structure;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

import java.util.List;
import java.util.Optional;

/**
 * 一次结构判定结果
 *
 * @param controllerPos 控制器位置
 * @param facing        判定使用的控制器正面
 * @param pattern       使用的结构模板
 * @param state         判定状态
 * @param problems      失败原因
 * @param positions     结构覆盖的全部世界坐标
 * @param slots         槽位匹配结果
 * @param parallelsBonus 替代物提供的并行加成
 * @param threadsBonus   替代物提供的线程加成
 */
public record StructureMatch(
    BlockPos controllerPos,
    Direction facing,
    StructurePattern pattern,
    State state,
    List<StructureProblem> problems,
    List<BlockPos> positions,
    List<SlotMatch> slots,
    int parallelsBonus,
    int threadsBonus
) {
    public StructureMatch {
        problems = List.copyOf(problems);
        positions = List.copyOf(positions);
        slots = List.copyOf(slots);
    }

    public boolean formed() {
        return this.state == State.FORMED;
    }

    public boolean unloaded() {
        return this.state == State.UNLOADED;
    }

    public Optional<StructureProblem> firstProblem() {
        return this.problems.stream().findFirst();
    }

    public List<BlockPos> portPositions() {
        return this.slots.stream()
            .filter(slot -> slot.portKind() != null)
            .map(SlotMatch::pos)
            .toList();
    }

    public enum State {
        FORMED,
        INVALID,
        UNLOADED
    }
}
