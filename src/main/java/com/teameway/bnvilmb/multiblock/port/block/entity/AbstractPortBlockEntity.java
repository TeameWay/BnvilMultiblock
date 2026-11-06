package com.teameway.bnvilmb.multiblock.port.block.entity;

import com.lowdragmc.lowdraglib2.syncdata.annotation.DescSynced;
import com.lowdragmc.lowdraglib2.syncdata.holder.blockentity.ISyncPersistRPCBlockEntity;
import com.lowdragmc.lowdraglib2.syncdata.storage.FieldManagedStorage;
import com.lowdragmc.lowdraglib2.syncdata.storage.IManagedStorage;
import com.teameway.bnvilmb.multiblock.port.MultiblockPort;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * 端口方块实体基类。
 *
 * <p>端口可以共享，因此 NBT 保存控制器位置列表；旧存档的单个 {@code controller_pos}
 * 会在读取时迁移成只含一个元素的集合。控制器位置同时写进 {@link DescSynced} 字段，
 * 客户端界面才能列出端口当前链接的控制器。</p>
 */
public abstract class AbstractPortBlockEntity extends BlockEntity implements MultiblockPort, ISyncPersistRPCBlockEntity {
    private final Set<BlockPos> controllerPositions = new LinkedHashSet<>();
    private final FieldManagedStorage syncStorage = new FieldManagedStorage(this);

    @DescSynced
    private String[] controllerList = new String[0];

    protected AbstractPortBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public IManagedStorage getSyncStorage() {
        return this.syncStorage;
    }

    @Override
    public Set<BlockPos> controllerPositions() {
        return Set.copyOf(this.controllerPositions);
    }

    @Override
    public List<BlockPos> visibleControllers() {
        List<BlockPos> result = new ArrayList<>(this.controllerList.length);
        for (String raw : this.controllerList) {
            String[] parts = raw.split(",");
            if (parts.length != 3) {
                continue;
            }
            try {
                result.add(new BlockPos(
                    Integer.parseInt(parts[0]),
                    Integer.parseInt(parts[1]),
                    Integer.parseInt(parts[2])
                ));
            } catch (NumberFormatException ignored) {
                // 同步字段损坏时跳过该条，界面仍然可用
            }
        }
        return List.copyOf(result);
    }

    @Override
    public void addController(BlockPos controllerPos) {
        if (this.controllerPositions.add(controllerPos.immutable())) {
            this.refreshControllerList();
        }
    }

    @Override
    public void removeController(BlockPos controllerPos) {
        if (this.controllerPositions.remove(controllerPos)) {
            this.refreshControllerList();
        }
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.controllerPositions.clear();
        for (BlockPos pos : input.listOrEmpty("controllers", BlockPos.CODEC)) {
            this.controllerPositions.add(pos.immutable());
        }
        if (this.controllerPositions.isEmpty()) {
            input.read("controller_pos", BlockPos.CODEC).ifPresent(pos -> this.controllerPositions.add(pos.immutable()));
        }
        this.rebuildControllerList();
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        ValueOutput.TypedOutputList<BlockPos> list = output.list("controllers", BlockPos.CODEC);
        for (BlockPos pos : this.controllerPositions) {
            list.add(pos);
        }
    }

    /**
     * 重建同步用的控制器坐标数组
     *
     * <p>只在内容变化时写字段并同步，避免每次加载都触发一次网络包。</p>
     */
    private void refreshControllerList() {
        String[] previous = this.controllerList;
        this.rebuildControllerList();
        this.setChanged();
        if (!Arrays.equals(previous, this.controllerList)) {
            this.markDirty("controllerList");
            this.sync(true);
        }
    }

    private void rebuildControllerList() {
        String[] next = new String[this.controllerPositions.size()];
        int index = 0;
        for (BlockPos pos : this.controllerPositions) {
            next[index++] = pos.getX() + "," + pos.getY() + "," + pos.getZ();
        }
        this.controllerList = next;
    }
}
