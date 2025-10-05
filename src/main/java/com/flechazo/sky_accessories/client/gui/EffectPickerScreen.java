package com.flechazo.sky_accessories.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class EffectPickerScreen extends Screen {
    private final Screen parent;
    private final boolean positiveMode;
    private final Consumer<List<ResourceLocation>> onComplete;

    private EditBox searchBox;
    private List<ResourceLocation> allEffects;
    private List<ResourceLocation> filteredEffects;
    private Set<ResourceLocation> selectedEffects;
    private int scrollOffset = 0;

    private static final int ITEM_HEIGHT = 22;
    private static final int MAX_VISIBLE_ITEMS = 8;
    private static final int BUTTON_HEIGHT = 20;
    private static final int BUTTON_GAP = 5;
    private static final int MARGIN = 20;

    private LayoutInfo layout;

    public EffectPickerScreen(Screen parent, boolean positiveMode, Consumer<List<ResourceLocation>> onComplete) {
        super(Component.translatable(positiveMode ?
                "gui.sky_accessories.select_positive_effects" :
                "gui.sky_accessories.select_negative_effects"));
        this.parent = parent;
        this.positiveMode = positiveMode;
        this.onComplete = onComplete;
        this.selectedEffects = new HashSet<>();
    }

    @Override
    protected void init() {
        super.init();

        initializeEffects();
        calculateLayout();
        createWidgets();
    }

    private void initializeEffects() {
        allEffects = ForgeRegistries.MOB_EFFECTS.getKeys().stream()
                .sorted(Comparator.comparing(this::getEffectDisplayName))
                .collect(Collectors.toList());
        filteredEffects = new ArrayList<>(allEffects);
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

        layout = new LayoutInfo(
                centerX, listWidth, titleY, backButtonY, searchY, hintY, countY,
                listStartY, listHeight, visibleItems, buttonY
        );
    }

    private void createWidgets() {
        addRenderableWidget(Button.builder(
                Component.translatable("gui.sky_accessories.back"),
                b -> Minecraft.getInstance().setScreen(parent)
        ).bounds(10, layout.backButtonY, 60, BUTTON_HEIGHT).build());

        int searchWidth = layout.listWidth - 100;
        searchBox = new EditBox(this.font,
                layout.centerX - layout.listWidth / 2, layout.searchY,
                searchWidth, BUTTON_HEIGHT,
                Component.translatable("gui.sky_accessories.search_effects"));
        searchBox.setResponder(this::updateFilter);
        addRenderableWidget(searchBox);

        addRenderableWidget(Button.builder(
                Component.translatable("gui.sky_accessories.clear_search"),
                b -> {
                    searchBox.setValue("");
                    updateFilter("");
                }
        ).bounds(layout.centerX + layout.listWidth / 2 - 95, layout.searchY, 95, BUTTON_HEIGHT).build());

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
            scrollOffset = Math.min(Math.max(0, filteredEffects.size() - layout.visibleItems), scrollOffset + 1);
        }).bounds(scrollBtnX, layout.listStartY + layout.listHeight - 20, 20, 20).build());
    }

    private void createBottomButtons() {
        int buttonWidth = 85;
        int totalWidth = 4 * buttonWidth + 3 * BUTTON_GAP;
        int startX = layout.centerX - totalWidth / 2;

        int x = startX;

        // 全选按钮
        addRenderableWidget(Button.builder(
                Component.translatable("gui.sky_accessories.select_all"),
                b -> {
                    selectedEffects.clear();
                    selectedEffects.addAll(filteredEffects);
                }
        ).bounds(x, layout.buttonY, buttonWidth, BUTTON_HEIGHT).build());
        x += buttonWidth + BUTTON_GAP;

        // 清空选择按钮
        addRenderableWidget(Button.builder(
                Component.translatable("gui.sky_accessories.clear_selection"),
                b -> selectedEffects.clear()
        ).bounds(x, layout.buttonY, buttonWidth, BUTTON_HEIGHT).build());
        x += buttonWidth + BUTTON_GAP;

        // 保存选择按钮
        addRenderableWidget(Button.builder(
                Component.translatable("gui.sky_accessories.save_selection"),
                b -> {
                    onComplete.accept(new ArrayList<>(selectedEffects));
                    Minecraft.getInstance().setScreen(parent);
                }
        ).bounds(x, layout.buttonY, buttonWidth, BUTTON_HEIGHT).build());
        x += buttonWidth + BUTTON_GAP;

        // 取消按钮
        addRenderableWidget(Button.builder(
                Component.translatable("gui.cancel"),
                b -> Minecraft.getInstance().setScreen(parent)
        ).bounds(x, layout.buttonY, buttonWidth, BUTTON_HEIGHT).build());
    }

    private void updateFilter(String query) {
        String lowerQuery = query.toLowerCase().trim();
        if (lowerQuery.isEmpty()) {
            filteredEffects = new ArrayList<>(allEffects);
        } else {
            filteredEffects = allEffects.stream()
                    .filter(effect -> {
                        String displayName = getEffectDisplayName(effect).toLowerCase();
                        String id = effect.toString().toLowerCase();
                        return displayName.contains(lowerQuery) || id.contains(lowerQuery);
                    })
                    .collect(Collectors.toList());
        }
        scrollOffset = 0;
    }

    private void updateScrollOffset() {
        scrollOffset = Math.min(scrollOffset, Math.max(0, filteredEffects.size() - layout.visibleItems));
    }

    private String getEffectDisplayName(ResourceLocation effect) {
        MobEffect mobEffect = ForgeRegistries.MOB_EFFECTS.getValue(effect);
        if (mobEffect != null) {
            return Component.translatable(mobEffect.getDescriptionId()).getString();
        }
        return effect.toString();
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);

        // 绘制标题
        graphics.drawCenteredString(this.font, this.title, layout.centerX, layout.titleY, 0xFFFFFF);

        // 绘制提示信息
        Component hintText = Component.translatable("gui.sky_accessories.click_to_select_hint");
        graphics.drawCenteredString(this.font, hintText, layout.centerX, layout.hintY, 0xAAAAAA);

        // 绘制选择计数
        Component countText = Component.translatable("gui.sky_accessories.selected_count", selectedEffects.size());
        int listX = layout.centerX - layout.listWidth / 2;
        graphics.drawString(this.font, countText, listX, layout.countY, 0x00FF00);

        // 绘制列表
        renderEffectList(graphics, mouseX, mouseY);

        // 绘制滚动条
        renderScrollBar(graphics);
    }

    private void renderEffectList(GuiGraphics graphics, int mouseX, int mouseY) {
        int listX = layout.centerX - layout.listWidth / 2;

        // 绘制列表背景
        graphics.fill(listX - 2, layout.listStartY - 2,
                listX + layout.listWidth + 2, layout.listStartY + layout.listHeight + 2, 0x80000000);

        // 绘制列表项
        for (int i = 0; i < layout.visibleItems && i + scrollOffset < filteredEffects.size(); i++) {
            ResourceLocation effect = filteredEffects.get(i + scrollOffset);
            int itemY = layout.listStartY + i * ITEM_HEIGHT;

            renderEffectItem(graphics, effect, listX, itemY, mouseX, mouseY);
        }
    }

    private void renderEffectItem(GuiGraphics graphics, ResourceLocation effect, int listX, int itemY, int mouseX, int mouseY) {
        boolean isSelected = selectedEffects.contains(effect);
        boolean isHovered = mouseX >= listX && mouseX <= listX + layout.listWidth &&
                mouseY >= itemY && mouseY <= itemY + ITEM_HEIGHT;

        // 背景色
        int bgColor = isSelected ? 0x8000FF00 : (isHovered ? 0x80FFFFFF : 0x40000000);
        graphics.fill(listX, itemY, listX + layout.listWidth, itemY + ITEM_HEIGHT, bgColor);

        // 选中标记
        if (isSelected) {
            graphics.drawString(this.font, "✓", listX + layout.listWidth - 15, itemY + 6, 0x00FF00);
        }

        // 效果名称和ID
        String displayName = getEffectDisplayName(effect);
        graphics.drawString(this.font, displayName, listX + 5, itemY + 3, 0xFFFFFF);
        String id = effect.toString();
        graphics.drawString(this.font, id, listX + 5, itemY + 13, 0xAAAAAA);
    }

    private void renderScrollBar(GuiGraphics graphics) {
        if (filteredEffects.size() > layout.visibleItems) {
            int scrollBarX = layout.centerX + layout.listWidth / 2 + 35;
            int thumbHeight = Math.max(10, layout.listHeight * layout.visibleItems / filteredEffects.size());
            int thumbY = layout.listStartY + (layout.listHeight - thumbHeight) * scrollOffset /
                    Math.max(1, filteredEffects.size() - layout.visibleItems);

            graphics.fill(scrollBarX, layout.listStartY, scrollBarX + 6, layout.listStartY + layout.listHeight, 0x80000000);
            graphics.fill(scrollBarX + 1, thumbY, scrollBarX + 5, thumbY + thumbHeight, 0xFFAAAAAA);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (super.mouseClicked(mouseX, mouseY, button)) {
            return true;
        }

        // 检查列表项点击
        int listX = layout.centerX - layout.listWidth / 2;
        if (mouseX >= listX && mouseX <= listX + layout.listWidth &&
                mouseY >= layout.listStartY && mouseY <= layout.listStartY + layout.listHeight) {

            int clickedIndex = (int) ((mouseY - layout.listStartY) / ITEM_HEIGHT) + scrollOffset;
            if (clickedIndex >= 0 && clickedIndex < filteredEffects.size()) {
                ResourceLocation effect = filteredEffects.get(clickedIndex);
                if (selectedEffects.contains(effect)) {
                    selectedEffects.remove(effect);
                } else {
                    selectedEffects.add(effect);
                }
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        if (filteredEffects.size() > layout.visibleItems) {
            scrollOffset = Math.max(0, Math.min(filteredEffects.size() - layout.visibleItems,
                    scrollOffset - (int) delta));
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

    // 布局信息类
    private record LayoutInfo(int centerX, int listWidth, int titleY, int backButtonY, int searchY, int hintY,
                                  int countY, int listStartY, int listHeight, int visibleItems, int buttonY) {
    }
}