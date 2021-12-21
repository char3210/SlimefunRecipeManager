package me.char321.sfrecipes.command;

import me.char321.sfrecipes.SFRM;
import org.bukkit.command.CommandSender;

public class ReloadCommand extends SubCommand {
    private SFRM plugin;

    public ReloadCommand(SFRM plugin) {
        this.plugin = plugin;
    }

    @Override
    public void onExecute(CommandSender sender, String[] args) {
        SFRM.instance().reloadConfigs();
    }

    @Override
    public String getCommandName() {
        return "reload";
    }
}
