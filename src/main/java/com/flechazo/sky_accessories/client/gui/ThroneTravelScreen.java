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
        int mainW = 180;
        int sideW = 90;
        int bindW = 60;
        int bh = 20;
        int x = (w - (bindW + mainW + sideW)) / 2;
        int y = 40;
        List<ResourceLocation> dims = new ArrayList<>(ClientCache.DIMENSIONS);
        if (dims.isEmpty()) {
            addRenderableWidget(Button.builder(Component.translatable("message.sky_accessories.travel_loading"), b -> {})
                    .bounds(x, y, mainW, bh).build());
            return;
        }
        int i = 0;
        for (var id : dims) {
            int by = y + i * (bh + 4);
            String key = "dimension." + id.getNamespace() + "." + id.getPath();
            // 绑定按钮（左侧）
            addRenderableWidget(Button.builder(Component.translatable("gui.sky_accessories.bind"), b -> {
                SkyNet.sendBindTeleport(id);
            }).bounds(x, by, bindW, bh).build());
            // 主传送按钮（中间）
            addRenderableWidget(Button.builder(Component.translatable(key), b -> {
                SkyNet.sendTeleportRequest(id, 0);
                onClose();
            }).bounds(x + bindW + 4, by, mainW, bh).build());
            // 特种按钮（右侧）
            ResourceLocation overworld = new ResourceLocation("minecraft", "overworld");
            ResourceLocation the_end = new ResourceLocation("minecraft", "the_end");
            ResourceLocation the_nether = new ResourceLocation("minecraft", "the_nether");
            if (id.equals(overworld)) {
                addRenderableWidget(Button.builder(Component.translatable("gui.sky_accessories.tp_spawn"), b -> {
                    SkyNet.sendTeleportRequest(id, 1);
                    onClose();
                }).bounds(x + bindW + 4 + mainW + 4, by, sideW, bh).build());
            } else if (id.equals(the_end)) {
                addRenderableWidget(Button.builder(Component.translatable("gui.sky_accessories.tp_end_platform"), b -> {
                    SkyNet.sendTeleportRequest(id, 2);
                    onClose();
                }).bounds(x + bindW + 4 + mainW + 4, by, sideW, bh).build());
            } else if (id.equals(the_nether)) {
                addRenderableWidget(Button.builder(Component.translatable("gui.sky_accessories.tp_nether_roof"), b -> {
                    SkyNet.sendTeleportRequest(id, 3);
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
    public boolean isPauseScreen() { return false; }
}