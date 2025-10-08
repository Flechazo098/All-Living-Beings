package com.flechazo.all_living_beings.client.gui;

import com.flechazo.all_living_beings.network.NetworkHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class MainMenuScreen extends Screen {
    public MainMenuScreen() {
        super(Component.translatable("gui.all_living_beings.main_menu.title"));
    }

    @Override
    protected void init() {
        int w = this.width;
        int h = this.height;
        int bw = 180;
        int bh = 20;
        int x = (w - bw) / 2;
        int y = h / 2 - bh - 6;

        addRenderableWidget(Button.builder(Component.translatable("gui.all_living_beings.menu.travel"), b -> {
            NetworkHandler.requestDimensions();
        }).bounds(x, y, bw, bh).build());

        addRenderableWidget(Button.builder(Component.translatable("gui.all_living_beings.menu.config"), b -> {
            NetworkHandler.requestGodConfig();
        }).bounds(x, y + bh + 8, bw, bh).build());
    }

    @Override
    public void render(GuiGraphics gg, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(gg);
        super.render(gg, mouseX, mouseY, partialTicks);
    }

    @Override
    public void onClose() {
        super.onClose();
        Minecraft.getInstance().setScreen(null);
    }

    @Override
    public boolean isPauseScreen() { return false; }
}