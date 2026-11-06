package com.teameway.bnvilmb.multiblock.structure;

import com.teameway.bnvilmb.BnvilMultiBlock;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.ChunkEvent;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

/**
 * 结构生命周期事件。
 *
 * <p>方块状态变化由 {@code LevelMixin} 捕获并只标脏；这里负责服务端 tick 处理待检查队列，
 * 以及区块加载/卸载和世界卸载。</p>
 */
@EventBusSubscriber(modid = BnvilMultiBlock.MODID)
public final class StructureEvents {
    private StructureEvents() {
    }

    @SubscribeEvent
    public static void onLevelTick(LevelTickEvent.Post event) {
        if (event.getLevel() instanceof ServerLevel level) {
            StructureManager.get(level).tick(level);
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
            StructureManager.get(level).invalidateChunk(event.getChunk().getPos());
        }
    }

    @SubscribeEvent
    public static void onLevelUnload(LevelEvent.Unload event) {
        if (event.getLevel() instanceof ServerLevel level) {
            StructureManager.remove(level);
        }
    }
}
