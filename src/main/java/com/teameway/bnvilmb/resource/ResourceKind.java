package com.teameway.bnvilmb.resource;

import com.mojang.serialization.Codec;
import net.minecraft.util.StringRepresentable;

/**
 * 多方块配方可用的资源种类
 *
 * <p>新增种类时在这里补枚举值，再实现对应的 {@link ResourceIngredient} 与 {@link ResourceOutput}；
 * 配方匹配与执行的代码不用跟着改。</p>
 */
public enum ResourceKind implements StringRepresentable {
    /**
     * 物品
     */
    ITEM("item"),
    /**
     * 流体
     */
    FLUID("fluid"),
    /**
     * 能量（FE），标量资源
     */
    ENERGY("energy");

    /**
     * 序列化用 codec
     */
    public static final Codec<ResourceKind> CODEC = StringRepresentable.fromEnum(ResourceKind::values);

    private final String name;

    ResourceKind(String name) {
        this.name = name;
    }

    @Override
    public String getSerializedName() {
        return this.name;
    }
}
