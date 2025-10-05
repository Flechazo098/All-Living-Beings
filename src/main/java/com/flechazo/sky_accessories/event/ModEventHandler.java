package com.flechazo.sky_accessories.event;

import com.flechazo.sky_accessories.utils.Util;
import com.flechazo.sky_accessories.config.Config;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.eventbus.api.SubscribeEvent;

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
}
