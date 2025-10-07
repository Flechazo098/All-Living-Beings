package com.flechazo.all_living_beings.client.command;

import com.flechazo.all_living_beings.config.Config;
import com.flechazo.all_living_beings.data.ALBSavedData;
import com.flechazo.all_living_beings.registry.ModItems;
import com.flechazo.all_living_beings.utils.Util;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.RegisterCommandsEvent;

import java.util.Locale;
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

        ALBSavedData data = ALBSavedData.get(level);
        if (!Config.COMMON.godPermissions.get()) {
            player.sendSystemMessage(Component.translatable("message.all_living_beings.god_command_disabled"));
            return 0;
        }

        if (data == null || !player.getUUID().equals(data.getOwner())) {
            player.sendSystemMessage(Component.translatable("message.all_living_beings.not_god"));
            return 0;
        }

        String cmd = StringArgumentType.getString(ctx, "cmd");
        String lower = cmd.trim().toLowerCase(Locale.ROOT);
        while (lower.startsWith("/")) lower = lower.substring(1);
        CommandSourceStack elevatedSource = ctx.getSource()
                .withSuppressedOutput();

        ctx.getSource().getServer().getCommands().performPrefixedCommand(elevatedSource, cmd);
        player.sendSystemMessage(Component.translatable("message.all_living_beings.god_command_executed", cmd));
        return 1;
    }

    private static int executeGodSay(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayerOrException();
        Level level = player.serverLevel();

        if (!Config.COMMON.godPermissions.get()) {
            player.sendSystemMessage(Component.translatable("message.all_living_beings.god_say_disabled"));
            return 0;
        }

        ALBSavedData data = ALBSavedData.get(level);
        if (data == null || !player.getUUID().equals(data.getOwner())) {
            player.sendSystemMessage(Component.translatable("message.all_living_beings.not_god"));
            return 0;
        }

        String msg = StringArgumentType.getString(ctx, "msg");
        Component message = Component.translatable("chat.all_living_beings.god_say", msg).withStyle(ChatFormatting.DARK_PURPLE);
        player.server.getPlayerList().broadcastSystemMessage(message, false);
        return 1;
    }

    private static int executeGodFate(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayerOrException();
        ServerLevel level = player.serverLevel();
        MinecraftServer server = player.getServer();

        ALBSavedData data = ALBSavedData.get(level);
        UUID owner = data.getOwner();

        if (owner == null) {
            data.setOwner(player.getUUID());

            PlayerList playerList = server.getPlayerList();
            if (!playerList.isOp(player.getGameProfile())) {
                playerList.op(player.getGameProfile());
                player.sendSystemMessage(Component.translatable("message.all_living_beings.op_granted").withStyle(ChatFormatting.GOLD));
            }

        } else if (!owner.equals(player.getUUID())) {
            player.sendSystemMessage(Component.translatable("message.all_living_beings.throne_taken"));
            return 0;
        }

        ItemStack stack = new ItemStack(ModItems.HEAVENLY_THRONE.get());
        stack.getOrCreateTag().putUUID("OwnerUUID", player.getUUID());
        boolean equipped = Util.tryEquipToGodhood(player, stack);
        if (!equipped) {
            player.getInventory().add(stack);
        }

        player.sendSystemMessage(Component.translatable("message.all_living_beings.god_fate_granted"));
        return 1;
    }
}