package com.teameway.bnvilmb.multiblock.pattern;

import com.teameway.bnvilmb.multiblock.port.PortKind;
import com.teameway.bnvilmb.multiblock.port.PortRegistry;
import dev.anvilcraft.lib.v2.util.predicate.BlockStatePredicate;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.List;

/**
 * pattern 常用谓词。
 *
 * <p>对应 GTM 的 {@code Predicates}，但端口谓词使用本项目的 {@link PortKind}，
 * 替代方块谓词额外携带并行/线程加成。</p>
 */
public final class Predicates {
    private Predicates() {
    }

    /**
     * @param blocks 方块
     * @return 匹配这些方块的 predicate
     */
    public static MultiPredicate blocks(Block... blocks) {
        return blocks("Block", blocks);
    }

    /**
     * @param name 调试名称
     * @param blocks 方块
     * @return 匹配这些方块的 predicate
     */
    public static MultiPredicate blocks(String name, Block... blocks) {
        BlockStatePredicate predicate = BlockStatePredicate.builder().of(blocks).build();
        return multi(name, predicate);
    }

    /**
     * @param name 调试名称
     * @param predicate 方块谓词
     * @return 匹配该谓词的 predicate
     */
    public static MultiPredicate multi(String name, BlockStatePredicate predicate) {
        return MultiPredicate.of(
            name,
            context -> predicate.test(context.level(), context.state(), context.blockEntity()),
            candidates(predicate)
        );
    }

    /**
     * @param predicate 方块谓词
     * @return 匹配该谓词的 predicate
     */
    public static MultiPredicate blocks(BlockStatePredicate predicate) {
        return multi("Block", predicate);
    }

    /**
     * @param states 允许的方块状态
     * @return 匹配这些状态的 predicate
     */
    public static MultiPredicate states(BlockState... states) {
        List<BlockState> allowed = List.of(states);
        List<BlockInfo> candidates = new ArrayList<>(states.length);
        for (BlockState state : states) {
            candidates.add(new BlockInfo(state, null));
        }
        return MultiPredicate.of("State", context -> allowed.contains(context.state()), candidates);
    }

    /**
     * @param blocks 方块标签查询
     * @param tag 方块标签
     * @return 匹配该标签的 predicate
     */
    public static MultiPredicate tag(HolderGetter<Block> blocks, TagKey<Block> tag) {
        return multi("Tag", BlockStatePredicate.builder().of(blocks, tag).build());
    }

    /**
     * @param kinds 端口种类
     * @return 匹配这些端口种类的 predicate
     */
    public static MultiPredicate port(PortKind... kinds) {
        List<PortKind> allowed = List.of(kinds);
        List<BlockInfo> candidates = new ArrayList<>();
        for (PortKind kind : allowed) {
            for (Block block : PortRegistry.blocks(kind)) {
                candidates.add(new BlockInfo(block.defaultBlockState(), null));
            }
        }
        return MultiPredicate.of(
            "Port",
            context -> {
                PortRegistry.Descriptor descriptor = PortRegistry.descriptor(context.state());
                return descriptor != null && allowed.contains(descriptor.kind());
            },
            candidates
        ).setPriority(10).setPort(true);
    }

    /**
     * @param predicate 方块谓词
     * @param parallels 并行加成
     * @param threads 线程加成
     * @return 带加成的替代方块 predicate
     */
    public static MultiPredicate substitute(BlockStatePredicate predicate, int parallels, int threads) {
        return multi("Substitute", predicate).setBonus(parallels, threads);
    }

    /**
     * @param block 控制器方块
     * @return 控制器 predicate
     */
    public static MultiPredicate controller(Block block) {
        return blocks("Controller", block).setController(true);
    }

    /**
     * @return 恒匹配 predicate
     */
    public static MultiPredicate any() {
        return MultiPredicate.any();
    }

    /**
     * @return 空气 predicate
     */
    public static MultiPredicate air() {
        return MultiPredicate.air();
    }

    private static List<BlockInfo> candidates(BlockStatePredicate predicate) {
        List<BlockInfo> result = new ArrayList<>();
        for (Holder<Block> holder : predicate.getBlocks()) {
            Block block = holder.value();
            if (block != Blocks.AIR) {
                result.add(new BlockInfo(block.defaultBlockState(), null));
            }
        }
        return result;
    }
}
