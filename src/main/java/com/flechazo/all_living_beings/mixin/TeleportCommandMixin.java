package com.flechazo.all_living_beings.mixin;

import com.flechazo.all_living_beings.utils.TeleportUtil;
import com.flechazo.all_living_beings.utils.Util;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.commands.TeleportCommand;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.RelativeMovement;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Set;

@Mixin(TeleportCommand.class)
public class TeleportCommandMixin {

    @Inject(method = "performTeleport", at = @At("HEAD"))
    private static void skyAccessories$onSourceTeleport(CommandSourceStack source, Entity targets, ServerLevel level, double x, double y, double z, Set<RelativeMovement> relative, float yaw, float pitch, TeleportCommand.LookAt lookAt, CallbackInfo ci) {
        if (!(source.getEntity() instanceof ServerPlayer executor)) return;
        if (!Util.isOwnerActive(executor)) return;

        if (targets.equals(executor)) {
            TeleportUtil.grantNextTeleport(executor);
        }
    }
}
