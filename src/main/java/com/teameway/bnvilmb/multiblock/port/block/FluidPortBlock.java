package com.teameway.bnvilmb.multiblock.port.block;

import com.teameway.bnvilmb.init.multiblock.BnvilPorts;
import com.teameway.bnvilmb.multiblock.port.PortKind;
import com.teameway.bnvilmb.multiblock.port.PortTier;
import dev.anvilcraft.lib.v2.registrum.util.entry.BlockEntityEntry;
import net.minecraft.world.level.block.entity.BlockEntity;

/**
 * 流体端口方块，对应输入仓与输出仓
 */
public class FluidPortBlock extends AbstractPortBlock {
    public FluidPortBlock(Properties properties, PortKind portKind, PortTier tier) {
        super(properties, portKind, tier);
    }

    @Override
    protected BlockEntityEntry<? extends BlockEntity> blockEntityEntry() {
        return BnvilPorts.FLUID_PORT_TYPE;
    }
}
