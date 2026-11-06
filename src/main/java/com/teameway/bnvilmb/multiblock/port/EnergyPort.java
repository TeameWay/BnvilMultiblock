package com.teameway.bnvilmb.multiblock.port;

import net.neoforged.neoforge.transfer.energy.EnergyHandler;

/**
 * 能量端口
 *
 * <p>方块实体应把同一个 {@link EnergyHandler} 注册到 {@code Capabilities.Energy.BLOCK}；
 * 端口表按种类收集这些处理器。</p>
 */
public interface EnergyPort extends MultiblockPort {
    /**
     * @return 能量处理器
     */
    EnergyHandler energyHandler();

    /**
     * 返回界面显示用的储量。
     *
     * <p>服务端与处理器是同一份数据；客户端处理器不保证同步，具体实现可覆盖此方法返回同步字段。</p>
     *
     * @return 当前储量
     */
    default long storedEnergy() {
        return this.energyHandler().getAmountAsLong();
    }
}
