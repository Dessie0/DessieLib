package me.dessie.dessielib.inventoryapi;

import me.dessie.dessielib.inventoryapi.inventory.CustomInventory;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.function.Consumer;

/**
 * Data class that is passed by consumer when a Player clicks an {@link ItemBuilder} within any {@link CustomInventory}
 *
 * This result will be provided through the ItemBuilder's {@link ItemBuilder#onClick(Consumer)} method.
 */
public record ClickResult(Player player, ItemBuilder item,
                          ClickType clickType, ItemStack heldItem, CustomInventory<?> inventory) {

    /**
     * @return the {@link Player} that clicked the item.
     */
    public Player getPlayer() {
        return this.player;
    }

    /**
     * @return The {@link ItemBuilder} that was clicked.
     */
    public ItemBuilder getItem() {
        return this.item;
    }

    /**
     * Returns which {@link ClickType} was given via the {@link org.bukkit.event.inventory.InventoryClickEvent}.
     *
     * @return The event's ClickType.
     */
    public ClickType getClickType() {
        return this.clickType;
    }

    /**
     * @return What {@link ItemStack} the user was holding when they clicked the {@link ItemBuilder}.
     */
    public ItemStack getHeldItem() {
        return this.heldItem;
}

    /**
     * @return The {@link CustomInventory} that was clicked.
     */
    public CustomInventory<?> getInventory() {
        return this.inventory;
    }

}
