package com.flechazo.all_living_beings.utils;

import com.flechazo.all_living_beings.config.Config;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraftforge.registries.ForgeRegistries;
import top.theillusivec4.curios.api.CuriosApi;

import java.util.function.Consumer;

public class EmperorBuffUtils {

    public static void updateFlightAbilities(ServerPlayer sp, boolean isOwner) {
        if (isOwner) {
            sp.getAbilities().mayfly = true;
        } else {
            if (sp.getAbilities().flying) {
                sp.getAbilities().flying = false;
            }
            sp.getAbilities().mayfly = false;
        }
        sp.onUpdateAbilities();
    }

    public static void applyCastTimeReductionAttribute(ServerPlayer sp, boolean isOwner) {
        Attribute attr = ForgeRegistries.ATTRIBUTES.getValue(new ResourceLocation("irons_spellbooks", "cast_time_reduction"));
        if (attr == null) return;
        var inst = sp.getAttribute(attr);
        if (inst == null) return;

        var existing = inst.getModifier(Util.CAST_TIME_MODIFIER_UUID);
        if (isOwner) {
            double value = 50.0;
            var modifier = new AttributeModifier(
                    Util.CAST_TIME_MODIFIER_UUID,
                    "alb_emperor_cast_time_reduction",
                    value,
                    AttributeModifier.Operation.ADDITION
            );
            if (existing == null) {
                inst.addTransientModifier(modifier);
            } else if (existing.getAmount() != value || existing.getOperation() != AttributeModifier.Operation.ADDITION) {
                inst.removeModifier(Util.CAST_TIME_MODIFIER_UUID);
                inst.addTransientModifier(modifier);
            }
        } else {
            if (existing != null) {
                inst.removeModifier(Util.CAST_TIME_MODIFIER_UUID);
            }
        }
    }

    public static void notifyPurgeResult(ServerPlayer sp, PurgeResult result) {
        if (result == PurgeResult.OWNER_EXCESS) {
            sp.displayClientMessage(Component.translatable("message.all_living_beings.excess_fate_removed")
                    .withStyle(ChatFormatting.YELLOW), true);
        } else if (result == PurgeResult.NON_OWNER) {
            sp.displayClientMessage(Component.translatable("message.all_living_beings.non_owner_fate_purged")
                    .withStyle(ChatFormatting.RED), true);
        }
    }

    public static void applyEmperorBuffs(ServerPlayer sp) {
        if (!Config.COMMON.buffsEnabled.get()) return;
        int mode = Config.COMMON.buffMode.get();
        if (mode == 0) return;

        Consumer<String> applier = id -> {
            var rl = new ResourceLocation(id);
            var effect = ForgeRegistries.MOB_EFFECTS.getValue(rl);
            if (effect != null) {
                sp.addEffect(new MobEffectInstance(effect, 240, 0, true, false));
            }
        };

        if (mode == 1 || mode == 3) {
            for (String id : Config.COMMON.positiveEffectIds.get()) applier.accept(id);
        }
        if (mode == 2 || mode == 3) {
            for (String id : Config.COMMON.negativeEffectIds.get()) applier.accept(id);
        }
    }

    public static PlayerTeam ensureEmperorTeam(ServerPlayer sp) {
        var scoreboard = sp.server.getScoreboard();
        var team = scoreboard.getPlayerTeam(Util.EMPEROR_TEAM);
        if (team == null) {
            team = scoreboard.addPlayerTeam(Util.EMPEROR_TEAM);
            team.setPlayerPrefix(Util.emperorTitle());
        }
        return team;
    }

    public static void updateEmperorTeamMembership(ServerPlayer sp) {
        var scoreboard = sp.server.getScoreboard();
        var team = ensureEmperorTeam(sp);
        String name = sp.getScoreboardName();
        var current = scoreboard.getPlayersTeam(name);
        boolean isOwner = Util.isOwnerActive(sp);

        if (isOwner) {
            if (current != team) scoreboard.addPlayerToTeam(name, team);
        } else {
            if (current == team) scoreboard.removePlayerFromTeam(name);
        }
    }

    public static void forceUnequipAll(LivingEntity target, ServerPlayer caster) {
        for (var slot : EquipmentSlot.values()) {
            var st = target.getItemBySlot(slot);
            if (!st.isEmpty()) {
                target.setItemSlot(slot, ItemStack.EMPTY);
                dropOrAddToInventory(target, caster, st);
            }
        }

        // 卸下所有 Curios 物品
        CuriosApi.getCuriosInventory(target).ifPresent(handler -> {
            handler.getCurios().forEach((slotId, stacksHandler) -> {
                var inv = stacksHandler.getStacks();
                for (int i = 0; i < inv.getSlots(); i++) {
                    var st = inv.getStackInSlot(i);
                    if (!st.isEmpty()) {
                        inv.setStackInSlot(i, ItemStack.EMPTY);
                        dropOrAddToInventory(target, caster, st);
                    }
                }
            });
        });
    }

    private static void dropOrAddToInventory(LivingEntity target, ServerPlayer caster, ItemStack st) {
        if (target instanceof ServerPlayer sp) {
            if (!sp.getInventory().add(st)) sp.drop(st, true);
        } else {
            target.spawnAtLocation(st);
        }
    }
}