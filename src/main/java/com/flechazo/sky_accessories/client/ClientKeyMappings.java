package com.flechazo.sky_accessories.client;

import com.flechazo.sky_accessories.SkyAccessories;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

@Mod.EventBusSubscriber(modid = SkyAccessories.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientKeyMappings {
    public static final KeyMapping OPEN_THRONE_GUI = new KeyMapping(
            "key.sky_accessories.open_travel",
            GLFW.GLFW_KEY_G,
            "key.categories.sky_accessories");

    public static final KeyMapping OPEN_CONFIG_GUI = new KeyMapping(
            "key.sky_accessories.open_config",
            GLFW.GLFW_KEY_H,
            "key.categories.sky_accessories");

    @SubscribeEvent
    public static void registerKeys(RegisterKeyMappingsEvent e) {
        e.register(OPEN_THRONE_GUI);
        e.register(OPEN_CONFIG_GUI);
    }
}