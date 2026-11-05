package com.teameway.bnvilmb.multiblock.structure;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.anvilcraft.lib.v2.util.predicate.BlockStatePredicate;
import net.minecraft.world.level.block.state.BlockState;

/**
 * 非端口替代物
 *
 * <p>默认只参与结构判定；{@code parallels}/{@code threads} 留作机壳等级加成接口，
 * 当前默认 0，不会改变现有机器数值。</p>
 *
 * @param predicate 方块谓词
 * @param parallels 并行加成
 * @param threads   线程加成
 */
public record Substitute(BlockStatePredicate predicate, int parallels, int threads) {
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

    public boolean matches(BlockState state) {
        return this.predicate.testWithoutEntity(state);
    }
}
