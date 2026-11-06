package com.teameway.bnvilmb.ui.port;

import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Label;
import com.teameway.bnvilmb.multiblock.port.PortKind;
import com.teameway.bnvilmb.ui.UiContext;
import com.teameway.bnvilmb.ui.shell.MainSection;
import net.minecraft.network.chat.Component;

import java.util.EnumMap;
import java.util.Map;

/**
 * 端口 main 注册表
 *
 * <p>新增端口种类时在这里加一行注册，{@code PortUiFactory} 不需要改动。</p>
 */
public final class PortSections {
    private static final Map<PortKind, PortSectionFactory> FACTORIES = new EnumMap<>(PortKind.class);

    static {
        register(PortKind.ITEM_INPUT, ItemBusSection::new);
        register(PortKind.ITEM_OUTPUT, ItemBusSection::new);
        register(PortKind.FLUID_INPUT, FluidHatchSection::new);
        register(PortKind.FLUID_OUTPUT, FluidHatchSection::new);
        register(PortKind.ENERGY_INPUT, EnergyHatchSection::new);
        register(PortKind.ENERGY_OUTPUT, EnergyHatchSection::new);
        register(PortKind.PARALLEL, ControlHatchSection::new);
        register(PortKind.THREAD, ControlHatchSection::new);
    }

    private PortSections() {
    }

    public static void register(PortKind kind, PortSectionFactory factory) {
        FACTORIES.put(kind, factory);
    }

    public static MainSection create(PortUiData data) {
        PortSectionFactory factory = FACTORIES.get(data.kind());
        if (factory != null) {
            return factory.create(data);
        }
        return new MainSection() {
            @Override
            public UIElement create(UiContext context) {
                Label label = new Label();
                label.setText(Component.translatable("gui.bnvil_multiblock.port_panel.unknown"));
                return label;
            }
        };
    }
}
