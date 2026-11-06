package com.teameway.bnvilmb.dev.port.block.entity;

import com.lowdragmc.lowdraglib2.syncdata.annotation.DescSynced;
import com.teameway.bnvilmb.multiblock.controller.ControllerBlockEntity;
import com.teameway.bnvilmb.multiblock.port.ControlPort;
import com.teameway.bnvilmb.multiblock.port.PortKind;
import com.teameway.bnvilmb.multiblock.port.PortTier;
import com.teameway.bnvilmb.multiblock.port.block.AbstractPortBlock;
import com.teameway.bnvilmb.multiblock.port.block.entity.AbstractPortBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/**
 * 控制端口方块实体，对应并行仓与线程仓
 *
 * <p>等级决定上限，实际提供量可以在 0 到上限之间调整。提供量随方块实体存档并同步到客户端，
 * 变化后通知所有链接的控制器重建端口表，让并行与线程槽位立刻跟着变。</p>
 */
public class ControlPortBlockEntity extends AbstractPortBlockEntity implements ControlPort {
    private final PortTier tier;

    @DescSynced
    private int provided;

    public ControlPortBlockEntity(BlockEntityType<ControlPortBlockEntity> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        this.tier = ((AbstractPortBlock) state.getBlock()).tier();
        this.provided = this.maxProvided();
    }

    @Override
    public PortKind portKind() {
        return ((AbstractPortBlock) this.getBlockState().getBlock()).portKind();
    }

    @Override
    public PortTier tier() {
        return this.tier;
    }

    @Override
    public int provided() {
        return this.provided;
    }

    @Override
    public int maxProvided() {
        return this.portKind() == PortKind.THREAD ? this.tier.threads() : this.tier.parallels();
    }

    @Override
    public void setProvided(int value) {
        int next = Mth.clamp(value, 0, this.maxProvided());
        if (next == this.provided) {
            return;
        }
        this.provided = next;
        this.setChanged();
        this.sync(true);
        this.notifyControllers();
    }

    @Override
    public int threadsDelta() {
        return this.portKind() == PortKind.THREAD ? this.provided : 0;
    }

    @Override
    public int parallelsDelta() {
        return this.portKind() == PortKind.PARALLEL ? this.provided : 0;
    }

    private void notifyControllers() {
        if (!(this.level instanceof ServerLevel serverLevel)) {
            return;
        }
        for (BlockPos pos : this.controllerPositions()) {
            if (serverLevel.getBlockEntity(pos) instanceof ControllerBlockEntity controller) {
                controller.onPortSettingsChanged();
            }
        }
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.provided = Mth.clamp(input.getIntOr("provided", this.maxProvided()), 0, this.maxProvided());
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putInt("provided", this.provided);
    }
}
