package com.flechazo.sky_accessories.client.gui;

import com.flechazo.sky_accessories.client.ClientCache;
import com.flechazo.sky_accessories.network.SkyNet;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

public class ThroneTravelScreen extends Screen {
    public ThroneTravelScreen() {
        super(Component.translatable("gui.sky_accessories.travel.title"));
    }

    @Override
    protected void init() {
        int w = this.width;
        int bw = 180;
        int bh = 20;
        int x = (w - bw) / 2;
        int y = 40;
        List<ResourceLocation> dims = new ArrayList<>(ClientCache.DIMENSIONS);
        if (dims.isEmpty()) {
            // 尚未同步，提示用户
            addRenderableWidget(Button.builder(Component.translatable("message.sky_accessories.travel_loading"), b -> {
            }).bounds(x, y, bw, bh).build());
            return;
        }
        int i = 0;
        for (var id : dims) {
            int by = y + i * (bh + 4);
            addRenderableWidget(Button.builder(Component.literal(id.toString()), b -> {
                SkyNet.sendTeleportRequest(id);
                onClose();
            }).bounds(x, by, bw, bh).build());
            i++;
        }
    }

    @Override
    public void onClose() {
        super.onClose();
        Minecraft.getInstance().setScreen(null);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}