package com.teameway.bnvilmb.ui.port;

import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.teameway.bnvilmb.ui.UiContext;
import com.teameway.bnvilmb.ui.shell.MainSection;
import com.teameway.bnvilmb.ui.widget.EnergyGauge;

/**
 * 能源仓的 main 上半部分
 */
public final class EnergyHatchSection extends MainSection {
    private final PortUiData data;

    public EnergyHatchSection(PortUiData data) {
        this.data = data;
    }

    @Override
    public boolean centerContent() {
        return true;
    }

    @Override
    public UIElement create(UiContext context) {
        return new EnergyGauge(this.data::storedEnergy, this.data::energyCapacity);
    }
}
