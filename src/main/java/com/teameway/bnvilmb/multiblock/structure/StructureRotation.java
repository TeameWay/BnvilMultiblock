package com.teameway.bnvilmb.multiblock.structure;

import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;

/**
 * 本地坐标到世界坐标的旋转
 *
 * <p>网格定义在 {@code canonicalFront} 朝向，运行时按控制器当前正面绕 Y 轴顺时针旋转，
 * 每次旋转 90 度。只支持水平四向；朝向不在水平面上时直接抛异常，避免静默得到错误结构。</p>
 */
public final class StructureRotation {
    private StructureRotation() {
    }

    /**
     * 把本地偏移旋转到控制器朝向
     *
     * @param offset 本地偏移，原点为控制器
     * @param canonicalFront 主朝向
     * @param facing 控制器当前正面
     * @param orientation 朝向模式
     * @return 旋转后的偏移
     * @throws IllegalArgumentException 朝向模式不为 {@code NONE} 且任一朝向不在水平面上
     */
    public static Vec3i rotate(
        Vec3i offset,
        Direction canonicalFront,
        Direction facing,
        StructureOrientation orientation
    ) {
        if (orientation == StructureOrientation.NONE) {
            return offset;
        }
        if (!canonicalFront.getAxis().isHorizontal() || !facing.getAxis().isHorizontal()) {
            throw new IllegalArgumentException("Only horizontal facing is supported: " + canonicalFront + " -> " + facing);
        }
        int steps = steps(canonicalFront, facing);
        int x = offset.getX();
        int z = offset.getZ();
        for (int i = 0; i < steps; i++) {
            int nextX = -z;
            z = x;
            x = nextX;
        }
        return new Vec3i(x, offset.getY(), z);
    }

    /** 计算 from 顺时针转到 to 需要的 90 度步数 */
    private static int steps(Direction from, Direction to) {
        Direction current = from;
        for (int i = 0; i < 4; i++) {
            if (current == to) {
                return i;
            }
            current = current.getClockWise();
        }
        throw new IllegalArgumentException("Not a horizontal direction: " + to);
    }
}
