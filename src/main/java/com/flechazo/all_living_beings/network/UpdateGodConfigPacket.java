package com.flechazo.all_living_beings.network;

import com.flechazo.all_living_beings.config.Config;
import com.flechazo.all_living_beings.data.ALBSavedData;
import com.flechazo.all_living_beings.utils.GodConfigIO;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.List;
import java.util.function.Supplier;

public class UpdateGodConfigPacket {
    private final boolean absoluteDefense;
    private final boolean absoluteAutonomy;
    private final boolean godPermissions;
    private final boolean godSuppression;
    private final boolean godAttack;
    private final boolean eternalTranscendence;
    private final int fixedAttackDamage;
    private final boolean instantKillEnabled;
    private final boolean buffsEnabled;
    private final int buffMode;
    private final List<String> effectIds;
    private final List<String> gazeEffectIds;
    private final List<Integer> gazeEffectDurations;
    private final int mobAttitude;
    private final double stepAssistHeight;
    private final List<String> bossEntityTypeIds;
    private final int instantMiningMode;
    private final boolean instantMiningDrops;
    private final boolean disableAirMiningSlowdown;

    public UpdateGodConfigPacket(boolean absoluteDefense,
                                 boolean absoluteAutonomy,
                                 boolean godPermissions,
                                 boolean godSuppression,
                                 boolean godAttack,
                                 boolean eternalTranscendence,
                                 int fixedAttackDamage,
                                 boolean instantKillEnabled,
                                 boolean buffsEnabled,
                                 int buffMode,
                                 List<String> effectIds,
                                 List<String> gazeEffectIds,
                                 List<Integer> gazeEffectDurations,
                                 int mobAttitude,
                                 double stepAssistHeight,
                                 List<String> bossEntityTypeIds,
                                 int instantMiningMode,
                                 boolean instantMiningDrops,
                                 boolean disableAirMiningSlowdown) {
        this.absoluteDefense = absoluteDefense;
        this.absoluteAutonomy = absoluteAutonomy;
        this.godPermissions = godPermissions;
        this.godSuppression = godSuppression;
        this.godAttack = godAttack;
        this.eternalTranscendence = eternalTranscendence;
        this.fixedAttackDamage = fixedAttackDamage;
        this.instantKillEnabled = instantKillEnabled;
        this.buffsEnabled = buffsEnabled;
        this.buffMode = buffMode;
        this.effectIds = effectIds;
        this.gazeEffectIds = gazeEffectIds;
        this.gazeEffectDurations = gazeEffectDurations;
        this.mobAttitude = mobAttitude;
        this.stepAssistHeight = stepAssistHeight;
        this.bossEntityTypeIds = bossEntityTypeIds;
        this.instantMiningMode = instantMiningMode;
        this.instantMiningDrops = instantMiningDrops;
        this.disableAirMiningSlowdown = disableAirMiningSlowdown;
    }

    public static void encode(UpdateGodConfigPacket pkt, FriendlyByteBuf buf) {
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

    public static UpdateGodConfigPacket decode(FriendlyByteBuf buf) {
        GodConfigIO.Values v = GodConfigIO.read(buf);
        return new UpdateGodConfigPacket(v.absoluteDefense(), v.absoluteAutonomy(), v.godPermissions(), v.godSuppression(), v.godAttack(),
                v.eternalTranscendence(), v.fixedAttackDamage(), v.instantKillEnabled(), v.buffsEnabled(), v.buffMode(), v.effectIds(), v.gazeEffectIds(), v.gazeEffectDurations(),
                v.mobAttitude(), v.stepAssistHeight(), v.bossEntityTypeIds(), v.instantMiningMode(), v.instantMiningDrops(), v.disableAirMiningSlowdown());
    }

    public static void handle(UpdateGodConfigPacket pkt, Supplier<NetworkEvent.Context> ctxSupplier) {
        var ctx = ctxSupplier.get();
        ctx.enqueueWork(() -> {
            ServerPlayer sp = ctx.getSender();
            if (sp == null) return;
            ALBSavedData data = ALBSavedData.get(sp.level());
            if (data == null || !sp.getUUID().equals(data.getOwner())) return;
            Config.COMMON.absoluteDefense.set(pkt.absoluteDefense);
            Config.COMMON.absoluteAutonomy.set(pkt.absoluteAutonomy);
            Config.COMMON.godPermissions.set(pkt.godPermissions);
            Config.COMMON.godSuppression.set(pkt.godSuppression);
            Config.COMMON.godAttack.set(pkt.godAttack);
            Config.COMMON.eternalTranscendence.set(pkt.eternalTranscendence);
            Config.COMMON.fixedAttackDamage.set(pkt.fixedAttackDamage);
            Config.COMMON.instantKillEnabled.set(pkt.instantKillEnabled);
            Config.COMMON.buffsEnabled.set(pkt.buffsEnabled);
            Config.COMMON.buffMode.set(pkt.buffMode);
            Config.COMMON.effectIds.set(pkt.effectIds);
            Config.COMMON.gazeEffectIds.set(pkt.gazeEffectIds);
            Config.COMMON.gazeEffectDurations.set(pkt.gazeEffectDurations);
            Config.COMMON.mobAttitude.set(pkt.mobAttitude);
            Config.COMMON.stepAssistHeight.set(pkt.stepAssistHeight);
            Config.COMMON.bossEntityTypeIds.set(pkt.bossEntityTypeIds);
            Config.COMMON.instantMiningMode.set(pkt.instantMiningMode);
            Config.COMMON.instantMiningDrops.set(pkt.instantMiningDrops);
            Config.COMMON.disableAirMiningSlowdown.set(pkt.disableAirMiningSlowdown);
        });
        ctx.setPacketHandled(true);
    }
}