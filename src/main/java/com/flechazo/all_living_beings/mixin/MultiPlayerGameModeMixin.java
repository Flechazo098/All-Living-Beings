package com.flechazo.all_living_beings.mixin;

import com.flechazo.all_living_beings.registry.ModItems;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundSetCreativeModeSlotPacket;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * 拦截创造模式物品栏的摧毁物品
 */
@Mixin(MultiPlayerGameMode.class)
public abstract class MultiPlayerGameModeMixin {

    @Shadow
    @Final
    private Minecraft minecraft;

    @Redirect(
            method = "handleCreativeModeItemAdd",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/multiplayer/ClientPacketListener;send(Lnet/minecraft/network/protocol/Packet;)V"
            )
    )
    private void redirectPacketSend(ClientPacketListener instance, Packet<?> packet) {

        if (packet instanceof ServerboundSetCreativeModeSlotPacket slotPacket) {

            ItemStack stackToSet = slotPacket.getItem();
            int slotId = slotPacket.getSlotNum();

            if (stackToSet.isEmpty()) {

                AbstractContainerMenu menu = this.minecraft.player.inventoryMenu;

                if (slotId >= 1 && slotId < menu.slots.size()) {

                    ItemStack currentStack = menu.getSlot(slotId).getItem();

                    if (currentStack.getItem() == ModItems.HEAVENLY_THRONE.get()) {
                        return;
                    }
                }
            }
        }
        instance.send(packet);
    }
}