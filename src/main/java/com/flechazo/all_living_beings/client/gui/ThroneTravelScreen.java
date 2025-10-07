package com.flechazo.all_living_beings.client.gui;

import com.flechazo.all_living_beings.client.ClientCache;
import com.flechazo.all_living_beings.registry.NetworkHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

public class ThroneTravelScreen extends Screen {
    public ThroneTravelScreen() {
        super(Component.translatable("gui.all_living_beings.travel.title"));
    }

    @Override
    protected void init() {
        int w = this.width;
        int mainW = 180;
        int sideW = 90;
        int bindW = 60;
        int bh = 20;
        int x = (w - (bindW + mainW + sideW)) / 2;
        int y = 40;
        List<ResourceLocation> dims = new ArrayList<>(ClientCache.DIMENSIONS);
        if (dims.isEmpty()) {
            addRenderableWidget(Button.builder(Component.translatable("message.all_living_beings.travel_loading"), b -> {
                    })
                    .bounds(x, y, mainW, bh).build());
            return;
        }
        int i = 0;
        for (var id : dims) {
            int by = y + i * (bh + 4);
            String key = "dimension." + id.getNamespace() + "." + id.getPath();

            addRenderableWidget(Button.builder(Component.translatable("gui.all_living_beings.bind"), b -> {
                NetworkHandler.sendBindTeleport(id);
            }).bounds(x, by, bindW, bh).build());

            addRenderableWidget(Button.builder(Component.translatable(key), b -> {
                NetworkHandler.sendTeleportRequest(id, 0);
                onClose();
            }).bounds(x + bindW + 4, by, mainW, bh).build());

            ResourceLocation overworld = new ResourceLocation("minecraft", "overworld");
            ResourceLocation the_end = new ResourceLocation("minecraft", "the_end");
            ResourceLocation the_nether = new ResourceLocation("minecraft", "the_nether");
            if (id.equals(overworld)) {
                addRenderableWidget(Button.builder(Component.translatable("gui.all_living_beings.tp_spawn"), b -> {
                    NetworkHandler.sendTeleportRequest(id, 1);
                    onClose();
                }).bounds(x + bindW + 4 + mainW + 4, by, sideW, bh).build());
            } else if (id.equals(the_end)) {
                addRenderableWidget(Button.builder(Component.translatable("gui.all_living_beings.tp_end_platform"), b -> {
                    NetworkHandler.sendTeleportRequest(id, 2);
                    onClose();
                }).bounds(x + bindW + 4 + mainW + 4, by, sideW, bh).build());
            } else if (id.equals(the_nether)) {
                addRenderableWidget(Button.builder(Component.translatable("gui.all_living_beings.tp_nether_roof"), b -> {
                    NetworkHandler.sendTeleportRequest(id, 3);
                    onClose();
                }).bounds(x + bindW + 4 + mainW + 4, by, sideW, bh).build());
            }
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