package com.flechazo.all_living_beings.utils;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandlerModifiable;
import top.theillusivec4.curios.api.CuriosApi;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class PlayerInventoryUtils {
    private PlayerInventoryUtils() {
    }

    private static void forEachCuriosSlotInternal(LivingEntity entity,
                                                  BiConsumer<IItemHandlerModifiable, Integer> consumer) {
        CuriosApi.getCuriosInventory(entity).ifPresent(handler ->
                handler.getCurios().forEach((slotId, stacksHandler) -> {
                    IItemHandlerModifiable inv = stacksHandler.getStacks();
                    for (int i = 0; i < inv.getSlots(); i++) {
                        consumer.accept(inv, i);
                    }
                })
        );
    }

    private static void forEachCuriosSlotInternal(LivingEntity entity, String slotId,
                                                  BiConsumer<IItemHandlerModifiable, Integer> consumer) {
        CuriosApi.getCuriosInventory(entity).ifPresent(handler ->
                handler.getStacksHandler(slotId).ifPresent(stacksHandler -> {
                    IItemHandlerModifiable inv = stacksHandler.getStacks();
                    for (int i = 0; i < inv.getSlots(); i++) {
                        consumer.accept(inv, i);
                    }
                })
        );
    }

    public static void forEachPlayerStack(ServerPlayer player, Consumer<ItemStack> consumer) {
        player.getInventory().items.forEach(consumer);
        player.getInventory().armor.forEach(consumer);
        player.getInventory().offhand.forEach(consumer);
        forEachCuriosSlotInternal(player, (inv, i) -> consumer.accept(inv.getStackInSlot(i)));
    }

    public static void forEachCuriosStack(LivingEntity entity, Consumer<ItemStack> consumer) {
        forEachCuriosSlotInternal(entity, (inv, i) -> consumer.accept(inv.getStackInSlot(i)));
    }

    public static void forEachCuriosSlot(LivingEntity entity,
                                         BiConsumer<IItemHandlerModifiable, Integer> consumer) {
        forEachCuriosSlotInternal(entity, consumer);
    }

    public static void forEachCuriosSlot(LivingEntity entity, String slotId,
                                         BiConsumer<IItemHandlerModifiable, Integer> consumer) {
        forEachCuriosSlotInternal(entity, slotId, consumer);
    }
}