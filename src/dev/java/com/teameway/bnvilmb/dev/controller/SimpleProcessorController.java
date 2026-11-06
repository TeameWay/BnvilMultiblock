package com.teameway.bnvilmb.dev.controller;

import com.teameway.bnvilmb.multiblock.StructureController;
import com.teameway.bnvilmb.multiblock.controller.ControllerBlockEntity;
import com.teameway.bnvilmb.multiblock.pattern.BlockPattern;
import com.teameway.bnvilmb.multiblock.pattern.PatternState;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.block.Block;

import java.util.function.Supplier;

/**
 * 简易多方块机器的控制器。
 *
 * <p>结构模板由代码提供，不再从数据包注册表解析。</p>
 *
 * @param block 控制器方块
 * @param patternSupplier 结构模板提供者
 */
public record SimpleProcessorController(Block block, Supplier<BlockPattern> patternSupplier) implements StructureController {

    /**
     * @return 结构模板
     */
    @Override
    public BlockPattern pattern() {
        return this.patternSupplier.get();
    }

    /**
     * 成型时在控制器上方生成末地烛粒子，并播放信标激活音效
     *
     * @param level 服务端世界
     * @param controller 控制器方块实体
     * @param state 结构状态
     */
    @Override
    public void onFormed(ServerLevel level, ControllerBlockEntity controller, PatternState state) {
        var pos = controller.getBlockPos();
        level.sendParticles(
            ParticleTypes.END_ROD,
            pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5,
            16, 0.4, 0.4, 0.4, 0.02
        );
        level.playSound(null, pos, SoundEvents.BEACON_ACTIVATE, SoundSource.BLOCKS, 1.0F, 1.0F);
    }

    /**
     * 解散时播放信标失效音效
     *
     * @param level 服务端世界
     * @param controller 控制器方块实体
     * @param state 解散前的结构状态
     */
    @Override
    public void onUnformed(ServerLevel level, ControllerBlockEntity controller, PatternState state) {
        level.playSound(null, controller.getBlockPos(), SoundEvents.BEACON_DEACTIVATE, SoundSource.BLOCKS, 1.0F, 1.0F);
    }
}
