package com.flechazo.all_living_beings.network;

import com.flechazo.all_living_beings.utils.TeleportUtil;
import com.flechazo.all_living_beings.utils.Util;
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
import net.minecraftforge.network.NetworkEvent;

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
                        Component.translatable("message.all_living_beings.not_god").withStyle(ChatFormatting.RED), true);
                return;
            }

            ResourceKey<Level> targetKey = ResourceKey.create(Registries.DIMENSION, pkt.dimId);
            ServerLevel dst = sp.server.getLevel(targetKey);
            if (dst == null) {
                sp.displayClientMessage(
                        Component.translatable("message.all_living_beings.teleport_denied", pkt.dimId.toString())
                                .withStyle(ChatFormatting.RED), true);
                return;
            }

            BlockPos targetPos = TeleportUtil.getTargetPosForMode(dst, pkt.mode, pkt.dimId);
            TeleportUtil.teleportPlayer(sp, dst, targetPos, pkt.mode);

            sp.displayClientMessage(
                    Component.translatable("message.all_living_beings.teleport_success", pkt.dimId.toString())
                            .withStyle(ChatFormatting.GREEN), true);

            TeleportUtil.grantNextTeleport(sp);
        });
        ctx.setPacketHandled(true);
    }
}
