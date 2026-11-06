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
 * <p>端口归属写入方块实体的 {@code controller_pos} 字段，区块卸载后仍然保留；重新加载后，
 * 结构匹配与结构管理器仍能据此判断端口是否已被其他控制器认领。</p>
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
