package com.teameway.bnvilmb.multiblock.structure;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

import java.util.List;
import java.util.Optional;

/**
 * 一次结构判定结果
 *
 * <p>{@link State#UNLOADED} 时只扫描到第一个未加载的位置，后面的方块没有检查；
 * {@link State#INVALID} 时 {@code problems} 至少有一条。</p>
 *
 * @param controllerPos 控制器位置
 * @param facing 判定使用的控制器正面
 * @param pattern 使用的结构模板
 * @param state 判定状态
 * @param problems 失败原因，按发现顺序
 * @param positions 结构覆盖的全部世界坐标
 * @param slots 槽位匹配结果
 * @param parallelsBonus 替代方块提供的并行加成
 * @param threadsBonus 替代方块提供的线程加成
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

    /**
     * @return 是否成型
     */
    public boolean formed() {
        return this.state == State.FORMED;
    }

    /**
     * @return 是否因区块未加载而中止
     */
    public boolean unloaded() {
        return this.state == State.UNLOADED;
    }

    /**
     * @return 第一条失败原因，没有时为空
     */
    public Optional<StructureProblem> firstProblem() {
        return this.problems.stream().findFirst();
    }

    /**
     * @return 所有识别为端口的世界坐标，顺序与 {@code slots} 一致
     */
    public List<BlockPos> portPositions() {
        return this.slots.stream()
            .filter(slot -> slot.portKind() != null)
            .map(SlotMatch::pos)
            .toList();
    }

    /**
     * 判定状态
     */
    public enum State {
        /** 固定方块与角色数量全部满足 */
        FORMED,
        /** 存在至少一条失败原因 */
        INVALID,
        /** 结构范围内有区块未加载，判定提前返回 */
        UNLOADED
    }
}
