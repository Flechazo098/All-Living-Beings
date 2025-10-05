package com.flechazo.sky_accessories.network;

import com.flechazo.sky_accessories.client.ClientTitleCache;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

public class SyncTitlePacket {
    private final UUID playerId;
    private final Component title;
    private final boolean add;

    public SyncTitlePacket(UUID playerId, Component title, boolean add) {
        this.playerId = playerId;
        this.title = title;
        this.add = add;
    }

    public static void encode(SyncTitlePacket pkt, FriendlyByteBuf buf) {
        buf.writeUUID(pkt.playerId);
        buf.writeBoolean(pkt.add);
        buf.writeComponent(pkt.title);
    }

    public static SyncTitlePacket decode(FriendlyByteBuf buf) {
        UUID id = buf.readUUID();
        boolean add = buf.readBoolean();
        Component title = buf.readComponent();
        return new SyncTitlePacket(id, title, add);
    }

    public static void handle(SyncTitlePacket pkt, Supplier<NetworkEvent.Context> ctxSupplier) {
        NetworkEvent.Context ctx = ctxSupplier.get();
        ctx.enqueueWork(() -> {
            if (pkt.add) {
                ClientTitleCache.put(pkt.playerId, pkt.title);
            } else {
                ClientTitleCache.remove(pkt.playerId);
            }
        });
        ctx.setPacketHandled(true);
    }
}