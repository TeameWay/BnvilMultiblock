package com.teameway.bnvilmb.multiblock.controller;

import com.teameway.bnvilmb.multiblock.StructureController;
import com.teameway.bnvilmb.multiblock.structure.StructureMatch;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.block.Block;

/**
 * 简易多方块机器的控制器
 *
 * <p>对应方块 {@code bnvil_multiblock:simple_controller}，结构模板 ID 为
 * {@code bnvil_multiblock:simple_processor}，由 datagen 输出到
 * {@code data/bnvil_multiblock/structure_patterns/simple_processor.json}。</p>
 *
 * @param block 控制器方块
 * @param patternId 结构模板 ID
 */
public record SimpleProcessorController(Block block, Identifier patternId) implements StructureController {

    /**
     * 成型时在控制器上方生成末地烛粒子，并播放信标激活音效
     *
     * @param level 服务端世界
     * @param controller 控制器方块实体
     * @param match 结构匹配结果
     */
    @Override
    public void onFormed(ServerLevel level, ControllerBlockEntity controller, StructureMatch match) {
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
     */
    @Override
    public void onUnformed(ServerLevel level, ControllerBlockEntity controller) {
        level.playSound(null, controller.getBlockPos(), SoundEvents.BEACON_DEACTIVATE, SoundSource.BLOCKS, 1.0F, 1.0F);
    }
}
