package com.flechazo.all_living_beings.mixin;

import com.flechazo.all_living_beings.event.CommandInterceptEvent;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.commands.ClearInventoryCommands;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Collection;
import java.util.function.Predicate;

@Mixin(ClearInventoryCommands.class)
public class ClearInventoryCommandsMixin {

    @Inject(method = "clearInventory", at = @At("HEAD"), cancellable = true)
    private static void onClearInventory(CommandSourceStack source, Collection<ServerPlayer> targets,
                                         Predicate<ItemStack> predicate, int maxCount,
                                         CallbackInfoReturnable<Integer> cir) {
        CommandInterceptEvent.ClearCommandEvent event = new CommandInterceptEvent.ClearCommandEvent(source, "/clear ...");
        MinecraftForge.EVENT_BUS.post(event);
        if (event.isCanceled()) cir.setReturnValue(0);
    }
}