package com.flechazo.all_living_beings.mixin.iss;

import com.flechazo.all_living_beings.utils.Util;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.capabilities.magic.TargetEntityCastData;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetActionBarTextPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.function.Predicate;

@Mixin(value = Utils.class)
public class UtilsMixin {

    @Inject(
            method = "preCastTargetHelper(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/LivingEntity;Lio/redspace/ironsspellbooks/api/magic/MagicData;Lio/redspace/ironsspellbooks/api/spells/AbstractSpell;IFZLjava/util/function/Predicate;)Z",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/network/ServerGamePacketListenerImpl;send(Lnet/minecraft/network/protocol/Packet;)V",
                    shift = At.Shift.AFTER
            ),
            locals = LocalCapture.NO_CAPTURE
    )
    private static void overrideSuccessActionBar(
            Level level, LivingEntity caster, MagicData playerMagicData, AbstractSpell spell,
            int range, float aimAssist, boolean sendFailureMessage, Predicate<LivingEntity> filter,
            CallbackInfoReturnable<Boolean> cir
    ) {
        if (playerMagicData.getAdditionalCastData() instanceof TargetEntityCastData castData) {
            LivingEntity target = castData.getTarget((ServerLevel) level);
            if (target instanceof ServerPlayer sp && Util.isBoundOwner(sp)) {
                if (caster instanceof ServerPlayer serverPlayer) {
                    serverPlayer.connection.send(new ClientboundSetActionBarTextPacket(
                            Component.translatable("message.all_living_beings.target_god_blocked")
                                    .withStyle(ChatFormatting.RED)
                    ));
                }
            }
        }
    }
}
