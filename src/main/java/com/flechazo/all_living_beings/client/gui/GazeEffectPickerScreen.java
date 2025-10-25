package com.flechazo.all_living_beings.client.gui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

public class GazeEffectPickerScreen extends AbstractPickerScreen {
    private final List<String> initialEffectIds;
    private final List<Integer> initialDurations;
    private final BiConsumer<List<ResourceLocation>, List<Integer>> onComplete;
    private final Map<ResourceLocation, EditBox> durationBoxes = new HashMap<>();
    private final Map<ResourceLocation, Integer> effectDurations = new HashMap<>();

    public GazeEffectPickerScreen(Screen parent, List<String> effectIds, List<Integer> durations, BiConsumer<List<ResourceLocation>, List<Integer>> onComplete) {
        super(Component.translatable("gui.all_living_beings.select_gaze_effects"), parent, null);
        this.initialEffectIds = new ArrayList<>(effectIds);
        this.initialDurations = new ArrayList<>(durations);
        this.onComplete = onComplete;

        for (int i = 0; i < Math.min(effectIds.size(), durations.size()); i++) {
            ResourceLocation effect = new ResourceLocation(effectIds.get(i));
            effectDurations.put(effect, durations.get(i));
        }
    }

    @Override
    protected void initializeItems() {
        allItems = ForgeRegistries.MOB_EFFECTS.getKeys().stream()
                .sorted(Comparator.comparing(this::getItemDisplayName))
                .collect(Collectors.toList());
        filteredItems = new ArrayList<>(allItems);

        for (String effectId : initialEffectIds) {
            ResourceLocation effect = new ResourceLocation(effectId);
            if (allItems.contains(effect)) {
                selectedItems.add(effect);
            }
        }
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
        return Component.translatable("gui.all_living_beings.search_gaze_effects");
    }

    private void createDurationInputs() {
        durationBoxes.clear();

        int listX = layout.centerX() - layout.listWidth() / 2;
        for (int i = 0; i < layout.visibleItems() && i + scrollOffset < filteredItems.size(); i++) {
            ResourceLocation item = filteredItems.get(i + scrollOffset);
            if (selectedItems.contains(item)) {
                int itemY = layout.listStartY() + i * ITEM_HEIGHT;
                EditBox durationBox = new EditBox(font, listX + layout.listWidth() - 55, itemY + 2, 50, ITEM_HEIGHT - 4, Component.literal("Duration"));
                durationBox.setValue(String.valueOf(effectDurations.getOrDefault(item, 600)));
                durationBox.setFilter(s -> s.matches("\\d*") && (s.isEmpty() || Integer.parseInt(s) <= 72000));
                addRenderableWidget(durationBox);
                durationBoxes.put(item, durationBox);
            }
        }
    }

    @Override
    protected void init() {
        super.init();
        createDurationInputs();
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

    @Override
    protected void addSaveButton(int x, int y, int width) {
        Button saveButton = Button.builder(Component.translatable("gui.all_living_beings.save_selection"), b -> {
            for (Map.Entry<ResourceLocation, EditBox> entry : durationBoxes.entrySet()) {
                ResourceLocation effect = entry.getKey();
                EditBox durationBox = entry.getValue();
                if (selectedItems.contains(effect) && !durationBox.getValue().isEmpty()) {
                    try {
                        effectDurations.put(effect, Integer.parseInt(durationBox.getValue()));
                    } catch (NumberFormatException e) {
                        effectDurations.put(effect, 600);
                    }
                }
            }

            List<ResourceLocation> effectIds = new ArrayList<>(selectedItems);
            List<Integer> durations = selectedItems.stream()
                    .map(effect -> effectDurations.getOrDefault(effect, 600))
                    .collect(Collectors.toList());

            onComplete.accept(effectIds, durations);
            minecraft.setScreen(parent);
        }).bounds(x, y, width, 20).build();
        addRenderableWidget(saveButton);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (super.mouseClicked(mouseX, mouseY, button)) {
            return true;
        }

        int listX = layout.centerX() - layout.listWidth() / 2;
        if (mouseX >= listX && mouseX <= listX + layout.listWidth() - 60 &&
                mouseY >= layout.listStartY() && mouseY <= layout.listStartY() + layout.listHeight()) {
            int clickedIndex = (int) ((mouseY - layout.listStartY()) / ITEM_HEIGHT) + scrollOffset;
            if (clickedIndex >= 0 && clickedIndex < filteredItems.size()) {
                ResourceLocation item = filteredItems.get(clickedIndex);
                if (selectedItems.contains(item)) {
                    selectedItems.remove(item);
                    effectDurations.remove(item);
                } else {
                    selectedItems.add(item);
                    effectDurations.put(item, 1200); // 默认60秒
                }
                init();
                return true;
            }
        }
        return false;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics);

        graphics.drawCenteredString(this.font, this.title, layout.centerX(), layout.titleY(), 0xFFFFFF);
        Component hintText = Component.translatable("gui.all_living_beings.click_to_select_hint");
        graphics.drawCenteredString(this.font, hintText, layout.centerX(), layout.hintY(), 0xAAAAAA);

        Component countText = Component.translatable("gui.all_living_beings.selected_count", selectedItems.size());
        int listX = layout.centerX() - layout.listWidth() / 2;
        graphics.drawString(this.font, countText, listX, layout.countY(), 0x00FF00);

        graphics.drawString(font, Component.translatable("gui.all_living_beings.duration_ticks").getString(),
                layout.centerX() + 100, layout.countY(), 0xFFFFFF);

        renderItemList(graphics, mouseX, mouseY);
        renderScrollBar(graphics);

        super.render(graphics, mouseX, mouseY, partialTick);
    }

    private void renderItemList(GuiGraphics graphics, int mouseX, int mouseY) {
        int listX = layout.centerX() - layout.listWidth() / 2;
        graphics.fill(listX - 2, layout.listStartY() - 2,
                listX + layout.listWidth() + 2, layout.listStartY() + layout.listHeight() + 2, 0x80000000);

        for (int i = 0; i < layout.visibleItems() && i + scrollOffset < filteredItems.size(); i++) {
            ResourceLocation item = filteredItems.get(i + scrollOffset);
            int itemY = layout.listStartY() + i * ITEM_HEIGHT;
            renderItem(graphics, item, listX, itemY, mouseX, mouseY);
        }
    }

    private void renderItem(GuiGraphics graphics, ResourceLocation item, int listX, int itemY, int mouseX, int mouseY) {
        boolean isSelected = selectedItems.contains(item);
        boolean isHovered = mouseX >= listX && mouseX <= listX + layout.listWidth() - 60 &&
                mouseY >= itemY && mouseY <= itemY + ITEM_HEIGHT;

        int bgColor = isSelected ? 0x8000FF00 : (isHovered ? 0x80FFFFFF : 0x40000000);
        graphics.fill(listX, itemY, listX + layout.listWidth() - 60, itemY + ITEM_HEIGHT, bgColor);

        if (isSelected) {
            graphics.drawString(this.font, "✓", listX + layout.listWidth() - 75, itemY + 6, 0x00FF00);
        }

        String displayName = getItemDisplayName(item);
        graphics.drawString(this.font, displayName, listX + 5, itemY + 3, 0xFFFFFF);
        graphics.drawString(this.font, item.toString(), listX + 5, itemY + 13, 0xAAAAAA);
    }

    private void renderScrollBar(GuiGraphics graphics) {
        if (filteredItems.size() > layout.visibleItems()) {
            int scrollBarX = layout.centerX() + layout.listWidth() / 2 + 35;
            int thumbHeight = Math.max(10, layout.listHeight() * layout.visibleItems() / filteredItems.size());
            int thumbY = layout.listStartY() + (layout.listHeight() - thumbHeight) * scrollOffset /
                    Math.max(1, filteredItems.size() - layout.visibleItems());

            graphics.fill(scrollBarX, layout.listStartY(), scrollBarX + 6, layout.listStartY() + layout.listHeight(), 0x80000000);
            graphics.fill(scrollBarX + 1, thumbY, scrollBarX + 5, thumbY + thumbHeight, 0xFFAAAAAA);
        }
    }
}