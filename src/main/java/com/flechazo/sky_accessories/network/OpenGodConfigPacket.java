package com.flechazo.sky_accessories.network;

import com.flechazo.sky_accessories.client.gui.GodConfigScreen;
import com.flechazo.sky_accessories.utils.GodConfigIO;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.List;
import java.util.function.Supplier;

public class OpenGodConfigPacket {
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

    public OpenGodConfigPacket(boolean absoluteDefense,
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

    public static void encode(OpenGodConfigPacket pkt, FriendlyByteBuf buf) {
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

    public static OpenGodConfigPacket decode(FriendlyByteBuf buf) {
        var v = GodConfigIO.read(buf);
        return new OpenGodConfigPacket(v.absoluteDefense(), v.absoluteAutonomy(), v.godPermissions(), v.godSuppression(), v.godAttack(),
                v.eternalTranscendence(), v.fixedAttackDamage(), v.buffsEnabled(), v.buffMode(), v.positiveEffectIds(), v.negativeEffectIds());
    }

    public static void handle(OpenGodConfigPacket pkt, Supplier<NetworkEvent.Context> ctxSupplier) {
        var ctx = ctxSupplier.get();
        ctx.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
            Minecraft.getInstance().setScreen(new GodConfigScreen(
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
                    pkt.negativeEffectIds
            ));
        }));
        ctx.setPacketHandled(true);
    }
}