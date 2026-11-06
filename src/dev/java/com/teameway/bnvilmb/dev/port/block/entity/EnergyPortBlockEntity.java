package com.teameway.bnvilmb.dev.port.block.entity;

import com.lowdragmc.lowdraglib2.syncdata.annotation.DescSynced;
import com.lowdragmc.lowdraglib2.syncdata.holder.blockentity.ISyncPersistRPCBlockEntity;
import com.lowdragmc.lowdraglib2.syncdata.storage.FieldManagedStorage;
import com.lowdragmc.lowdraglib2.syncdata.storage.IManagedStorage;
import com.teameway.bnvilmb.multiblock.port.EnergyPort;
import com.teameway.bnvilmb.multiblock.port.PortKind;
import com.teameway.bnvilmb.multiblock.port.PortTier;
import com.teameway.bnvilmb.multiblock.port.block.AbstractPortBlock;
import com.teameway.bnvilmb.multiblock.port.block.entity.AbstractPortBlockEntity;
import com.teameway.bnvilmb.resource.ingredient.LongEnergyHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;

/**
 * 能量端口方块实体
 *
 * <p>服务端每 {@value #SAMPLE_INTERVAL} tick 把 {@link LongEnergyHandler} 的当前储量写入
 * {@link DescSynced} 字段，再同步到客户端；容量由等级决定。</p>
 */
public class EnergyPortBlockEntity extends AbstractPortBlockEntity implements EnergyPort, ISyncPersistRPCBlockEntity {
    private static final int SAMPLE_INTERVAL = 10;

    private final FieldManagedStorage syncStorage = new FieldManagedStorage(this);
    private final PortTier tier;
    private final LongEnergyHandler handler;
    private int sampleCounter;

    @DescSynced
    private long energyStored;

    public EnergyPortBlockEntity(BlockEntityType<EnergyPortBlockEntity> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        this.tier = ((AbstractPortBlock) state.getBlock()).tier();
        this.handler = new LongEnergyHandler(this.tier.energyCapacity());
    }

    @Override
    public IManagedStorage getSyncStorage() {
        return this.syncStorage;
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
    public EnergyHandler energyHandler() {
        return this.handler;
    }

    /**
     * 服务端每 tick 由方块 ticker 调用；累计到 {@value #SAMPLE_INTERVAL} tick 时采样一次储量
     */
    public void serverTick() {
        if (++this.sampleCounter < SAMPLE_INTERVAL) {
            return;
        }
        this.sampleCounter = 0;
        this.energyStored = this.handler.getAmountAsLong();
    }

    /**
     * @return 上一次采样得到的储量
     */
    @Override
    public long storedEnergy() {
        return this.energyStored;
    }

    /**
     * @return 等级规定的能量容量（FE）
     */
    public long energyCapacity() {
        return this.tier.energyCapacity();
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.handler.deserialize(input.childOrEmpty("handler"));
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        this.handler.serialize(output.child("handler"));
    }
}
