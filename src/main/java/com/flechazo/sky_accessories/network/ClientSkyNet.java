package com.flechazo.sky_accessories.network;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ClientSkyNet {
    public static void init() {
        SkyNet.CHANNEL.registerMessage(100, SyncDimensionsPacket.class,
                SyncDimensionsPacket::encode,
                SyncDimensionsPacket::decode,
                PacketClientHandler::handleSyncDimensions);
        SkyNet.CHANNEL.registerMessage(101, OpenGodConfigPacket.class,
                OpenGodConfigPacket::encode,
                OpenGodConfigPacket::decode,
                PacketClientHandler::handleOpenGodConfig);
    }
}