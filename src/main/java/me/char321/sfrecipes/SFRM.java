package me.char321.sfrecipes;

import io.github.thebusybiscuit.slimefun4.api.SlimefunAddon;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.core.multiblocks.MultiBlockMachine;
import io.github.thebusybiscuit.slimefun4.implementation.Slimefun;
import io.github.thebusybiscuit.slimefun4.libraries.dough.config.Config;
import io.github.thebusybiscuit.slimefun4.utils.SlimefunUtils;
import me.char321.sfrecipes.command.SFRMCommand;
import me.char321.sfrecipes.utils.ItemUtils;
import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
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
        Map<String, Map<String, ItemStack[]>> recipeTypes = new HashMap<>();
        List<RecipeType> typeRegistry = new ArrayList<>();

        // populate recipeTypes
        for(SlimefunItem item : Slimefun.getRegistry().getAllSlimefunItems()) {
            RecipeType recipeType = item.getRecipeType();
            if(!typeRegistry.contains(recipeType) && recipeType.getMachine() instanceof MultiBlockMachine) {
                typeRegistry.add(recipeType);
            }
        }

        for(RecipeType recipeType : typeRegistry) {
            String recipeKey = recipeType.getKey().getKey();
            MultiBlockMachine machine = (MultiBlockMachine) recipeType.getMachine();
            List<ItemStack[]> machineRecipes = machine.getRecipes();

            Map<String, ItemStack[]> recipeTypeRecipes = new HashMap<>();
            for (int i = 0; i < machineRecipes.size() - 1; i+=2) {
                ItemStack[] recipe = machineRecipes.get(i);
                String output = ItemUtils.getId(machineRecipes.get(i+1)[0]);
                recipeTypeRecipes.put(output, recipe);
            }
            recipeTypes.put(recipeKey, recipeTypeRecipes);
        }

        //apply
        for(String id : recipes.getKeys()) {
            applyRecipe(id, recipeTypes);
        }

        //very apply
        for(RecipeType recipeType : typeRegistry) {
            Map<String, ItemStack[]> recipemap = recipeTypes.get(recipeType.getKey().getKey());
            List<ItemStack[]> recipesout = new ArrayList<>();
            for (String key : recipemap.keySet()) {
                recipesout.add(recipemap.get(key));
                recipesout.add(new ItemStack[]{ItemUtils.getItem(key)});
            }
            
            MultiBlockMachine machine = (MultiBlockMachine) recipeType.getMachine();
            machine.getRecipes().clear();
            machine.getRecipes().addAll(recipesout);
        }
    }

    public void applyRecipe(String id, Map<String, Map<String, ItemStack[]>> recipeTypes) {
        SlimefunItem target = SlimefunItem.getById(id);
        if(target == null) {
            warn(id + " in recipes.yml is not a valid Slimefun item!");
            return;
        }

        RecipeType recipeType = target.getRecipeType();
        Map<String, ItemStack[]> machineRecipes = recipeTypes.get(recipeType.getKey().getKey());
        if(machineRecipes == null) {
            warn("unsupported " + id);
            return;
        }
        machineRecipes.remove(id);

        List<String> recipe = this.recipes.getStringList(id + ".recipe");
        if(recipe.size() == 9) {
            target.setRecipe(deserialize(recipe, target.getRecipe()));
        }

        int newOutputAmount = this.recipes.getInt(id + ".amount");
        if(newOutputAmount != 0) {
            ItemStack targetStack = target.getItem().clone();
            targetStack.setAmount(newOutputAmount);
            target.setRecipeOutput(targetStack);
        }

        String newRecipeType = this.recipes.getString(id + ".type");
        if(newRecipeType != null && !newRecipeType.isEmpty()) {

        }

        machineRecipes.put(id, target.getRecipe());
//        target.getRecipeType().register(target.getRecipe(), target.getRecipeOutput());
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
