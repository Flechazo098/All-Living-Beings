package com.flechazo.sky_accessories.network;

import com.flechazo.sky_accessories.SkyAccessoriesSavedData;
import com.flechazo.sky_accessories.utils.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class BindTeleportPacket {
    private final ResourceLocation dimId;
    public BindTeleportPacket(ResourceLocation dimId) { this.dimId = dimId; }
    public static void encode(BindTeleportPacket pkt, FriendlyByteBuf buf) { buf.writeResourceLocation(pkt.dimId); }
    public static BindTeleportPacket decode(FriendlyByteBuf buf) { return new BindTeleportPacket(buf.readResourceLocation()); }
    public static void handle(BindTeleportPacket pkt, Supplier<NetworkEvent.Context> ctxSupplier) {
        var ctx = ctxSupplier.get();
        ctx.enqueueWork(() -> {
            ServerPlayer sp = ctx.getSender();
            if (sp == null) return;
            if (!Util.isOwnerActive(sp)) return;
            SkyAccessoriesSavedData data = SkyAccessoriesSavedData.get(sp.level());
            if (data == null) return;
            BlockPos here = sp.blockPosition();
            data.setBound(pkt.dimId, here);
        });
        ctx.setPacketHandled(true);
    }
}