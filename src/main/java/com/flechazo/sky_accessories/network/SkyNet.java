package com.flechazo.sky_accessories.network;

import com.flechazo.sky_accessories.SkyAccessories;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public class SkyNet {
    private static final String PROTOCOL = "1";
    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(SkyAccessories.MODID, "main"),
            () -> PROTOCOL, PROTOCOL::equals, PROTOCOL::equals
    );

    public static void init() {
        int id = 0;
        CHANNEL.registerMessage(id++, TeleportRequestPacket.class,
                TeleportRequestPacket::encode,
                TeleportRequestPacket::decode,
                TeleportRequestPacket::handle);
        CHANNEL.registerMessage(id++, RequestDimensionsPacket.class,
                RequestDimensionsPacket::encode,
                RequestDimensionsPacket::decode,
                RequestDimensionsPacket::handle);
        CHANNEL.registerMessage(id++, SyncDimensionsPacket.class,
                SyncDimensionsPacket::encode,
                SyncDimensionsPacket::decode,
                SyncDimensionsPacket::handle);
        CHANNEL.registerMessage(id++, SyncTitlePacket.class,
                SyncTitlePacket::encode,
                SyncTitlePacket::decode,
                SyncTitlePacket::handle);
    }

    public static void sendTeleportRequest(ResourceLocation dimId) {
        CHANNEL.sendToServer(new TeleportRequestPacket(dimId));
    }

    public static void requestDimensions() {
        CHANNEL.sendToServer(new RequestDimensionsPacket());
    }
}