package com.flechazo.sky_accessories.network;

import com.flechazo.sky_accessories.SkyAccessories;
import com.flechazo.sky_accessories.config.Config;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.List;

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
        CHANNEL.registerMessage(id++, BindTeleportPacket.class,
                BindTeleportPacket::encode,
                BindTeleportPacket::decode,
                BindTeleportPacket::handle);
        CHANNEL.registerMessage(id++, OpenGodConfigPacket.class,
                OpenGodConfigPacket::encode,
                OpenGodConfigPacket::decode,
                OpenGodConfigPacket::handle);
        CHANNEL.registerMessage(id++, UpdateGodConfigPacket.class,
                UpdateGodConfigPacket::encode,
                UpdateGodConfigPacket::decode,
                UpdateGodConfigPacket::handle);
        CHANNEL.registerMessage(id++, RequestOpenGodConfigPacket.class,
                RequestOpenGodConfigPacket::encode,
                RequestOpenGodConfigPacket::decode,
                RequestOpenGodConfigPacket::handle
        );
    }

    public static void requestDimensions() {
        CHANNEL.sendToServer(new RequestDimensionsPacket());
    }

    public static void requestGodConfig() {
        CHANNEL.sendToServer(new RequestOpenGodConfigPacket());
    }

    public static void sendTeleportRequest(ResourceLocation dimId, int mode) {
        CHANNEL.sendToServer(new TeleportRequestPacket(dimId, mode));
    }

    public static void sendBindTeleport(ResourceLocation dimId) {
        CHANNEL.sendToServer(new BindTeleportPacket(dimId));
    }

    public static void sendOpenGodConfig(ServerPlayer to) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> to), new OpenGodConfigPacket(
                Config.COMMON.absoluteDefense.get(),
                Config.COMMON.absoluteAutonomy.get(),
                Config.COMMON.godPermissions.get(),
                Config.COMMON.godSuppression.get(),
                Config.COMMON.godAttack.get(),
                Config.COMMON.eternalTranscendence.get(),
                Config.COMMON.fixedAttackDamage.get(),
                Config.COMMON.buffsEnabled.get(),
                Config.COMMON.buffMode.get(),
                List.copyOf(Config.COMMON.positiveEffectIds.get()),
                List.copyOf(Config.COMMON.negativeEffectIds.get())
        ));
    }

    public static void updateGodConfig(boolean absoluteDefense,
                                       boolean absoluteAutonomy,
                                       boolean godPermissions,
                                       boolean godSuppression,
                                       boolean godAttack,
                                       boolean eternalTranscendence,
                                       int fixedAttackDamage,
                                       boolean buffsEnabled,
                                       int buffMode,
                                       List<String> positiveEffectIds,
                                       List<String> negativeEffectIds) {
        CHANNEL.sendToServer(new UpdateGodConfigPacket(
                absoluteDefense,
                absoluteAutonomy,
                godPermissions,
                godSuppression,
                godAttack,
                eternalTranscendence,
                fixedAttackDamage,
                buffsEnabled,
                buffMode,
                positiveEffectIds,
                negativeEffectIds
        ));
    }
}