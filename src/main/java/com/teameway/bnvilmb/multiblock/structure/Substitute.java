package com.teameway.bnvilmb.multiblock.structure;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.anvilcraft.lib.v2.util.predicate.BlockStatePredicate;
import net.minecraft.world.level.block.state.BlockState;

/**
 * 非端口替代方块
 *
 * <p>默认只参与结构判定；{@code parallels} 与 {@code threads} 预留给机壳等级加成，当前默认 0，
 * 不会改变现有机器数值。匹配调用 {@code testWithoutEntity}，不读取方块实体数据。</p>
 *
 * <p>构造时拒绝负数加成。</p>
 *
 * @param predicate 方块谓词
 * @param parallels 并行加成
 * @param threads 线程加成
 */
public record Substitute(BlockStatePredicate predicate, int parallels, int threads) {
    /** 替代方块编解码器，JSON 字段为 {@code predicate}、{@code parallels} 与 {@code threads} */
    public static final Codec<Substitute> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        BlockStatePredicate.CODEC.fieldOf("predicate").forGetter(Substitute::predicate),
        Codec.INT.optionalFieldOf("parallels", 0).forGetter(Substitute::parallels),
        Codec.INT.optionalFieldOf("threads", 0).forGetter(Substitute::threads)
    ).apply(instance, Substitute::new));

    public Substitute {
        if (parallels < 0 || threads < 0) {
            throw new IllegalArgumentException("Substitute bonuses must be non-negative");
        }
    }

    /**
     * @param state 方块状态
     * @return 谓词是否匹配，不检查方块实体
     */
    public boolean matches(BlockState state) {
        return this.predicate.testWithoutEntity(state);
    }
}
