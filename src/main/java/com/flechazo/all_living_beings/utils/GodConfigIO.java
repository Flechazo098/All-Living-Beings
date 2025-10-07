package com.flechazo.all_living_beings.utils;

import net.minecraft.network.FriendlyByteBuf;

import java.util.ArrayList;
import java.util.List;

public final class GodConfigIO {
    public record Values(boolean absoluteDefense, boolean absoluteAutonomy, boolean godPermissions,
                         boolean godSuppression, boolean godAttack, boolean eternalTranscendence, int fixedAttackDamage,
                         boolean buffsEnabled, int buffMode, List<String> positiveEffectIds,
                         List<String> negativeEffectIds) {
    }

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
                             List<String> negativeEffectIds) {
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
        return new Values(absoluteDefense, absoluteAutonomy, godPermissions, godSuppression, godAttack,
                eternalTranscendence, fixedAttackDamage, buffsEnabled, buffMode, pos, neg);
    }
}