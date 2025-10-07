package com.flechazo.all_living_beings.event.handler;

import com.flechazo.all_living_beings.AllLivingBeings;
import com.flechazo.all_living_beings.client.command.GodCommands;
import com.flechazo.all_living_beings.config.Config;
import com.flechazo.all_living_beings.data.ALBSavedData;
import com.flechazo.all_living_beings.network.SyncTitlePacket;
import com.flechazo.all_living_beings.registry.NetworkHandler;
import com.flechazo.all_living_beings.utils.TeleportUtil;
import com.flechazo.all_living_beings.utils.Util;
import io.redspace.ironsspellbooks.api.events.SpellOnCastEvent;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.projectile.Projectile;
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

        // 1) 前缀与头衔
        Util.updateEmperorTeamMembership(sp);

        // 2) 绑定状态
        boolean isOwner = Util.isBoundOwner(sp);

        // 3) 自动佩戴天座（Curios）
        Util.autoEquipThroneIfOwner(sp);

        // 4) 飞行能力开关
        Util.updateFlightAbilities(sp, isOwner);

        // 5) 铁魔法吟唱时间缩短属性
        Util.applyCastTimeReductionAttribute(sp, isOwner);

        // 6) 神权压制（强制卸下目标装备）
        Util.handleGodSuppression(sp, isOwner);

        // 7) 清理命运堆叠并提示
        Util.purgeFateStacksAndNotify(sp);

        // 8) 天帝增益
        Util.tickEmperorBuffs(sp, isOwner);
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

    @SubscribeEvent
    public static void onSpellCast(SpellOnCastEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer sp)) return;
        if (!Util.isOwnerActive(sp)) return;
        event.setManaCost(1);
    }
}