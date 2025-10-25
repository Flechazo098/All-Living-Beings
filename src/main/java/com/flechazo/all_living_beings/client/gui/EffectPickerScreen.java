package com.flechazo.all_living_beings.client.gui;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class EffectPickerScreen extends AbstractPickerScreen {
    private final boolean positiveMode;

    public EffectPickerScreen(Screen parent, boolean positiveMode, Consumer<List<ResourceLocation>> onComplete) {
        super(Component.translatable(positiveMode ?
                "gui.all_living_beings.select_positive_effects" :
                "gui.all_living_beings.select_negative_effects"), parent, onComplete);
        this.positiveMode = positiveMode;
    }

    public EffectPickerScreen(Screen parent, boolean positiveMode, Consumer<List<ResourceLocation>> onComplete, List<String> preselectedEffects) {
        super(Component.translatable(positiveMode ?
                "gui.all_living_beings.select_positive_effects" :
                "gui.all_living_beings.select_negative_effects"), parent, onComplete, preselectedEffects);
        this.positiveMode = positiveMode;
    }

    @Override
    protected void initializeItems() {
        allItems = ForgeRegistries.MOB_EFFECTS.getKeys().stream()
                .sorted(Comparator.comparing(this::getItemDisplayName))
                .collect(Collectors.toList());
        filteredItems = new ArrayList<>(allItems);
    }

    @Override
    protected String getItemDisplayName(ResourceLocation effect) {
        MobEffect mobEffect = ForgeRegistries.MOB_EFFECTS.getValue(effect);
        if (mobEffect != null) {
            return Component.translatable(mobEffect.getDescriptionId()).getString();
        }
        return effect.toString();
    }

    @Override
    protected Component getSearchPlaceholder() {
        return Component.translatable("gui.all_living_beings.search_effects");
    }

    @Override
    protected void createBottomButtons() {
        int buttonWidth = 85;
        int totalWidth = 4 * buttonWidth + 3 * BUTTON_GAP;
        int startX = layout.centerX() - totalWidth / 2;

        addSelectAllButton(startX, layout.buttonY(), buttonWidth);
        addClearSelectionButton(startX + buttonWidth + BUTTON_GAP, layout.buttonY(), buttonWidth);
        addSaveButton(startX + 2 * (buttonWidth + BUTTON_GAP), layout.buttonY(), buttonWidth);
        addCancelButton(startX + 3 * (buttonWidth + BUTTON_GAP), layout.buttonY(), buttonWidth);
    }
}