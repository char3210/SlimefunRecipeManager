package me.char321.sfrecipes.utils;

import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.implementation.Slimefun;
import io.github.thebusybiscuit.slimefun4.libraries.dough.config.Config;
import me.char321.sfrecipes.SFRM;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.Optional;

public class ItemUtils {
    public static boolean itemExists(String id) {
        try{
            getItem(id);
            return true;
        } catch(IllegalArgumentException x) {
            return false;
        }
    }

    public static ItemStack getItem(String id) {
        if(id == null || id.equals("AIR") || id.equals("null")) {
            return null;
        }

        SlimefunItem item = SlimefunItem.getById(id);
        if(item != null) {
            return item.getItem();
        }

        Config itemstacks = SFRM.instance().getItemstacks();
        if(itemstacks != null && itemstacks.contains(id) && itemstacks.getValue(id) instanceof ItemStack) {
            return itemstacks.getItem(id);
        }

        Material material = Material.getMaterial(id);
        if(material != null) {
            return new ItemStack(material);
        }

        throw new IllegalArgumentException();
    }

    public static String getId(ItemStack item) {
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
            if(item.hasItemMeta()) {
                return "PLACEHOLDER";
            }
            return item.getType().name();
        }
    }
}
