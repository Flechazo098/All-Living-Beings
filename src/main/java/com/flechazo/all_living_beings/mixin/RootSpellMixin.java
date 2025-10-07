package com.flechazo.all_living_beings.mixin;

import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.spells.CastSource;
import io.redspace.ironsspellbooks.capabilities.magic.TargetEntityCastData;
import io.redspace.ironsspellbooks.spells.nature.RootSpell;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RootSpell.class)
public class RootSpellMixin {

    @Inject(method = "onCast", at = @At("HEAD"), cancellable = true, remap = false)
    private void preventPlayerRoot(Level level, int spellLevel, LivingEntity entity,
                                   CastSource castSource, MagicData playerMagicData,
                                   CallbackInfo ci) {
        if (playerMagicData.getAdditionalCastData() instanceof TargetEntityCastData castData) {
            LivingEntity target = castData.getTarget((ServerLevel) level);
            if (target instanceof Player) {
                ci.cancel();
            }
        }
    }
}
