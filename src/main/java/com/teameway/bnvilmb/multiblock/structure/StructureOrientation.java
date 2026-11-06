package com.teameway.bnvilmb.multiblock.structure;

import com.mojang.serialization.Codec;
import net.minecraft.util.StringRepresentable;

/**
 * 控制器朝向的使用方式
 *
 * <p>{@link #NONE} 保留模板原始坐标；{@link #HORIZONTAL} 按水平朝向绕 Y 轴旋转。</p>
 */
public enum StructureOrientation implements StringRepresentable {
    /** 结构固定，不随控制器朝向旋转 */
    NONE("none"),
    /** 只绕 Y 轴旋转，支持水平四向 */
    HORIZONTAL("horizontal");

    public static final Codec<StructureOrientation> CODEC = StringRepresentable.fromEnum(StructureOrientation::values);

    private final String name;

    StructureOrientation(String name) {
        this.name = name;
    }

    /**
     * @return 序列化名，数据文件中写作 {@code none} 或 {@code horizontal}
     */
    @Override
    public String getSerializedName() {
        return this.name;
    }
}
