package me.char321.sfrecipes.command;

import org.bukkit.command.CommandSender;

public abstract class SubCommand {
    abstract void onExecute(CommandSender sender, String[] args);
    abstract String getCommandName();
}
