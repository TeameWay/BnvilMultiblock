package com.teameway.bnvilmb.ui;

import com.lowdragmc.lowdraglib2.gui.factory.BlockUIMenuType;
import com.lowdragmc.lowdraglib2.gui.ui.ModularUI;
import com.teameway.bnvilmb.multiblock.controller.ControllerBlockEntity;
import com.teameway.bnvilmb.multiblock.port.MultiblockPort;
import com.teameway.bnvilmb.ui.controller.ControllerUiFactory;
import com.teameway.bnvilmb.ui.port.PortUiFactory;
import net.minecraft.world.entity.player.Player;

/**
 * 界面统一入口
 *
 * <p>方块类只调用这里，不直接依赖具体界面实现。</p>
 */
public final class BnvilUi {
    private BnvilUi() {
    }

    public static ModularUI controller(ControllerBlockEntity controller, Player player) {
        return ControllerUiFactory.create(controller, player);
    }

    public static ModularUI port(MultiblockPort port, BlockUIMenuType.BlockUIHolder holder) {
        return PortUiFactory.create(port, holder);
    }
}
