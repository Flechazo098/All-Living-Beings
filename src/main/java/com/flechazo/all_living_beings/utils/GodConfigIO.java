package com.flechazo.all_living_beings.utils;

import net.minecraft.network.FriendlyByteBuf;

import java.util.ArrayList;
import java.util.List;

public final class GodConfigIO {
    public static void write(FriendlyByteBuf buf,
                             boolean absoluteDefense,
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
        buf.writeBoolean(absoluteDefense);
        buf.writeBoolean(absoluteAutonomy);
        buf.writeBoolean(godPermissions);
        buf.writeBoolean(godSuppression);
        buf.writeBoolean(godAttack);
        buf.writeBoolean(eternalTranscendence);
        buf.writeVarInt(fixedAttackDamage);
        buf.writeBoolean(instantKillEnabled);
        buf.writeBoolean(buffsEnabled);
        buf.writeVarInt(buffMode);
        buf.writeVarInt(effectIds.size());
        for (var s : effectIds) buf.writeUtf(s);
        buf.writeVarInt(gazeEffectIds.size());
        for (var s : gazeEffectIds) buf.writeUtf(s);
        buf.writeVarInt(gazeEffectDurations.size());
        for (var d : gazeEffectDurations) buf.writeVarInt(d);
        buf.writeVarInt(mobAttitude);
        buf.writeDouble(stepAssistHeight);
        buf.writeVarInt(bossEntityTypeIds.size());
        for (var s : bossEntityTypeIds) buf.writeUtf(s);
        buf.writeVarInt(instantMiningMode);
        buf.writeBoolean(instantMiningDrops);
        buf.writeBoolean(disableAirMiningSlowdown);
    }

    public static Values read(FriendlyByteBuf buf) {
        boolean absoluteDefense = buf.readBoolean();
        boolean absoluteAutonomy = buf.readBoolean();
        boolean godPermissions = buf.readBoolean();
        boolean godSuppression = buf.readBoolean();
        boolean godAttack = buf.readBoolean();
        boolean eternalTranscendence = buf.readBoolean();
        int fixedAttackDamage = buf.readVarInt();
        boolean instantKillEnabled = buf.readBoolean();
        boolean buffsEnabled = buf.readBoolean();
        int buffMode = buf.readVarInt();
        int eSize = buf.readVarInt();
        List<String> effects = new ArrayList<>(eSize);
        for (int i = 0; i < eSize; i++) effects.add(buf.readUtf());
        int gSize = buf.readVarInt();
        List<String> gazeEffects = new ArrayList<>(gSize);
        for (int i = 0; i < gSize; i++) gazeEffects.add(buf.readUtf());
        int dSize = buf.readVarInt();
        List<Integer> gazeDurations = new ArrayList<>(dSize);
        for (int i = 0; i < dSize; i++) gazeDurations.add(buf.readVarInt());
        int mobAttitude = buf.readVarInt();
        double stepAssistHeight = buf.readDouble();
        int bSize = buf.readVarInt();
        List<String> bossList = new ArrayList<>(bSize);
        for (int i = 0; i < bSize; i++) bossList.add(buf.readUtf());
        int instantMiningMode = buf.readVarInt();
        boolean instantMiningDrops = buf.readBoolean();
        boolean disableAirMiningSlowdown = buf.readBoolean();
        return new Values(absoluteDefense, absoluteAutonomy, godPermissions, godSuppression, godAttack,
                eternalTranscendence, fixedAttackDamage, instantKillEnabled, buffsEnabled, buffMode, effects, gazeEffects, gazeDurations, mobAttitude, stepAssistHeight, bossList, instantMiningMode, instantMiningDrops, disableAirMiningSlowdown);
    }

    public record Values(boolean absoluteDefense, boolean absoluteAutonomy, boolean godPermissions,
                         boolean godSuppression, boolean godAttack, boolean eternalTranscendence, int fixedAttackDamage,
                         boolean instantKillEnabled, boolean buffsEnabled, int buffMode, List<String> effectIds,
                         List<String> gazeEffectIds, List<Integer> gazeEffectDurations, int mobAttitude,
                         double stepAssistHeight,
                         List<String> bossEntityTypeIds, int instantMiningMode, boolean instantMiningDrops,
                         boolean disableAirMiningSlowdown) {
    }
}