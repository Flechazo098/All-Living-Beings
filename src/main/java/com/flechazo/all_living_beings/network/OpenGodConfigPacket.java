package com.flechazo.all_living_beings.network;

import com.flechazo.all_living_beings.utils.GodConfigIO;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.List;
import java.util.function.Supplier;

public record OpenGodConfigPacket(boolean absoluteDefense, boolean absoluteAutonomy, boolean godPermissions,
                                  boolean godSuppression, boolean godAttack, boolean eternalTranscendence,
                                  int fixedAttackDamage, boolean instantKillEnabled, boolean buffsEnabled, int buffMode,
                                  List<String> effectIds, List<String> gazeEffectIds, List<Integer> gazeEffectDurations,
                                  int mobAttitude, double stepAssistHeight,
                                  List<String> bossEntityTypeIds, int instantMiningMode, boolean instantMiningDrops,
                                  boolean disableAirMiningSlowdown) {

    public static void encode(OpenGodConfigPacket pkt, FriendlyByteBuf buf) {
        GodConfigIO.write(buf,
                pkt.absoluteDefense,
                pkt.absoluteAutonomy,
                pkt.godPermissions,
                pkt.godSuppression,
                pkt.godAttack,
                pkt.eternalTranscendence,
                pkt.fixedAttackDamage,
                pkt.instantKillEnabled,
                pkt.buffsEnabled,
                pkt.buffMode,
                pkt.effectIds,
                pkt.gazeEffectIds,
                pkt.gazeEffectDurations,
                pkt.mobAttitude,
                pkt.stepAssistHeight,
                pkt.bossEntityTypeIds,
                pkt.instantMiningMode,
                pkt.instantMiningDrops,
                pkt.disableAirMiningSlowdown);
    }

    public static OpenGodConfigPacket decode(FriendlyByteBuf buf) {
        var v = GodConfigIO.read(buf);
        return new OpenGodConfigPacket(v.absoluteDefense(), v.absoluteAutonomy(), v.godPermissions(), v.godSuppression(), v.godAttack(),
                v.eternalTranscendence(), v.fixedAttackDamage(), v.instantKillEnabled(), v.buffsEnabled(), v.buffMode(), v.effectIds(), v.gazeEffectIds(), v.gazeEffectDurations(),
                v.mobAttitude(), v.stepAssistHeight(), v.bossEntityTypeIds(), v.instantMiningMode(), v.instantMiningDrops(), v.disableAirMiningSlowdown());
    }

    public static void handle(OpenGodConfigPacket pkt, Supplier<NetworkEvent.Context> ctxSupplier) {
        var ctx = ctxSupplier.get();
        ctx.enqueueWork(() -> PacketClientHandler.handleOpenGodConfig(pkt, ctxSupplier));
        ctx.setPacketHandled(true);
    }
}