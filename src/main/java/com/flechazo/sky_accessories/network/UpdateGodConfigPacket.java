package com.flechazo.sky_accessories.network;

import com.flechazo.sky_accessories.config.Config;
import com.flechazo.sky_accessories.SkyAccessoriesSavedData;
import com.flechazo.sky_accessories.utils.GodConfigIO;
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
    private final boolean buffsEnabled;
    private final int buffMode;
    private final List<String> positiveEffectIds;
    private final List<String> negativeEffectIds;

    public UpdateGodConfigPacket(boolean absoluteDefense,
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
        this.absoluteDefense = absoluteDefense;
        this.absoluteAutonomy = absoluteAutonomy;
        this.godPermissions = godPermissions;
        this.godSuppression = godSuppression;
        this.godAttack = godAttack;
        this.eternalTranscendence = eternalTranscendence;
        this.fixedAttackDamage = fixedAttackDamage;
        this.buffsEnabled = buffsEnabled;
        this.buffMode = buffMode;
        this.positiveEffectIds = positiveEffectIds;
        this.negativeEffectIds = negativeEffectIds;
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
                pkt.buffsEnabled,
                pkt.buffMode,
                pkt.positiveEffectIds,
                pkt.negativeEffectIds);
    }

    public static UpdateGodConfigPacket decode(FriendlyByteBuf buf) {
        var v = GodConfigIO.read(buf);
        return new UpdateGodConfigPacket(v.absoluteDefense(), v.absoluteAutonomy(), v.godPermissions(), v.godSuppression(), v.godAttack(),
                v.eternalTranscendence(), v.fixedAttackDamage(), v.buffsEnabled(), v.buffMode(), v.positiveEffectIds(), v.negativeEffectIds());
    }

    public static void handle(UpdateGodConfigPacket pkt, Supplier<NetworkEvent.Context> ctxSupplier) {
        var ctx = ctxSupplier.get();
        ctx.enqueueWork(() -> {
            ServerPlayer sp = ctx.getSender();
            if (sp == null) return;
            SkyAccessoriesSavedData data = SkyAccessoriesSavedData.get(sp.level());
            if (data == null || !sp.getUUID().equals(data.getOwner())) return;
            Config.COMMON.absoluteDefense.set(pkt.absoluteDefense);
            Config.COMMON.absoluteAutonomy.set(pkt.absoluteAutonomy);
            Config.COMMON.godPermissions.set(pkt.godPermissions);
            Config.COMMON.godSuppression.set(pkt.godSuppression);
            Config.COMMON.godAttack.set(pkt.godAttack);
            Config.COMMON.eternalTranscendence.set(pkt.eternalTranscendence);
            Config.COMMON.fixedAttackDamage.set(pkt.fixedAttackDamage);
            Config.COMMON.buffsEnabled.set(pkt.buffsEnabled);
            Config.COMMON.buffMode.set(pkt.buffMode);
            Config.COMMON.positiveEffectIds.set(pkt.positiveEffectIds);
            Config.COMMON.negativeEffectIds.set(pkt.negativeEffectIds);
        });
        ctx.setPacketHandled(true);
    }
}