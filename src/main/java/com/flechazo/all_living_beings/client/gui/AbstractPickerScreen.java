package com.flechazo.all_living_beings.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public abstract class AbstractPickerScreen extends Screen {
    protected static final int ITEM_HEIGHT = 22;
    protected static final int MAX_VISIBLE_ITEMS = 8;
    protected static final int BUTTON_HEIGHT = 20;
    protected static final int BUTTON_GAP = 5;
    protected static final int MARGIN = 20;

    protected final Screen parent;
    protected final Consumer<List<ResourceLocation>> onComplete;
    protected final Set<ResourceLocation> selectedItems;

    protected EditBox searchBox;
    protected List<ResourceLocation> allItems;
    protected List<ResourceLocation> filteredItems;
    protected int scrollOffset = 0;
    protected LayoutInfo layout;

    protected AbstractPickerScreen(Component title, Screen parent, Consumer<List<ResourceLocation>> onComplete) {
        super(title);
        this.parent = parent;
        this.onComplete = onComplete;
        this.selectedItems = new HashSet<>();
    }

    @Override
    protected void init() {
        super.init();
        if (allItems == null || allItems.isEmpty()) {
            initializeItems();
        }
        calculateLayout();
        createWidgets();
    }

    protected abstract void initializeItems();
    protected abstract String getItemDisplayName(ResourceLocation item);
    protected abstract Component getSearchPlaceholder();
    protected abstract void createBottomButtons();

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
                searchWidth, BUTTON_HEIGHT, getSearchPlaceholder());
        searchBox.setResponder(this::updateFilter);
        addRenderableWidget(searchBox);

        addRenderableWidget(Button.builder(Component.translatable("gui.all_living_beings.clear_search"),
                b -> { searchBox.setValue(""); updateFilter(""); })
                .bounds(layout.centerX + layout.listWidth / 2 - 95, layout.searchY, 95, BUTTON_HEIGHT).build());

        createScrollButtons();
        createBottomButtons();
        updateScrollOffset();
    }

    private void createScrollButtons() {
        int scrollBtnX = layout.centerX + layout.listWidth / 2 + 10;

        addRenderableWidget(Button.builder(Component.literal("▲"), b -> {
            scrollOffset = Math.max(0, scrollOffset - 1);
        }).bounds(scrollBtnX, layout.listStartY, 20, 20).build());

        addRenderableWidget(Button.builder(Component.literal("▼"), b -> {
            scrollOffset = Math.min(Math.max(0, filteredItems.size() - layout.visibleItems), scrollOffset + 1);
        }).bounds(scrollBtnX, layout.listStartY + layout.listHeight - 20, 20, 20).build());
    }

    protected void addSelectAllButton(int x, int y, int width) {
        addRenderableWidget(Button.builder(Component.translatable("gui.all_living_beings.select_all"), b -> {
            selectedItems.clear();
            selectedItems.addAll(filteredItems);
        }).bounds(x, y, width, BUTTON_HEIGHT).build());
    }

    protected void addClearSelectionButton(int x, int y, int width) {
        addRenderableWidget(Button.builder(Component.translatable("gui.all_living_beings.clear_selection"), b -> {
            selectedItems.clear();
        }).bounds(x, y, width, BUTTON_HEIGHT).build());
    }

    protected void addConfirmButton(int x, int y, int width) {
        addRenderableWidget(Button.builder(Component.translatable("gui.all_living_beings.confirm"), b -> {
            onComplete.accept(new ArrayList<>(selectedItems));
            Minecraft.getInstance().setScreen(parent);
        }).bounds(x, y, width, BUTTON_HEIGHT).build());
    }

    protected void addSaveButton(int x, int y, int width) {
        addRenderableWidget(Button.builder(Component.translatable("gui.all_living_beings.save_selection"), b -> {
            onComplete.accept(new ArrayList<>(selectedItems));
            Minecraft.getInstance().setScreen(parent);
        }).bounds(x, y, width, BUTTON_HEIGHT).build());
    }

    protected void addCancelButton(int x, int y, int width) {
        addRenderableWidget(Button.builder(Component.translatable("gui.cancel"), b -> {
            Minecraft.getInstance().setScreen(parent);
        }).bounds(x, y, width, BUTTON_HEIGHT).build());
    }

    private void updateFilter(String query) {
        String lowerQuery = query.toLowerCase().trim();
        if (lowerQuery.isEmpty()) {
            filteredItems = new ArrayList<>(allItems);
        } else {
            filteredItems = allItems.stream()
                    .filter(item -> {
                        String displayName = getItemDisplayName(item).toLowerCase();
                        String id = item.toString().toLowerCase();
                        return displayName.contains(lowerQuery) || id.contains(lowerQuery);
                    })
                    .collect(Collectors.toList());
        }
        scrollOffset = 0;
        updateScrollOffset();
    }

    private void updateScrollOffset() {
        scrollOffset = Math.min(scrollOffset, Math.max(0, filteredItems.size() - layout.visibleItems));
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);

        graphics.drawCenteredString(this.font, this.title, layout.centerX, layout.titleY, 0xFFFFFF);
        Component hintText = Component.translatable("gui.all_living_beings.click_to_select_hint");
        graphics.drawCenteredString(this.font, hintText, layout.centerX, layout.hintY, 0xAAAAAA);

        Component countText = Component.translatable("gui.all_living_beings.selected_count", selectedItems.size());
        int listX = layout.centerX - layout.listWidth / 2;
        graphics.drawString(this.font, countText, listX, layout.countY, 0x00FF00);

        renderItemList(graphics, mouseX, mouseY);
        renderScrollBar(graphics);
    }

    private void renderItemList(GuiGraphics graphics, int mouseX, int mouseY) {
        int listX = layout.centerX - layout.listWidth / 2;
        graphics.fill(listX - 2, layout.listStartY - 2,
                listX + layout.listWidth + 2, layout.listStartY + layout.listHeight + 2, 0x80000000);

        for (int i = 0; i < layout.visibleItems && i + scrollOffset < filteredItems.size(); i++) {
            ResourceLocation item = filteredItems.get(i + scrollOffset);
            int itemY = layout.listStartY + i * ITEM_HEIGHT;
            renderItem(graphics, item, listX, itemY, mouseX, mouseY);
        }
    }

    private void renderItem(GuiGraphics graphics, ResourceLocation item, int listX, int itemY, int mouseX, int mouseY) {
        boolean isSelected = selectedItems.contains(item);
        boolean isHovered = mouseX >= listX && mouseX <= listX + layout.listWidth &&
                mouseY >= itemY && mouseY <= itemY + ITEM_HEIGHT;

        int bgColor = isSelected ? 0x8000FF00 : (isHovered ? 0x80FFFFFF : 0x40000000);
        graphics.fill(listX, itemY, listX + layout.listWidth, itemY + ITEM_HEIGHT, bgColor);

        if (isSelected) {
            graphics.drawString(this.font, "✓", listX + layout.listWidth - 15, itemY + 6, 0x00FF00);
        }

        String displayName = getItemDisplayName(item);
        graphics.drawString(this.font, displayName, listX + 5, itemY + 3, 0xFFFFFF);
        graphics.drawString(this.font, item.toString(), listX + 5, itemY + 13, 0xAAAAAA);
    }

    private void renderScrollBar(GuiGraphics graphics) {
        if (filteredItems.size() > layout.visibleItems) {
            int scrollBarX = layout.centerX + layout.listWidth / 2 + 35;
            int thumbHeight = Math.max(10, layout.listHeight * layout.visibleItems / filteredItems.size());
            int thumbY = layout.listStartY + (layout.listHeight - thumbHeight) * scrollOffset /
                    Math.max(1, filteredItems.size() - layout.visibleItems);

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
            if (clickedIndex >= 0 && clickedIndex < filteredItems.size()) {
                ResourceLocation item = filteredItems.get(clickedIndex);
                if (selectedItems.contains(item)) {
                    selectedItems.remove(item);
                } else {
                    selectedItems.add(item);
                }
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        if (filteredItems.size() > layout.visibleItems) {
            scrollOffset = Math.max(0, Math.min(filteredItems.size() - layout.visibleItems, scrollOffset - (int) delta));
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
    public boolean isPauseScreen() {
        return false;
    }

    protected record LayoutInfo(int centerX, int listWidth, int titleY, int backButtonY, int searchY, int hintY,
                              int countY, int listStartY, int listHeight, int visibleItems, int buttonY) {}
}