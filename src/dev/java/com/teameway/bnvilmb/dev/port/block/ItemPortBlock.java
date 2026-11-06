package com.teameway.bnvilmb.dev.port.block;

import com.teameway.bnvilmb.dev.init.multiblock.BnvilPorts;
import com.teameway.bnvilmb.multiblock.port.PortKind;
import com.teameway.bnvilmb.multiblock.port.PortTier;
import com.teameway.bnvilmb.multiblock.port.block.AbstractPortBlock;
import dev.anvilcraft.lib.v2.registrum.util.entry.BlockEntityEntry;
import net.minecraft.world.level.block.entity.BlockEntity;

/**
 * 物品端口方块，对应输入总线与输出总线
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
