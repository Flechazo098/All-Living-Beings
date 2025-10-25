package com.flechazo.all_living_beings.network;

import com.flechazo.all_living_beings.data.ALBSavedData;
import com.flechazo.all_living_beings.utils.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class BindTeleportPacket {
    private final ResourceLocation dimId;

    public BindTeleportPacket(ResourceLocation dimId) {
        this.dimId = dimId;
    }

    public static void encode(BindTeleportPacket pkt, FriendlyByteBuf buf) {
        buf.writeResourceLocation(pkt.dimId);
    }

    public static BindTeleportPacket decode(FriendlyByteBuf buf) {
        return new BindTeleportPacket(buf.readResourceLocation());
    }

    public static void handle(BindTeleportPacket pkt, Supplier<NetworkEvent.Context> ctxSupplier) {
        var ctx = ctxSupplier.get();
        ctx.enqueueWork(() -> {
            ServerPlayer sp = ctx.getSender();
            if (sp == null) return;
            if (!Util.isOwnerActive(sp)) return;
            ALBSavedData data = ALBSavedData.get(sp.level());
            if (data == null) return;
            BlockPos here = sp.blockPosition();
            data.setBound(pkt.dimId, here);

            sp.displayClientMessage(
                    Component.translatable(
                            "message.all_living_beings.bind_success",
                            pkt.dimId.toString(), here.getX(), here.getY(), here.getZ()
                    ),
                    true
            );
        });
        ctx.setPacketHandled(true);
    }
}