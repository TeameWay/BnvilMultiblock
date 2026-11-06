package com.teameway.bnvilmb.ui;

import com.teameway.bnvilmb.ui.nav.UiNavigation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;

/**
 * 界面构建上下文
 *
 * @param player 打开界面的玩家
 * @param host 界面所属方块实体
 * @param navigation 跨方块界面的跳转入口
 */
public record UiContext(Player player, BlockEntity host, UiNavigation navigation) {
}
