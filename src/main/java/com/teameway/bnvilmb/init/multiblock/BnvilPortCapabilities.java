package com.teameway.bnvilmb.init.multiblock;

import com.teameway.bnvilmb.BnvilMultiBlock;
import com.teameway.bnvilmb.common.multiblock.port.block.entity.EnergyPortBlockEntity;
import com.teameway.bnvilmb.common.multiblock.port.block.entity.FluidPortBlockEntity;
import com.teameway.bnvilmb.common.multiblock.port.block.entity.ItemPortBlockEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

/**
 * 端口能力注册
 *
 * <p>对外暴露的处理器与控制器内部是同一个实例，两侧共享库存。</p>
 */
@EventBusSubscriber(modid = BnvilMultiBlock.MODID)
public class BnvilPortCapabilities {

    /**
     * 注册物品/流体/能量能力
     */
    @SubscribeEvent
    public static void onRegisterCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlock(
            Capabilities.Item.BLOCK,
            (_, _, _, blockEntity, _) ->
                blockEntity instanceof ItemPortBlockEntity port ? port.itemHandler() : null,
            BnvilPorts.ITEM_INPUT.get(),
            BnvilPorts.ITEM_OUTPUT.get()
        );
        event.registerBlock(
            Capabilities.Fluid.BLOCK,
            (_, _, _, blockEntity, _) ->
                blockEntity instanceof FluidPortBlockEntity port ? port.fluidHandler() : null,
            BnvilPorts.FLUID_INPUT.get(),
            BnvilPorts.FLUID_OUTPUT.get()
        );
        event.registerBlock(
            Capabilities.Energy.BLOCK,
            (_, _, _, blockEntity, _) ->
                blockEntity instanceof EnergyPortBlockEntity port ? port.energyHandler() : null,
            BnvilPorts.ENERGY_INPUT.get(),
            BnvilPorts.ENERGY_OUTPUT.get()
        );
        BnvilMultiBlock.LOGGER.debug("Registered multiblock port capabilities");
    }
}