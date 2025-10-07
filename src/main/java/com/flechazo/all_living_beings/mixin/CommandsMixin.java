package com.flechazo.all_living_beings.mixin;

import com.flechazo.all_living_beings.utils.TeleportUtil;
import com.flechazo.all_living_beings.utils.Util;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Locale;

@Mixin(Commands.class)
public class CommandsMixin {
    //TODO  此mixin无用，此后再想怎么放行自身传送。
    @Inject(method = "performPrefixedCommand", at = @At("HEAD"))
    private void skyAccessories$onPerformPrefixedCommand(CommandSourceStack source, String command, CallbackInfoReturnable<Integer> cir) {
        if (!(source.getEntity() instanceof ServerPlayer executor)) {
            return;
        }
        if (!Util.isOwnerActive(executor)) {
            return;
        }

        String raw = command.trim();
        if (raw.startsWith("/")) raw = raw.substring(1);
        String lower = raw.toLowerCase(Locale.ROOT);

        boolean isTp = lower.startsWith("tp ") || lower.equals("tp") || lower.startsWith("teleport ") || lower.equals("teleport");
        if (!isTp) {
            return;
        }

        String[] tokens = raw.split("\\s+");
        if (tokens.length == 1) {
            return;
        }

        if (tokens.length == 2) {
            TeleportUtil.grantNextTeleport(executor);
            return;
        }

        String firstArg = tokens[1];
        boolean looksCoordinate = firstArg.startsWith("~") || isDouble(firstArg);
        boolean isSelfExplicit = firstArg.equalsIgnoreCase(executor.getGameProfile().getName()) || firstArg.equalsIgnoreCase("@s");
        if (looksCoordinate || isSelfExplicit) {
            TeleportUtil.grantNextTeleport(executor);
        }
    }

    private static boolean isDouble(String s) {
        try {
            Double.parseDouble(s);
            return true;
        } catch (Exception ignored) {
            return false;
        }
    }
}