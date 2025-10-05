package com.flechazo.sky_accessories.network;

import com.flechazo.sky_accessories.SkyAccessoriesSavedData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class RequestOpenGodConfigPacket {

    public RequestOpenGodConfigPacket() {}

    public static void encode(RequestOpenGodConfigPacket pkt, FriendlyByteBuf buf) {
    }

    public static RequestOpenGodConfigPacket decode(FriendlyByteBuf buf) {
        return new RequestOpenGodConfigPacket();
    }

    public static void handle(RequestOpenGodConfigPacket pkt, Supplier<NetworkEvent.Context> ctxSupplier) {
        var ctx = ctxSupplier.get();
        ctx.enqueueWork(() -> {
            ServerPlayer player = ctx.getSender();
            if (player != null) {
                Level level = player.serverLevel();
                SkyAccessoriesSavedData data = SkyAccessoriesSavedData.get(level);
                if (data != null && player.getUUID().equals(data.getOwner())) {
                    SkyNet.sendOpenGodConfig(player);
                } else {
                    player.sendSystemMessage(Component.translatable("message.sky_accessories.not_god"));
                }
            }
        });
        ctx.setPacketHandled(true);
    }
}