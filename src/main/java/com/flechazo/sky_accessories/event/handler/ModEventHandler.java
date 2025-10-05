package com.flechazo.sky_accessories.event.handler;

import com.flechazo.sky_accessories.SkyAccessories;
import com.flechazo.sky_accessories.event.CommandInterceptEvent;
import com.flechazo.sky_accessories.event.LivingSetHealthEvent;
import com.flechazo.sky_accessories.utils.Util;
import com.flechazo.sky_accessories.config.Config;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = SkyAccessories.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
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
            src.sendFailure(Component.translatable("message.sky_accessories.give_blocked"));
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onClearCommand(CommandInterceptEvent.ClearCommandEvent event) {
        var src = event.getSource();
        if (src.getEntity() instanceof ServerPlayer sp && Util.isOwnerActive(sp)) {
            src.sendFailure(Component.translatable("message.sky_accessories.clear_blocked"));
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onCuriosClearCommand(CommandInterceptEvent.CuriosClearCommandEvent event) {
        var src = event.getSource();
        if (src.getEntity() instanceof ServerPlayer sp && Util.isOwnerActive(sp)) {
            src.sendFailure(Component.translatable("message.sky_accessories.curios_clear_blocked"));
            event.setCanceled(true);
        }
    }
}
