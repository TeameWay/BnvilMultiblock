package com.teameway.bnvilmb.dev.port.block;

import com.teameway.bnvilmb.dev.init.multiblock.BnvilPorts;
import com.teameway.bnvilmb.dev.port.block.entity.EnergyPortBlockEntity;
import com.teameway.bnvilmb.multiblock.port.PortKind;
import com.teameway.bnvilmb.multiblock.port.PortTier;
import com.teameway.bnvilmb.multiblock.port.block.AbstractPortBlock;
import dev.anvilcraft.lib.v2.registrum.util.entry.BlockEntityEntry;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

/**
 * 能量端口方块，对应能量输入与能量输出
 */
public class EnergyPortBlock extends AbstractPortBlock {
    public EnergyPortBlock(Properties properties, PortKind portKind, PortTier tier) {
        super(properties, portKind, tier);
    }

    @Override
    protected BlockEntityEntry<? extends BlockEntity> blockEntityEntry() {
        return BnvilPorts.ENERGY_PORT_TYPE;
    }

    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(
        Level level,
        BlockState state,
        BlockEntityType<T> type
    ) {
        if (level.isClientSide()) {
            return null;
        }
        return (tickLevel, pos, tickState, blockEntity) -> {
            if (tickLevel instanceof ServerLevel && blockEntity instanceof EnergyPortBlockEntity energy) {
                energy.serverTick();
            }
        };
    }
}
