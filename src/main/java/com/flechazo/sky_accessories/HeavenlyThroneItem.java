package com.flechazo.sky_accessories;

import com.flechazo.sky_accessories.client.ClientKeyMappings;
import com.flechazo.sky_accessories.config.Config;
import com.flechazo.sky_accessories.utils.Util;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import org.lwjgl.glfw.GLFW;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurio;

import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;

public class HeavenlyThroneItem extends Item {
    public HeavenlyThroneItem(Properties props) {
        super(props);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide) {
            SkyAccessoriesSavedData data = SkyAccessoriesSavedData.get(level);
            UUID current = data.getOwner();
            UUID me = player.getUUID();
            if (current == null) {
                data.setOwner(me);
                stack.getOrCreateTag().putUUID("OwnerUUID", me);
                player.displayClientMessage(Component.translatable("message.sky_accessories.throne_bound"), true);
            } else if (current.equals(me)) {
                player.displayClientMessage(Component.translatable("message.sky_accessories.already_god"), true);
            } else {
                player.displayClientMessage(Component.translatable("message.sky_accessories.throne_taken"), true);
            }
            if (player instanceof ServerPlayer sp) {
                boolean equipped = Util.tryEquipToGodhood(sp, stack);
                if (equipped) {
                    player.displayClientMessage(Component.translatable("message.sky_accessories.auto_equipped_godhood"), true);
                }
            }
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return true;
    }

    @Override
    public boolean isFireResistant() {
        return true;
    }

    @Override
    public boolean onDroppedByPlayer(ItemStack item, Player player) {
        return false;
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.sky_accessories.fate.lore1"));
        tooltip.add(Component.translatable("tooltip.sky_accessories.fate.lore2"));
        boolean shift = InputConstants.isKeyDown(GLFW.glfwGetCurrentContext(), GLFW.GLFW_KEY_LEFT_SHIFT)
                || InputConstants.isKeyDown(GLFW.glfwGetCurrentContext(), GLFW.GLFW_KEY_RIGHT_SHIFT);
        boolean alt = InputConstants.isKeyDown(GLFW.glfwGetCurrentContext(), GLFW.GLFW_KEY_LEFT_ALT)
                || InputConstants.isKeyDown(GLFW.glfwGetCurrentContext(), GLFW.GLFW_KEY_RIGHT_ALT);
        if (shift) {
            tooltip.add(Component.translatable("tooltip.sky_accessories.fate.mechanics.title"));
            tooltip.add(Component.translatable("tooltip.sky_accessories.fate.mechanics.defense"));
            tooltip.add(Component.translatable("tooltip.sky_accessories.fate.mechanics.autonomy"));
            tooltip.add(Component.translatable("tooltip.sky_accessories.fate.mechanics.travel"));
            tooltip.add(Component.translatable("tooltip.sky_accessories.fate.mechanics.suppress"));
        } else if (alt) {
            tooltip.add(Component.translatable("tooltip.sky_accessories.fate.debug.title"));
            tooltip.add(Component.translatable("tooltip.sky_accessories.fate.debug.damage_cap", "1"));
            tooltip.add(Component.translatable("tooltip.sky_accessories.fate.debug.absolute_defense", Config.COMMON.absoluteDefense.get()));
            tooltip.add(Component.translatable("tooltip.sky_accessories.fate.debug.absolute_autonomy", Config.COMMON.absoluteAutonomy.get()));
        } else {
            tooltip.add(Component.translatable("tooltip.sky_accessories.fate.hint_shift"));
            tooltip.add(Component.translatable("tooltip.sky_accessories.fate.hint_alt"));
        }
        tooltip.add(Component.translatable("tooltip.sky_accessories.fate.open_gui", ClientKeyMappings.OPEN_THRONE_GUI.getTranslatedKeyMessage()));
    }

    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundTag nbt) {
        return CuriosApi.createCurioProvider(new ICurio() {
            @Override
            public ItemStack getStack() {
                return stack;
            }

            @Override
            public boolean canEquip(SlotContext slotContext) {
                return false;
            }

            @Override
            public boolean canUnequip(SlotContext slotContext) {
                return false;
            }

            @Override
            public boolean canSync(SlotContext slotContext) {
                return false;
            }
        });
    }
}