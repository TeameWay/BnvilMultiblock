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
 * <p>方块为 {@code bnvil_multiblock:simple_controller}，结构定义 ID 为 {@code bnvil_multiblock:simple_processor}，
 * 由 datagen 输出到 {@code data/bnvil_multiblock/structure_patterns/simple_processor.json}。</p>
 */
public record SimpleProcessorController(Block block, Identifier patternId) implements StructureController {

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

    @Override
    public void onUnformed(ServerLevel level, ControllerBlockEntity controller) {
        level.playSound(null, controller.getBlockPos(), SoundEvents.BEACON_DEACTIVATE, SoundSource.BLOCKS, 1.0F, 1.0F);
    }
}
