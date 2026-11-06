package com.teameway.bnvilmb.dev.init.multiblock;

import com.teameway.bnvilmb.BnvilMultiBlock;
import com.teameway.bnvilmb.dev.port.block.entity.EnergyPortBlockEntity;
import com.teameway.bnvilmb.dev.port.block.entity.FluidPortBlockEntity;
import com.teameway.bnvilmb.dev.port.block.entity.ItemPortBlockEntity;
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
     * 注册物品、流体与能量能力
     *
     * <p>输入与输出端口各注册一份，方块实体类型对不上时返回 {@code null}，该方块不暴露这项能力。</p>
     *
     * @param event 能力注册事件
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