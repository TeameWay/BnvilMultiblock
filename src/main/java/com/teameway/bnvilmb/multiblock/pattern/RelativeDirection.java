package com.teameway.bnvilmb.multiblock.pattern;

import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;

/**
 * 结构模板中的相对方向。
 *
 * <p>GTM 的 pattern 用相对方向描述切片、行和字符的推进方向。这里保留同样的概念，
 * 但不依赖 GTM 的机器框架。局部坐标约定为：
 * {@code RIGHT = +X}、{@code UP = +Y}、{@code FRONT = -Z}。</p>
 */
public enum RelativeDirection {
    FRONT(0, 0, -1),
    BACK(0, 0, 1),
    LEFT(-1, 0, 0),
    RIGHT(1, 0, 0),
    UP(0, 1, 0),
    DOWN(0, -1, 0);

    private final Vec3i localOffset;

    RelativeDirection(int x, int y, int z) {
        this.localOffset = new Vec3i(x, y, z);
    }

    /**
     * @return 局部坐标中的单位偏移
     */
    public Vec3i localOffset() {
        return this.localOffset;
    }

    /**
     * 把局部相对方向映射到控制器当前的世界方向。
     *
     * @param front 控制器正面
     * @param up 控制器上方，必须与正面垂直
     * @param flip 是否左右镜像
     * @return 世界方向
     * @throws IllegalArgumentException 正面与上方不垂直
     */
    public Direction getRelativeFacing(Direction front, Direction up, boolean flip) {
        Vec3i frontVector = front.getUnitVec3i();
        Vec3i upVector = up.getUnitVec3i();
        if (dot(frontVector, upVector) != 0) {
            throw new IllegalArgumentException("front and up must be perpendicular: " + front + ", " + up);
        }

        Vec3i rightVector = cross(frontVector, upVector);
        if (flip) {
            rightVector = multiply(rightVector, -1);
        }

        Vec3i result = switch (this) {
            case FRONT -> frontVector;
            case BACK -> multiply(frontVector, -1);
            case UP -> upVector;
            case DOWN -> multiply(upVector, -1);
            case RIGHT -> rightVector;
            case LEFT -> multiply(rightVector, -1);
        };
        return Direction.getNearest(result.getX(), result.getY(), result.getZ(), Direction.NORTH);
    }

    /**
     * 校验一组方向是否互相垂直且不重复。
     *
     * @param directions 三个推进方向
     * @throws IllegalArgumentException 方向不合法
     */
    public static void validateFacingsArray(RelativeDirection[] directions) {
        if (directions.length != 3) {
            throw new IllegalArgumentException("Expected exactly three directions");
        }
        for (int i = 0; i < directions.length; i++) {
            for (int j = i + 1; j < directions.length; j++) {
                if (directions[i] == directions[j]) {
                    throw new IllegalArgumentException("Duplicate relative direction: " + directions[i]);
                }
                if (dot(directions[i].localOffset, directions[j].localOffset) != 0) {
                    throw new IllegalArgumentException("Directions must be perpendicular: " + directions[i] + ", " + directions[j]);
                }
            }
        }
    }

    private static int dot(Vec3i a, Vec3i b) {
        return a.getX() * b.getX() + a.getY() * b.getY() + a.getZ() * b.getZ();
    }

    private static Vec3i cross(Vec3i a, Vec3i b) {
        return new Vec3i(
            a.getY() * b.getZ() - a.getZ() * b.getY(),
            a.getZ() * b.getX() - a.getX() * b.getZ(),
            a.getX() * b.getY() - a.getY() * b.getX()
        );
    }

    private static Vec3i multiply(Vec3i vector, int factor) {
        return new Vec3i(vector.getX() * factor, vector.getY() * factor, vector.getZ() * factor);
    }
}
