package com.teameway.bnvilmb.ui.port;

import com.teameway.bnvilmb.multiblock.port.ControlPort;
import com.teameway.bnvilmb.multiblock.port.EnergyPort;
import com.teameway.bnvilmb.multiblock.port.FluidPort;
import com.teameway.bnvilmb.multiblock.port.ItemPort;
import com.teameway.bnvilmb.multiblock.port.MultiblockPort;
import com.teameway.bnvilmb.multiblock.port.PortKind;
import com.teameway.bnvilmb.multiblock.port.PortTier;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.item.ItemResource;
import org.jspecify.annotations.Nullable;

import java.util.List;

/**
 * 端口方块实体的界面适配
 */
public final class PortBlockEntityData implements PortUiData {
    private final MultiblockPort port;
    private final Component blockName;

    public PortBlockEntityData(MultiblockPort port, Component blockName) {
        this.port = port;
        this.blockName = blockName;
    }

    @Override
    public Component blockName() {
        return this.blockName;
    }

    @Override
    public PortKind kind() {
        return this.port.portKind();
    }

    @Override
    public PortTier tier() {
        return this.port.tier();
    }

    @Override
    public int slotCount() {
        ResourceHandler<ItemResource> handler = this.itemHandler();
        return handler == null ? this.port.tier().slots() : handler.size();
    }

    @Override
    public @Nullable ResourceHandler<ItemResource> itemHandler() {
        return this.port instanceof ItemPort item ? item.itemHandler() : null;
    }

    @Override
    public @Nullable ResourceHandler<FluidResource> fluidHandler() {
        return this.port instanceof FluidPort fluid ? fluid.fluidHandler() : null;
    }

    @Override
    public long storedEnergy() {
        return this.port instanceof EnergyPort energy ? energy.storedEnergy() : 0L;
    }

    @Override
    public long energyCapacity() {
        return this.port.tier().energyCapacity();
    }

    @Override
    public int provided() {
        return this.port instanceof ControlPort control ? control.provided() : 0;
    }

    @Override
    public int maxProvided() {
        return this.port instanceof ControlPort control ? control.maxProvided() : 0;
    }

    @Override
    public List<BlockPos> controllers() {
        List<BlockPos> synced = this.port.visibleControllers();
        // 服务端以真实归属集合为准；客户端只能用同步字段
        return synced.isEmpty() ? List.copyOf(this.port.controllerPositions()) : synced;
    }

    @Override
    public BlockPos pos() {
        return this.port instanceof BlockEntity entity ? entity.getBlockPos() : BlockPos.ZERO;
    }
}
