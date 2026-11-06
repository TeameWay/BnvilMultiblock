package com.teameway.bnvilmb.ui.button;

import com.teameway.bnvilmb.multiblock.controller.ControllerBlockEntity;
import com.teameway.bnvilmb.multiblock.port.MultiblockPort;
import com.teameway.bnvilmb.multiblock.port.PortRef;
import com.teameway.bnvilmb.ui.UiContext;
import com.teameway.bnvilmb.ui.controller.ControllerBlockEntityData;
import com.teameway.bnvilmb.ui.controller.ControllerUiData;
import com.teameway.bnvilmb.ui.panel.FloatingPanel;
import com.teameway.bnvilmb.ui.panel.PortListPanel;
import com.teameway.bnvilmb.ui.port.PortBlockEntityData;
import com.teameway.bnvilmb.ui.port.PortUiData;
import com.teameway.bnvilmb.ui.widget.PortCard;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 端口按钮
 *
 * <p>控制器界面列出所有端口，端口界面列出链接的控制器。两种列表都支持 shift 点击跳转。</p>
 */
public final class PortsButton extends SideButton {
    public PortsButton() {
        super("ports", PRIORITY_PORTS, "P");
        style(style -> style.tooltips(Component.translatable("gui.bnvil_multiblock.button.ports")));
    }

    @Override
    public boolean isAvailable(UiContext context) {
        return true;
    }

    @Override
    protected FloatingPanel createPanel(UiContext context) {
        if (context.host() instanceof ControllerBlockEntity controller) {
            ControllerUiData data = new ControllerBlockEntityData(controller);
            return new PortListPanel(
                "ports",
                Component.translatable("gui.bnvil_multiblock.panel.ports"),
                () -> portEntries(data),
                context.navigation()
            );
        }
        if (context.host() instanceof MultiblockPort port) {
            Component name = context.host().getBlockState().getBlock().getName();
            PortUiData data = new PortBlockEntityData(port, name);
            return new PortListPanel(
                "controllers",
                Component.translatable("gui.bnvil_multiblock.panel.controllers"),
                () -> controllerEntries(data),
                context.navigation()
            );
        }
        return new PortListPanel(
            "ports",
            Component.translatable("gui.bnvil_multiblock.panel.ports"),
            List::of,
            context.navigation()
        );
    }

    private static List<PortCard.Entry> portEntries(ControllerUiData data) {
        List<PortCard.Entry> entries = new ArrayList<>();
        for (int index = 0; index < data.portCount(); index++) {
            PortRef ref = data.portRefAt(index);
            entries.add(new PortCard.Entry(
                Component.translatable("gui.bnvil_multiblock.port_kind." + ref.kind().getSerializedName()),
                Component.literal(ref.pos().getX() + "," + ref.pos().getY() + "," + ref.pos().getZ()),
                ref.pos(),
                List.of(
                    Component.translatable("gui.bnvil_multiblock.port_panel.tier", ref.tier()),
                    Component.translatable("gui.bnvil_multiblock.port_panel.tooltip")
                )
            ));
        }
        return entries;
    }

    private static List<PortCard.Entry> controllerEntries(PortUiData data) {
        List<PortCard.Entry> entries = new ArrayList<>();
        for (BlockPos pos : data.controllers()) {
            entries.add(new PortCard.Entry(
                Component.translatable("gui.bnvil_multiblock.panel.controller"),
                Component.literal(pos.getX() + "," + pos.getY() + "," + pos.getZ()),
                pos,
                List.of(Component.translatable("gui.bnvil_multiblock.panel.controller.tooltip"))
            ));
        }
        return entries;
    }
}
