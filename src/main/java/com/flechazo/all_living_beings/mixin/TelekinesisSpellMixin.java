package com.flechazo.all_living_beings.mixin;

import com.flechazo.all_living_beings.utils.Util;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.capabilities.magic.TargetEntityCastData;
import io.redspace.ironsspellbooks.spells.eldritch.TelekinesisSpell;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = TelekinesisSpell.class, remap = false)
public class TelekinesisSpellMixin {
    @Inject(
            method = "checkPreCastConditions",
            at = @At(
                    value = "INVOKE",
                    target = "Lio/redspace/ironsspellbooks/api/magic/MagicData;setAdditionalCastData(Lio/redspace/ironsspellbooks/api/spells/ICastData;)V",
                    shift = At.Shift.BEFORE
            ),
            cancellable = true
    )
    private void preventCastOnBoundOwner(
            Level level,
            int spellLevel,
            LivingEntity entity,
            MagicData playerMagicData,
            CallbackInfoReturnable<Boolean> cir
    ) {
        if (playerMagicData.getAdditionalCastData() instanceof TargetEntityCastData castData) {
            var target = castData.getTarget((ServerLevel) level);
            if (target instanceof ServerPlayer serverPlayer && Util.isBoundOwner(serverPlayer)) {
                cir.setReturnValue(false);
            }
        }
    }
}