package me.char321.sfrecipes;

import io.github.thebusybiscuit.slimefun4.api.SlimefunAddon;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.implementation.Slimefun;
import io.github.thebusybiscuit.slimefun4.libraries.dough.config.Config;
import me.char321.sfrecipes.command.SFRMCommand;
import me.char321.sfrecipes.command.SFRMTabCompleter;
import me.char321.sfrecipes.utils.ItemUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;

public class SFRM extends JavaPlugin implements SlimefunAddon {
    private static SFRM instance;
    private Config recipes;
    private Config itemstacks;

    @Override
    public void onEnable() {
        instance = this;

        info("Loading recipes from recipes.yml...");
        this.recipes = new Config(this, "recipes.yml");

        info("Loading ItemStacks from itemstacks.yml...");
        this.itemstacks = new Config(this, "itemstacks.yml");

        //We cannot guarantee that all addons have loaded at this point
        Bukkit.getScheduler().runTaskLater(this, this::applyRecipes, 0L);

        this.getCommand("sfrecipemanager").setExecutor(new SFRMCommand(this));
    }

    public void reloadConfigs() {
        recipes = new Config(this, "recipes.yml");
        itemstacks = new Config(this, "itemstacks.yml");

        applyRecipes();
    }

    public void applyRecipes() {
        info("Applying recipes from config...");
        for(String id : recipes.getKeys()) {
            applyRecipe(id);
        }
    }

    public void applyRecipe(String id) {
        SlimefunItem target = SlimefunItem.getById(id);
        if(target == null) {
            warn(id + " in recipes.yml is not a valid Slimefun item!");
            return;
        }

        List<String> recipe = recipes.getStringList(id + ".recipe");
        if(recipe.size() == 9) {
            target.setRecipe(deserialize(recipe, target.getRecipe()));
        }

        int outputAmount = recipes.getInt(id + ".amount");
        if(outputAmount != 0) {
            ItemStack targetStack = target.getItem().clone();
            targetStack.setAmount(outputAmount);
            target.setRecipeOutput(targetStack);
        }

        String recipeType = recipes.getString(id + ".type");
        if(recipeType != null && !recipeType.isEmpty()) {
            //how

        }
    }

    public ItemStack[] deserialize(List<String> list, ItemStack[] def) {
        if (list.size() != 9) {
            throw new IllegalArgumentException();
        }

        List<ItemStack> recipe = new ArrayList<>();
        for(int i=0;i<9;i++) {
            String ingredient = list.get(i);
            if (ingredient.equals("PLACEHOLDER")) {
                recipe.add(def[i]);
            } else {
                try {
                    recipe.add(ItemUtils.getItem(ingredient));
                } catch (IllegalArgumentException e) {
                    warn("Ingredient " + ingredient + " is not a valid item identifier! " +
                            "You can define the itemstack in itemstacks.yml. " +
                            "Defaulting to AIR...");
                    recipe.add(null);
                }
            }
        }

        return recipe.toArray(new ItemStack[0]);
    }

    @Override
    public void onDisable() {

    }

    @Override
    public String getBugTrackerURL() {
        return "https://github.com/qwertyuioplkjhgfd/SlimefunRecipeManager/issues";
    }

    @Override
    public JavaPlugin getJavaPlugin() {
        return this;
    }

    public static SFRM instance() {
        return instance;
    }

    public Config getRecipes() {
        return recipes;
    }

    public Config getItemstacks() {
        return itemstacks;
    }

    public void info(Object message) {
        getLogger().log(Level.INFO, message.toString());
    }

    public void warn(Object message) {
        getLogger().log(Level.WARNING, message.toString());
    }

}
