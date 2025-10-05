package com.flechazo.sky_accessories.network;

import com.flechazo.sky_accessories.SkyAccessoriesSavedData;
import com.flechazo.sky_accessories.utils.Util;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.portal.PortalInfo;
import net.minecraftforge.common.util.ITeleporter;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Function;
import java.util.function.Supplier;

public class TeleportRequestPacket {
    private final ResourceLocation dimId;
    private final int mode; // 0 default, 1 spawn, 2 end platform, 3 nether top

    public TeleportRequestPacket(ResourceLocation dimId, int mode) {
        this.dimId = dimId;
        this.mode = mode;
    }

    public static void encode(TeleportRequestPacket pkt, FriendlyByteBuf buf) {
        buf.writeResourceLocation(pkt.dimId);
        buf.writeVarInt(pkt.mode);
    }

    public static TeleportRequestPacket decode(FriendlyByteBuf buf) {
        return new TeleportRequestPacket(buf.readResourceLocation(), buf.readVarInt());
    }

    public static void handle(TeleportRequestPacket pkt, Supplier<NetworkEvent.Context> ctxSupplier) {
        NetworkEvent.Context ctx = ctxSupplier.get();
        ctx.enqueueWork(() -> {
            ServerPlayer sp = ctx.getSender();
            if (sp == null) return;

            if (!Util.isOwnerActive(sp)) {
                sp.displayClientMessage(
                        Component.translatable("message.sky_accessories.not_god").withStyle(ChatFormatting.RED), true);
                return;
            }

            ResourceKey<Level> targetKey = ResourceKey.create(Registries.DIMENSION, pkt.dimId);
            ServerLevel dst = sp.server.getLevel(targetKey);
            if (dst == null) {
                sp.displayClientMessage(
                        Component.translatable("message.sky_accessories.teleport_denied", pkt.dimId.toString())
                                .withStyle(ChatFormatting.RED), true);
                return;
            }

            Util.grantNextTeleport(sp);

            BlockPos tp;
            switch (pkt.mode) {
                case 1 -> tp = findWorldSpawn(dst);
                case 2 -> tp = findOrCreateEndPlatform(dst);
                case 3 -> tp = findOrCreateNetherRoof(dst);
                default -> {
                    SkyAccessoriesSavedData data = SkyAccessoriesSavedData.get(sp.level());
                    BlockPos bound = data != null ? data.getBound(pkt.dimId) : null;
                    tp = bound != null ? bound : new BlockPos(0, 100, 0);
                }
            }

            dst.getChunk(tp.getX() >> 4, tp.getZ() >> 4);

            if (pkt.mode == 2 || pkt.mode == 3) {
                sp.changeDimension(dst, new ITeleporter() {
                    @Override
                    public PortalInfo getPortalInfo(Entity entity, ServerLevel destWorld, Function<ServerLevel, PortalInfo> defaultPortalInfo) {
                        return new PortalInfo(tp.getCenter(), entity.getDeltaMovement(), entity.getYRot(), entity.getXRot());
                    }

                    @Override
                    public Entity placeEntity(Entity entity, ServerLevel currentWorld, ServerLevel destWorld, float yaw, Function<Boolean, Entity> repositionEntity) {
                        Entity e = repositionEntity.apply(true);
                        e.teleportTo(tp.getX() + 0.5, tp.getY() + 0.1, tp.getZ() + 0.5);
                        return e;
                    }

                    @Override
                    public boolean playTeleportSound(ServerPlayer player, ServerLevel sourceWorld, ServerLevel destWorld) {
                        return ITeleporter.super.playTeleportSound(player, sourceWorld, destWorld);
                    }
                });
            } else {
                sp.teleportTo(dst, tp.getX() + 0.5, tp.getY() + 0.1, tp.getZ() + 0.5, sp.getYRot(), sp.getXRot());
            }

            sp.displayClientMessage(
                    Component.translatable("message.sky_accessories.teleport_success", pkt.dimId.toString())
                            .withStyle(ChatFormatting.GREEN), true);
        });
        ctx.setPacketHandled(true);
    }


    private static BlockPos findWorldSpawn(ServerLevel level) {
        BlockPos spawn = level.getSharedSpawnPos();
        return findSafeSpawn(level, spawn);
    }

    private static BlockPos findOrCreateEndPlatform(ServerLevel level) {
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

    private static BlockPos findOrCreateNetherRoof(ServerLevel level) {
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

    private static BlockPos findSafeSpawn(ServerLevel level, BlockPos at) {
        int x = at.getX();
        int z = at.getZ();
        int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
        return new BlockPos(x, Math.max(1, y), z);
    }
}
