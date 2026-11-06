package com.teameway.bnvilmb.multiblock.port;

import net.minecraft.core.BlockPos;

/**
 * 界面使用的端口摘要
 *
 * <p>控制器同步字段里存的是字符串，界面解析一次后转成这个记录，避免卡片反复拆分文本。</p>
 *
 * @param kind 端口种类
 * @param tier 等级名
 * @param pos 端口坐标
 */
public record PortRef(PortKind kind, String tier, BlockPos pos) {
}
