package com.teameway.bnvilmb.ui.port;

import com.teameway.bnvilmb.multiblock.port.PortKind;
import com.teameway.bnvilmb.multiblock.port.PortTier;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.item.ItemResource;
import org.jspecify.annotations.Nullable;

import java.util.List;

/**
 * 端口界面只读数据
 */
public interface PortUiData {
    Component blockName();

    PortKind kind();

    PortTier tier();

    int slotCount();

    @Nullable
    ResourceHandler<ItemResource> itemHandler();

    @Nullable
    ResourceHandler<FluidResource> fluidHandler();

    long storedEnergy();

    long energyCapacity();

    int provided();

    int maxProvided();

    List<BlockPos> controllers();

    BlockPos pos();
}
