package me.dessie.dessielib.inventoryapi;

import me.dessie.dessielib.inventoryapi.inventory.CustomInventory;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

public record ClickResult(Player player, ItemBuilder item,
                          ClickType clickType, ItemStack heldItem, CustomInventory inventory) {

    public Player getPlayer() {
        return this.player;
    }

    public ItemBuilder getItem() {
        return this.item;
    }

    public ClickType getClickType() {
        return this.clickType;
    }

    public ItemStack getHeldItem() {
        return this.heldItem;
    }

    public CustomInventory getInventory() {
        return this.inventory;
    }

}
