package com.teameway.bnvilmb.mixin;

import com.teameway.bnvilmb.multiblock.structure.StructureManager;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 在方块状态真正写入后把结构标脏。
 *
 * <p>对应 GTM 的 {@code LevelMixin}：注入 {@code markAndNotifyBlock} 中的
 * {@code setBlocksDirty} 调用，只处理服务端，并且只做索引查询和标脏，不在方块更新中途做判定。</p>
 */
@Mixin(Level.class)
public abstract class LevelMixin {
    @Inject(
        method = "markAndNotifyBlock",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/level/Level;setBlocksDirty("
                     + "Lnet/minecraft/core/BlockPos;"
                     + "Lnet/minecraft/world/level/block/state/BlockState;"
                     + "Lnet/minecraft/world/level/block/state/BlockState;"
                     + ")V",
            remap = true
        ),
        remap = false
    )
    private void bnvil$markStructureDirty(
        BlockPos pos,
        LevelChunk chunk,
        BlockState oldState,
        BlockState blockState,
        int updateFlags,
        int updateLimit,
        CallbackInfo callback
    ) {
        if ((Object) this instanceof ServerLevel serverLevel) {
            StructureManager.get(serverLevel).markDirty(pos);
        }
    }
}
