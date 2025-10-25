package com.flechazo.all_living_beings.utils;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

public class MiningUtil {

    private static final ConcurrentLinkedQueue<MiningTask> miningQueue = new ConcurrentLinkedQueue<>();
    private static final int BLOCKS_PER_TICK = 50; // 每tick处理的方块数量

    public static class MiningTask {
        final ServerLevel level;
        final ServerPlayer player;
        final List<BlockPos> positions;
        final boolean dropItems;
        int currentIndex = 0;

        MiningTask(ServerLevel level, ServerPlayer player, List<BlockPos> positions, boolean dropItems) {
            this.level = level;
            this.player = player;
            this.positions = positions;
            this.dropItems = dropItems;
        }

        boolean processBlocks() {
            int processed = 0;
            while (currentIndex < positions.size() && processed < BLOCKS_PER_TICK) {
                BlockPos pos = positions.get(currentIndex);
                BlockState state = level.getBlockState(pos);

                if (!state.isAir()) {
                    level.destroyBlock(pos, dropItems, player);
                }

                currentIndex++;
                processed++;
            }
            return currentIndex >= positions.size();
        }
    }
    public static void processMiningQueue() {
        // 任务完成，移除
        miningQueue.removeIf(MiningTask::processBlocks);
    }
}
