package com.teameway.bnvilmb.common.multiblock.port.block;

import com.teameway.bnvilmb.common.multiblock.port.PortKind;
import com.teameway.bnvilmb.common.multiblock.port.PortTier;
import com.teameway.bnvilmb.init.multiblock.BnvilPorts;
import dev.anvilcraft.lib.v2.registrum.util.entry.BlockEntityEntry;
import net.minecraft.world.level.block.entity.BlockEntity;

/**
 * 物品端口方块（输入总线 / 输出总线）
 */
public class ItemPortBlock extends AbstractPortBlock {
    public ItemPortBlock(Properties properties, PortKind portKind, PortTier tier) {
        super(properties, portKind, tier);
    }

    @Override
    protected BlockEntityEntry<? extends BlockEntity> blockEntityEntry() {
        return BnvilPorts.ITEM_PORT_TYPE;
    }
}
