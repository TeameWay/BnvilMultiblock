package com.teameway.bnvilmb.event;

import com.teameway.bnvilmb.BnvilMultiBlock;
import com.teameway.bnvilmb.init.BnvilRecipeTypes;
import com.teameway.bnvilmb.recipe.MultiblockRecipeIndex;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.OnDatapackSyncEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;

/**
 * 服务端配方索引维护
 *
 * <p>服务端启动时建一次；每次数据包同步（玩家加入、{@code /reload}）时重建，并请求把多方块配方类型
 * 同步给客户端，否则客户端拿不到这些配方。</p>
 */
@EventBusSubscriber(modid = BnvilMultiBlock.MODID)
public class MultiblockRecipeIndexEvents {
    private MultiblockRecipeIndexEvents() {
    }

    /**
     * 服务端启动后构建索引
     *
     * @param event 服务端启动事件
     */
    @SubscribeEvent
    public static void onServerStarted(ServerStartedEvent event) {
        MultiblockRecipeIndex.rebuild(event.getServer().getRecipeManager().recipeMap(), "server started");
    }

    /**
     * 数据包同步时重建索引，并请求同步多方块配方类型
     *
     * @param event 数据包同步事件
     */
    @SubscribeEvent
    public static void onDatapackSync(OnDatapackSyncEvent event) {
        event.sendRecipes(BnvilRecipeTypes.MULTIBLOCK.get());
        MultiblockRecipeIndex.rebuild(event.getPlayerList().getServer().getRecipeManager().recipeMap(), "datapack sync");
    }
}
