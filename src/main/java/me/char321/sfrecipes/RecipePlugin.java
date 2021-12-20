package me.char321.sfrecipes;

import io.github.thebusybiscuit.slimefun4.api.SlimefunAddon;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.core.SlimefunRegistry;
import io.github.thebusybiscuit.slimefun4.implementation.Slimefun;
import io.github.thebusybiscuit.slimefun4.implementation.SlimefunItems;
import io.github.thebusybiscuit.slimefun4.libraries.dough.config.Config;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Slime;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;

public class RecipePlugin extends JavaPlugin implements SlimefunAddon {

    private Config recipes;
    private Config itemstacks;

    @Override
    public void onEnable() {
        Bukkit.getScheduler().runTaskLater(this, () -> {
            info("Loading recipes from recipes.yml...");
            this.recipes = new Config(this, "recipes.yml");

            info("Loading ItemStacks from itemstacks.yml...");
            this.itemstacks = new Config(this, "itemstacks.yml");

            for(String id : recipes.getKeys()) {
                SlimefunItem target = SlimefunItem.getById(id);
                if(target == null) {
                    warn(id + " in recipes.yml is not a valid Slimefun item!");
                    continue;
                }

                List<String> recipe = recipes.getStringList(id + ".recipe");
                if(recipe.size() == 9) {
                    target.setRecipe(deserialize(recipe));
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
        }, 0L);

        this.getCommand("genrecipes").setExecutor((a, b, c, d) -> {
            for(Map.Entry<String, SlimefunItem> entry: Slimefun.getRegistry().getSlimefunItemIds().entrySet()) {
                String id = entry.getKey();
                SlimefunItem item = entry.getValue();

                int output = item.getRecipeOutput().getAmount();
                if(output != 1) {
                    recipes.setValue(id + ".amount", item.getRecipeOutput().getAmount());
                }

                List<String> idrecipe = new ArrayList<>();
                for(ItemStack ingredient : item.getRecipe()) {
                    String ingredientid = getId(ingredient);
                    idrecipe.add(ingredientid);
                    if(!itemExists(ingredientid)) {
                        ItemStack clone = new ItemStack(ingredient);
                        itemstacks.setValue(ingredientid, clone);
                    }
                }
                recipes.setValue(id+".recipe", idrecipe.toArray(new String[0]));
            }
            recipes.save();
            itemstacks.save();
            return true;
        });

    }

    public ItemStack[] deserialize(List<String> list) {
        if (list.size() != 9) {
            throw new IllegalArgumentException();
        }

        List<ItemStack> recipe = new ArrayList<>();
        for(String ingredient : list) {
            try {
                recipe.add(getItem(ingredient));
            } catch (IllegalArgumentException e) {
                warn("Ingredient " + ingredient + " is not a valid item identifier! " +
                        "You can define the itemstack in itemstacks.yml. " +
                        "Defaulting to AIR...");
                recipe.add(null);
            }
        }

        return recipe.toArray(new ItemStack[0]);
    }

    public boolean itemExists(String id) {
        try{
            getItem(id);
            return true;
        } catch(IllegalArgumentException x) {
            return false;
        }
    }

    public ItemStack getItem(String id) {
        if(id == null || id.equals("AIR") || id.equals("null")) {
            return null;
        }

        SlimefunItem item = SlimefunItem.getById(id);
        if(item != null) {
            return item.getItem();
        }

        if(itemstacks != null && itemstacks.contains(id) && itemstacks.getValue(id) instanceof ItemStack) {
            return (ItemStack) itemstacks.getValue(id);
        }

        Material material = Material.getMaterial(id);
        if(material != null) {
            return new ItemStack(material);
        }

        throw new IllegalArgumentException();
    }

    //the inverse function of getItem
    public String getId(ItemStack item) {
        if(item == null) {
            return "AIR";
        } else if(item instanceof SlimefunItemStack) {
            SlimefunItemStack sfitem = (SlimefunItemStack)item;
            return sfitem.getItemId();
        } else {
            Optional<String> id = Slimefun.getItemDataService().getItemData(item);
            if(id.isPresent()) {
                return id.get();
            }
            return item.getType().name();
        }
    }

    @Override
    public void onDisable() {

    }

    @Override
    public String getBugTrackerURL() {
        return null;
    }

    @Override
    public JavaPlugin getJavaPlugin() {
        return this;
    }

    public void info(String message) {
        getLogger().log(Level.INFO, message);
    }

    public void warn(String message) {
        getLogger().log(Level.WARNING, message);
    }

}
