package com.teameway.bnvilmb.multiblock.pattern;

import lombok.Setter;
import org.jspecify.annotations.Nullable;

/**
 * pattern 的一层。
 *
 * <p>行内字符按 {@link MultiblockPatternBuilder} 的字符方向推进。支持 GTM 式的最小/最大重复次数，
 * 当前默认固定一次；重复切片由 {@link BlockPattern} 负责展开。</p>
 */
public final class PatternSlice {
    private final char[][] pattern;
    private final int minRepeats;
    private final int maxRepeats;
    @Setter
    private int actualRepeats;

    public PatternSlice(String[] rows) {
        this(1, 1, rows);
    }

    public PatternSlice(int minRepeats, int maxRepeats, String[] rows) {
        this.minRepeats = minRepeats;
        this.maxRepeats = maxRepeats;
        this.actualRepeats = minRepeats;
        this.pattern = new char[rows.length][];
        for (int row = 0; row < rows.length; row++) {
            this.pattern[row] = rows[row].toCharArray();
        }
    }

    public int minRepeats() {
        return this.minRepeats;
    }

    public int maxRepeats() {
        return this.maxRepeats;
    }

    public int actualRepeats() {
        return this.actualRepeats;
    }

    public int height() {
        return this.pattern.length;
    }

    public int width() {
        return this.pattern.length == 0 ? 0 : this.pattern[0].length;
    }

    public char charAt(int row, int column) {
        return this.pattern[row][column];
    }

    /**
     * @param target 目标字符
     * @return 第一次出现的位置 {@code [row, column]}，没有时为 {@code null}
     */
    public int @Nullable [] firstInstanceOf(char target) {
        for (int row = 0; row < this.pattern.length; row++) {
            for (int column = 0; column < this.pattern[row].length; column++) {
                if (this.pattern[row][column] == target) {
                    return new int[] { row, column };
                }
            }
        }
        return null;
    }
}
