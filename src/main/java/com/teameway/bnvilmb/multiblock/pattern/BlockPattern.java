package com.teameway.bnvilmb.multiblock.pattern;

import it.unimi.dsi.fastutil.chars.Char2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * 代码定义的多方块 pattern。
 *
 * <p>这是 GTM {@code BlockPattern} 的适配版：保留 slice / direction / offset / predicate 结构，
 * 使用本项目的 {@link MultiPredicate} 与 {@link PatternState}。判定分为缓存快速校验和完整判定。</p>
 */
public final class BlockPattern {
    private final PatternSlice[] slices;
    private final RelativeDirection[] directions;
    private final OriginOffset offset;
    private final Char2ObjectMap<MultiPredicate> predicates;
    private final char controllerChar;
    private final boolean allowFlip;
    private final Set<Identifier> variants;

    public BlockPattern(
        PatternSlice[] slices,
        RelativeDirection[] directions,
        OriginOffset offset,
        Char2ObjectMap<MultiPredicate> predicates,
        char controllerChar,
        boolean allowFlip,
        Set<Identifier> variants
    ) {
        if (slices.length == 0) {
            throw new IllegalArgumentException("Pattern must have at least one slice");
        }
        this.slices = slices.clone();
        this.directions = directions.clone();
        this.offset = offset;
        this.predicates = predicates;
        this.controllerChar = controllerChar;
        this.allowFlip = allowFlip;
        this.variants = Set.copyOf(variants);
    }

    /**
     * 先做缓存快速校验，失败后再做完整判定。
     *
     * @param level 世界
     * @param state 结构状态
     * @param controllerPos 控制器坐标
     * @param frontFacing 控制器正面
     * @param upwardsFacing 控制器上方
     * @param allowFlip 是否允许自动翻转
     * @return 是否有效
     */
    public boolean checkPatternFastAt(
        Level level,
        PatternState state,
        BlockPos controllerPos,
        Direction frontFacing,
        Direction upwardsFacing,
        boolean allowFlip
    ) {
        if (!state.cache().isEmpty() && controllerPos.equals(state.cachedControllerPos())) {
            boolean pass = true;
            for (Long2ObjectMap.Entry<BlockInfo> entry : state.cache().long2ObjectEntrySet()) {
                BlockPos pos = BlockPos.of(entry.getLongKey());
                if (!level.isLoaded(pos)) {
                    pass = false;
                    break;
                }
                BlockInfo info = entry.getValue();
                if (level.getBlockState(pos) != info.state()) {
                    pass = false;
                    break;
                }
                if (level.getBlockEntity(pos) != info.blockEntity()) {
                    pass = false;
                    break;
                }
            }
            if (pass) {
                state.setCacheHit(true);
                state.setFacing(frontFacing, upwardsFacing, state.flipped());
                return !state.hasErrors();
            }
        }

        state.setCacheHit(false);
        boolean valid = this.checkPatternAt(level, state, controllerPos, frontFacing, upwardsFacing, false);
        if (valid) {
            state.setFacing(frontFacing, upwardsFacing, false);
            return true;
        }
        if (this.allowFlip && allowFlip) {
            valid = this.checkPatternAt(level, state, controllerPos, frontFacing, upwardsFacing, true);
            if (valid) {
                state.setFacing(frontFacing, upwardsFacing, true);
                return true;
            }
        }
        state.setFacing(frontFacing, upwardsFacing, false);
        return false;
    }

    /**
     * 完整判定。
     *
     * @param level 世界
     * @param state 结构状态
     * @param controllerPos 控制器坐标
     * @param frontFacing 控制器正面
     * @param upwardsFacing 控制器上方
     * @param flip 是否镜像
     * @return 是否有效
     */
    public boolean checkPatternAt(
        Level level,
        PatternState state,
        BlockPos controllerPos,
        Direction frontFacing,
        Direction upwardsFacing,
        boolean flip
    ) {
        state.clearCache();
        state.setErrors(List.of());
        PredicateContext context = new PredicateContext();
        context.reset();
        context.setLevel(level);
        context.setFlipped(flip);
        context.setCheckSlice(true);

        int sliceOffset = 0;
        for (PatternSlice slice : this.slices) {
            for (int repeat = 0; repeat < slice.maxRepeats(); repeat++) {
                context.clearSliceCounts();
                Set<MultiPredicate> visited = new LinkedHashSet<>();
                if (!this.checkSlice(level, state, context, slice, controllerPos, frontFacing, upwardsFacing, flip, sliceOffset, visited)) {
                    state.setErrors(context.errors());
                    slice.setActualRepeats(repeat);
                    return false;
                }
                sliceOffset++;
                for (MultiPredicate predicate : visited) {
                    if (!predicate.testSliceMin(context)) {
                        state.setErrors(context.errors());
                        slice.setActualRepeats(repeat + 1);
                        return false;
                    }
                }
            }
            slice.setActualRepeats(slice.maxRepeats());
        }

        for (MultiPredicate predicate : this.predicates.values()) {
            if (!predicate.testGlobalMin(context)) {
                state.setErrors(context.errors());
                return false;
            }
        }

        state.setErrors(List.of());
        state.setBonuses(context.parallelBonus(), context.threadBonus());
        return true;
    }

    private boolean checkSlice(
        Level level,
        PatternState state,
        PredicateContext context,
        PatternSlice slice,
        BlockPos controllerPos,
        Direction frontFacing,
        Direction upwardsFacing,
        boolean flip,
        int sliceOffset,
        Set<MultiPredicate> visited
    ) {
        Direction sliceDirection = this.directions[0].getRelativeFacing(frontFacing, upwardsFacing, flip);
        Direction stringDirection = this.directions[1].getRelativeFacing(frontFacing, upwardsFacing, flip);
        Direction charDirection = this.directions[2].getRelativeFacing(frontFacing, upwardsFacing, flip);

        BlockPos.MutableBlockPos start = controllerPos.mutable();
        this.offset.apply(start, frontFacing, upwardsFacing, flip);
        start.move(sliceDirection, sliceOffset);

        for (int row = 0; row < slice.height(); row++) {
            BlockPos.MutableBlockPos rowStart = start.mutable().move(stringDirection, row);
            for (int column = 0; column < slice.width(); column++) {
                BlockPos pos = rowStart.mutable().move(charDirection, column);
                if (!level.isLoaded(pos)) {
                    context.addError(PatternError.unloaded(pos));
                    return false;
                }
                context.updatePos(pos);
                MultiPredicate predicate = this.predicates.get(slice.charAt(row, column));
                if (predicate == null) {
                    context.addError(PatternError.pattern(pos, "Undefined pattern symbol"));
                    return false;
                }
                if (!predicate.isAny()) {
                    state.cache(pos, context.blockInfo());
                }
                if (predicate.testAt(context) != MultiPredicate.MatchResult.MATCH) {
                    return false;
                }
                visited.add(predicate);
            }
        }
        return true;
    }

    /**
     * @param controllerPos 控制器坐标
     * @param frontFacing 控制器正面
     * @param upwardsFacing 控制器上方
     * @param flip 是否镜像
     * @return 结构覆盖的全部世界坐标
     */
    public List<BlockPos> positions(BlockPos controllerPos, Direction frontFacing, Direction upwardsFacing, boolean flip) {
        List<BlockPos> result = new ArrayList<>();
        Direction sliceDirection = this.directions[0].getRelativeFacing(frontFacing, upwardsFacing, flip);
        Direction stringDirection = this.directions[1].getRelativeFacing(frontFacing, upwardsFacing, flip);
        Direction charDirection = this.directions[2].getRelativeFacing(frontFacing, upwardsFacing, flip);
        int sliceOffset = 0;
        for (PatternSlice slice : this.slices) {
            for (int repeat = 0; repeat < slice.maxRepeats(); repeat++) {
                BlockPos.MutableBlockPos start = controllerPos.mutable();
                this.offset.apply(start, frontFacing, upwardsFacing, flip);
                start.move(sliceDirection, sliceOffset);
                for (int row = 0; row < slice.height(); row++) {
                    BlockPos.MutableBlockPos rowStart = start.mutable().move(stringDirection, row);
                    for (int column = 0; column < slice.width(); column++) {
                        result.add(rowStart.mutable().move(charDirection, column).immutable());
                    }
                }
                sliceOffset++;
            }
        }
        return List.copyOf(result);
    }

    /**
     * @param controllerPos 控制器坐标
     * @param frontFacing 控制器正面
     * @param upwardsFacing 控制器上方
     * @param flip 是否镜像
     * @return 每个位置的坐标和 predicate，供 JEI 预览使用
     */
    public List<Cell> cells(BlockPos controllerPos, Direction frontFacing, Direction upwardsFacing, boolean flip) {
        List<Cell> result = new ArrayList<>();
        Direction sliceDirection = this.directions[0].getRelativeFacing(frontFacing, upwardsFacing, flip);
        Direction stringDirection = this.directions[1].getRelativeFacing(frontFacing, upwardsFacing, flip);
        Direction charDirection = this.directions[2].getRelativeFacing(frontFacing, upwardsFacing, flip);
        int sliceOffset = 0;
        for (PatternSlice slice : this.slices) {
            for (int repeat = 0; repeat < slice.maxRepeats(); repeat++) {
                BlockPos.MutableBlockPos start = controllerPos.mutable();
                this.offset.apply(start, frontFacing, upwardsFacing, flip);
                start.move(sliceDirection, sliceOffset);
                for (int row = 0; row < slice.height(); row++) {
                    BlockPos.MutableBlockPos rowStart = start.mutable().move(stringDirection, row);
                    for (int column = 0; column < slice.width(); column++) {
                        BlockPos pos = rowStart.mutable().move(charDirection, column).immutable();
                        MultiPredicate predicate = this.predicates.get(slice.charAt(row, column));
                        if (predicate != null) {
                            result.add(new Cell(pos, predicate));
                        }
                    }
                }
                sliceOffset++;
            }
        }
        return List.copyOf(result);
    }

    public PatternSlice[] slices() {
        return this.slices.clone();
    }

    public RelativeDirection[] directions() {
        return this.directions.clone();
    }

    public OriginOffset offset() {
        return this.offset;
    }

    public Char2ObjectMap<MultiPredicate> predicates() {
        return this.predicates;
    }

    public char controllerChar() {
        return this.controllerChar;
    }

    public boolean allowFlip() {
        return this.allowFlip;
    }

    public Set<Identifier> variants() {
        return this.variants;
    }

    /**
     * @return 控制器符号对应的 predicate
     */
    public MultiPredicate controllerPredicate() {
        return this.predicates.get(this.controllerChar);
    }

    /**
     * 预览用的单格。
     *
     * @param pos 相对控制器的坐标
     * @param predicate 该格允许的 predicate
     */
    public record Cell(BlockPos pos, MultiPredicate predicate) {
    }
}
