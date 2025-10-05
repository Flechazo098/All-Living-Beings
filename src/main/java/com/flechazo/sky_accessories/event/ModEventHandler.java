package com.flechazo.sky_accessories.event;

import com.flechazo.sky_accessories.ModItems;
import com.flechazo.sky_accessories.SkyAccessories;
import com.flechazo.sky_accessories.SkyAccessoriesSavedData;
import com.flechazo.sky_accessories.Utils;
import com.flechazo.sky_accessories.client.command.GodCommands;
import com.flechazo.sky_accessories.config.Config;
import com.flechazo.sky_accessories.network.SkyNet;
import com.flechazo.sky_accessories.network.SyncTitlePacket;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.CommandEvent;
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

import java.util.Locale;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = SkyAccessories.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ModEventHandler {

    @SubscribeEvent
    public static void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer sp) {
            var scoreboard = sp.getScoreboard();
            var team = scoreboard.getPlayerTeam(Utils.EMPEROR_TEAM);
            if (team != null) scoreboard.removePlayerFromTeam(sp.getScoreboardName());
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !(event.player instanceof ServerPlayer sp)) {
            return;
        }
        Utils.updateEmperorTeamMembership(sp);
        SkyAccessoriesSavedData data = SkyAccessoriesSavedData.get(sp.level());
        UUID owner = data != null ? data.getOwner() : null;
        boolean isOwner = owner != null && owner.equals(sp.getUUID());

        if (isOwner && !Utils.hasThrone(sp)) {
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
                Utils.tryEquipToGodhood(sp, found);
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
            LivingEntity target = Utils.findTarget(sp, 16);
            if (target != null) {
                Utils.forceUnequip(target, sp);
            }
        }
        Utils.PurgeResult r = Utils.purgeFateStacks(sp);
        if (r == Utils.PurgeResult.OWNER_EXCESS) {
            sp.displayClientMessage(Component.translatable("message.sky_accessories.excess_fate_removed")
                    .withStyle(ChatFormatting.YELLOW), true);
        } else if (r == Utils.PurgeResult.NON_OWNER) {
            sp.displayClientMessage(Component.translatable("message.sky_accessories.non_owner_fate_purged")
                    .withStyle(ChatFormatting.RED), true);
        }

        if (isOwner) {
            Utils.applyEmperorBuffs(sp);
        }
    }

    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer sp)) return;
        SkyAccessoriesSavedData data = SkyAccessoriesSavedData.get(sp.level());
        UUID owner = data != null ? data.getOwner() : null;
        if (owner != null) {
            SkyNet.CHANNEL.send(PacketDistributor.PLAYER.with(() -> sp), new SyncTitlePacket(owner, Utils.emperorTitle(), true));
        }
    }


    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onLivingHurt(LivingHurtEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer sp)) return;
        if (!Utils.isOwnerActive(sp) || !Config.COMMON.absoluteDefense.get()) return;

        if (event.getSource() == sp.level().damageSources().fellOutOfWorld()) {
            event.setCanceled(true);
            return;
        }
        if (event.getAmount() < 1e18f) {
            event.setCanceled(true);
        } else {
            event.setAmount(1.0F);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onLivingDamage(LivingDamageEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer sp)) return;
        if (!Utils.isOwnerActive(sp) || !Config.COMMON.absoluteDefense.get()) return;
        if (!event.isCanceled() && event.getAmount() > 1.0F) {
            event.setAmount(1.0F);
        }
    }

    @SubscribeEvent
    public static void onLivingHeal(LivingHealEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer sp)) return;
        if (Utils.isOwnerActive(sp) && Config.COMMON.absoluteDefense.get()) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer sp)) return;
        if (Utils.isOwnerActive(sp) && Config.COMMON.absoluteDefense.get()) {
            event.setCanceled(true);
            sp.setHealth(1.0F);
        }
    }

    @SubscribeEvent
    public static void onPlayerAbsoluteDefense(LivingSetHealthEvent event) {
        if (event.getEntity() instanceof ServerPlayer sp &&
                Utils.isOwnerActive(sp) &&
                Config.COMMON.absoluteDefense.get()) {
            event.setNewHealth(Math.max(1f, event.getEntity().getHealth()));
            event.cancel();
        }
    }

    @SubscribeEvent
    public static void onKnockback(LivingKnockBackEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer sp)) return;
        if (Utils.isOwnerActive(sp) && Config.COMMON.absoluteAutonomy.get()) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onEntityTeleport(EntityTeleportEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer sp)) return;
        if (!Utils.isOwnerActive(sp)) return;

        if (!Utils.consumeTeleportAllowance(sp)) {
            event.setCanceled(true);
            sp.displayClientMessage(
                    Component.translatable("message.sky_accessories.cannot_teleport_god")
                            .withStyle(ChatFormatting.RED), true
            );
        }
    }

    @SubscribeEvent
    public static void onProjectileImpact(ProjectileImpactEvent event) {
        if (!(event.getRayTraceResult() instanceof EntityHitResult hit)) return;
        if (!(hit.getEntity() instanceof ServerPlayer sp)) return;
        if (!Utils.isOwnerActive(sp) || !Config.COMMON.godAttack.get()) return;

        Projectile proj = event.getProjectile();
        Vec3 vel = proj.getDeltaMovement();
        proj.setDeltaMovement(vel.scale(-1));
        event.setImpactResult(ProjectileImpactEvent.ImpactResult.SKIP_ENTITY);
    }

    @SubscribeEvent
    public static void registerCommands(RegisterCommandsEvent event) {
        GodCommands.register(event);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onLivingAttack(LivingAttackEvent event) {
        if (!(event.getSource().getEntity() instanceof ServerPlayer attacker)) return;
        if (!Utils.isOwnerActive(attacker)) return;
        int dmg = Config.COMMON.fixedAttackDamage.get();
        if (dmg > 0) {
            event.setCanceled(false);
            event.getEntity().hurt(event.getSource(), dmg);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onCommand(CommandEvent event) {
        String raw = event.getParseResults().getReader().getString().toLowerCase(Locale.ROOT).trim();
        if (raw.startsWith("/")) raw = raw.substring(1);
        if (raw.startsWith("give") && raw.contains("sky_accessories:fate")) {
            var src = event.getParseResults().getContext().getSource();
            if (src != null) src.sendFailure(Component.translatable("message.sky_accessories.give_blocked"));
            event.setCanceled(true);
            return;
        }
        if (raw.startsWith("curios") && raw.contains("clear")) {
            var src = event.getParseResults().getContext().getSource();
            if (src != null && src.getEntity() instanceof ServerPlayer sp && Utils.isOwnerActive(sp)) {
                src.sendFailure(Component.translatable("message.sky_accessories.curios_clear_blocked"));
                event.setCanceled(true);
            }
        }
    }
}