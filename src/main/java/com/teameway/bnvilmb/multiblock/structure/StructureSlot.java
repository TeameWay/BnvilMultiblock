package com.teameway.bnvilmb.multiblock.structure;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.List;

/**
 * 一个槽位允许的角色集合
 *
 * <p>槽位不绑定具体角色，角色只按全局数量约束；{@code roles} 的顺序用于挑选候选角色，
 * JEI 预览的示例方块也按这个顺序轮换。构造时复制列表。</p>
 *
 * @param roles 允许的角色 ID，按顺序决定候选与预览顺序
 * @param allowAir 是否允许这个位置为空
 */
public record StructureSlot(List<String> roles, boolean allowAir) {
    /** 槽位定义编解码器，JSON 字段为 {@code roles} 与 {@code allow_air} */
    public static final Codec<StructureSlot> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        Codec.STRING.listOf().fieldOf("roles").forGetter(StructureSlot::roles),
        Codec.BOOL.optionalFieldOf("allow_air", false).forGetter(StructureSlot::allowAir)
    ).apply(instance, StructureSlot::new));

    public StructureSlot {
        roles = List.copyOf(roles);
    }
}
