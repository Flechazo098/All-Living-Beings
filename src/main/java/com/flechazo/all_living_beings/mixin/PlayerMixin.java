package com.flechazo.all_living_beings.mixin;

import com.flechazo.all_living_beings.config.Config;
import com.flechazo.all_living_beings.utils.Util;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Player.class)
public class PlayerMixin {

    @Redirect(method = "getDigSpeed", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;onGround()Z"))
    private boolean disableAirMiningSlowdown(Player player) {
        if (player instanceof ServerPlayer sp &&
                Util.isOwnerActive(sp) &&
                Config.COMMON.disableAirMiningSlowdown.get()) {
            return true;
        }
        return player.onGround();
    }
}