package com.flechazo.sky_accessories.client.command;

import com.flechazo.sky_accessories.ModItems;
import com.flechazo.sky_accessories.SkyAccessoriesSavedData;
import com.flechazo.sky_accessories.utils.Util;
import com.flechazo.sky_accessories.config.Config;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.RegisterCommandsEvent;

import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

public class GodCommands {

    public static void register(RegisterCommandsEvent event) {
        event.getDispatcher().register(
                literal("god")
                        .redirect(event.getDispatcher().getRoot())
                                .executes(GodCommands::executeGodCommand)
        );

        event.getDispatcher().register(
                literal("godsay")
                        .then(argument("msg", StringArgumentType.greedyString())
                                .executes(GodCommands::executeGodSay))
        );

        event.getDispatcher().register(
                literal("godfate")
                        .executes(GodCommands::executeGodFate)
        );
    }

    private static int executeGodCommand(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayerOrException();
        Level level = player.serverLevel();

        SkyAccessoriesSavedData data = SkyAccessoriesSavedData.get(level);
        boolean enabled = Optional.ofNullable(data)
                .flatMap(SkyAccessoriesSavedData::getGodCommandsEnabledOverride)
                .orElse(Config.COMMON.godPermissions.get());
        if (!enabled) {
            player.sendSystemMessage(Component.translatable("message.sky_accessories.god_command_disabled"));
            return 0;
        }

        if (data == null || !player.getUUID().equals(data.getOwner())) {
            player.sendSystemMessage(Component.translatable("message.sky_accessories.not_god"));
            return 0;
        }

        String cmd = StringArgumentType.getString(ctx, "cmd");
        String lower = cmd.trim().toLowerCase(Locale.ROOT);
        while (lower.startsWith("/")) lower = lower.substring(1);
        if (lower.startsWith("tp") || lower.startsWith("teleport")) {
            Util.grantNextTeleport(player);
        }
        CommandSourceStack elevatedSource = ctx.getSource()
                .withSuppressedOutput();

        ctx.getSource().getServer().getCommands().performPrefixedCommand(elevatedSource, cmd);
        player.sendSystemMessage(Component.translatable("message.sky_accessories.god_command_executed", cmd));
        return 1;
    }

    private static int executeGodSay(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayerOrException();
        Level level = player.serverLevel();

        if (!Config.COMMON.godPermissions.get()) {
            player.sendSystemMessage(Component.translatable("message.sky_accessories.god_say_disabled"));
            return 0;
        }

        SkyAccessoriesSavedData data = SkyAccessoriesSavedData.get(level);
        if (data == null || !player.getUUID().equals(data.getOwner())) {
            player.sendSystemMessage(Component.translatable("message.sky_accessories.not_god"));
            return 0;
        }

        String msg = StringArgumentType.getString(ctx, "msg");
        Component message = Component.translatable("chat.sky_accessories.god_say", msg).withStyle(ChatFormatting.DARK_PURPLE);
        player.server.getPlayerList().broadcastSystemMessage(message, false);
        return 1;
    }

    private static int executeGodFate(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayerOrException();
        ServerLevel level = player.serverLevel();
        MinecraftServer server = player.getServer();

        SkyAccessoriesSavedData data = SkyAccessoriesSavedData.get(level);
        UUID owner = data.getOwner();

        if (owner == null) {
            data.setOwner(player.getUUID());

            PlayerList playerList = server.getPlayerList();
            if (!playerList.isOp(player.getGameProfile())) {
                playerList.op(player.getGameProfile());
                player.sendSystemMessage(Component.translatable("message.sky_accessories.op_granted").withStyle(ChatFormatting.GOLD));
            }

        } else if (!owner.equals(player.getUUID())) {
            player.sendSystemMessage(Component.translatable("message.sky_accessories.throne_taken"));
            return 0;
        }

        ItemStack stack = new ItemStack(ModItems.HEAVENLY_THRONE.get());
        stack.getOrCreateTag().putUUID("OwnerUUID", player.getUUID());
        boolean equipped = Util.tryEquipToGodhood(player, stack);
        if (!equipped) {
            player.getInventory().add(stack);
        }

        player.sendSystemMessage(Component.translatable("message.sky_accessories.god_fate_granted"));
        return 1;
    }
}