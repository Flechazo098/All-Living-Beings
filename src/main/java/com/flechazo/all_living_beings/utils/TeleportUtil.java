package com.flechazo.all_living_beings.utils;

import com.flechazo.all_living_beings.data.ALBSavedData;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.portal.PortalInfo;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.ITeleporter;

import java.util.Collections;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

public final class TeleportUtil {
    private static final Set<UUID> TELEPORT_ALLOW = Collections.newSetFromMap(new ConcurrentHashMap<>());

    private TeleportUtil() {
    }

    public static void grantNextTeleport(ServerPlayer p) {
        TELEPORT_ALLOW.add(p.getUUID());
    }

    public static boolean consumeTeleportAllowance(ServerPlayer p) {
        return TELEPORT_ALLOW.remove(p.getUUID());
    }

    public static BlockPos findWorldSpawn(ServerLevel level) {
        BlockPos spawn = level.getSharedSpawnPos();
        return findSafeSpawn(level, spawn);
    }

    public static BlockPos findOrCreateEndPlatform(ServerLevel level) {
        BlockPos center = new BlockPos(100, 50, 0);
        boolean needsBuild = false;

        for (int dx = -2; dx <= 2; dx++) {
            for (int dz = -2; dz <= 2; dz++) {
                BlockPos pos = center.offset(dx, 0, dz);
                if (!level.getBlockState(pos).is(Blocks.OBSIDIAN)) {
                    needsBuild = true;
                    break;
                }
            }
            if (needsBuild) break;
        }

        if (needsBuild) {
            for (int dx = -2; dx <= 2; dx++) {
                for (int dz = -2; dz <= 2; dz++) {
                    BlockPos p = center.offset(dx, 0, dz);
                    level.setBlock(p, Blocks.OBSIDIAN.defaultBlockState(), 3);
                    level.setBlock(p.above(), Blocks.AIR.defaultBlockState(), 3);
                    level.setBlock(p.above(2), Blocks.AIR.defaultBlockState(), 3);
                }
            }
        }

        return center.above();
    }

    public static BlockPos findOrCreateNetherRoof(ServerLevel level) {
        int max = level.getMaxBuildHeight() - 1;
        int foundY = -1;

        for (int y = max; y >= 128; y--) {
            if (level.getBlockState(new BlockPos(0, y, 0)).is(Blocks.BEDROCK)) {
                foundY = y;
                break;
            }
        }

        int baseY = (foundY >= 128) ? Math.min(foundY + 1, max - 2) : 128;
        return new BlockPos(0, baseY, 0);
    }

    public static BlockPos findSafeSpawn(ServerLevel level, BlockPos at) {
        int x = at.getX();
        int z = at.getZ();
        int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
        return new BlockPos(x, Math.max(1, y), z);
    }

    public static BlockPos getTargetPosForMode(ServerLevel level, int mode, ResourceLocation dimId) {
        return switch (mode) {
            case 1 -> findWorldSpawn(level);
            case 2 -> findOrCreateEndPlatform(level);
            case 3 -> findOrCreateNetherRoof(level);
            default -> {
                ALBSavedData data = ALBSavedData.get(level);
                BlockPos bound = data != null ? data.getBound(dimId) : null;
                yield bound != null ? bound : new BlockPos(0, 100, 0);
            }
        };
    }

    public static void teleportPlayer(ServerPlayer player, ServerLevel targetLevel, BlockPos pos, int mode) {
        double x = pos.getX() + 0.5;
        double y = pos.getY() + 0.1;
        double z = pos.getZ() + 0.5;
        float yaw = player.getYRot();
        float pitch = player.getXRot();

        if (mode == 2 || mode == 3) {
            player.changeDimension(targetLevel, new ITeleporter() {
                @Override
                public PortalInfo getPortalInfo(Entity entity, ServerLevel destWorld, Function<ServerLevel, PortalInfo> defaultPortalInfo) {
                    return new PortalInfo(new Vec3(x, y, z), entity.getDeltaMovement(), entity.getYRot(), entity.getXRot());
                }

                @Override
                public Entity placeEntity(Entity entity, ServerLevel currentWorld, ServerLevel destWorld, float yaw, Function<Boolean, Entity> repositionEntity) {
                    Entity e = repositionEntity.apply(true);
                    ((ServerPlayer) e).teleportTo(destWorld, x, y, z, yaw, pitch);
                    return e;
                }

                @Override
                public boolean playTeleportSound(ServerPlayer player, ServerLevel sourceWorld, ServerLevel destWorld) {
                    return ITeleporter.super.playTeleportSound(player, sourceWorld, destWorld);
                }
            });
        } else {
            player.teleportTo(targetLevel, x, y, z, yaw, pitch);
        }
    }
}