package com.flechazo.all_living_beings.event;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.Event;

public abstract class CommandInterceptEvent extends Event {
    private final CommandSourceStack source;
    private final String command;
    private boolean canceled = false;

    public CommandInterceptEvent(CommandSourceStack source, String command) {
        this.source = source;
        this.command = command;
    }

    public CommandSourceStack getSource() {
        return source;
    }

    public String getCommand() {
        return command;
    }

    public boolean isCanceled() {
        return canceled;
    }

    public void setCanceled(boolean canceled) {
        this.canceled = canceled;
    }

    public static class GiveCommandEvent extends CommandInterceptEvent {
        private final Item item;
        private final int count;

        public GiveCommandEvent(CommandSourceStack source, String command, Item item, int count) {
            super(source, command);
            this.item = item;
            this.count = count;
        }

        public Item getItem() {
            return item;
        }

        public int getCount() {
            return count;
        }
    }

    public static class ClearCommandEvent extends CommandInterceptEvent {
        public ClearCommandEvent(CommandSourceStack source, String command) {
            super(source, command);
        }
    }

    public static class CuriosClearCommandEvent extends CommandInterceptEvent {
        public CuriosClearCommandEvent(CommandSourceStack source, String command) {
            super(source, command);
        }
    }

}
