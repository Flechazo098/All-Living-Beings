package com.flechazo.all_living_beings.client;

import com.flechazo.all_living_beings.AllLivingBeings;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

@Mod.EventBusSubscriber(modid = AllLivingBeings.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientKeyMappings {
    public static final KeyMapping OPEN_THRONE_GUI = new KeyMapping(
            "key.all_living_beings.open_travel",
            GLFW.GLFW_KEY_G,
            "key.categories.all_living_beings");

    @SubscribeEvent
    public static void registerKeys(RegisterKeyMappingsEvent e) {
        e.register(OPEN_THRONE_GUI);
    }
}