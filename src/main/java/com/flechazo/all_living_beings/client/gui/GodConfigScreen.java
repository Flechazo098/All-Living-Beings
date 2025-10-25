package com.flechazo.all_living_beings.client.gui;

import com.flechazo.all_living_beings.network.NetworkHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GodConfigScreen extends Screen {
    private static final int TOTAL_PAGES = 4;
    private final List<String> effectIds;
    private final List<String> gazeEffectIds;
    private final List<Integer> gazeEffectDurations;
    private boolean absoluteDefense;
    private boolean absoluteAutonomy;
    private boolean godPermissions;
    private boolean godSuppression;
    private boolean godAttack;
    private boolean eternalTranscendence;
    private int fixedAttackDamage;
    private boolean instantKillEnabled;
    private boolean buffsEnabled;
    private int buffMode;
    private EditBox damageInput;
    private int currentPage = 0;
    private int mobAttitude;
    private double stepAssistHeight;
    private EditBox stepInput;
    private final List<String> bossEntityTypeIds;
    private int instantMiningMode;
    private boolean instantMiningDrops;
    private boolean disableAirMiningSlowdown;

    public GodConfigScreen(boolean absoluteDefense,
                           boolean absoluteAutonomy,
                           boolean godPermissions,
                           boolean godSuppression,
                           boolean godAttack,
                           boolean eternalTranscendence,
                           int fixedAttackDamage,
                           boolean instantKillEnabled,
                           boolean buffsEnabled,
                           int buffMode,
                           List<String> effectIds,
                           List<String> gazeEffectIds,
                           List<Integer> gazeEffectDurations,
                           int mobAttitude,
                           double stepAssistHeight,
                           List<String> bossEntityTypeIds,
                           int instantMiningMode,
                           boolean instantMiningDrops,
                           boolean disableAirMiningSlowdown) {
        super(Component.translatable("gui.all_living_beings.config.title"));
        this.absoluteDefense = absoluteDefense;
        this.absoluteAutonomy = absoluteAutonomy;
        this.godPermissions = godPermissions;
        this.godSuppression = godSuppression;
        this.godAttack = godAttack;
        this.eternalTranscendence = eternalTranscendence;
        this.fixedAttackDamage = fixedAttackDamage;
        this.instantKillEnabled = instantKillEnabled;
        this.buffsEnabled = buffsEnabled;
        this.buffMode = buffMode;
        this.effectIds = new ArrayList<>(effectIds);
        this.gazeEffectIds = new ArrayList<>(gazeEffectIds);
        this.gazeEffectDurations = new ArrayList<>(gazeEffectDurations);
        this.mobAttitude = mobAttitude;
        this.stepAssistHeight = stepAssistHeight;
        this.bossEntityTypeIds = new ArrayList<>(bossEntityTypeIds);
        this.instantMiningMode = instantMiningMode;
        this.instantMiningDrops = instantMiningDrops;
        this.disableAirMiningSlowdown = disableAirMiningSlowdown;
    }

    private void initMiningSettingsPage(int centerX, int buttonWidth, int buttonHeight, int spacing) {
        int currentY = 50;

        var instantMiningModeBtn = CycleButton.builder((Integer v) -> Component.translatable("cfg.all_living_beings.instantMiningMode." + v))
                .withValues(Arrays.asList(0, 1, 2, 3, 4))
                .withInitialValue(instantMiningMode)
                .create(centerX - buttonWidth / 2, currentY, buttonWidth, buttonHeight,
                        Component.translatable("cfg.all_living_beings.instantMiningMode"),
                        (btn, value) -> {
                            instantMiningMode = value;
                            push();
                        });
        instantMiningModeBtn.setTooltip(Tooltip.create(
                Component.translatable("tooltip.all_living_beings.instantMiningMode")));
        addRenderableWidget(instantMiningModeBtn);

        currentY += spacing;

        var instantMiningDropsBtn = CycleButton.onOffBuilder(instantMiningDrops)
                .create(centerX - buttonWidth / 2, currentY, buttonWidth, buttonHeight,
                        Component.translatable("cfg.all_living_beings.instantMiningDrops"),
                        (btn, value) -> {
                            instantMiningDrops = value;
                            push();
                        });
        instantMiningDropsBtn.setTooltip(Tooltip.create(
                Component.translatable("tooltip.all_living_beings.instantMiningDrops")));
        addRenderableWidget(instantMiningDropsBtn);

        currentY += spacing;

        var disableAirMiningSlowdownBtn = CycleButton.onOffBuilder(disableAirMiningSlowdown)
                .create(centerX - buttonWidth / 2, currentY, buttonWidth, buttonHeight,
                        Component.translatable("cfg.all_living_beings.disableAirMiningSlowdown"),
                        (btn, value) -> {
                            disableAirMiningSlowdown = value;
                            push();
                        });
        disableAirMiningSlowdownBtn.setTooltip(Tooltip.create(
                Component.translatable("tooltip.all_living_beings.disableAirMiningSlowdown")));
        addRenderableWidget(disableAirMiningSlowdownBtn);
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
            case 3:
                initMiningSettingsPage(centerX, buttonWidth, buttonHeight, spacing);
                break;
        }
    }

    private void initBasicAbilitiesPage(int centerX, int buttonWidth, int buttonHeight, int spacing) {
        int currentY = 40;

        var absoluteDefenseBtn = CycleButton.onOffBuilder(absoluteDefense)
                .create(centerX - buttonWidth / 2, currentY, buttonWidth, buttonHeight,
                        Component.translatable("cfg.all_living_beings.absoluteDefense"),
                        (btn, value) -> {
                            absoluteDefense = value;
                            push();
                        });
        absoluteDefenseBtn.setTooltip(Tooltip.create(
                Component.translatable("tooltip.all_living_beings.absoluteDefense")));
        addRenderableWidget(absoluteDefenseBtn);
        currentY += spacing;

        var absoluteAutonomyBtn = CycleButton.onOffBuilder(absoluteAutonomy)
                .create(centerX - buttonWidth / 2, currentY, buttonWidth, buttonHeight,
                        Component.translatable("cfg.all_living_beings.absoluteAutonomy"),
                        (btn, value) -> {
                            absoluteAutonomy = value;
                            push();
                        });
        absoluteAutonomyBtn.setTooltip(Tooltip.create(
                Component.translatable("tooltip.all_living_beings.absoluteAutonomy")));
        addRenderableWidget(absoluteAutonomyBtn);
        currentY += spacing;

        var godPermissionsBtn = CycleButton.onOffBuilder(godPermissions)
                .create(centerX - buttonWidth / 2, currentY, buttonWidth, buttonHeight,
                        Component.translatable("cfg.all_living_beings.godPermissions"),
                        (btn, value) -> {
                            godPermissions = value;
                            push();
                        });
        godPermissionsBtn.setTooltip(Tooltip.create(
                Component.translatable("tooltip.all_living_beings.godPermissions")));
        addRenderableWidget(godPermissionsBtn);
        currentY += spacing;

        var godSuppressionBtn = CycleButton.onOffBuilder(godSuppression)
                .create(centerX - buttonWidth / 2, currentY, buttonWidth, buttonHeight,
                        Component.translatable("cfg.all_living_beings.godSuppression"),
                        (btn, value) -> {
                            godSuppression = value;
                            push();
                        });
        godSuppressionBtn.setTooltip(Tooltip.create(
                Component.translatable("tooltip.all_living_beings.godSuppression")));
        addRenderableWidget(godSuppressionBtn);
        currentY += spacing;

        var godAttackBtn = CycleButton.onOffBuilder(godAttack)
                .create(centerX - buttonWidth / 2, currentY, buttonWidth, buttonHeight,
                        Component.translatable("cfg.all_living_beings.godAttack"),
                        (btn, value) -> {
                            godAttack = value;
                            push();
                        });
        godAttackBtn.setTooltip(Tooltip.create(
                Component.translatable("tooltip.all_living_beings.godAttack")));
        addRenderableWidget(godAttackBtn);
        currentY += spacing;

        var eternalTranscendenceBtn = CycleButton.onOffBuilder(eternalTranscendence)
                .create(centerX - buttonWidth / 2, currentY, buttonWidth, buttonHeight,
                        Component.translatable("cfg.all_living_beings.eternalTranscendence"),
                        (btn, value) -> {
                            eternalTranscendence = value;
                            push();
                        });
        eternalTranscendenceBtn.setTooltip(Tooltip.create(
                Component.translatable("tooltip.all_living_beings.eternalTranscendence")));
        addRenderableWidget(eternalTranscendenceBtn);
    }

    private void initNumericSettingsPage(int centerX, int buttonWidth, int buttonHeight, int spacing) {
        int currentY = 55;
        int applyButtonWidth = 55;
        int inputWidth = buttonWidth - applyButtonWidth - 10;
        int killButtonWidth = 80;
        int damageInputWidth = buttonWidth - applyButtonWidth - killButtonWidth - 20;

        damageInput = new EditBox(this.font, centerX - buttonWidth / 2, currentY, damageInputWidth, buttonHeight,
                Component.translatable("cfg.all_living_beings.fixedAttackDamage.input"));
        damageInput.setValue(String.valueOf(fixedAttackDamage));
        damageInput.setResponder(s -> {
            try {
                int value = Integer.parseInt(s.trim());
                if (value >= 0) fixedAttackDamage = value;
            } catch (NumberFormatException ignored) {
            }
        });
        addRenderableWidget(damageInput);

        var instantKillBtn = CycleButton.onOffBuilder(instantKillEnabled)
                .create(centerX - buttonWidth / 2 + damageInputWidth + 5, currentY, killButtonWidth, buttonHeight,
                        Component.translatable("cfg.all_living_beings.instantKill"),
                        (btn, value) -> {
                            instantKillEnabled = value;
                            push();
                        });
        instantKillBtn.setTooltip(Tooltip.create(
                Component.translatable("tooltip.all_living_beings.instantKill")));
        addRenderableWidget(instantKillBtn);

        var applyBtn = Button.builder(Component.translatable("gui.all_living_beings.apply"), b -> push())
                .bounds(centerX - buttonWidth / 2 + damageInputWidth + killButtonWidth + 10, currentY, applyButtonWidth, buttonHeight)
                .build();
        applyBtn.setTooltip(Tooltip.create(
                Component.translatable("tooltip.all_living_beings.apply")));
        addRenderableWidget(applyBtn);

        currentY += spacing;

        var buffModeBtn = CycleButton.builder((Integer v) -> Component.translatable("cfg.all_living_beings.buffMode." + v))
                .withValues(Arrays.asList(0, 1, 2, 3))
                .withInitialValue(buffMode)
                .create(centerX - buttonWidth / 2, currentY, buttonWidth, buttonHeight,
                        Component.translatable("cfg.all_living_beings.buffMode"),
                        (btn, value) -> {
                            buffMode = value;
                            push();
                        });
        buffModeBtn.setTooltip(Tooltip.create(
                Component.translatable("tooltip.all_living_beings.buffMode")));
        addRenderableWidget(buffModeBtn);

        currentY += spacing;

        var buffsEnabledBtn = CycleButton.onOffBuilder(buffsEnabled)
                .create(centerX - buttonWidth / 2, currentY, buttonWidth, buttonHeight,
                        Component.translatable("cfg.all_living_beings.buffsEnabled"),
                        (btn, value) -> {
                            buffsEnabled = value;
                            push();
                        });
        buffsEnabledBtn.setTooltip(Tooltip.create(
                Component.translatable("tooltip.all_living_beings.buffsEnabled")));
        addRenderableWidget(buffsEnabledBtn);

        currentY += spacing;

        var manageBossBtn = Button.builder(
                        Component.translatable("gui.all_living_beings.manage_boss_entities", bossEntityTypeIds.size()),
                        b -> Minecraft.getInstance().setScreen(
                                new EntityPickerScreen(this, bossEntityTypeIds, this::onBossEntitiesPicked)))
                .bounds(centerX - buttonWidth / 2, currentY, buttonWidth, buttonHeight)
                .build();
        manageBossBtn.setTooltip(Tooltip.create(
                Component.translatable("tooltip.all_living_beings.manageBossEntities")));
        addRenderableWidget(manageBossBtn);

        currentY += spacing;

        var mobAttitudeBtn = CycleButton.builder((Integer v) -> Component.translatable("cfg.all_living_beings.mobAttitude." + v))
                .withValues(Arrays.asList(0, 1, 2, 3))
                .withInitialValue(mobAttitude)
                .create(centerX - buttonWidth / 2, currentY, buttonWidth, buttonHeight,
                        Component.translatable("cfg.all_living_beings.mobAttitude"),
                        (btn, value) -> {
                            mobAttitude = value;
                            push();
                        });
        mobAttitudeBtn.setTooltip(Tooltip.create(
                Component.translatable("tooltip.all_living_beings.mobAttitude")));
        addRenderableWidget(mobAttitudeBtn);

        currentY += spacing;

        stepInput = new EditBox(this.font, centerX - buttonWidth / 2, currentY, inputWidth, buttonHeight,
                Component.translatable("cfg.all_living_beings.stepAssistHeight.input"));
        stepInput.setValue(String.valueOf(stepAssistHeight));
        stepInput.setResponder(s -> {
            try {
                double v = Double.parseDouble(s.trim());
                if (v >= 0.0) stepAssistHeight = v;
            } catch (NumberFormatException ignored) {
            }
        });
        addRenderableWidget(stepInput);

        var stepApplyBtn = Button.builder(Component.translatable("gui.all_living_beings.apply"), b -> push())
                .bounds(centerX - buttonWidth / 2 + inputWidth + 10, currentY, applyButtonWidth, buttonHeight)
                .build();
        stepApplyBtn.setTooltip(Tooltip.create(
                Component.translatable("tooltip.all_living_beings.apply")));
        addRenderableWidget(stepApplyBtn);
    }

    private void initEffectManagementPage(int centerX, int buttonWidth, int buttonHeight, int spacing) {
        int currentY = 50; // 上移30像素
        int leftColumnWidth = (buttonWidth - 10) / 2;
        int leftColumnX = centerX - buttonWidth / 2;
        int rightColumnX = centerX + 5;

        var manageEffectsBtn = Button.builder(
                        Component.translatable("gui.all_living_beings.manage_effects", effectIds.size()),
                        b -> Minecraft.getInstance().setScreen(new EffectPickerScreen(this, true, this::onEffectsPicked, effectIds)))
                .bounds(leftColumnX, currentY, leftColumnWidth, buttonHeight).build();
        manageEffectsBtn.setTooltip(Tooltip.create(
                Component.translatable("tooltip.all_living_beings.manageEffects")));
        addRenderableWidget(manageEffectsBtn);

        var manageGazeEffectsBtn = Button.builder(
                        Component.translatable("gui.all_living_beings.manage_gaze_effects", gazeEffectIds.size()),
                        b -> Minecraft.getInstance().setScreen(new GazeEffectPickerScreen(this, gazeEffectIds, gazeEffectDurations, this::onGazeEffectsPicked)))
                .bounds(rightColumnX, currentY, leftColumnWidth, buttonHeight).build();
        manageGazeEffectsBtn.setTooltip(Tooltip.create(
                Component.translatable("tooltip.all_living_beings.manageGazeEffects")));
        addRenderableWidget(manageGazeEffectsBtn);

        currentY += spacing;

        var clearEffectsBtn = Button.builder(Component.translatable("gui.all_living_beings.clear_effects"), b -> {
            effectIds.clear();
            push();
            init();
        }).bounds(leftColumnX, currentY, leftColumnWidth, buttonHeight).build();
        clearEffectsBtn.setTooltip(Tooltip.create(
                Component.translatable("tooltip.all_living_beings.clearEffects")));
        addRenderableWidget(clearEffectsBtn);

        var clearGazeEffectsBtn = Button.builder(Component.translatable("gui.all_living_beings.clear_gaze_effects"), b -> {
            gazeEffectIds.clear();
            gazeEffectDurations.clear();
            push();
            init();
        }).bounds(rightColumnX, currentY, leftColumnWidth, buttonHeight).build();
        clearGazeEffectsBtn.setTooltip(Tooltip.create(
                Component.translatable("tooltip.all_living_beings.clearGazeEffects")));
        addRenderableWidget(clearGazeEffectsBtn);
    }

    private void onEffectsPicked(List<ResourceLocation> effects) {
        effectIds.clear();
        effects.forEach(effect -> effectIds.add(effect.toString()));
        push();
        init();
    }

    private void onGazeEffectsPicked(List<ResourceLocation> effects, List<Integer> durations) {
        gazeEffectIds.clear();
        gazeEffectDurations.clear();
        effects.forEach(effect -> gazeEffectIds.add(effect.toString()));
        gazeEffectDurations.addAll(durations);
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
            case 3 -> Component.translatable("gui.all_living_beings.mining_settings").getString();
            default -> "";
        };
        graphics.drawCenteredString(this.font, pageTitle, centerX, 20, 0xFFFFFF); // 上移10像素

        if (currentPage == 1) {
            graphics.drawString(this.font, Component.translatable("cfg.all_living_beings.fixedAttackDamage.label").getString(),
                    centerX - 100, 40, 0xFFFFFF); // 调整位置
            if (stepInput != null) {
                graphics.drawString(this.font, Component.translatable("cfg.all_living_beings.stepAssistHeight.label").getString(),
                        stepInput.getX(), stepInput.getY() - 15, 0xFFFFFF); // 在输入框上方15像素处绘制标签
            }
        }

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
                instantKillEnabled,
                buffsEnabled,
                buffMode,
                effectIds,
                gazeEffectIds,
                gazeEffectDurations,
                mobAttitude,
                stepAssistHeight,
                bossEntityTypeIds,
                instantMiningMode,
                instantMiningDrops,
                disableAirMiningSlowdown
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