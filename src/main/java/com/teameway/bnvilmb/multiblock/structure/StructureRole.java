package com.teameway.bnvilmb.multiblock.structure;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.teameway.bnvilmb.multiblock.port.PortKind;

import java.util.List;

/**
 * 结构角色
 *
 * <p>角色只约束全局数量，不绑定具体槽位，因此同一角色的端口可以放在任意槽位。端口方块按
 * {@link PortKind} 固有种类匹配；非端口方块只能通过 {@link #substitutes()} 参与。</p>
 *
 * <p>构造时复制列表并校验数量范围：{@code min} 不能为负，{@code max} 为 {@code -1} 时表示
 * 不限，否则不能小于 {@code min}；端口种类与替代方块不能同时为空。</p>
 *
 * @param portKinds 该角色认可的端口种类
 * @param substitutes 非端口替代方块
 * @param min 全局最少数量
 * @param max 全局最多数量，{@code -1} 表示不限
 */
public record StructureRole(List<PortKind> portKinds, List<Substitute> substitutes, int min, int max) {
    /** 角色编解码器，JSON 字段为 {@code port_kinds}、{@code substitutes}、{@code min} 与 {@code max} */
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

    /**
     * @return 数量上限，{@code max < 0} 时返回 {@code Integer.MAX_VALUE}
     */
    public int maxCount() {
        return this.max < 0 ? Integer.MAX_VALUE : this.max;
    }

    /**
     * @param kind 端口种类
     * @return 该角色是否接受这种端口
     */
    public boolean accepts(PortKind kind) {
        return this.portKinds.contains(kind);
    }
}
