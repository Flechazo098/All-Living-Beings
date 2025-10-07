package com.flechazo.all_living_beings.utils;

import com.flechazo.all_living_beings.config.Config;
import com.flechazo.all_living_beings.data.ALBSavedData;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.scores.PlayerTeam;

import java.util.UUID;

public class Util {
    public static final String EMPEROR_TEAM = "sky_emperor_title";
    public static final UUID CAST_TIME_MODIFIER_UUID = UUID.fromString("f2c8f6c7-6a6c-4f1e-93e6-0a0403f6b2d9");


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
        return GodhoodEquipmentUtils.hasThrone(player);
    }

    public static boolean tryEquipToGodhood(ServerPlayer player, ItemStack stack) {
        return GodhoodEquipmentUtils.tryEquipToGodhood(player, stack);
    }

    public static void autoEquipThroneIfOwner(ServerPlayer sp) {
        if (!isBoundOwner(sp) || hasThrone(sp)) return;
        GodhoodEquipmentUtils.autoEquipThroneFromInventory(sp);
    }

    public static PurgeResult purgeFateStacks(ServerPlayer player) {
        return GodhoodEquipmentUtils.purgeFateStacks(player);
    }


    public static void updateFlightAbilities(ServerPlayer sp, boolean isOwner) {
        EmperorBuffUtils.updateFlightAbilities(sp, isOwner);
    }

    public static void applyCastTimeReductionAttribute(ServerPlayer sp, boolean isOwner) {
        EmperorBuffUtils.applyCastTimeReductionAttribute(sp, isOwner);
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
        EmperorBuffUtils.notifyPurgeResult(sp, r);
    }

    public static void tickEmperorBuffs(ServerPlayer sp, boolean isOwner) {
        if (isOwner) {
            EmperorBuffUtils.applyEmperorBuffs(sp);
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
        EmperorBuffUtils.forceUnequipAll(target, caster);
    }

    public static Component emperorTitle() {
        return Component.translatable("title.all_living_beings.emperor").withStyle(ChatFormatting.GOLD);
    }

    public static PlayerTeam ensureEmperorTeam(ServerPlayer sp) {
        return EmperorBuffUtils.ensureEmperorTeam(sp);
    }

    public static void updateEmperorTeamMembership(ServerPlayer sp) {
        EmperorBuffUtils.updateEmperorTeamMembership(sp);
    }
}