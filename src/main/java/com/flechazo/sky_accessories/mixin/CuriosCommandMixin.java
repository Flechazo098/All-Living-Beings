package com.flechazo.sky_accessories.mixin;

import com.flechazo.sky_accessories.event.CommandInterceptEvent;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.common.MinecraftForge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import top.theillusivec4.curios.server.command.CuriosCommand;

@Mixin(CuriosCommand.class)
public class CuriosCommandMixin {

    @Inject(method = "clearSlotsForPlayer", at = @At("HEAD"), cancellable = true, remap = false)
    private static void onCuriosClear(CommandSourceStack source, ServerPlayer player, String slot,
                                      CallbackInfoReturnable<Integer> cir) {
        CommandInterceptEvent.CuriosClearCommandEvent event = new CommandInterceptEvent.CuriosClearCommandEvent(source, "/curios clear ...");
        MinecraftForge.EVENT_BUS.post(event);
        if (event.isCanceled()) cir.setReturnValue(0);
    }

}