package com.flechazo.all_living_beings.event.handler;

import com.flechazo.all_living_beings.AllLivingBeings;
import com.flechazo.all_living_beings.client.command.GodCommands;
import com.flechazo.all_living_beings.config.Config;
import com.flechazo.all_living_beings.data.ALBSavedData;
import com.flechazo.all_living_beings.item.HeavenlyThroneItem;
import com.flechazo.all_living_beings.network.SyncTitlePacket;
import com.flechazo.all_living_beings.registry.ModItems;
import com.flechazo.all_living_beings.network.NetworkHandler;
import com.flechazo.all_living_beings.utils.TeleportUtil;
import com.flechazo.all_living_beings.utils.Util;
import io.redspace.ironsspellbooks.api.events.SpellOnCastEvent;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
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
import vazkii.botania.api.BotaniaForgeCapabilities;
import vazkii.botania.api.mana.ManaItem;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
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
        Util.applyISSCastTimeReductionAttribute(sp, isOwner);

        // 6) 神权压制（强制卸下目标装备）
        Util.handleGodSuppression(sp, isOwner);

        // 7) 清理命运堆叠并提示
        Util.purgeFateStacksAndNotify(sp);

        // 8) 天帝增益
        Util.tickEmperorBuffs(sp, isOwner);

        // 9) 佩戴后攻击距离与挖掘距离 +64
        Util.applyReachAndMiningDistance(sp, isOwner);

        // 10) 奔跑时台阶高度提升（可配置）
        Util.applyStepAssistWhenSprinting(sp, isOwner);
    }


    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !(event.side.isServer())) return;
        for (ServerPlayer player : event.getServer().getPlayerList().getPlayers()) {
            if (player == null) continue;

            boolean isOwner = Util.isBoundOwner(player);
            // 植物魔法无限 Mana
            Util.applyBOTInfinityManager(player, isOwner);
        }
    }

    @SubscribeEvent
    public static void onLivingTickPreventBossTarget(LivingEvent.LivingTickEvent event) {
        var entity = event.getEntity();
        if (!(entity instanceof Mob mob)) return;

        var target = mob.getTarget();
        if (target == null) return;

        if (Util.shouldPreventTargeting(mob, target)) {
            mob.setTarget(null);
            mob.setAggressive(false);
            mob.setLastHurtMob(null);
        }
    }

    @SubscribeEvent
    public static void onLivingChangeTarget(LivingChangeTargetEvent event) {
        LivingEntity entity = event.getEntity();
        LivingEntity newTarget = event.getNewTarget();

        if (!(entity instanceof Mob mob)) return;
        if (newTarget == null) return;

        if (Util.shouldPreventTargeting(mob, newTarget)) {
            event.setCanceled(true);
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
        var owner = proj.getOwner();
    
        // 若无法识别射手，按当前方向远离玩家一段再放行，避免重碰撞
        if (!(owner instanceof LivingEntity shooter)) {
            Vec3 dir = proj.getDeltaMovement().normalize();
            if (dir.lengthSqr() < 1e-6) dir = sp.getViewVector(1.0F);
            double speed = Math.max(1.5, proj.getDeltaMovement().length());
            Vec3 start = sp.getEyePosition().add(sp.getLookAngle().scale(0.3));
            proj.setPos(start.x, start.y, start.z);
            proj.setDeltaMovement(dir.scale(speed));
            proj.hurtMarked = true;
            proj.hasImpulse = true;
            if (proj instanceof Arrow arrow) {
                arrow.setCritArrow(true);
                arrow.inGround = false;
            }
            event.setImpactResult(ProjectileImpactEvent.ImpactResult.SKIP_ENTITY);
            return;
        }
    
        // 将弹射物移至玩家眼前并朝射手发射
        Vec3 start = sp.getEyePosition().add(sp.getLookAngle().scale(0.3));
        Vec3 aim = shooter.getEyePosition();
        Vec3 dir = aim.subtract(start).normalize();
        double speed = Math.max(1.5, proj.getDeltaMovement().length());
    
        proj.setPos(start.x, start.y, start.z);
        proj.setDeltaMovement(dir.scale(speed * 1.25));
    
        // 旋转朝向（避免物理抖动）
        proj.setYRot((float)(Math.atan2(dir.z, dir.x) * 180.0 / Math.PI) - 90.0F);
        proj.setXRot((float)(-(Math.atan2(dir.y, Math.sqrt(dir.x*dir.x + dir.z*dir.z)) * 180.0 / Math.PI)));
    
        // 归属改为玩家，使回击能正常伤害射手
        proj.setOwner(sp);
    
        proj.hurtMarked = true;
        proj.hasImpulse = true;
    
        if (proj instanceof Arrow arrow) {
            arrow.setCritArrow(true);
            arrow.inGround = false;
        }
    
        // 跳过当前命中处理，继续飞行，不再对玩家造成碰撞
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

    @SubscribeEvent
    public static void attachItemCaps(AttachCapabilitiesEvent<ItemStack> event) {
        ItemStack stack = event.getObject();
        if (stack.getItem() == ModItems.HEAVENLY_THRONE.get()) {
            event.addCapability(new ResourceLocation(AllLivingBeings.MODID, "fate"), new ICapabilityProvider() {
                final LazyOptional<ManaItem> mana = LazyOptional.of(() -> new HeavenlyThroneItem.ManaItemImpl(stack));

                @Override
                public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
                    if (cap == BotaniaForgeCapabilities.MANA_ITEM) {
                        return mana.cast();
                    }
                    return LazyOptional.empty();
                }
            });
        }
    }
}