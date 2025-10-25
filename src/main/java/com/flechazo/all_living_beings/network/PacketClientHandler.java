package com.flechazo.all_living_beings.network;

import com.flechazo.all_living_beings.client.ClientCache;
import com.flechazo.all_living_beings.client.gui.GodConfigScreen;
import com.flechazo.all_living_beings.client.gui.ThroneTravelScreen;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

@OnlyIn(Dist.CLIENT)
public class PacketClientHandler {
    public static void handleSyncDimensions(SyncDimensionsPacket pkt, Supplier<NetworkEvent.Context> ctxSupplier) {
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

    public static void handleOpenGodConfig(OpenGodConfigPacket pkt, Supplier<NetworkEvent.Context> ctxSupplier) {
        NetworkEvent.Context ctx = ctxSupplier.get();
        ctx.enqueueWork(() -> Minecraft.getInstance().setScreen(new GodConfigScreen(
                pkt.absoluteDefense(),
                pkt.absoluteAutonomy(),
                pkt.godPermissions(),
                pkt.godSuppression(),
                pkt.godAttack(),
                pkt.eternalTranscendence(),
                pkt.fixedAttackDamage(),
                pkt.instantKillEnabled(),
                pkt.buffsEnabled(),
                pkt.buffMode(),
                pkt.effectIds(),
                pkt.gazeEffectIds(),
                pkt.gazeEffectDurations(),
                pkt.mobAttitude(),
                pkt.stepAssistHeight(),
                pkt.bossEntityTypeIds(),
                pkt.instantMiningMode(),
                pkt.instantMiningDrops(),
                pkt.disableAirMiningSlowdown()
        )));
        ctx.setPacketHandled(true);
    }
}