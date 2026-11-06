package com.teameway.bnvilmb.dev.port.block;

import com.teameway.bnvilmb.dev.init.multiblock.BnvilPorts;
import com.teameway.bnvilmb.multiblock.port.PortKind;
import com.teameway.bnvilmb.multiblock.port.PortTier;
import com.teameway.bnvilmb.multiblock.port.block.AbstractPortBlock;
import dev.anvilcraft.lib.v2.registrum.util.entry.BlockEntityEntry;
import net.minecraft.world.level.block.entity.BlockEntity;

/**
 * 控制端口方块，对应并行仓与线程仓
 *
 * <p>不承载物品、流体或能量，只在端口表里贡献线程与并行增量。</p>
 */
public class ControlPortBlock extends AbstractPortBlock {
    public ControlPortBlock(Properties properties, PortKind portKind, PortTier tier) {
        super(properties, portKind, tier);
    }

    @Override
    protected BlockEntityEntry<? extends BlockEntity> blockEntityEntry() {
        return BnvilPorts.CONTROL_PORT_TYPE;
    }
}
