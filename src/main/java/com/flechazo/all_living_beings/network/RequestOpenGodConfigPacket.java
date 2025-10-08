package com.flechazo.all_living_beings.network;

import com.flechazo.all_living_beings.data.ALBSavedData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class RequestOpenGodConfigPacket {

    public RequestOpenGodConfigPacket() {
    }

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
                ALBSavedData data = ALBSavedData.get(level);
                if (data != null && player.getUUID().equals(data.getOwner())) {
                    NetworkHandler.sendOpenGodConfig(player);
                } else {
                    player.sendSystemMessage(Component.translatable("message.all_living_beings.not_god"));
                }
            }
        });
        ctx.setPacketHandled(true);
    }
}