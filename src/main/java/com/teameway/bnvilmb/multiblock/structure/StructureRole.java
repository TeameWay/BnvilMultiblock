package com.teameway.bnvilmb.multiblock.structure;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.teameway.bnvilmb.multiblock.port.PortKind;

import java.util.List;

/**
 * 结构角色
 *
 * <p>端口方块按 {@link PortKind} 固有匹配；非端口方块只能通过 {@link #substitutes()} 参与。</p>
 *
 * @param portKinds  该角色认可的端口种类
 * @param substitutes 非端口替代物
 * @param min        全局最少数量
 * @param max        全局最多数量，{@code -1} 表示不限
 */
public record StructureRole(List<PortKind> portKinds, List<Substitute> substitutes, int min, int max) {
    public static final Codec<StructureRole> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        PortKind.CODEC.listOf().optionalFieldOf("port_kinds", List.of()).forGetter(StructureRole::portKinds),
        Substitute.CODEC.listOf().optionalFieldOf("substitutes", List.of()).forGetter(StructureRole::substitutes),
        Codec.INT.optionalFieldOf("min", 0).forGetter(StructureRole::min),
        Codec.INT.optionalFieldOf("max", -1).forGetter(StructureRole::max)
    ).apply(instance, StructureRole::new));

    public StructureRole {
        portKinds = List.copyOf(portKinds);
        substitutes = List.copyOf(substitutes);
        if (min < 0 || (max >= 0 && max < min)) {
            throw new IllegalArgumentException("Invalid role count range: " + min + ".." + max);
        }
        if (portKinds.isEmpty() && substitutes.isEmpty()) {
            throw new IllegalArgumentException("Role must accept at least one port kind or substitute");
        }
    }

    public int maxCount() {
        return this.max < 0 ? Integer.MAX_VALUE : this.max;
    }

    public boolean accepts(PortKind kind) {
        return this.portKinds.contains(kind);
    }
}
