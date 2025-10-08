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
                             boolean buffsEnabled,
                             int buffMode,
                             List<String> positiveEffectIds,
                             List<String> negativeEffectIds,
                             int mobAttitude,
                             double stepAssistHeight,
                             List<String> bossEntityTypeIds) {
        buf.writeBoolean(absoluteDefense);
        buf.writeBoolean(absoluteAutonomy);
        buf.writeBoolean(godPermissions);
        buf.writeBoolean(godSuppression);
        buf.writeBoolean(godAttack);
        buf.writeBoolean(eternalTranscendence);
        buf.writeVarInt(fixedAttackDamage);
        buf.writeBoolean(buffsEnabled);
        buf.writeVarInt(buffMode);
        buf.writeVarInt(positiveEffectIds.size());
        for (var s : positiveEffectIds) buf.writeUtf(s);
        buf.writeVarInt(negativeEffectIds.size());
        for (var s : negativeEffectIds) buf.writeUtf(s);
        buf.writeVarInt(mobAttitude);
        buf.writeDouble(stepAssistHeight);
        buf.writeVarInt(bossEntityTypeIds.size());
        for (var s : bossEntityTypeIds) buf.writeUtf(s);
    }

    public static Values read(FriendlyByteBuf buf) {
        boolean absoluteDefense = buf.readBoolean();
        boolean absoluteAutonomy = buf.readBoolean();
        boolean godPermissions = buf.readBoolean();
        boolean godSuppression = buf.readBoolean();
        boolean godAttack = buf.readBoolean();
        boolean eternalTranscendence = buf.readBoolean();
        int fixedAttackDamage = buf.readVarInt();
        boolean buffsEnabled = buf.readBoolean();
        int buffMode = buf.readVarInt();
        int pSize = buf.readVarInt();
        List<String> pos = new ArrayList<>(pSize);
        for (int i = 0; i < pSize; i++) pos.add(buf.readUtf());
        int nSize = buf.readVarInt();
        List<String> neg = new ArrayList<>(nSize);
        for (int i = 0; i < nSize; i++) neg.add(buf.readUtf());
        int mobAttitude = buf.readVarInt();
        double stepAssistHeight = buf.readDouble();
        int bSize = buf.readVarInt();
        List<String> bossList = new ArrayList<>(bSize);
        for (int i = 0; i < bSize; i++) bossList.add(buf.readUtf());
        return new Values(absoluteDefense, absoluteAutonomy, godPermissions, godSuppression, godAttack,
                eternalTranscendence, fixedAttackDamage, buffsEnabled, buffMode, pos, neg, mobAttitude, stepAssistHeight, bossList);
    }

    public record Values(boolean absoluteDefense, boolean absoluteAutonomy, boolean godPermissions,
                         boolean godSuppression, boolean godAttack, boolean eternalTranscendence, int fixedAttackDamage,
                         boolean buffsEnabled, int buffMode, List<String> positiveEffectIds,
                         List<String> negativeEffectIds, int mobAttitude, double stepAssistHeight,
                         List<String> bossEntityTypeIds) {
    }
}