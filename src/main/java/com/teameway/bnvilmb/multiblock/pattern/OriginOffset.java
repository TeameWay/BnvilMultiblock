package com.teameway.bnvilmb.multiblock.pattern;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

import java.util.EnumMap;
import java.util.Map;

/**
 * 相对控制器原点的偏移。
 *
 * <p>和 GTM 的 {@code OriginOffset} 一样，偏移按相对方向保存，应用时再映射到
 * 控制器当前的世界朝向。这样同一份 pattern 可以用于六向朝向和翻转。</p>
 */
public final class OriginOffset {
    private final Map<RelativeDirection, Integer> offsets = new EnumMap<>(RelativeDirection.class);

    /**
     * 增加一个相对偏移。
     *
     * @param direction 相对方向
     * @param amount 偏移量
     * @return 自身
     */
    public OriginOffset move(RelativeDirection direction, int amount) {
        if (amount != 0) {
            this.offsets.merge(direction, amount, Integer::sum);
            this.offsets.remove(direction, 0);
        }
        return this;
    }

    /**
     * @param direction 相对方向
     * @return 当前偏移量
     */
    public int get(RelativeDirection direction) {
        return this.offsets.getOrDefault(direction, 0);
    }

    /**
     * 把偏移应用到世界坐标。
     *
     * @param pos 待修改的坐标
     * @param front 控制器正面
     * @param up 控制器上方
     * @param flip 是否左右镜像
     */
    public void apply(BlockPos.MutableBlockPos pos, Direction front, Direction up, boolean flip) {
        for (Map.Entry<RelativeDirection, Integer> entry : this.offsets.entrySet()) {
            Direction facing = entry.getKey().getRelativeFacing(front, up, flip);
            pos.move(facing, entry.getValue());
        }
    }

    /**
     * @return 偏移副本
     */
    public OriginOffset copy() {
        OriginOffset copy = new OriginOffset();
        copy.offsets.putAll(this.offsets);
        return copy;
    }
}
