package com.teameway.bnvilmb.multiblock.structure;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.List;

/**
 * 一个槽位允许的角色集合
 *
 * @param roles   允许的角色 ID，按顺序决定候选与诊断顺序
 * @param allowAir 是否允许这个位置为空
 */
public record StructureSlot(List<String> roles, boolean allowAir) {
    public static final Codec<StructureSlot> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        Codec.STRING.listOf().fieldOf("roles").forGetter(StructureSlot::roles),
        Codec.BOOL.optionalFieldOf("allow_air", false).forGetter(StructureSlot::allowAir)
    ).apply(instance, StructureSlot::new));

    public StructureSlot {
        roles = List.copyOf(roles);
    }
}
