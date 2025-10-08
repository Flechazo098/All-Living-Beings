package com.flechazo.all_living_beings.utils;

import com.flechazo.all_living_beings.config.Config;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.registries.ForgeRegistries;
import top.theillusivec4.curios.api.CuriosApi;
import vazkii.botania.api.mana.ManaItem;
import vazkii.botania.xplat.XplatAbstractions;

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

    public static void applyISSCastTimeReductionAttribute(ServerPlayer sp, boolean isOwner) {
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

    public static void applyBOTInfinityMana(ServerPlayer sp, boolean isOwner) {
        if (!isOwner) return;

        PlayerInventoryUtils.forEachPlayerStack(sp, EmperorBuffUtils::chargeStack);
    }

    private static void chargeStack(ItemStack st) {
        ManaItem mana = XplatAbstractions.INSTANCE.findManaItem(st);
        if (mana != null) {
            int missing = Math.max(0, mana.getMaxMana() - mana.getMana());
            if (missing > 0) {
                mana.addMana(missing);
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

    public static void applyReachAndMiningDistance(ServerPlayer sp, boolean isOwner) {
        if (!isOwner || !Util.hasThrone(sp)) {
            Attribute atk = ForgeMod.ENTITY_REACH.get();
            Attribute br = ForgeMod.BLOCK_REACH.get();
            var atkInst = sp.getAttribute(atk);
            var brInst = sp.getAttribute(br);
            if (atkInst != null && atkInst.getModifier(Util.ENTITY_REACH_MODIFIER_UUID) != null) {
                atkInst.removeModifier(Util.ENTITY_REACH_MODIFIER_UUID);
            }
            if (brInst != null && brInst.getModifier(Util.BLOCK_REACH_MODIFIER_UUID) != null) {
                brInst.removeModifier(Util.BLOCK_REACH_MODIFIER_UUID);
            }
            return;
        }
        Attribute atk = ForgeMod.ENTITY_REACH.get();
        Attribute br = ForgeMod.BLOCK_REACH.get();
        var atkInst = sp.getAttribute(atk);
        var brInst = sp.getAttribute(br);
        if (atkInst != null) {
            var existing = atkInst.getModifier(Util.ENTITY_REACH_MODIFIER_UUID);
            var mod = new AttributeModifier(Util.ENTITY_REACH_MODIFIER_UUID, "alb_emperor_attack_range", 64.0, AttributeModifier.Operation.ADDITION);
            if (existing == null) atkInst.addTransientModifier(mod); else if (existing.getAmount() != 64.0 || existing.getOperation() != AttributeModifier.Operation.ADDITION) {
                atkInst.removeModifier(Util.ENTITY_REACH_MODIFIER_UUID);
                atkInst.addTransientModifier(mod);
            }
        }
        if (brInst != null) {
            var existing = brInst.getModifier(Util.BLOCK_REACH_MODIFIER_UUID);
            var mod = new AttributeModifier(Util.BLOCK_REACH_MODIFIER_UUID, "alb_emperor_block_reach", 64.0, AttributeModifier.Operation.ADDITION);
            if (existing == null) brInst.addTransientModifier(mod); else if (existing.getAmount() != 64.0 || existing.getOperation() != AttributeModifier.Operation.ADDITION) {
                brInst.removeModifier(Util.BLOCK_REACH_MODIFIER_UUID);
                brInst.addTransientModifier(mod);
            }
        }
    }

    public static void applyStepAssistWhenSprinting(ServerPlayer sp, boolean isOwner) {
        Attribute step = ForgeMod.STEP_HEIGHT_ADDITION.get();
        var inst = sp.getAttribute(step);
        if (inst == null) return;
        double v = Config.COMMON.stepAssistHeight.get();
        if (isOwner && Util.hasThrone(sp) && sp.isSprinting() && v > 0.0) {
            var existing = inst.getModifier(Util.STEP_HEIGHT_MODIFIER_UUID);
            var mod = new AttributeModifier(Util.STEP_HEIGHT_MODIFIER_UUID, "alb_emperor_step_height", v, AttributeModifier.Operation.ADDITION);
            if (existing == null) inst.addTransientModifier(mod); else if (existing.getAmount() != v || existing.getOperation() != AttributeModifier.Operation.ADDITION) {
                inst.removeModifier(Util.STEP_HEIGHT_MODIFIER_UUID);
                inst.addTransientModifier(mod);
            }
        } else {
            if (inst.getModifier(Util.STEP_HEIGHT_MODIFIER_UUID) != null) inst.removeModifier(Util.STEP_HEIGHT_MODIFIER_UUID);
        }
    }

    public static boolean shouldPreventTargeting(Mob mob, LivingEntity target) {
        if (!(target instanceof ServerPlayer sp)) return false;
        if (!Util.isOwnerActive(sp)) return false;

        int mode = Config.COMMON.mobAttitude.get();
        if (mode == 1) return false; // mode 1: 允许攻击，无需阻止

        var typeId = ForgeRegistries.ENTITY_TYPES.getKey(mob.getType());
        boolean isBoss = typeId != null && Config.COMMON.bossEntityTypeIds.get().contains(typeId.toString());

        return switch (mode) {
            case 0 -> isBoss;           // 只阻止 Boss
            case 2 -> !isBoss;          // 只阻止非 Boss
            case 3 -> true;             // 总是阻止
            default -> false;
        };
    }

}