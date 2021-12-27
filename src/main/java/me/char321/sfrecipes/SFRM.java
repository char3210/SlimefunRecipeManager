package me.char321.sfrecipes;

import io.github.thebusybiscuit.slimefun4.api.SlimefunAddon;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.core.multiblocks.MultiBlockMachine;
import io.github.thebusybiscuit.slimefun4.implementation.Slimefun;
import io.github.thebusybiscuit.slimefun4.libraries.dough.config.Config;
import io.github.thebusybiscuit.slimefun4.libraries.dough.reflection.ReflectionUtils;
import me.char321.sfrecipes.command.SFRMCommand;
import me.char321.sfrecipes.command.SFRMTabCompleter;
import me.char321.sfrecipes.utils.ItemUtils;
import me.mrCookieSlime.Slimefun.Objects.SlimefunItem.abstractItems.MachineRecipe;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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

        try {
            Field field = RecipeType.class.getDeclaredField("consumer");
            field.setAccessible(true);
            BiConsumer<ItemStack[], ItemStack> consumer = (BiConsumer<ItemStack[], ItemStack>) field.get(target.getRecipeType());
            if(consumer != null) {
                warn("unsupported " + id);
                return;
            }
        } catch (Exception x) {
            x.printStackTrace();
        }

        SlimefunItem machineitem = target.getRecipeType().getMachine();
        if(machineitem instanceof MultiBlockMachine) {
            MultiBlockMachine machine = (MultiBlockMachine) machineitem;
            List<ItemStack[]> recipes = machine.getRecipes();
            int outputi = recipes.indexOf(new ItemStack[]{target.getItem()});
            for(int i=1;i<recipes.size();i+=2) {
                String recipeid = ItemUtils.getId(recipes.get(i)[0]);
                if(recipeid.equals(ItemUtils.getId(target.getRecipeOutput()))) {
                    outputi = i;
                    break;
                }
            }
            int inputi = outputi-1;
            if(outputi == -1) {
                warn("how " + target.getId());
                return;
            }
            recipes.remove(outputi);
            recipes.remove(inputi);

            target.getRecipeType().register(target.getRecipe(), target.getRecipeOutput());
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
