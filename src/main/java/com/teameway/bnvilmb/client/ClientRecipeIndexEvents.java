package com.teameway.bnvilmb.client;

import com.teameway.bnvilmb.BnvilMultiBlock;
import com.teameway.bnvilmb.recipe.MultiblockRecipeIndex;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RecipesReceivedEvent;

/**
 * 客户端配方索引维护
 *
 * <p>只挂 {@code Dist.CLIENT}；服务端同步的配方到达后重建 {@link MultiblockRecipeIndex}，
 * 客户端侧的配方查询随后读这份新索引。</p>
 */
@EventBusSubscriber(modid = BnvilMultiBlock.MODID, value = Dist.CLIENT)
public class ClientRecipeIndexEvents {
    private ClientRecipeIndexEvents() {
    }

    /**
     * 收到服务端同步的配方后重建索引
     *
     * <p>触发原因固定传 {@code client received}，只用于日志。</p>
     *
     * @param event 客户端配方接收事件
     */
    @SubscribeEvent
    public static void onRecipesReceived(RecipesReceivedEvent event) {
        MultiblockRecipeIndex.rebuild(event.getRecipeMap(), "client received");
    }
}
