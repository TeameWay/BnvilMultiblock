package com.teameway.bnvilmb.multiblock.pattern;

import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.Block;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.StringJoiner;
import java.util.function.Predicate;

/**
 * 一个 pattern 符号对应的谓词组合。
 *
 * <p>这是 GTM {@code MultiPredicate} 的简化版：一个符号可以匹配多种方块，也可以和端口谓词
 * 组合；数量限制挂在 predicate 上，而不是单独的槽位表上。端口、替代方块和外壳都可以用同一套
 * {@code where(...)} 表达。</p>
 */
public final class MultiPredicate {
    private final Logic logic;
    private final List<Base> bases;
    private final List<MultiPredicate> children;
    private final String description;

    private int minCount = -1;
    private int maxCount = -1;
    private int minSliceCount = -1;
    private int maxSliceCount = -1;
    private int previewCount = -1;
    private int priority;
    private boolean controller;
    private boolean any;
    private boolean air;
    private boolean portPredicate;
    private int parallelsBonus;
    private int threadsBonus;

    private MultiPredicate(Logic logic, List<Base> bases, List<MultiPredicate> children, String description) {
        this.logic = logic;
        this.bases = List.copyOf(bases);
        this.children = List.copyOf(children);
        this.description = description;
    }

    /**
     * 创建一个单谓词。
     *
     * @param name 调试名称
     * @param test 方块测试
     * @param candidates 候选方块
     * @return predicate
     */
    public static MultiPredicate of(String name, Predicate<PredicateContext> test, List<BlockInfo> candidates) {
        return new MultiPredicate(Logic.OR, List.of(new Base(name, test, candidates)), List.of(), name);
    }

    /**
     * @return 恒匹配的 predicate，不进入缓存
     */
    public static MultiPredicate any() {
        MultiPredicate predicate = of("Any", context -> true, List.of());
        predicate.any = true;
        return predicate;
    }

    /**
     * @return 空气 predicate
     */
    public static MultiPredicate air() {
        MultiPredicate predicate = of("Air", context -> context.state().isAir(), List.of());
        predicate.air = true;
        return predicate;
    }

    /**
     * 组合两个 predicate。
     *
     * @param other 另一个 predicate
     * @param logic 组合逻辑
     * @return 组合后的 predicate
     */
    public MultiPredicate combine(@Nullable MultiPredicate other, Logic logic) {
        if (other == null) {
            return this;
        }
        List<MultiPredicate> combined = new ArrayList<>(
            this.children.isEmpty() || this.logic != logic ? List.of(this) : this.children
        );
        combined.add(other);
        combined.sort(Comparator.comparingInt(MultiPredicate::priority).reversed());
        return new MultiPredicate(logic, List.of(), combined, this.description + " " + logic + " " + other.description);
    }

    /**
     * @param other 另一个 predicate
     * @return AND 组合
     */
    public MultiPredicate and(@Nullable MultiPredicate other) {
        return combine(other, Logic.AND);
    }

    /**
     * @param other 另一个 predicate
     * @return OR 组合
     */
    public MultiPredicate or(@Nullable MultiPredicate other) {
        return combine(other, Logic.OR);
    }

    /**
     * @param other 另一个 predicate
     * @return XOR 组合
     */
    public MultiPredicate xor(@Nullable MultiPredicate other) {
        return combine(other, Logic.XOR);
    }

    /**
     * 测试当前位置是否命中。
     *
     * <p>{@link MatchResult#ERROR} 表示命中了但超过 max，调用方必须立即失败，不能继续尝试
     * 其它候选；否则“物品输入最多 2 个”会被替代方块绕过。</p>
     *
     * @param context 判定上下文
     * @return 匹配结果
     */
    public MatchResult testAt(PredicateContext context) {
        if (this.any) {
            return MatchResult.MATCH;
        }
        if (!this.bases.isEmpty()) {
            for (Base base : this.bases) {
                if (!base.test.test(context)) {
                    continue;
                }
                int global = context.incrementGlobal(this);
                if (this.maxCount >= 0 && global > this.maxCount) {
                    context.addError(PatternError.count(this.description, global, this.minCount, this.maxCount));
                    return MatchResult.ERROR;
                }
                if (context.checkSlice()) {
                    int slice = context.incrementSlice(this);
                    if (this.maxSliceCount >= 0 && slice > this.maxSliceCount) {
                        context.addError(PatternError.count(this.description, slice, this.minSliceCount, this.maxSliceCount));
                        return MatchResult.ERROR;
                    }
                }
                context.addBonus(this.parallelsBonus, this.threadsBonus);
                return MatchResult.MATCH;
            }
            this.addNoMatchError(context);
            return MatchResult.NO_MATCH;
        }

        for (MultiPredicate child : this.children) {
            int errors = context.errorCount();
            MatchResult result = child.testAt(context);
            if (result == MatchResult.MATCH) {
                return MatchResult.MATCH;
            }
            if (result == MatchResult.ERROR) {
                return MatchResult.ERROR;
            }
            context.truncateErrors(errors);
        }
        this.addNoMatchError(context);
        return MatchResult.NO_MATCH;
    }

    private void addNoMatchError(PredicateContext context) {
        List<Block> expected = this.candidates().stream().map(info -> info.state().getBlock()).toList();
        if (!expected.isEmpty()) {
            context.addError(this.portPredicate
                ? PatternError.port(context.pos(), expected)
                : PatternError.block(context.pos(), expected));
        } else {
            context.addError(PatternError.pattern(context.pos(), this.description));
        }
    }

    /**
     * 全局最小值检查。
     *
     * @param context 判定上下文
     * @return 是否满足
     */
    public boolean testGlobalMin(PredicateContext context) {
        boolean own = this.minCount < 0 || context.globalCount(this) >= this.minCount;
        if (!own) {
            context.addError(PatternError.count(this.description, context.globalCount(this), this.minCount, this.maxCount));
        }
        if (this.children.isEmpty()) {
            return own;
        }
        return switch (this.logic) {
            case AND -> {
                boolean result = own;
                for (MultiPredicate child : this.children) {
                    result &= child.testGlobalMin(context);
                }
                yield result;
            }
            case OR, XOR -> {
                if (own) {
                    yield true;
                }
                boolean result = false;
                for (MultiPredicate child : this.children) {
                    result |= child.testGlobalMin(context);
                }
                yield result;
            }
        };
    }

    /**
     * 分层最小值检查。
     *
     * @param context 判定上下文
     * @return 是否满足
     */
    public boolean testSliceMin(PredicateContext context) {
        if (!context.checkSlice()) {
            return true;
        }
        boolean own = this.minSliceCount < 0 || context.sliceCount(this) >= this.minSliceCount;
        if (!own) {
            context.addError(PatternError.count(this.description, context.sliceCount(this), this.minSliceCount, this.maxSliceCount));
        }
        if (this.children.isEmpty()) {
            return own;
        }
        return switch (this.logic) {
            case AND -> {
                boolean result = own;
                for (MultiPredicate child : this.children) {
                    result &= child.testSliceMin(context);
                }
                yield result;
            }
            case OR, XOR -> {
                if (own) {
                    yield true;
                }
                boolean result = false;
                for (MultiPredicate child : this.children) {
                    result |= child.testSliceMin(context);
                }
                yield result;
            }
        };
    }

    /**
     * @return 候选方块，按声明顺序去重
     */
    public List<BlockInfo> candidates() {
        Set<BlockInfo> result = new LinkedHashSet<>();
        for (Base base : this.bases) {
            result.addAll(base.candidates);
        }
        for (MultiPredicate child : this.children) {
            result.addAll(child.candidates());
        }
        return List.copyOf(result);
    }

    /**
     * @param min 全局最少数量
     * @return 自身
     */
    public MultiPredicate setMinGlobalLimited(int min) {
        this.minCount = min;
        return this;
    }

    /**
     * @param max 全局最多数量
     * @return 自身
     */
    public MultiPredicate setMaxGlobalLimited(int max) {
        this.maxCount = max;
        return this;
    }

    /**
     * @param min 全局最少数量
     * @param max 全局最多数量
     * @return 自身
     */
    public MultiPredicate setGlobalMinMax(int min, int max) {
        this.minCount = min;
        this.maxCount = max;
        return this;
    }

    /**
     * @param limit 精确全局数量
     * @return 自身
     */
    public MultiPredicate setExactLimit(int limit) {
        return this.setGlobalMinMax(limit, limit);
    }

    /**
     * @param min 每层最少数量
     * @return 自身
     */
    public MultiPredicate setMinLayerLimited(int min) {
        this.minSliceCount = min;
        return this;
    }

    /**
     * @param max 每层最多数量
     * @return 自身
     */
    public MultiPredicate setMaxLayerLimited(int max) {
        this.maxSliceCount = max;
        return this;
    }

    /**
     * @param count 预览数量
     * @return 自身
     */
    public MultiPredicate setPreviewCount(int count) {
        this.previewCount = count;
        return this;
    }

    /**
     * @param priority 组合时的优先级，数值越大越先匹配
     * @return 自身
     */
    public MultiPredicate setPriority(int priority) {
        this.priority = priority;
        return this;
    }

    /**
     * @param parallels 并行加成
     * @param threads 线程加成
     * @return 自身
     */
    public MultiPredicate setBonus(int parallels, int threads) {
        this.parallelsBonus = parallels;
        this.threadsBonus = threads;
        return this;
    }

    /**
     * @param controller 是否为控制器符号
     * @return 自身
     */
    public MultiPredicate setController(boolean controller) {
        this.controller = controller;
        return this;
    }

    /**
     * @param portPredicate 是否为端口 predicate
     * @return 自身
     */
    public MultiPredicate setPort(boolean portPredicate) {
        this.portPredicate = portPredicate;
        return this;
    }

    /**
     * @return 是否为恒匹配 predicate
     */
    public boolean isAny() {
        return this.any;
    }

    /**
     * @return 是否为空气 predicate
     */
    public boolean isAir() {
        return this.air;
    }

    /**
     * @return 是否为控制器 predicate
     */
    public boolean isController() {
        return this.controller;
    }

    /**
     * @return 优先级
     */
    public int priority() {
        return this.priority;
    }

    /**
     * @return 预览数量
     */
    public int previewCount() {
        return this.previewCount;
    }

    public int minCount() {
        return this.minCount;
    }

    public int maxCount() {
        return this.maxCount;
    }

    public int minSliceCount() {
        return this.minSliceCount;
    }

    public int maxSliceCount() {
        return this.maxSliceCount;
    }

    /**
     * @return 调试描述
     */
    public String description() {
        return this.description;
    }

    /**
     * @return 组合逻辑
     */
    public Logic logic() {
        return this.logic;
    }

    /**
     * @return 子 predicate
     */
    public List<MultiPredicate> children() {
        return this.children;
    }

    /**
     * @return 文本描述，供调试与提示
     */
    public Component text() {
        return Component.literal(this.description);
    }

    @Override
    public String toString() {
        StringJoiner joiner = new StringJoiner(", ", this.description + "[", "]");
        for (Base base : this.bases) {
            joiner.add(base.name);
        }
        for (MultiPredicate child : this.children) {
            joiner.add(child.toString());
        }
        return joiner.toString();
    }

    /**
     * 组合逻辑。
     */
    public enum Logic {
        OR,
        AND,
        XOR
    }

    /**
     * 单次位置匹配结果。
     */
    public enum MatchResult {
        MATCH,
        NO_MATCH,
        ERROR
    }

    private record Base(String name, Predicate<PredicateContext> test, List<BlockInfo> candidates) {
        private Base {
            candidates = List.copyOf(candidates);
        }
    }
}
