package com.teameway.bnvilmb.ui.controller;

import com.teameway.bnvilmb.multiblock.controller.ControllerBlockEntity;
import com.teameway.bnvilmb.multiblock.port.PortRef;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

/**
 * 控制器方块实体的界面适配
 */
public final class ControllerBlockEntityData implements ControllerUiData {
    private final ControllerBlockEntity controller;

    public ControllerBlockEntityData(ControllerBlockEntity controller) {
        this.controller = controller;
    }

    @Override
    public Component blockName() {
        return this.controller.getBlockState().getBlock().getName();
    }

    @Override
    public boolean formed() {
        return !this.controller.machineType().isEmpty();
    }

    @Override
    public boolean working() {
        for (int slot = 0; slot < this.controller.threadSlots(); slot++) {
            if (!this.controller.recipeIdAt(slot).isEmpty()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String structureError() {
        return this.controller.structureError();
    }

    @Override
    public Component stateText() {
        if (!this.formed()) {
            String error = this.structureError();
            return error.isEmpty()
                   ? Component.translatable("gui.bnvil_multiblock.controller.state.unformed")
                   : Component.literal(error);
        }
        return Component.translatable(
            this.working()
            ? "gui.bnvil_multiblock.controller.state.working"
            : "gui.bnvil_multiblock.controller.state.idle"
        );
    }

    @Override
    public Component variantsText() {
        String raw = this.controller.variantText();
        if (raw.isEmpty()) {
            return Component.translatable("gui.bnvil_multiblock.controller.info.variants", "-");
        }
        StringBuilder text = new StringBuilder();
        for (String part : raw.split(", ")) {
            if (!text.isEmpty()) {
                text.append(", ");
            }
            text.append(path(part));
        }
        return Component.translatable("gui.bnvil_multiblock.controller.info.variants", text.toString());
    }

    @Override
    public int parallelSlots() {
        return this.controller.parallelSlots();
    }

    @Override
    public int threadSlots() {
        return this.controller.threadSlots();
    }

    @Override
    public int threadCount() {
        return this.controller.threadSlots();
    }

    @Override
    public float progressAt(int slot) {
        return this.controller.progressAt(slot);
    }

    @Override
    public int batchAt(int slot) {
        return this.controller.batchAt(slot);
    }

    @Override
    public int durationAt(int slot) {
        return this.controller.durationAt(slot);
    }

    @Override
    public String recipeIdAt(int slot) {
        return this.controller.recipeIdAt(slot);
    }

    @Override
    public String machineType() {
        return this.controller.machineType();
    }

    @Override
    public int portCount() {
        return this.controller.portCount();
    }

    @Override
    public PortRef portRefAt(int index) {
        return this.controller.portRefAt(index);
    }

    @Override
    public BlockPos pos() {
        return this.controller.getBlockPos();
    }

    private static String path(String id) {
        Identifier parsed = Identifier.tryParse(id);
        return parsed == null ? id : parsed.getPath();
    }
}
