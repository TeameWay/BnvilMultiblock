package com.teameway.bnvilmb.multiblock.pattern;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * 一次 pattern 判定中的上下文。
 *
 * <p>保存当前方块、全局/分层计数、替代方块加成和错误列表。判定过程中只在这个对象里累计，
 * 结束后再写回 {@link PatternState}。</p>
 */
public final class PredicateContext {
    private final List<PatternError> errors = new ArrayList<>();
    private final Object2IntMap<MultiPredicate> globalCounts = new Object2IntOpenHashMap<>();
    private final Object2IntMap<MultiPredicate> sliceCounts = new Object2IntOpenHashMap<>();
    private @Nullable Level level;
    private final BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
    private @Nullable BlockState state;
    private @Nullable BlockEntity blockEntity;
    private boolean checkSlice = true;
    private boolean flipped;
    private int parallelBonus;
    private int threadBonus;

    /**
     * @param level 判定所在世界
     */
    public void setLevel(Level level) {
        this.level = level;
    }

    /**
     * @return 判定所在世界
     */
    public Level level() {
        if (this.level == null) {
            throw new IllegalStateException("Predicate context has no level");
        }
        return this.level;
    }

    /**
     * 更新当前判定位置，并读取方块状态与方块实体。
     *
     * @param pos 世界坐标
     */
    public void updatePos(BlockPos pos) {
        this.pos.set(pos);
        this.state = this.level().getBlockState(pos);
        this.blockEntity = this.level().getBlockEntity(pos);
    }

    /**
     * @return 当前坐标
     */
    public BlockPos pos() {
        return this.pos.immutable();
    }

    /**
     * @return 当前方块状态
     */
    public BlockState state() {
        if (this.state == null) {
            throw new IllegalStateException("Predicate context has no block state");
        }
        return this.state;
    }

    /**
     * @return 当前方块实体，没有时为 {@code null}
     */
    public @Nullable BlockEntity blockEntity() {
        return this.blockEntity;
    }

    /**
     * @return 当前位置的快照
     */
    public BlockInfo blockInfo() {
        return new BlockInfo(this.state(), this.blockEntity);
    }

    /**
     * @return 当前是否按层检查
     */
    public boolean checkSlice() {
        return this.checkSlice;
    }

    /**
     * @param checkSlice 是否按层检查
     */
    public void setCheckSlice(boolean checkSlice) {
        this.checkSlice = checkSlice;
    }

    /**
     * @return 当前是否镜像
     */
    public boolean flipped() {
        return this.flipped;
    }

    /**
     * @param flipped 是否镜像
     */
    public void setFlipped(boolean flipped) {
        this.flipped = flipped;
    }

    /**
     * 记录一次全局命中。
     *
     * @param predicate 命中的 predicate
     * @return 命中后的全局数量
     */
    public int incrementGlobal(MultiPredicate predicate) {
        return this.globalCounts.mergeInt(predicate, 1, Integer::sum);
    }

    /**
     * 记录一次分层命中。
     *
     * @param predicate 命中的 predicate
     * @return 命中后的分层数量
     */
    public int incrementSlice(MultiPredicate predicate) {
        if (!this.checkSlice) {
            return 0;
        }
        return this.sliceCounts.mergeInt(predicate, 1, Integer::sum);
    }

    /**
     * @param predicate predicate
     * @return 全局命中数
     */
    public int globalCount(MultiPredicate predicate) {
        return this.globalCounts.getInt(predicate);
    }

    /**
     * @param predicate predicate
     * @return 分层命中数
     */
    public int sliceCount(MultiPredicate predicate) {
        return this.checkSlice ? this.sliceCounts.getInt(predicate) : 0;
    }

    /**
     * 清空分层计数。
     */
    public void clearSliceCounts() {
        this.sliceCounts.clear();
    }

    /**
     * 清空全局计数。
     */
    public void clearGlobalCounts() {
        this.globalCounts.clear();
    }

    /**
     * 增加替代方块加成。
     *
     * @param parallels 并行加成
     * @param threads 线程加成
     */
    public void addBonus(int parallels, int threads) {
        this.parallelBonus += parallels;
        this.threadBonus += threads;
    }

    /**
     * @return 并行加成
     */
    public int parallelBonus() {
        return this.parallelBonus;
    }

    /**
     * @return 线程加成
     */
    public int threadBonus() {
        return this.threadBonus;
    }

    /**
     * 添加一条错误。
     *
     * @param error 错误
     */
    public void addError(PatternError error) {
        this.errors.add(error);
    }

    /**
     * @return 当前错误数量
     */
    public int errorCount() {
        return this.errors.size();
    }

    /**
     * 丢弃某个位置之后追加的错误，用于尝试下一个候选。
     *
     * @param count 保留的错误数量
     */
    public void truncateErrors(int count) {
        while (this.errors.size() > count) {
            this.errors.remove(this.errors.size() - 1);
        }
    }

    /**
     * @return 错误列表，只读
     */
    public List<PatternError> errors() {
        return List.copyOf(this.errors);
    }

    /**
     * 重置判定状态。
     */
    public void reset() {
        this.errors.clear();
        this.globalCounts.clear();
        this.sliceCounts.clear();
        this.checkSlice = true;
        this.flipped = false;
        this.parallelBonus = 0;
        this.threadBonus = 0;
        this.state = null;
        this.blockEntity = null;
    }
}
