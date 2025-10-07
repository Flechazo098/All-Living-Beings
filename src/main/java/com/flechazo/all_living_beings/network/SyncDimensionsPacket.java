package com.flechazo.all_living_beings.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public record SyncDimensionsPacket(List<ResourceLocation> ids) {

    public static void encode(SyncDimensionsPacket pkt, FriendlyByteBuf buf) {
        buf.writeVarInt(pkt.ids.size());
        for (var id : pkt.ids) buf.writeResourceLocation(id);
    }

    public static SyncDimensionsPacket decode(FriendlyByteBuf buf) {
        int n = buf.readVarInt();
        List<ResourceLocation> ids = new ArrayList<>(n);
        for (int i = 0; i < n; i++) ids.add(buf.readResourceLocation());
        return new SyncDimensionsPacket(ids);
    }

    public static void handle(SyncDimensionsPacket pkt, Supplier<NetworkEvent.Context> ctxSupplier) {
        var ctx = ctxSupplier.get();
        ctx.enqueueWork(() -> PacketClientHandler.handleSyncDimensions(pkt, ctxSupplier));
        ctx.setPacketHandled(true);
    }
}