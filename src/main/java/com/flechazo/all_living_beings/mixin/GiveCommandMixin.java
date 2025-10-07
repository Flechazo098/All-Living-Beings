package com.flechazo.all_living_beings.mixin;

import com.flechazo.all_living_beings.event.CommandInterceptEvent;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.item.ItemInput;
import net.minecraft.server.commands.GiveCommand;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraftforge.common.MinecraftForge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Collection;

@Mixin(GiveCommand.class)
public class GiveCommandMixin {

    @Inject(method = "giveItem", at = @At("HEAD"))
    private static void onGiveItem(
            CommandSourceStack source,
            ItemInput itemInput,
            Collection<ServerPlayer> targets,
            int count,
            CallbackInfoReturnable<Integer> cir) {

        Item item = itemInput.getItem();

        CommandInterceptEvent.GiveCommandEvent event = new CommandInterceptEvent.GiveCommandEvent(
                source, "/give", item, count
        );
        MinecraftForge.EVENT_BUS.post(event);
    }
}