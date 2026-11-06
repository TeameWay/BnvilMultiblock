package com.teameway.bnvilmb.multiblock.port;

import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.fluid.FluidResource;

/**
 * 流体端口
 *
 * <p>方块实体应把同一个 {@link ResourceHandler} 注册到 {@code Capabilities.Fluid.BLOCK}；
 * 端口表按种类收集这些处理器。</p>
 */
public interface FluidPort extends MultiblockPort {
    /**
     * @return 流体处理器
     */
    ResourceHandler<FluidResource> fluidHandler();
}
