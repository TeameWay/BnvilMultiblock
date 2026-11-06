package com.teameway.bnvilmb.multiblock.structure;

import com.teameway.bnvilmb.BnvilMultiBlock;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.level.ChunkEvent;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.neoforged.neoforge.event.level.block.BreakBlockEvent;

/**
 * 结构脏标记事件
 *
 * <p>订阅器只注册在专用服务端。这里只标脏，不直接重算：控制器方块实体在下一 tick 调用
 * {@code StructureManager#tick(StructureInstance)} 统一处理，避免在破坏事件里读到尚未移除的方块。</p>
 */
@EventBusSubscriber(modid = BnvilMultiBlock.MODID, value = Dist.DEDICATED_SERVER)
public final class StructureEvents {
    private StructureEvents() {
    }

    /**
     * 实体放置方块后标脏
     *
     * @param event 放置事件
     */
    @SubscribeEvent
    public static void onPlace(BlockEvent.EntityPlaceEvent event) {
        if (event.getLevel() instanceof ServerLevel level) {
            StructureManager.get(level).markDirty(event.getPos());
        }
    }

    /**
     * 方块被破坏后标脏
     *
     * @param event 破坏事件
     */
    @SubscribeEvent
    public static void onBreak(BreakBlockEvent event) {
        if (event.getLevel() instanceof ServerLevel level) {
            StructureManager.get(level).markDirty(event.getPos());
        }
    }

    /**
     * 收到邻居更新后标脏
     *
     * @param event 邻居更新事件
     */
    @SubscribeEvent
    public static void onNeighborNotify(BlockEvent.NeighborNotifyEvent event) {
        if (event.getLevel() instanceof ServerLevel level) {
            StructureManager.get(level).markDirty(event.getPos());
        }
    }

    /**
     * 区块加载时标脏覆盖该区块的控制器
     *
     * @param event 区块加载事件
     */
    @SubscribeEvent
    public static void onChunkLoad(ChunkEvent.Load event) {
        if (event.getLevel() instanceof ServerLevel level) {
            StructureManager.get(level).markDirtyChunk(event.getChunk().getPos());
        }
    }

    /**
     * 区块卸载时标脏覆盖该区块的控制器
     *
     * @param event 区块卸载事件
     */
    @SubscribeEvent
    public static void onChunkUnload(ChunkEvent.Unload event) {
        if (event.getLevel() instanceof ServerLevel level) {
            StructureManager.get(level).markDirtyChunk(event.getChunk().getPos());
        }
    }

    /**
     * 世界卸载时移除该世界的管理器
     *
     * @param event 世界卸载事件
     */
    @SubscribeEvent
    public static void onLevelUnload(LevelEvent.Unload event) {
        if (event.getLevel() instanceof ServerLevel level) {
            StructureManager.remove(level);
        }
    }
}
