package com.teameway.bnvilmb.ui.controller;

import com.teameway.bnvilmb.multiblock.port.PortRef;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;

/**
 * 控制器界面只读数据
 *
 * <p>界面不直接持有 {@code ControllerBlockEntity}，服务端与客户端同步字段都通过这层读取。</p>
 */
public interface ControllerUiData {
    Component blockName();

    boolean formed();

    boolean working();

    String structureError();

    Component stateText();

    Component variantsText();

    int parallelSlots();

    int threadSlots();

    int threadCount();

    float progressAt(int slot);

    int batchAt(int slot);

    int durationAt(int slot);

    String recipeIdAt(int slot);

    String machineType();

    int portCount();

    PortRef portRefAt(int index);

    BlockPos pos();
}
