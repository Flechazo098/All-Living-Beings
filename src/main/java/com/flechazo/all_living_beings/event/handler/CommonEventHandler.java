package com.flechazo.all_living_beings.event.handler;

import com.flechazo.all_living_beings.AllLivingBeings;
import com.flechazo.all_living_beings.client.command.GodCommands;
import com.flechazo.all_living_beings.config.Config;
import com.flechazo.all_living_beings.data.ALBSavedData;
import com.flechazo.all_living_beings.network.SyncTitlePacket;
import com.flechazo.all_living_beings.registry.ModItems;
import com.flechazo.all_living_beings.registry.NetworkHandler;
import com.flechazo.all_living_beings.utils.TeleportUtil;
import com.flechazo.all_living_beings.utils.Util;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityTeleportEvent;
import net.minecraftforge.event.entity.ProjectileImpactEvent;
import net.minecraftforge.event.entity.living.*;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;

import java.util.UUID;

@Mod.EventBusSubscriber(modid = AllLivingBeings.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class CommonEventHandler {

    @SubscribeEvent
    public static void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer sp) {
            var scoreboard = sp.getScoreboard();
            var team = scoreboard.getPlayerTeam(Util.EMPEROR_TEAM);
            if (team != null) scoreboard.removePlayerFromTeam(sp.getScoreboardName());
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !(event.player instanceof ServerPlayer sp)) {
            return;
        }
        Util.updateEmperorTeamMembership(sp);
        ALBSavedData data = ALBSavedData.get(sp.level());
        UUID owner = data != null ? data.getOwner() : null;
        boolean isOwner = owner != null && owner.equals(sp.getUUID());

        if (isOwner && !Util.hasThrone(sp)) {
            ItemStack found = ItemStack.EMPTY;
            for (ItemStack stack : sp.getInventory().items) {
                if (stack.is(ModItems.HEAVENLY_THRONE.get())) {
                    found = stack;
                    break;
                }
            }
            if (found.isEmpty()) {
                for (ItemStack stack : sp.getInventory().armor) {
                    if (stack.is(ModItems.HEAVENLY_THRONE.get())) {
                        found = stack;
                        break;
                    }
                }
            }
            if (!found.isEmpty()) {
                Util.tryEquipToGodhood(sp, found);
            }
        }

        if (isOwner) {
            sp.getAbilities().mayfly = true;
        } else {
            if (sp.getAbilities().flying) {
                sp.getAbilities().flying = false;
            }
            sp.getAbilities().mayfly = false;
        }
        sp.onUpdateAbilities();

        if (isOwner && Config.COMMON.godSuppression.get()) {
            LivingEntity target = Util.findTarget(sp, 16);
            if (target != null) {
                Util.forceUnequip(target, sp);
            }
        }
        Util.PurgeResult r = Util.purgeFateStacks(sp);
        if (r == Util.PurgeResult.OWNER_EXCESS) {
            sp.displayClientMessage(Component.translatable("message.all_living_beings.excess_fate_removed")
                    .withStyle(ChatFormatting.YELLOW), true);
        } else if (r == Util.PurgeResult.NON_OWNER) {
            sp.displayClientMessage(Component.translatable("message.all_living_beings.non_owner_fate_purged")
                    .withStyle(ChatFormatting.RED), true);
        }

        if (isOwner) {
            Util.applyEmperorBuffs(sp);
        }
    }

    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer sp)) return;
        ALBSavedData data = ALBSavedData.get(sp.level());
        UUID owner = data != null ? data.getOwner() : null;
        if (owner != null) {
            NetworkHandler.CHANNEL.send(PacketDistributor.PLAYER.with(() -> sp), new SyncTitlePacket(owner, Util.emperorTitle(), true));
        }
    }


    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        var source = event.getSource();
        var attacker = source.getEntity();

        if (attacker instanceof ServerPlayer player) {
            if (Util.isOwnerActive(player)) {
                int fixedDmg = Config.COMMON.fixedAttackDamage.get();
                if (fixedDmg > 0) {
                    event.setAmount(fixedDmg);
                    return;
                }
            }
        }

        if (event.getEntity() instanceof ServerPlayer sp) {
            if (Util.isOwnerActive(sp) && Config.COMMON.absoluteDefense.get()) {
                if (source == sp.level().damageSources().fellOutOfWorld()) {
                    event.setCanceled(true);
                    return;
                }
                if (event.getAmount() < 1e18f) {
                    event.setCanceled(true);
                } else {
                    event.setAmount(1.0F);
                }
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onLivingDamage(LivingDamageEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer sp)) return;
        if (!Util.isOwnerActive(sp) || !Config.COMMON.absoluteDefense.get()) return;
        if (!event.isCanceled() && event.getAmount() > 1.0F) {
            event.setAmount(1.0F);
        }
    }

    @SubscribeEvent
    public static void onLivingHeal(LivingHealEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer sp)) return;
        if (Util.isOwnerActive(sp) && Config.COMMON.absoluteDefense.get()) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer sp)) return;
        if (Util.isOwnerActive(sp) && Config.COMMON.absoluteDefense.get()) {
            event.setCanceled(true);
            sp.setHealth(1.0F);
        }
    }

    @SubscribeEvent
    public static void onKnockback(LivingKnockBackEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer sp)) return;
        if (Util.isOwnerActive(sp) && Config.COMMON.absoluteAutonomy.get()) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onEntityTeleport(EntityTeleportEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer sp)) return;
        if (!Util.isOwnerActive(sp)) return;

        if (!TeleportUtil.consumeTeleportAllowance(sp)) {
            event.setCanceled(true);
            sp.displayClientMessage(
                    Component.translatable("message.all_living_beings.cannot_teleport_god")
                            .withStyle(ChatFormatting.RED), true
            );
        }
    }

    @SubscribeEvent
    public static void onProjectileImpact(ProjectileImpactEvent event) {
        if (!(event.getRayTraceResult() instanceof EntityHitResult hit)) return;
        if (!(hit.getEntity() instanceof ServerPlayer sp)) return;
        if (!Util.isOwnerActive(sp) || !Config.COMMON.godAttack.get()) return;

        Projectile proj = event.getProjectile();
        Vec3 vel = proj.getDeltaMovement();
        proj.setDeltaMovement(vel.scale(-1));
        event.setImpactResult(ProjectileImpactEvent.ImpactResult.SKIP_ENTITY);
    }

    @SubscribeEvent
    public static void registerCommands(RegisterCommandsEvent event) {
        GodCommands.register(event);
    }
}