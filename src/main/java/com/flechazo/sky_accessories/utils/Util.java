package com.flechazo.sky_accessories.utils;

import com.flechazo.sky_accessories.ModItems;
import com.flechazo.sky_accessories.SkyAccessoriesSavedData;
import com.flechazo.sky_accessories.config.Config;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.registries.ForgeRegistries;
import top.theillusivec4.curios.api.CuriosApi;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

public class Util {
    public static final String EMPEROR_TEAM = "sky_emperor_title";

    public static boolean isOwnerActive(ServerPlayer p) {
        SkyAccessoriesSavedData data = SkyAccessoriesSavedData.get(p.level());
        return data != null && p.getUUID().equals(data.getOwner()) && hasThrone(p);
    }

    public static boolean hasThrone(ServerPlayer player) {
        return CuriosApi.getCuriosInventory(player)
                .map(handler -> handler.getStacksHandler("godhood")
                        .map(slotHandler -> {
                            IItemHandlerModifiable inv = slotHandler.getStacks();
                            for (int i = 0; i < inv.getSlots(); i++) {
                                if (inv.getStackInSlot(i).is(ModItems.HEAVENLY_THRONE.get())) {
                                    return true;
                                }
                            }
                            return false;
                        })
                        .orElse(false))
                .orElse(false);
    }

    public static boolean tryEquipToGodhood(ServerPlayer player, ItemStack stack) {
        if (stack.isEmpty() || !stack.is(ModItems.HEAVENLY_THRONE.get())) {
            return false;
        }

        return CuriosApi.getCuriosInventory(player)
                .map(handler -> handler.getStacksHandler("godhood")
                        .map(slotHandler -> {
                            IItemHandlerModifiable inv = slotHandler.getStacks();

                            for (int i = 0; i < inv.getSlots(); i++) {
                                if (inv.getStackInSlot(i).isEmpty()) {
                                    inv.setStackInSlot(i, stack.split(1));
                                    return true;
                                }
                            }

                            for (int i = 0; i < inv.getSlots(); i++) {
                                if (inv.getStackInSlot(i).is(ModItems.HEAVENLY_THRONE.get())) {
                                    return false;
                                }
                            }

                            if (inv.getSlots() > 0) {
                                inv.setStackInSlot(0, stack.split(1));
                                return true;
                            }

                            return false;
                        })
                        .orElse(false))
                .orElse(false);
    }

    public static LivingEntity findTarget(ServerPlayer p, double range) {
        Vec3 eye = p.getEyePosition();
        Vec3 dir = p.getViewVector(1.0F);
        Vec3 end = eye.add(dir.scale(range));
        var box = p.getBoundingBox().expandTowards(dir.scale(range)).inflate(1.0);
        Optional<LivingEntity> hit = p.level().getEntitiesOfClass(LivingEntity.class, box, e -> e != p).stream().findFirst();
        return hit.orElse(null);
    }

    public static void forceUnequip(LivingEntity target, ServerPlayer caster) {
        // 盔甲与主手/副手
        for (var slot : EquipmentSlot.values()) {
            var st = target.getItemBySlot(slot);
            if (!st.isEmpty()) {
                target.setItemSlot(slot, ItemStack.EMPTY);
                if (target instanceof ServerPlayer sp) {
                    if (!sp.getInventory().add(st)) sp.drop(st, true);
                } else {
                    target.spawnAtLocation(st);
                }
            }
        }
        // Curios 全部强制卸下
        CuriosApi.getCuriosInventory(target).ifPresent(handler -> {
            handler.getCurios().forEach((slotId, stacksHandler) -> {
                var inv = stacksHandler.getStacks();
                for (int i = 0; i < inv.getSlots(); i++) {
                    var st = inv.getStackInSlot(i);
                    if (!st.isEmpty()) {
                        inv.setStackInSlot(i, ItemStack.EMPTY);
                        if (target instanceof ServerPlayer sp) {
                            if (!sp.getInventory().add(st)) sp.drop(st, true);
                        } else {
                            target.spawnAtLocation(st);
                        }
                    }
                }
            });
        });
    }

    private static final Set<UUID> TELEPORT_ALLOW = Collections.newSetFromMap(new ConcurrentHashMap<>());

    public static void grantNextTeleport(ServerPlayer p) {
        TELEPORT_ALLOW.add(p.getUUID());
    }

    public static boolean consumeTeleportAllowance(ServerPlayer p) {
        return TELEPORT_ALLOW.remove(p.getUUID());
    }

    public enum PurgeResult {NONE, OWNER_EXCESS, NON_OWNER}

    public static PurgeResult purgeFateStacks(ServerPlayer player) {
        SkyAccessoriesSavedData data = SkyAccessoriesSavedData.get(player.level());
        UUID owner = data != null ? data.getOwner() : null;

        if (owner == null) {
            return PurgeResult.NONE;
        }

        AtomicBoolean removed = new AtomicBoolean(false);
        boolean isOwner = owner.equals(player.getUUID());

        CuriosApi.getCuriosInventory(player).ifPresent(curiosHandler -> {
            curiosHandler.getStacksHandler("godhood").ifPresent(slotHandler -> {
                IItemHandlerModifiable inv = slotHandler.getStacks();
                int count = 0;
                for (int i = 0; i < inv.getSlots(); i++) {
                    ItemStack stack = inv.getStackInSlot(i);
                    if (stack.is(ModItems.HEAVENLY_THRONE.get())) {
                        if (isOwner && count == 0) {
                            count++;
                            continue;
                        }
                        inv.setStackInSlot(i, ItemStack.EMPTY);
                        removed.set(true);
                    }
                }
            });
        });

        for (int i = 0; i < player.getInventory().items.size(); i++) {
            ItemStack s = player.getInventory().items.get(i);
            if (s.is(ModItems.HEAVENLY_THRONE.get())) {
                player.getInventory().items.set(i, ItemStack.EMPTY);
                removed.set(true);
            }
        }
        for (int i = 0; i < player.getInventory().armor.size(); i++) {
            ItemStack s = player.getInventory().armor.get(i);
            if (s.is(ModItems.HEAVENLY_THRONE.get())) {
                player.getInventory().armor.set(i, ItemStack.EMPTY);
                removed.set(true);
            }
        }

        if (!removed.get()) return PurgeResult.NONE;
        return isOwner ? PurgeResult.OWNER_EXCESS : PurgeResult.NON_OWNER;
    }

    public static Component emperorTitle() {
        return Component.translatable("title.sky_accessories.emperor").withStyle(net.minecraft.ChatFormatting.GOLD);
    }

    public static PlayerTeam ensureEmperorTeam(ServerPlayer sp) {
        var scoreboard = sp.server.getScoreboard();
        var team = scoreboard.getPlayerTeam(EMPEROR_TEAM);
        if (team == null) {
            team = scoreboard.addPlayerTeam(EMPEROR_TEAM);
            team.setPlayerPrefix(emperorTitle());
        }
        return team;
    }

    public static void updateEmperorTeamMembership(ServerPlayer sp) {
        var scoreboard = sp.server.getScoreboard();
        var team = ensureEmperorTeam(sp);
        String name = sp.getScoreboardName();
        var current = scoreboard.getPlayersTeam(name);
        boolean isOwner = isOwnerActive(sp);
        if (isOwner) {
            if (current != team) scoreboard.addPlayerToTeam(name, team);
        } else {
            if (current == team) scoreboard.removePlayerFromTeam(name);
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
}
