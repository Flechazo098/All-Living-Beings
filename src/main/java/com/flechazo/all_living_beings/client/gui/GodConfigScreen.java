package com.flechazo.all_living_beings.client.gui;

import com.flechazo.all_living_beings.network.NetworkHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GodConfigScreen extends Screen {
    private static final int TOTAL_PAGES = 3;
    private final List<String> positiveEffectIds;
    private final List<String> negativeEffectIds;
    private boolean absoluteDefense;
    private boolean absoluteAutonomy;
    private boolean godPermissions;
    private boolean godSuppression;
    private boolean godAttack;
    private boolean eternalTranscendence;
    private int fixedAttackDamage;
    private boolean buffsEnabled;
    private int buffMode;
    private EditBox damageInput;
    private int currentPage = 0;
    private int mobAttitude;
    private double stepAssistHeight;
    private EditBox stepInput;
    private final List<String> bossEntityTypeIds;
    private EditBox bossListInput;

    public GodConfigScreen(boolean absoluteDefense,
                           boolean absoluteAutonomy,
                           boolean godPermissions,
                           boolean godSuppression,
                           boolean godAttack,
                           boolean eternalTranscendence,
                           int fixedAttackDamage,
                           boolean buffsEnabled,
                           int buffMode,
                           List<String> positiveEffectIds,
                           List<String> negativeEffectIds,
                           int mobAttitude,
                           double stepAssistHeight,
                           List<String> bossEntityTypeIds) {
        super(Component.translatable("gui.all_living_beings.config.title"));
        this.absoluteDefense = absoluteDefense;
        this.absoluteAutonomy = absoluteAutonomy;
        this.godPermissions = godPermissions;
        this.godSuppression = godSuppression;
        this.godAttack = godAttack;
        this.eternalTranscendence = eternalTranscendence;
        this.fixedAttackDamage = fixedAttackDamage;
        this.buffsEnabled = buffsEnabled;
        this.buffMode = buffMode;
        this.positiveEffectIds = new ArrayList<>(positiveEffectIds);
        this.negativeEffectIds = new ArrayList<>(negativeEffectIds);
        this.mobAttitude = mobAttitude;
        this.stepAssistHeight = stepAssistHeight;
        this.bossEntityTypeIds = new ArrayList<>(bossEntityTypeIds);
    }

    @Override
    protected void init() {
        this.clearWidgets();

        int centerX = this.width / 2;
        int buttonWidth = 200;
        int buttonHeight = 20;
        int spacing = 25;

        addRenderableWidget(Button.builder(Component.translatable("gui.all_living_beings.previous_page"),
                        b -> {
                            currentPage = (currentPage - 1 + TOTAL_PAGES) % TOTAL_PAGES;
                            init();
                        })
                .bounds(centerX - 150, this.height - 70, 60, buttonHeight).build());

        addRenderableWidget(Button.builder(Component.translatable("gui.all_living_beings.next_page"),
                        b -> {
                            currentPage = (currentPage + 1) % TOTAL_PAGES;
                            init();
                        })
                .bounds(centerX + 90, this.height - 70, 60, buttonHeight).build());

        addRenderableWidget(Button.builder(Component.translatable("gui.done"), b -> onClose())
                .bounds(centerX - 50, this.height - 70, 100, buttonHeight).build());

        switch (currentPage) {
            case 0:
                initBasicAbilitiesPage(centerX, buttonWidth, buttonHeight, spacing);
                break;
            case 1:
                initNumericSettingsPage(centerX, buttonWidth, buttonHeight, spacing);
                break;
            case 2:
                initEffectManagementPage(centerX, buttonWidth, buttonHeight, spacing);
                break;
        }
    }

    private void initBasicAbilitiesPage(int centerX, int buttonWidth, int buttonHeight, int spacing) {
        int currentY = 60;

        addRenderableWidget(CycleButton.onOffBuilder(absoluteDefense)
                .create(centerX - buttonWidth / 2, currentY, buttonWidth, buttonHeight,
                        Component.translatable("cfg.all_living_beings.absoluteDefense"),
                        (btn, value) -> {
                            absoluteDefense = value;
                            push();
                        }));
        currentY += spacing;

        addRenderableWidget(CycleButton.onOffBuilder(absoluteAutonomy)
                .create(centerX - buttonWidth / 2, currentY, buttonWidth, buttonHeight,
                        Component.translatable("cfg.all_living_beings.absoluteAutonomy"),
                        (btn, value) -> {
                            absoluteAutonomy = value;
                            push();
                        }));
        currentY += spacing;

        addRenderableWidget(CycleButton.onOffBuilder(godPermissions)
                .create(centerX - buttonWidth / 2, currentY, buttonWidth, buttonHeight,
                        Component.translatable("cfg.all_living_beings.godPermissions"),
                        (btn, value) -> {
                            godPermissions = value;
                            push();
                        }));
        currentY += spacing;

        addRenderableWidget(CycleButton.onOffBuilder(godSuppression)
                .create(centerX - buttonWidth / 2, currentY, buttonWidth, buttonHeight,
                        Component.translatable("cfg.all_living_beings.godSuppression"),
                        (btn, value) -> {
                            godSuppression = value;
                            push();
                        }));
        currentY += spacing;

        addRenderableWidget(CycleButton.onOffBuilder(godAttack)
                .create(centerX - buttonWidth / 2, currentY, buttonWidth, buttonHeight,
                        Component.translatable("cfg.all_living_beings.godAttack"),
                        (btn, value) -> {
                            godAttack = value;
                            push();
                        }));
        currentY += spacing;

        addRenderableWidget(CycleButton.onOffBuilder(eternalTranscendence)
                .create(centerX - buttonWidth / 2, currentY, buttonWidth, buttonHeight,
                        Component.translatable("cfg.all_living_beings.eternalTranscendence"),
                        (btn, value) -> {
                            eternalTranscendence = value;
                            push();
                        }));
    }

    private void initNumericSettingsPage(int centerX, int buttonWidth, int buttonHeight, int spacing) {
        int currentY = 60;
        int applyButtonWidth = 55;
        int inputWidth = buttonWidth - applyButtonWidth - 10;

        damageInput = new EditBox(this.font, centerX - buttonWidth / 2, currentY, inputWidth, buttonHeight,
                Component.translatable("cfg.all_living_beings.fixedAttackDamage.input"));
        damageInput.setValue(String.valueOf(fixedAttackDamage));
        damageInput.setResponder(s -> {
            try {
                int value = Integer.parseInt(s.trim());
                if (value >= 0) fixedAttackDamage = value;
            } catch (NumberFormatException ignored) {}
        });
        addRenderableWidget(damageInput);

        addRenderableWidget(Button.builder(Component.translatable("gui.all_living_beings.apply"), b -> push())
                .bounds(centerX - buttonWidth / 2 + inputWidth + 10, currentY, applyButtonWidth, buttonHeight)
                .build());
    
        currentY += spacing;

        addRenderableWidget(CycleButton.builder((Integer v) -> Component.translatable("cfg.all_living_beings.buffMode." + v))
                .withValues(Arrays.asList(0, 1, 2, 3))
                .withInitialValue(buffMode)
                .create(centerX - buttonWidth / 2, currentY, buttonWidth, buttonHeight,
                        Component.translatable("cfg.all_living_beings.buffMode"),
                        (btn, value) -> {
                            buffMode = value;
                            push();
                        }));
    
        currentY += spacing;

        addRenderableWidget(CycleButton.onOffBuilder(buffsEnabled)
                .create(centerX - buttonWidth / 2, currentY, buttonWidth, buttonHeight,
                        Component.translatable("cfg.all_living_beings.buffsEnabled"),
                        (btn, value) -> {
                            buffsEnabled = value;
                            push();
                        }));
    
        currentY += spacing;

        addRenderableWidget(Button.builder(
                        Component.translatable("gui.all_living_beings.manage_boss_entities", bossEntityTypeIds.size()),
                        b -> Minecraft.getInstance().setScreen(
                                new EntityPickerScreen(this, bossEntityTypeIds, this::onBossEntitiesPicked)))
                .bounds(centerX - buttonWidth / 2, currentY, buttonWidth, buttonHeight)
                .build());
    
        currentY += spacing;

        addRenderableWidget(CycleButton.builder((Integer v) -> Component.translatable("cfg.all_living_beings.mobAttitude." + v))
                .withValues(Arrays.asList(0, 1, 2, 3))
                .withInitialValue(mobAttitude)
                .create(centerX - buttonWidth / 2, currentY, buttonWidth, buttonHeight,
                        Component.translatable("cfg.all_living_beings.mobAttitude"),
                        (btn, value) -> {
                            mobAttitude = value;
                            push();
                        }));
    
        currentY += spacing;

        stepInput = new EditBox(this.font, centerX - buttonWidth / 2, currentY, inputWidth, buttonHeight,
                Component.translatable("cfg.all_living_beings.stepAssistHeight.input"));
        stepInput.setValue(String.valueOf(stepAssistHeight));
        stepInput.setResponder(s -> {
            try {
                double v = Double.parseDouble(s.trim());
                if (v >= 0.0) stepAssistHeight = v;
            } catch (NumberFormatException ignored) {}
        });
        addRenderableWidget(stepInput);

        addRenderableWidget(Button.builder(Component.translatable("gui.all_living_beings.apply"), b -> push())
                .bounds(centerX - buttonWidth / 2 + inputWidth + 10, currentY, applyButtonWidth, buttonHeight)
                .build());
    }

    private void initEffectManagementPage(int centerX, int buttonWidth, int buttonHeight, int spacing) {
        int currentY = 80;
        int effectButtonWidth = (buttonWidth - 10) / 2;

        addRenderableWidget(Button.builder(
                        Component.translatable("gui.all_living_beings.manage_positive_effects", positiveEffectIds.size()),
                        b -> Minecraft.getInstance().setScreen(new EffectPickerScreen(this, true, this::onPositiveEffectsPicked)))
                .bounds(centerX - buttonWidth / 2, currentY, effectButtonWidth, buttonHeight).build());

        addRenderableWidget(Button.builder(
                        Component.translatable("gui.all_living_beings.manage_negative_effects", negativeEffectIds.size()),
                        b -> Minecraft.getInstance().setScreen(new EffectPickerScreen(this, false, this::onNegativeEffectsPicked)))
                .bounds(centerX - buttonWidth / 2 + effectButtonWidth + 10, currentY, effectButtonWidth, buttonHeight).build());
        currentY += spacing;

        addRenderableWidget(Button.builder(Component.translatable("gui.all_living_beings.clear_positive_effects"), b -> {
            positiveEffectIds.clear();
            push();
            init();
        }).bounds(centerX - buttonWidth / 2, currentY, effectButtonWidth, buttonHeight).build());

        addRenderableWidget(Button.builder(Component.translatable("gui.all_living_beings.clear_negative_effects"), b -> {
            negativeEffectIds.clear();
            push();
            init();
        }).bounds(centerX - buttonWidth / 2 + effectButtonWidth + 10, currentY, effectButtonWidth, buttonHeight).build());
    }

    private void onPositiveEffectsPicked(List<ResourceLocation> effects) {
        positiveEffectIds.clear();
        effects.forEach(effect -> positiveEffectIds.add(effect.toString()));
        push();
        init();
    }

    private void onNegativeEffectsPicked(List<ResourceLocation> effects) {
        negativeEffectIds.clear();
        effects.forEach(effect -> negativeEffectIds.add(effect.toString()));
        push();
        init();
    }

    private void onBossEntitiesPicked(List<ResourceLocation> entities) {
        bossEntityTypeIds.clear();
        entities.forEach(entityType -> bossEntityTypeIds.add(entityType.toString()));
        push();
        init();
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);

        int centerX = this.width / 2;

        String pageTitle = switch (currentPage) {
            case 0 -> Component.translatable("gui.all_living_beings.basic_abilities").getString();
            case 1 -> Component.translatable("gui.all_living_beings.numeric_settings").getString();
            case 2 -> Component.translatable("gui.all_living_beings.effect_management").getString();
            default -> "";
        };
        graphics.drawCenteredString(this.font, pageTitle, centerX, 30, 0xFFFFFF);

        String pageIndicator = Component.translatable("gui.all_living_beings.page_indicator",
                currentPage + 1, TOTAL_PAGES).getString();
        graphics.drawCenteredString(this.font, pageIndicator, centerX, this.height - 70, 0xAAAAAA);
    }

    private void push() {
        if (currentPage == 1 && damageInput != null) {
            try {
                int value = Integer.parseInt(damageInput.getValue().trim());
                fixedAttackDamage = Math.max(0, value);
            } catch (NumberFormatException ignored) {
            }
        }

        NetworkHandler.updateGodConfig(
                absoluteDefense,
                absoluteAutonomy,
                godPermissions,
                godSuppression,
                godAttack,
                eternalTranscendence,
                fixedAttackDamage,
                buffsEnabled,
                buffMode,
                positiveEffectIds,
                negativeEffectIds,
                mobAttitude,
                stepAssistHeight,
                bossEntityTypeIds
        );
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