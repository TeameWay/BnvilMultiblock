package com.teameway.bnvilmb.resource;

import com.teameway.bnvilmb.BnvilMultiBlock;
import net.minecraft.resources.Identifier;

/**
 * 索引使用的资源键
 *
 * <p>物品与流体用具体 ID，tag 在编译时展开，能量用固定键。索引只判断资源是否存在，
 * 数量校验留给匹配阶段。</p>
 *
 * @param kind 资源种类
 * @param id 资源 ID，能量取固定键
 */
public record ResourceKey(ResourceKind kind, Identifier id) {
    /**
     * 能量固定键
     */
    public static final ResourceKey ENERGY = new ResourceKey(ResourceKind.ENERGY, BnvilMultiBlock.id("energy"));
}
