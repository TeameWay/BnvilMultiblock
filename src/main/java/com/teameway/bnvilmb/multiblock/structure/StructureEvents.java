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
 * <p>所有事件只标脏，不直接重算；控制器方块实体在下一 tick 统一处理，避免破坏事件里
 * 方块还没真正移除导致误判。</p>
 */
@EventBusSubscriber(modid = BnvilMultiBlock.MODID, value = Dist.DEDICATED_SERVER)
public final class StructureEvents {
    private StructureEvents() {
    }

    @SubscribeEvent
    public static void onPlace(BlockEvent.EntityPlaceEvent event) {
        // StructureManager.get((ServerLevel) event.getLevel()).markDirty(event.getPos());
        if (event.getLevel() instanceof ServerLevel level) {
            StructureManager.get(level).markDirty(event.getPos());
        }
    }

    @SubscribeEvent
    public static void onBreak(BreakBlockEvent event) {
        if (event.getLevel() instanceof ServerLevel level) {
            StructureManager.get(level).markDirty(event.getPos());
        }
    }

    @SubscribeEvent
    public static void onNeighborNotify(BlockEvent.NeighborNotifyEvent event) {
        if (event.getLevel() instanceof ServerLevel level) {
            StructureManager.get(level).markDirty(event.getPos());
        }
    }

    @SubscribeEvent
    public static void onChunkLoad(ChunkEvent.Load event) {
        if (event.getLevel() instanceof ServerLevel level) {
            StructureManager.get(level).markDirtyChunk(event.getChunk().getPos());
        }
    }

    @SubscribeEvent
    public static void onChunkUnload(ChunkEvent.Unload event) {
        if (event.getLevel() instanceof ServerLevel level) {
            StructureManager.get(level).markDirtyChunk(event.getChunk().getPos());
        }
    }

    @SubscribeEvent
    public static void onLevelUnload(LevelEvent.Unload event) {
        if (event.getLevel() instanceof ServerLevel level) {
            StructureManager.remove(level);
        }
    }
}
