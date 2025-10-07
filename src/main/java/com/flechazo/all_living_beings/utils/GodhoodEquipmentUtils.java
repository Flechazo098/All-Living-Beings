package com.flechazo.all_living_beings.utils;

import com.flechazo.all_living_beings.data.ALBSavedData;
import com.flechazo.all_living_beings.registry.ModItems;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandlerModifiable;
import top.theillusivec4.curios.api.CuriosApi;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

public class GodhoodEquipmentUtils {
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

    public static void autoEquipThroneFromInventory(ServerPlayer sp) {
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
            tryEquipToGodhood(sp, found);
        }
    }

    public static PurgeResult purgeFateStacks(ServerPlayer player) {
        ALBSavedData data = ALBSavedData.get(player.level());
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

        clearInventoryStacks(player.getInventory().items, ModItems.HEAVENLY_THRONE.get(), removed);
        clearInventoryStacks(player.getInventory().armor, ModItems.HEAVENLY_THRONE.get(), removed);

        if (!removed.get()) return PurgeResult.NONE;
        return isOwner ? PurgeResult.OWNER_EXCESS : PurgeResult.NON_OWNER;
    }

    private static void clearInventoryStacks(List<ItemStack> items, Item item, AtomicBoolean removed) {
        for (int i = 0; i < items.size(); i++) {
            ItemStack s = items.get(i);
            if (s.is(item)) {
                items.set(i, ItemStack.EMPTY);
                removed.set(true);
            }
        }
    }
}