package com.teameway.bnvilmb.multiblock.pattern;

import it.unimi.dsi.fastutil.chars.Char2ObjectMap;
import it.unimi.dsi.fastutil.chars.Char2ObjectOpenHashMap;
import net.minecraft.resources.Identifier;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * GTM 风格的代码 pattern 构建器。
 *
 * <p>用法与 GTM 类似：{@code start(...)} 指定三个推进方向，{@code slice(...)} 写层，
 * {@code where(...)} 绑定字符与谓词，最后 {@code build()}。</p>
 */
public final class MultiblockPatternBuilder {
    private final RelativeDirection[] directions = new RelativeDirection[3];
    private final List<PatternSlice> slices = new ArrayList<>();
    private final Char2ObjectMap<MultiPredicate> predicates = new Char2ObjectOpenHashMap<>();
    private final Set<Character> usedChars = new LinkedHashSet<>();
    private final Set<Identifier> variants = new LinkedHashSet<>();
    private OriginOffset offset;
    private char controllerChar;
    private boolean allowFlip = true;
    private int width = -1;
    private int height = -1;

    private MultiblockPatternBuilder(RelativeDirection sliceDirection, RelativeDirection stringDirection, RelativeDirection charDirection) {
        this.directions[0] = sliceDirection;
        this.directions[1] = stringDirection;
        this.directions[2] = charDirection;
        RelativeDirection.validateFacingsArray(this.directions);
        this.where(' ', Predicates.any());
    }

    /**
     * @return 默认使用 FRONT / UP / RIGHT 的构建器
     */
    public static MultiblockPatternBuilder start() {
        return new MultiblockPatternBuilder(RelativeDirection.FRONT, RelativeDirection.UP, RelativeDirection.RIGHT);
    }

    /**
     * @param sliceDirection 层推进方向
     * @param stringDirection 行推进方向
     * @param charDirection 字符推进方向
     * @return 构建器
     */
    public static MultiblockPatternBuilder start(
        RelativeDirection sliceDirection,
        RelativeDirection stringDirection,
        RelativeDirection charDirection
    ) {
        return new MultiblockPatternBuilder(sliceDirection, stringDirection, charDirection);
    }

    /**
     * @param rows 层内每一行
     * @return 自身
     */
    public MultiblockPatternBuilder slice(String... rows) {
        return this.sliceRepeatable(1, 1, rows);
    }

    /**
     * @param minRepeats 最少重复次数
     * @param maxRepeats 最多重复次数
     * @param rows 层内每一行
     * @return 自身
     */
    public MultiblockPatternBuilder sliceRepeatable(int minRepeats, int maxRepeats, String... rows) {
        if (minRepeats <= 0 || maxRepeats < minRepeats) {
            throw new IllegalArgumentException("Invalid slice repeat range: " + minRepeats + ".." + maxRepeats);
        }
        if (rows.length == 0 || rows[0].isEmpty()) {
            throw new IllegalArgumentException("Slice must not be empty");
        }
        int rowWidth = rows[0].length();
        if (this.width < 0) {
            this.width = rowWidth;
            this.height = rows.length;
        }
        if (rows.length != this.height) {
            throw new IllegalArgumentException("All slices must have the same height");
        }
        for (String row : rows) {
            if (row.length() != this.width) {
                throw new IllegalArgumentException("All rows must have the same width");
            }
            for (char symbol : row.toCharArray()) {
                this.usedChars.add(symbol);
            }
        }
        this.slices.add(new PatternSlice(minRepeats, maxRepeats, rows));
        return this;
    }

    /**
     * @param symbol 单字符符号
     * @param predicate 方块谓词
     * @return 自身
     */
    public MultiblockPatternBuilder where(char symbol, MultiPredicate predicate) {
        this.predicates.put(symbol, predicate);
        this.usedChars.add(symbol);
        if (predicate.isController()) {
            this.controllerChar = symbol;
        }
        return this;
    }

    /**
     * @param symbol 控制器符号
     * @param block 控制器方块
     * @return 自身
     */
    public MultiblockPatternBuilder controller(char symbol, net.minecraft.world.level.block.Block block) {
        return this.where(symbol, Predicates.controller(block));
    }

    /**
     * @param offset 控制器原点偏移
     * @return 自身
     */
    public MultiblockPatternBuilder startOffset(OriginOffset offset) {
        this.offset = offset.copy();
        return this;
    }

    /**
     * @param allowFlip 是否允许自动翻转
     * @return 自身
     */
    public MultiblockPatternBuilder allowFlip(boolean allowFlip) {
        this.allowFlip = allowFlip;
        return this;
    }

    /**
     * @param variant 结构变体 ID
     * @return 自身
     */
    public MultiblockPatternBuilder variant(Identifier variant) {
        this.variants.add(variant);
        return this;
    }

    /**
     * @param variants 结构变体 ID
     * @return 自身
     */
    public MultiblockPatternBuilder variants(Identifier... variants) {
        for (Identifier variant : variants) {
            this.variants.add(variant);
        }
        return this;
    }

    /**
     * @return 构建完成的 pattern
     * @throws IllegalArgumentException 缺少谓词或控制器
     */
    public BlockPattern build() {
        for (char symbol : this.usedChars) {
            if (!this.predicates.containsKey(symbol)) {
                throw new IllegalArgumentException("Missing predicate for symbol '" + symbol + "'");
            }
        }
        if (this.controllerChar == 0) {
            throw new IllegalArgumentException("Pattern has no controller symbol");
        }
        if (this.slices.isEmpty()) {
            throw new IllegalArgumentException("Pattern has no slices");
        }
        OriginOffset resolvedOffset = this.offset;
        if (resolvedOffset == null) {
            resolvedOffset = this.legacyStartOffset();
        }
        return new BlockPattern(
            this.slices.toArray(PatternSlice[]::new),
            this.directions,
            resolvedOffset,
            this.predicates,
            this.controllerChar,
            this.allowFlip,
            this.variants
        );
    }

    private OriginOffset legacyStartOffset() {
        for (int sliceIndex = 0; sliceIndex < this.slices.size(); sliceIndex++) {
            int[] found = this.slices.get(sliceIndex).firstInstanceOf(this.controllerChar);
            if (found != null) {
                return new OriginOffset()
                    .move(this.directions[0], -sliceIndex)
                    .move(this.directions[1], -found[0])
                    .move(this.directions[2], -found[1]);
            }
        }
        throw new IllegalArgumentException("Controller symbol '" + this.controllerChar + "' is not in the pattern");
    }
}
