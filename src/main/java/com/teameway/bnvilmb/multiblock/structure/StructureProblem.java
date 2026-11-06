package com.teameway.bnvilmb.multiblock.structure;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;

/**
 * 结构判定失败原因
 *
 * <p>{@link Type#ROLE_MIN} 这类数量问题不对应单个方块，位置可能记作 {@link BlockPos#ZERO}；
 * {@code role} 在非角色问题里为空串，{@code max} 为 {@code -1} 表示不限。</p>
 *
 * @param pos 出错位置
 * @param type 错误类型
 * @param role 角色 ID，非角色问题时为空串
 * @param actual 实际数量
 * @param min 最少数量
 * @param max 最多数量，{@code -1} 表示不限
 */
public record StructureProblem(BlockPos pos, Type type, String role, int actual, int min, int max) {
    /** 翻译键前缀，完整键为前缀加 {@code fixed}、{@code role_min} 等后缀 */
    public static final String LANG_PREFIX = "structure.bnvil_multiblock.problem.";

    /**
     * 创建不带角色数量的失败原因
     *
     * @param pos 出错位置
     * @param type 错误类型
     */
    public StructureProblem(BlockPos pos, Type type) {
        this(pos, type, "", 0, 0, -1);
    }

    /**
     * 生成简短英文描述
     *
     * @return 不含翻译键的文本，供服务端同步与日志使用
     */
    public String describe() {
        return switch (this.type) {
            case FIXED -> "Block at " + this.pos.getX() + "," + this.pos.getY() + "," + this.pos.getZ() + " does not match";
            case SLOT -> "Block at " + this.pos.getX() + "," + this.pos.getY() + "," + this.pos.getZ() + " is not a valid port or substitute";
            case PORT_NOT_ALLOWED -> "Port at " + this.pos.getX() + "," + this.pos.getY() + "," + this.pos.getZ() + " is not allowed here";
            case PORT_CLAIMED -> "Port at " + this.pos.getX() + "," + this.pos.getY() + "," + this.pos.getZ() + " belongs to another controller";
            case ROLE_MIN -> "Not enough " + this.role + ": " + this.actual + " (need " + this.min + ")";
            case ROLE_MAX -> "Too many " + this.role + ": " + this.actual + " (max " + this.max + ")";
            case UNLOADED -> "Structure area is not fully loaded";
            case PATTERN -> "Structure does not satisfy its role counts";
        };
    }

    /**
     * 生成客户端文本
     *
     * @return 带翻译键与坐标或数量参数的文本组件
     */
    public Component message() {
        return switch (this.type) {
            case FIXED -> Component.translatable(LANG_PREFIX + "fixed", this.pos.getX(), this.pos.getY(), this.pos.getZ());
            case SLOT -> Component.translatable(LANG_PREFIX + "slot", this.pos.getX(), this.pos.getY(), this.pos.getZ());
            case PORT_NOT_ALLOWED -> Component.translatable(LANG_PREFIX + "port_not_allowed",
                this.pos.getX(), this.pos.getY(), this.pos.getZ());
            case PORT_CLAIMED -> Component.translatable(LANG_PREFIX + "port_claimed",
                this.pos.getX(), this.pos.getY(), this.pos.getZ());
            case ROLE_MIN -> Component.translatable(LANG_PREFIX + "role_min", this.role, this.actual, this.min);
            case ROLE_MAX -> Component.translatable(LANG_PREFIX + "role_max", this.role, this.actual, this.max);
            case UNLOADED -> Component.translatable(LANG_PREFIX + "unloaded");
            case PATTERN -> Component.translatable(LANG_PREFIX + "pattern");
        };
    }

    /**
     * 失败原因类型
     */
    public enum Type {
        /** 固定方块谓词不匹配 */
        FIXED,
        /** 槽位不允许空气却为空，或方块既非端口也不匹配任何替代方块 */
        SLOT,
        /** 端口种类不被该槽位的角色接受 */
        PORT_NOT_ALLOWED,
        /** 端口已经被其它控制器认领 */
        PORT_CLAIMED,
        /** 角色数量低于下限 */
        ROLE_MIN,
        /** 角色数量超过上限 */
        ROLE_MAX,
        /** 结构范围内有区块未加载 */
        UNLOADED,
        /** 替代方块分配失败，且没有更具体的问题可报 */
        PATTERN
    }
}
