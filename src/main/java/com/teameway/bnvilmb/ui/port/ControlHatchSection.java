package com.teameway.bnvilmb.ui.port;

import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.teameway.bnvilmb.multiblock.port.PortKind;
import com.teameway.bnvilmb.ui.UiContext;
import com.teameway.bnvilmb.ui.shell.MainSection;
import com.teameway.bnvilmb.ui.widget.Stepper;

/**
 * 并行仓与线程仓的 main 上半部分
 *
 * <p>只有一条 88×18 的加减框，居中显示；中间的数字可以直接输入。</p>
 */
public final class ControlHatchSection extends MainSection {
    private final PortUiData data;

    public ControlHatchSection(PortUiData data) {
        this.data = data;
    }

    @Override
    public boolean centerContent() {
        return true;
    }

    @Override
    public UIElement create(UiContext context) {
        boolean thread = this.data.kind() == PortKind.THREAD;
        String currentKey = thread
                            ? "gui.bnvil_multiblock.control.current.thread"
                            : "gui.bnvil_multiblock.control.current.parallel";
        return new Stepper(
            this.data::provided,
            this.data.maxProvided(),
            currentKey,
            value -> context.navigation().setControlPort(this.data.pos(), value)
        );
    }
}
