package com.teameway.bnvilmb.multiblock.port.block.entity;

import com.teameway.bnvilmb.multiblock.port.MultiblockPort;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;

/**
 * 端口方块实体基类
 *
 * <p>端口归属写进方块实体 NBT，区块卸载后仍然保留，避免两台控制器在重新加载时争用同一个端口。</p>
 */
public abstract class AbstractPortBlockEntity extends BlockEntity implements MultiblockPort {
    private @Nullable BlockPos controllerPos;

    protected AbstractPortBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public @Nullable BlockPos controllerPos() {
        return this.controllerPos;
    }

    @Override
    public void setControllerPos(@Nullable BlockPos controllerPos) {
        if (java.util.Objects.equals(this.controllerPos, controllerPos)) {
            return;
        }
        this.controllerPos = controllerPos;
        this.setChanged();
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.controllerPos = input.read("controller_pos", BlockPos.CODEC).orElse(null);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.storeNullable("controller_pos", BlockPos.CODEC, this.controllerPos);
    }
}
