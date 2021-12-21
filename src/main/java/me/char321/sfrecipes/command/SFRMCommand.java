package me.char321.sfrecipes.command;

import me.char321.sfrecipes.SFRM;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class SFRMCommand implements CommandExecutor {
    private List<SubCommand> subcommands = new LinkedList<>();

    public SFRMCommand(SFRM plugin) {
        subcommands.add(new ReloadCommand(SFRM.instance()));
        subcommands.add(new GenRecipesCommand(SFRM.instance()));

        plugin.getCommand("sfrecipemanager").setTabCompleter(new SFRMTabCompleter(this));
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length > 0) {
            for(SubCommand subcmd : subcommands) {
                if(args[0].equalsIgnoreCase(subcmd.getCommandName())) {
                    subcmd.onExecute(sender, args);
                    return true;
                }
            }
            sender.sendMessage("Unknown subcommand! Available subcommands are: " + subcommands.stream().map(SubCommand::getCommandName).collect(Collectors.joining(", ")));
            return false;
        }
        sender.sendMessage("SFRM version " + SFRM.instance().getDescription().getVersion());
        return true;
    }

    public List<SubCommand> getSubCommands() {
        return subcommands;
    }
}
