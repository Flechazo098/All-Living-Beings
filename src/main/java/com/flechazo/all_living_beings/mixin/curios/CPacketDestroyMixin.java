package com.flechazo.all_living_beings.mixin.curios;

import com.flechazo.all_living_beings.registry.ModItems;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotAttribute;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.inventory.IDynamicStackHandler;
import top.theillusivec4.curios.common.network.NetworkHandler;
import top.theillusivec4.curios.common.network.client.CPacketDestroy;
import top.theillusivec4.curios.common.network.server.sync.SPacketSyncStack;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.function.Supplier;

/**
 * 拦截curios自己的清理发包
 */
@Mixin(value = CPacketDestroy.class, remap = false)
public class CPacketDestroyMixin {

    /**
     * 完全替换原 handle 方法，实现选择性清理 Curios 槽位
     *
     * @author Flechazo
     * @reason 完全替换掉，因为lambda
     */
    @Overwrite
    public static void handle(CPacketDestroy msg, Supplier<NetworkEvent.Context> ctx) {
        NetworkEvent.Context context = ctx.get();
        context.enqueueWork(() -> {
            ServerPlayer sender = context.getSender();
            if (sender == null) return;

            CuriosApi.getCuriosInventory(sender).ifPresent(handler -> {
                handler.getCurios().values().forEach(stacksHandler -> {
                    IDynamicStackHandler stackHandler = stacksHandler.getStacks();
                    IDynamicStackHandler cosmeticStackHandler = stacksHandler.getCosmeticStacks();
                    String id = stacksHandler.getIdentifier();

                    for (int i = 0; i < stackHandler.getSlots(); i++) {
                        ItemStack stack = stackHandler.getStackInSlot(i);

                        if ("godhood".equals(id) || (!stack.isEmpty() && stack.is(ModItems.HEAVENLY_THRONE.get())))
                            continue;

                        UUID uuid = UUID.nameUUIDFromBytes((id + i).getBytes());
                        SlotContext slotContext = new SlotContext(id, sender, i, false,
                                stacksHandler.getRenders().size() > i && stacksHandler.getRenders().get(i));

                        Multimap<Attribute, AttributeModifier> attrMap = CuriosApi.getAttributeModifiers(slotContext, uuid, stack);
                        Multimap<String, AttributeModifier> slotMods = HashMultimap.create();
                        Set<SlotAttribute> toRemove = new HashSet<>();
                        for (Attribute attr : attrMap.keySet()) {
                            if (attr instanceof SlotAttribute wrapper) {
                                slotMods.putAll(wrapper.getIdentifier(), attrMap.get(attr));
                                toRemove.add(wrapper);
                            }
                        }
                        toRemove.forEach(attrMap::removeAll);

                        sender.getAttributes().removeAttributeModifiers(attrMap);
                        handler.removeSlotModifiers(slotMods);

                        CuriosApi.getCurio(stack).ifPresent(curio -> curio.onUnequip(slotContext, stack));

                        stackHandler.setStackInSlot(i, ItemStack.EMPTY);
                        cosmeticStackHandler.setStackInSlot(i, ItemStack.EMPTY);

                        SPacketSyncStack syncEquip = new SPacketSyncStack(sender.getId(), id, i, ItemStack.EMPTY,
                                SPacketSyncStack.HandlerType.EQUIPMENT, new CompoundTag());
                        SPacketSyncStack syncCosmetic = new SPacketSyncStack(sender.getId(), id, i, ItemStack.EMPTY,
                                SPacketSyncStack.HandlerType.COSMETIC, new CompoundTag());

                        NetworkHandler.INSTANCE.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> sender), syncEquip);
                        NetworkHandler.INSTANCE.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> sender), syncCosmetic);
                    }
                });
            });
        });

        context.setPacketHandled(true);
    }
}
