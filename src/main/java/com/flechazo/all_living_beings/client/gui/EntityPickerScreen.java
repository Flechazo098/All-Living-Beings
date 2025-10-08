package com.flechazo.all_living_beings.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class EntityPickerScreen extends Screen {
    private static final int ITEM_HEIGHT = 22;
    private static final int MAX_VISIBLE_ITEMS = 8;
    private static final int BUTTON_HEIGHT = 20;
    private static final int BUTTON_GAP = 5;
    private static final int MARGIN = 20;

    private final Screen parent;
    private final Consumer<List<ResourceLocation>> onComplete;
    private final Set<ResourceLocation> selectedEntities;

    private EditBox searchBox;
    private List<ResourceLocation> allEntities;
    private List<ResourceLocation> filteredEntities;
    private int scrollOffset = 0;
    private LayoutInfo layout;

    public EntityPickerScreen(Screen parent, List<String> initialIds,
                              Consumer<List<ResourceLocation>> onComplete) {
        super(Component.translatable("gui.all_living_beings.select_boss_entities"));
        this.parent = parent;
        this.onComplete = onComplete;
        this.selectedEntities = new HashSet<>();
        // 预选已有ID
        for (String id : initialIds) {
            try {
                this.selectedEntities.add(new ResourceLocation(id));
            } catch (Exception ignored) {}
        }
    }

    @Override
    protected void init() {
        super.init();
        if (allEntities == null || allEntities.isEmpty()) {
            initializeEntities();
        }
        calculateLayout();
        createWidgets();
    }

    private void initializeEntities() {
        var registry = ForgeRegistries.ENTITY_TYPES;
        allEntities = registry.getValues().stream()
                .filter(type -> {
                    MobCategory cat = type.getCategory();
                    return cat == MobCategory.MONSTER;
                })
                .map(registry::getKey)
                .filter(Objects::nonNull)
                .sorted(Comparator.comparing(this::getEntityDisplayName))
                .collect(Collectors.toList());

        filteredEntities = new ArrayList<>(allEntities);
    }


    private void calculateLayout() {
        int centerX = this.width / 2;
        int listWidth = Math.min(350, this.width - 80);

        int titleY = 15;
        int backButtonY = 10;
        int searchY = 45;
        int hintY = 75;
        int countY = 90;
        int listStartY = 105;

        int buttonAreaHeight = BUTTON_HEIGHT + MARGIN;
        int buttonY = this.height - buttonAreaHeight;

        int availableHeight = buttonY - listStartY - MARGIN;
        int maxListHeight = MAX_VISIBLE_ITEMS * ITEM_HEIGHT;
        int listHeight = Math.min(maxListHeight, availableHeight);
        int visibleItems = Math.max(1, listHeight / ITEM_HEIGHT);

        layout = new LayoutInfo(centerX, listWidth, titleY, backButtonY, searchY, hintY,
                countY, listStartY, listHeight, visibleItems, buttonY);
    }

    private void createWidgets() {
        addRenderableWidget(Button.builder(Component.translatable("gui.all_living_beings.back"),
                b -> Minecraft.getInstance().setScreen(parent))
                .bounds(10, layout.backButtonY, 60, BUTTON_HEIGHT).build());

        int searchWidth = layout.listWidth - 100;
        searchBox = new EditBox(this.font,
                layout.centerX - layout.listWidth / 2, layout.searchY,
                searchWidth, BUTTON_HEIGHT,
                Component.translatable("gui.all_living_beings.search_entities"));
        searchBox.setResponder(this::updateFilter);
        addRenderableWidget(searchBox);

        addRenderableWidget(Button.builder(Component.translatable("gui.all_living_beings.clear_search"),
                b -> { searchBox.setValue(""); updateFilter(""); })
                .bounds(layout.centerX + layout.listWidth / 2 - 90, layout.searchY, 90, BUTTON_HEIGHT).build());

        createScrollButtons();
        createBottomButtons();
    }

    private void createScrollButtons() {
        addRenderableWidget(Button.builder(Component.literal("▲"), b -> {
            scrollOffset = Math.max(0, scrollOffset - 1);
        }).bounds(layout.centerX + layout.listWidth / 2 + 25, layout.listStartY - 20, 20, 18).build());

        addRenderableWidget(Button.builder(Component.literal("▼"), b -> {
            scrollOffset = Math.min(Math.max(0, filteredEntities.size() - layout.visibleItems), scrollOffset + 1);
        }).bounds(layout.centerX + layout.listWidth / 2 + 25, layout.listStartY + layout.listHeight + 2, 20, 18).build());
    }

    private void createBottomButtons() {
        int buttonX = layout.centerX - layout.listWidth / 2;
        addRenderableWidget(Button.builder(Component.translatable("gui.all_living_beings.confirm"), b -> {
            onComplete.accept(new ArrayList<>(selectedEntities));
        }).bounds(buttonX, layout.buttonY, 80, BUTTON_HEIGHT).build());

        addRenderableWidget(Button.builder(Component.translatable("gui.all_living_beings.select_all"), b -> {
            selectedEntities.clear();
            selectedEntities.addAll(filteredEntities);
        }).bounds(buttonX + 85, layout.buttonY, 100, BUTTON_HEIGHT).build());

        addRenderableWidget(Button.builder(Component.translatable("gui.all_living_beings.clear_selection"), b -> {
            selectedEntities.clear();
        }).bounds(buttonX + 190, layout.buttonY, 120, BUTTON_HEIGHT).build());
    }

    private void updateFilter(String query) {
        String q = query.toLowerCase(Locale.ROOT);
        filteredEntities = allEntities.stream()
                .filter(id -> {
                    String name = getEntityDisplayName(id).toLowerCase(Locale.ROOT);
                    return id.toString().toLowerCase(Locale.ROOT).contains(q) || name.contains(q);
                })
                .collect(Collectors.toList());
        updateScrollOffset();
    }

    private void updateScrollOffset() {
        scrollOffset = Math.min(scrollOffset, Math.max(0, filteredEntities.size() - layout.visibleItems));
    }

    private String getEntityDisplayName(ResourceLocation id) {
        EntityType<?> type = ForgeRegistries.ENTITY_TYPES.getValue(id);
        if (type != null) {
            return type.getDescription().getString();
        }
        return id.toString();
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);

        graphics.drawCenteredString(this.font, this.title, layout.centerX, layout.titleY, 0xFFFFFF);
        Component hintText = Component.translatable("gui.all_living_beings.click_to_select_hint");
        graphics.drawCenteredString(this.font, hintText, layout.centerX, layout.hintY, 0xAAAAAA);

        Component countText = Component.translatable("gui.all_living_beings.selected_count", selectedEntities.size());
        int listX = layout.centerX - layout.listWidth / 2;
        graphics.drawString(this.font, countText, listX, layout.countY, 0x00FF00);

        renderEntityList(graphics, mouseX, mouseY);
        renderScrollBar(graphics);
    }

    private void renderEntityList(GuiGraphics graphics, int mouseX, int mouseY) {
        int listX = layout.centerX - layout.listWidth / 2;
        graphics.fill(listX - 2, layout.listStartY - 2,
                listX + layout.listWidth + 2, layout.listStartY + layout.listHeight + 2, 0x80000000);

        for (int i = 0; i < layout.visibleItems && i + scrollOffset < filteredEntities.size(); i++) {
            ResourceLocation id = filteredEntities.get(i + scrollOffset);
            int itemY = layout.listStartY + i * ITEM_HEIGHT;
            renderEntityItem(graphics, id, listX, itemY, mouseX, mouseY);
        }
    }

    private void renderEntityItem(GuiGraphics graphics, ResourceLocation id, int listX, int itemY, int mouseX, int mouseY) {
        boolean isSelected = selectedEntities.contains(id);
        boolean isHovered = mouseX >= listX && mouseX <= listX + layout.listWidth &&
                mouseY >= itemY && mouseY <= itemY + ITEM_HEIGHT;

        int bgColor = isSelected ? 0x8000FF00 : (isHovered ? 0x80FFFFFF : 0x40000000);
        graphics.fill(listX, itemY, listX + layout.listWidth, itemY + ITEM_HEIGHT, bgColor);

        if (isSelected) {
            graphics.drawString(this.font, "✓", listX + layout.listWidth - 15, itemY + 6, 0x00FF00);
        }

        String displayName = getEntityDisplayName(id);
        graphics.drawString(this.font, displayName, listX + 5, itemY + 3, 0xFFFFFF);
        graphics.drawString(this.font, id.toString(), listX + 5, itemY + 13, 0xAAAAAA);
    }

    private void renderScrollBar(GuiGraphics graphics) {
        if (filteredEntities.size() > layout.visibleItems) {
            int scrollBarX = layout.centerX + layout.listWidth / 2 + 35;
            int thumbHeight = Math.max(10, layout.listHeight * layout.visibleItems / filteredEntities.size());
            int thumbY = layout.listStartY + (layout.listHeight - thumbHeight) * scrollOffset /
                    Math.max(1, filteredEntities.size() - layout.visibleItems);

            graphics.fill(scrollBarX, layout.listStartY, scrollBarX + 6, layout.listStartY + layout.listHeight, 0x80000000);
            graphics.fill(scrollBarX + 1, thumbY, scrollBarX + 5, thumbY + thumbHeight, 0xFFAAAAAA);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (super.mouseClicked(mouseX, mouseY, button)) {
            return true;
        }

        int listX = layout.centerX - layout.listWidth / 2;
        if (mouseX >= listX && mouseX <= listX + layout.listWidth &&
                mouseY >= layout.listStartY && mouseY <= layout.listStartY + layout.listHeight) {
            int clickedIndex = (int) ((mouseY - layout.listStartY) / ITEM_HEIGHT) + scrollOffset;
            if (clickedIndex >= 0 && clickedIndex < filteredEntities.size()) {
                ResourceLocation id = filteredEntities.get(clickedIndex);
                if (selectedEntities.contains(id)) selectedEntities.remove(id);
                else selectedEntities.add(id);
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        if (filteredEntities.size() > layout.visibleItems) {
            scrollOffset = Math.max(0, Math.min(filteredEntities.size() - layout.visibleItems, scrollOffset - (int) delta));
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, delta);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == 256) { // ESC
            Minecraft.getInstance().setScreen(parent);
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean isPauseScreen() { return false; }

    private record LayoutInfo(int centerX, int listWidth, int titleY, int backButtonY, int searchY, int hintY,
                              int countY, int listStartY, int listHeight, int visibleItems, int buttonY) {}
}