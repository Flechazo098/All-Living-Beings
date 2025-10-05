package com.flechazo.sky_accessories.network;

import com.flechazo.sky_accessories.utils.Util;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class RequestDimensionsPacket {
    public static void encode(RequestDimensionsPacket pkt, FriendlyByteBuf buf) {
    }

    public static RequestDimensionsPacket decode(FriendlyByteBuf buf) {
        return new RequestDimensionsPacket();
    }

    public static void handle(RequestDimensionsPacket pkt, Supplier<NetworkEvent.Context> ctxSupplier) {
        NetworkEvent.Context ctx = ctxSupplier.get();
        ctx.enqueueWork(() -> {
            ServerPlayer sp = ctx.getSender();
            if (sp == null) return;
            if (!Util.isOwnerActive(sp)) {
                sp.displayClientMessage(Component.translatable("message.sky_accessories.not_god"), true);
                return;
            }
            List<ResourceLocation> ids = new ArrayList<>();
            for (var key : sp.server.levelKeys()) {
                ids.add(key.location());
            }
            SkyNet.CHANNEL.send(PacketDistributor.PLAYER.with(() -> sp), new SyncDimensionsPacket(ids));
        });
        ctx.setPacketHandled(true);
    }
}