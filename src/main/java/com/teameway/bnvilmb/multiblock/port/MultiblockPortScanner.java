package com.teameway.bnvilmb.multiblock.port;

import com.teameway.bnvilmb.BnvilMultiBlock;
import com.teameway.bnvilmb.multiblock.structure.StructureMatch;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.ArrayList;
import java.util.List;

/**
 * 端口扫描
 *
 * <p>只遍历 {@link StructureMatch#portPositions()} 给出的槽位，在成型与槽位变化时调用，不要每 tick 扫描。</p>
 */
public class MultiblockPortScanner {
    private MultiblockPortScanner() {
    }

    /**
     * 扫描结构中所有端口
     *
     * @param level 服务端世界
     * @param match 结构匹配结果，提供端口位置与替代物加成
     * @return 端口表
     */
    public static MultiblockPortTable scan(ServerLevel level, StructureMatch match) {
        List<MultiblockPort> ports = new ArrayList<>();
        for (BlockPos pos : match.portPositions()) {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity instanceof MultiblockPort port) {
                ports.add(port);
            }
        }
        MultiblockPortTable table = MultiblockPortTable.of(ports, match.parallelsBonus(), match.threadsBonus());
        BnvilMultiBlock.LOGGER.debug(
            "Scanned multiblock at {}: {} port(s), {} thread(s), {} parallel(s)",
            match.controllerPos(), table.portCount(), table.threads(), table.parallels()
        );
        return table;
    }
}
