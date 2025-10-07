package com.flechazo.all_living_beings.event.handler;

import com.flechazo.all_living_beings.AllLivingBeings;
import com.flechazo.all_living_beings.config.Config;
import com.flechazo.all_living_beings.event.CommandInterceptEvent;
import com.flechazo.all_living_beings.event.LivingSetHealthEvent;
import com.flechazo.all_living_beings.registry.ModItems;
import com.flechazo.all_living_beings.utils.Util;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = AllLivingBeings.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ModEventHandler {

    @SubscribeEvent
    public static void onPlayerAbsoluteDefense(LivingSetHealthEvent event) {
        if (event.getEntity() instanceof ServerPlayer sp &&
                Util.isOwnerActive(sp) &&
                Config.COMMON.absoluteDefense.get()) {
            event.setNewHealth(Math.max(1f, event.getEntity().getHealth()));
            event.cancel();
        }
    }

    @SubscribeEvent
    public static void onGiveCommand(CommandInterceptEvent.GiveCommandEvent event) {
        var src = event.getSource();
        if (src.getEntity() instanceof ServerPlayer sp && Util.isOwnerActive(sp)) {
            if (ModItems.HEAVENLY_THRONE.get().equals(event.getItem())) {
                event.getSource().sendFailure(Component.translatable(
                        "message.all_living_beings.give_blocked_item",
                        event.getItem().getDescription()
                ));
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    public static void onClearCommand(CommandInterceptEvent.ClearCommandEvent event) {
        var src = event.getSource();
        if (src.getEntity() instanceof ServerPlayer sp && Util.isOwnerActive(sp)) {
            src.sendFailure(Component.translatable("message.all_living_beings.clear_blocked"));
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onCuriosClearCommand(CommandInterceptEvent.CuriosClearCommandEvent event) {
        var src = event.getSource();
        if (src.getEntity() instanceof ServerPlayer sp && Util.isOwnerActive(sp)) {
            src.sendFailure(Component.translatable("message.all_living_beings.curios_clear_blocked"));
            event.setCanceled(true);
        }
    }
}
