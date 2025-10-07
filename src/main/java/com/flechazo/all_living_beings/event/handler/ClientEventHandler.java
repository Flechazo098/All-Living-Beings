package com.flechazo.all_living_beings.event.handler;

import com.flechazo.all_living_beings.AllLivingBeings;
import com.flechazo.all_living_beings.client.ClientKeyMappings;
import com.flechazo.all_living_beings.registry.NetworkHandler;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(value = Dist.CLIENT, modid = AllLivingBeings.MODID)
public class ClientEventHandler {
    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent e) {
        if (e.phase != TickEvent.Phase.END) return;
        var mc = Minecraft.getInstance();
        if (mc.player == null) return;

        while (ClientKeyMappings.OPEN_THRONE_GUI.consumeClick()) {
            NetworkHandler.requestDimensions();
        }

        while (ClientKeyMappings.OPEN_CONFIG_GUI.consumeClick()) {
            NetworkHandler.requestGodConfig();
        }
    }
}