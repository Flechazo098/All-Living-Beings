package com.flechazo.sky_accessories.network;

import com.flechazo.sky_accessories.client.ClientCache;
import com.flechazo.sky_accessories.client.gui.ThroneTravelScreen;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

@OnlyIn(Dist.CLIENT)
public class SyncDimensionsPacketClientHandler {
    public static void handle(SyncDimensionsPacket pkt, Supplier<NetworkEvent.Context> ctxSupplier) {
        NetworkEvent.Context ctx = ctxSupplier.get();
        ctx.enqueueWork(() -> {
            ClientCache.DIMENSIONS = pkt.ids();
            var mc = Minecraft.getInstance();
            if (mc.player != null) {
                mc.setScreen(new ThroneTravelScreen());
            }
        });
        ctx.setPacketHandled(true);
    }
}