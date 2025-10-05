package com.flechazo.sky_accessories.network;

import com.flechazo.sky_accessories.Utils;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class TeleportRequestPacket {
    private final ResourceLocation dimId;

    public TeleportRequestPacket(ResourceLocation dimId) {
        this.dimId = dimId;
    }

    public static void encode(TeleportRequestPacket pkt, FriendlyByteBuf buf) {
        buf.writeResourceLocation(pkt.dimId);
    }

    public static TeleportRequestPacket decode(FriendlyByteBuf buf) {
        return new TeleportRequestPacket(buf.readResourceLocation());
    }

    public static void handle(TeleportRequestPacket pkt, Supplier<NetworkEvent.Context> ctxSupplier) {
        NetworkEvent.Context ctx = ctxSupplier.get();
        ctx.enqueueWork(() -> {
            ServerPlayer sp = ctx.getSender();
            if (sp == null) return;

            if (!Utils.isOwnerActive(sp)) {
                sp.displayClientMessage(
                        Component.translatable("message.sky_accessories.not_god")
                                .withStyle(ChatFormatting.RED), true);
                return;
            }

            ResourceKey<Level> target = ResourceKey.create(Registries.DIMENSION, pkt.dimId);
            ServerLevel dst = sp.server.getLevel(target);
            if (dst != null) {
                Utils.grantNextTeleport(sp);
                BlockPos spawn = dst.getSharedSpawnPos();
                dst.getChunk(spawn.getX() >> 4, spawn.getZ() >> 4);
                BlockPos safe = findSafeSpawn(dst, spawn);
                sp.teleportTo(dst, safe.getX() + 0.5, safe.getY() + 0.1, safe.getZ() + 0.5, sp.getYRot(), sp.getXRot());
                sp.displayClientMessage(
                        Component.translatable("message.sky_accessories.teleport_success", pkt.dimId.toString())
                                .withStyle(ChatFormatting.GREEN), true);
            } else {
                sp.displayClientMessage(
                        Component.translatable("message.sky_accessories.teleport_denied", pkt.dimId.toString())
                                .withStyle(ChatFormatting.RED), true);
            }
        });
        ctx.setPacketHandled(true);
    }

    private static BlockPos findSafeSpawn(ServerLevel dst, BlockPos base) {
        boolean isNether = dst.dimension().equals(Level.NETHER);
        int minY = isNether ? 32 : dst.getMinBuildHeight();
        int maxY = isNether ? 118 : dst.getMaxBuildHeight() - 1;
        BlockPos top = dst.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, base);
        int startY = Math.min(Math.max(top.getY(), dst.getSeaLevel()), maxY);
        for (int y = startY; y >= minY; y--) {
            BlockPos feet = new BlockPos(top.getX(), y, top.getZ());
            var bsBelow = dst.getBlockState(feet.below());
            var bsFeet = dst.getBlockState(feet);
            var bsHead = dst.getBlockState(feet.above());
            boolean standable = !bsFeet.isSolid() && !bsHead.isSolid() && bsBelow.isSolid();
            boolean notBedrock = !bsBelow.is(Blocks.BEDROCK);
            if (standable && notBedrock) {
                return feet;
            }
        }
        return top;
    }
}