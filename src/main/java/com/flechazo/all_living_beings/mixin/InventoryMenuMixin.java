package com.flechazo.all_living_beings.mixin;

import com.flechazo.all_living_beings.utils.Util;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "net.minecraft.world.inventory.InventoryMenu$1")
public abstract class InventoryMenuMixin extends Slot {

    public InventoryMenuMixin(Container p_40223_, int p_40224_, int p_40225_, int p_40226_) {
        super(p_40223_, p_40224_, p_40225_, p_40226_);
    }

    @Inject(method = "mayPickup", at = @At(value = "HEAD"), cancellable = true)
    private void allowRemoveBinding(Player player, CallbackInfoReturnable<Boolean> cir) {
        if (!(player instanceof ServerPlayer sp)) {
            return;
        }

        if (!Util.isBoundOwner(sp)) {
            return;
        }

        cir.setReturnValue(true);
    }
}
