package com.flechazo.all_living_beings.client.gui;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class EntityPickerScreen extends AbstractPickerScreen {

    public EntityPickerScreen(Screen parent, List<String> initialIds, Consumer<List<ResourceLocation>> onComplete) {
        super(Component.translatable("gui.all_living_beings.select_boss_entities"), parent, onComplete);
        for (String id : initialIds) {
            try {
                this.selectedItems.add(new ResourceLocation(id));
            } catch (Exception ignored) {}
        }
    }

    @Override
    protected void initializeItems() {
        var registry = ForgeRegistries.ENTITY_TYPES;
        allItems = registry.getValues().stream()
                .filter(type -> type.getCategory() == MobCategory.MONSTER)
                .map(registry::getKey)
                .filter(Objects::nonNull)
                .sorted(Comparator.comparing(this::getItemDisplayName))
                .collect(Collectors.toList());

        filteredItems = new ArrayList<>(allItems);
    }

    @Override
    protected String getItemDisplayName(ResourceLocation id) {
        EntityType<?> type = ForgeRegistries.ENTITY_TYPES.getValue(id);
        if (type != null) {
            return type.getDescription().getString();
        }
        return id.toString();
    }

    @Override
    protected Component getSearchPlaceholder() {
        return Component.translatable("gui.all_living_beings.search_entities");
    }

    @Override
    protected void createBottomButtons() {
        int buttonX = layout.centerX() - layout.listWidth() / 2;
        addConfirmButton(buttonX, layout.buttonY(), 80);
        addSelectAllButton(buttonX + 85, layout.buttonY(), 100);
        addClearSelectionButton(buttonX + 190, layout.buttonY(), 120);
    }
}