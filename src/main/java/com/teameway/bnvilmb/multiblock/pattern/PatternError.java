package com.teameway.bnvilmb.multiblock.pattern;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.Block;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 结构判定错误。
 *
 * <p>和 GTM 的 {@code PatternError} 一样，错误保留位置、类型和候选方块，而不是只拼一个字符串。
 * 服务端日志使用 {@link #describe()}，客户端使用 {@link #message()}。</p>
 *
 * @param type 错误类型
 * @param pos 出错位置
 * @param detail 额外描述，没有时为空串
 * @param actual 实际数量
 * @param min 最少数量，{@code -1} 表示未设置
 * @param max 最多数量，{@code -1} 表示未设置
 * @param expected 期望方块，没有时为空列表
 */
public record PatternError(
    Type type,
    BlockPos pos,
    String detail,
    int actual,
    int min,
    int max,
    List<Block> expected
) {
    public static final String LANG_PREFIX = "structure.bnvil_multiblock.error.";

    public PatternError {
        expected = List.copyOf(expected);
    }

    public static PatternError block(BlockPos pos, List<Block> expected) {
        return new PatternError(Type.BLOCK, pos, "", 0, -1, -1, expected);
    }

    public static PatternError port(BlockPos pos, List<Block> expected) {
        return new PatternError(Type.PORT, pos, "", 0, -1, -1, expected);
    }

    public static PatternError shared(BlockPos pos) {
        return new PatternError(Type.SHARED, pos, "", 0, -1, -1, List.of());
    }

    public static PatternError count(String detail, int actual, int min, int max) {
        return new PatternError(max >= 0 && actual > max ? Type.MAX : Type.COUNT, BlockPos.ZERO, detail, actual, min, max, List.of());
    }

    public static PatternError unloaded(BlockPos pos) {
        return new PatternError(Type.UNLOADED, pos, "", 0, -1, -1, List.of());
    }

    public static PatternError controller(BlockPos pos) {
        return new PatternError(Type.CONTROLLER, pos, "", 0, -1, -1, List.of());
    }

    public static PatternError pattern(BlockPos pos, String detail) {
        return new PatternError(Type.PATTERN, pos, detail, 0, -1, -1, List.of());
    }

    /**
     * @return 英文简短描述，供服务端日志和旧的字符串同步使用
     */
    public String describe() {
        return switch (this.type) {
            case BLOCK -> "Block at " + pos(this.pos) + " does not match " + names();
            case PORT -> "Block at " + pos(this.pos) + " is not one of " + names();
            case SHARED -> "Port at " + pos(this.pos) + " is not shareable";
            case COUNT -> "Not enough " + this.detail + ": " + this.actual + " (need " + this.min + ")";
            case MAX -> "Too many " + this.detail + ": " + this.actual + " (max " + this.max + ")";
            case UNLOADED -> "Structure area is not fully loaded";
            case CONTROLLER -> "Controller at " + pos(this.pos) + " is missing or invalid";
            case PATTERN -> this.detail.isEmpty() ? "Structure does not satisfy its pattern" : this.detail;
        };
    }

    /**
     * @return 客户端文本
     */
    public Component message() {
        return switch (this.type) {
            case BLOCK -> Component.translatable(LANG_PREFIX + "block", pos(this.pos), names());
            case PORT -> Component.translatable(LANG_PREFIX + "port", pos(this.pos), names());
            case SHARED -> Component.translatable(LANG_PREFIX + "shared", pos(this.pos));
            case COUNT -> Component.translatable(LANG_PREFIX + "count", this.detail, this.actual, this.min);
            case MAX -> Component.translatable(LANG_PREFIX + "max", this.detail, this.actual, this.max);
            case UNLOADED -> Component.translatable(LANG_PREFIX + "unloaded");
            case CONTROLLER -> Component.translatable(LANG_PREFIX + "controller", pos(this.pos));
            case PATTERN -> Component.translatable(LANG_PREFIX + "pattern", this.detail);
        };
    }

    private String names() {
        if (this.expected.isEmpty()) {
            return "the expected block";
        }
        return this.expected.stream()
            .map(block -> block.getName().getString())
            .distinct()
            .collect(Collectors.joining(", "));
    }

    private static String pos(BlockPos pos) {
        return pos.getX() + "," + pos.getY() + "," + pos.getZ();
    }

    /**
     * 错误类型。
     */
    public enum Type {
        BLOCK,
        PORT,
        SHARED,
        COUNT,
        MAX,
        UNLOADED,
        CONTROLLER,
        PATTERN
    }
}
