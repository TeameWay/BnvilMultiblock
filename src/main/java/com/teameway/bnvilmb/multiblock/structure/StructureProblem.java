package com.teameway.bnvilmb.multiblock.structure;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;

/**
 * 结构判定失败原因
 *
 * @param pos    出错位置
 * @param type   错误类型
 * @param role   角色 ID，非角色错误时为空串
 * @param actual 实际数量
 * @param min    最少数量
 * @param max    最多数量，-1 表示不限
 */
public record StructureProblem(BlockPos pos, Type type, String role, int actual, int min, int max) {
    public static final String LANG_PREFIX = "structure.bnvil_multiblock.problem.";

    public StructureProblem(BlockPos pos, Type type) {
        this(pos, type, "", 0, 0, -1);
    }

    /**
     * 供服务端同步/日志使用的简短英文描述
     *
     * @return 不含翻译键的文本
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

    public enum Type {
        FIXED,
        SLOT,
        PORT_NOT_ALLOWED,
        PORT_CLAIMED,
        ROLE_MIN,
        ROLE_MAX,
        UNLOADED,
        PATTERN
    }
}
