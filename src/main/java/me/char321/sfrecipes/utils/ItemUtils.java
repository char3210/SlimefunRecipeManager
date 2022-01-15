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
        int amount = 1;
        if(id.indexOf('*') != -1) {
            amount = Integer.parseInt(id.substring(0, id.indexOf('*')));
            id = id.substring(id.indexOf('*')+1);
        }

        if(id.equals("AIR") || id.equals("null")) {
            return null;
        }

        SlimefunItem sfitem = SlimefunItem.getById(id);
        if(sfitem != null) {
            ItemStack item = sfitem.getItem().clone();
            item.setAmount(amount);
            return item;
        }

        Config itemstacks = SFRM.instance().getItemstacks();
        if(itemstacks != null && itemstacks.contains(id) && itemstacks.getValue(id) instanceof ItemStack) {
            ItemStack item = itemstacks.getItem(id).clone();
            item.setAmount(amount);
            return item;
        }

        Material material = Material.getMaterial(id);
        if(material != null) {
            return new ItemStack(material, amount);
        }

        throw new IllegalArgumentException();
    }

    public static String getId(ItemStack item) {
        if(item == null) {
            return "AIR";
        }

        String res;
        if(item instanceof SlimefunItemStack) {
            SlimefunItemStack sfitem = (SlimefunItemStack)item;
            res = sfitem.getItemId();
        } else {
            Optional<String> id = Slimefun.getItemDataService().getItemData(item);
            if(id.isPresent()) {
                res = id.get();
            } else if(item.hasItemMeta()) {
                return null;
            } else {
                res = item.getType().name();
            }
        }

        if(item.getAmount() != 1) {
            res = ""+item.getAmount()+"*"+res;
        }
        return res;
    }
}
