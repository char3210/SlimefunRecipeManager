package me.char321.sfrecipes.command;

import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.implementation.Slimefun;
import me.char321.sfrecipes.SFRM;
import me.char321.sfrecipes.utils.ItemUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class GenRecipesCommand extends SubCommand {
    private SFRM plugin;

    public GenRecipesCommand(SFRM plugin) {
        this.plugin = plugin;
    }

    @Override
    void onExecute(CommandSender sender, String[] args) {
        for(Map.Entry<String, SlimefunItem> entry: Slimefun.getRegistry().getSlimefunItemIds().entrySet()) {
            String id = entry.getKey();
            SlimefunItem item = entry.getValue();

            int output = item.getRecipeOutput().getAmount();
            if(output != 1) {
                plugin.getRecipes().setValue(id + ".amount", item.getRecipeOutput().getAmount());
            }

            List<String> idrecipe = new ArrayList<>();
            for(ItemStack ingredient : item.getRecipe()) {
                String ingredientid = ItemUtils.getId(ingredient);
                idrecipe.add(ingredientid);
                if(!ItemUtils.itemExists(ingredientid)) {
                    ItemStack clone = new ItemStack(ingredient);
                    plugin.getItemstacks().setValue(ingredientid, clone);
                }
            }
            plugin.getRecipes().setValue(id+".recipe", idrecipe.toArray(new String[0]));
        }
        plugin.getRecipes().save();
        plugin.getItemstacks().save();
    }

    @Override
    String getCommandName() {
        return "genrecipes";
    }
}
