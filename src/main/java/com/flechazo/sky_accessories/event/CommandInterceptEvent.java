package com.flechazo.sky_accessories.event;

import net.minecraft.commands.CommandSourceStack;
import net.minecraftforge.eventbus.api.Event;

public class CommandInterceptEvent extends Event {
    private final CommandSourceStack source;
    private final String command;
    private boolean canceled = false;

    public CommandInterceptEvent(CommandSourceStack source, String command) {
        this.source = source;
        this.command = command;
    }

    public CommandSourceStack getSource() { return source; }
    public String getCommand() { return command; }

    public void setCanceled(boolean canceled) { this.canceled = canceled; }
    public boolean isCanceled() { return canceled; }
    public static class GiveCommandEvent extends CommandInterceptEvent {
        public GiveCommandEvent(CommandSourceStack source, String command) { super(source, command); }
    }

    public static class ClearCommandEvent extends CommandInterceptEvent {
        public ClearCommandEvent(CommandSourceStack source, String command) { super(source, command); }
    }

    public static class CuriosClearCommandEvent extends CommandInterceptEvent {
        public CuriosClearCommandEvent(CommandSourceStack source, String command) { super(source, command); }
    }

}
