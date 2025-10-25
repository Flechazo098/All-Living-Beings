package com.flechazo.all_living_beings.utils;

import com.flechazo.all_living_beings.config.Config;
import com.flechazo.all_living_beings.data.ALBSavedData;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.scores.PlayerTeam;

import java.util.UUID;

public class Util {
    public static final String EMPEROR_TEAM = "sky_emperor_title";
    public static final UUID CAST_TIME_MODIFIER_UUID = UUID.fromString("f2c8f6c7-6a6c-4f1e-93e6-0a0403f6b2d9");
    public static final UUID ENTITY_REACH_MODIFIER_UUID = UUID.fromString("e1b7a6b0-5f7d-4a65-9d3c-6a5b9b0f2a11");
    public static final UUID BLOCK_REACH_MODIFIER_UUID = UUID.fromString("a5c9f3e2-4d81-4e2c-9e39-19d6f93b7c3f");
    public static final UUID STEP_HEIGHT_MODIFIER_UUID = UUID.fromString("b3f1c8d2-8e44-4b7f-9a9f-02c4c1a9e6d2");


    public static boolean isOwnerActive(ServerPlayer p) {
        ALBSavedData data = ALBSavedData.get(p.level());
        return data != null && p.getUUID().equals(data.getOwner()) && hasThrone(p);
    }

    public static boolean isBoundOwner(ServerPlayer sp) {
        ALBSavedData data = ALBSavedData.get(sp.level());
        UUID owner = data != null ? data.getOwner() : null;
        return owner != null && owner.equals(sp.getUUID());
    }

    public static boolean hasThrone(ServerPlayer player) {
        return GodhoodEquipmentUtil.hasThrone(player);
    }

    public static boolean tryEquipToGodhood(ServerPlayer player, ItemStack stack) {
        return GodhoodEquipmentUtil.tryEquipToGodhood(player, stack);
    }

    public static void autoEquipThroneIfOwner(ServerPlayer sp) {
        if (!isBoundOwner(sp) || hasThrone(sp)) return;
        GodhoodEquipmentUtil.autoEquipThroneFromInventory(sp);
    }

    public static PurgeResult purgeFateStacks(ServerPlayer player) {
        return GodhoodEquipmentUtil.purgeFateStacks(player);
    }


    public static void updateFlightAbilities(ServerPlayer sp, boolean isOwner) {
        EmperorBuffUtil.updateFlightAbilities(sp, isOwner);
    }

    public static void applyISSCastTimeReductionAttribute(ServerPlayer sp, boolean isOwner) {
        EmperorBuffUtil.applyISSCastTimeReductionAttribute(sp, isOwner);
    }

    public static void applyBOTInfinityManager(ServerPlayer sp, boolean isOwner) {
        EmperorBuffUtil.applyBOTInfinityMana(sp, isOwner);
    }


    public static void handleGodSuppression(ServerPlayer sp, boolean isOwner) {
        if (isOwner && Config.COMMON.godSuppression.get()) {
            LivingEntity target = findTarget(sp, 16);
            if (target != null) {
                forceUnequip(target, sp);
            }
        }
    }

    public static void purgeFateStacksAndNotify(ServerPlayer sp) {
        PurgeResult r = purgeFateStacks(sp);
        EmperorBuffUtil.notifyPurgeResult(sp, r);
    }

    public static void tickEmperorBuffs(ServerPlayer sp, boolean isOwner) {
        if (isOwner) {
            EmperorBuffUtil.applyEmperorBuffs(sp);
        }
    }

    public static void applyReachAndMiningDistance(ServerPlayer sp, boolean isOwner) {
        EmperorBuffUtil.applyReachAndMiningDistance(sp, isOwner);
    }

    public static void applyStepAssistWhenSprinting(ServerPlayer sp, boolean isOwner) {
        EmperorBuffUtil.applyStepAssistWhenSprinting(sp, isOwner);
    }

    public static boolean shouldPreventTargeting(Mob mob, LivingEntity target) {
        return EmperorBuffUtil.shouldPreventTargeting(mob, target);
    }

    public static void handleInstantKill(ServerPlayer sp, LivingEntity target) {
        EmperorBuffUtil.handleInstantKill(sp, target);
    }

    public static void applyGazeEffects(ServerPlayer sp, boolean isOwner) {
        if (isOwner) {
            EmperorBuffUtil.applyGazeEffects(sp);
        }
    }

    public static LivingEntity findTarget(ServerPlayer p, double range) {
        Vec3 eye = p.getEyePosition();
        Vec3 dir = p.getViewVector(1.0F);
        Vec3 end = eye.add(dir.scale(range));
        var box = p.getBoundingBox().expandTowards(dir.scale(range)).inflate(1.0);
        return p.level().getEntitiesOfClass(LivingEntity.class, box, e -> e != p).stream()
                .findFirst()
                .orElse(null);
    }

    public static void forceUnequip(LivingEntity target, ServerPlayer caster) {
        EmperorBuffUtil.forceUnequipAll(target, caster);
    }

    public static Component emperorTitle() {
        return Component.translatable("title.all_living_beings.emperor").withStyle(ChatFormatting.GOLD);
    }

    public static PlayerTeam ensureEmperorTeam(ServerPlayer sp) {
        return EmperorBuffUtil.ensureEmperorTeam(sp);
    }

    public static void updateEmperorTeamMembership(ServerPlayer sp) {
        EmperorBuffUtil.updateEmperorTeamMembership(sp);
    }
}