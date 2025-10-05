package com.flechazo.sky_accessories.client;

import com.flechazo.sky_accessories.network.SkyNet;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(value = Dist.CLIENT, modid = com.flechazo.sky_accessories.SkyAccessories.MODID)
public class ClientEvents {
    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent e) {
        if (e.phase != TickEvent.Phase.END) return;
        var mc = Minecraft.getInstance();
        if (mc.player == null) return;
        while (ClientKeyMappings.OPEN_THRONE_GUI.consumeClick()) {
            SkyNet.requestDimensions();
        }
    }
}