package me.dessie.dessielib.inventoryapi.inventory;

import me.dessie.dessielib.core.utils.Colors;
import me.dessie.dessielib.inventoryapi.ItemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

/**
 * Creates a {@link CustomInventory} that will be updated for all players that have it opened.
 */
public class SingleInventory extends PaginatedInventory<SingleInventory> {

    private Inventory inventory;

    /**
     * Creates a new SingleInventory with the provided size and name.
     *
     * The size must be a multiple of 9, no greater than 54 and no less than 9.
     * The name will automatically have any colors translated, including HEX colors.
     *
     * For example: "&amp;bInventory #ffffffName" would result in "Inventory" being aqua, and "Name" being white.
     *
     * @param size The size of the inventory.
     * @param name The name of the inventory.
     */
    public SingleInventory(int size, String name) {
        super(size, name);

        this.inventory = Bukkit.createInventory(null, size, Colors.color(name));
    }

    /**
     * Returns the {@link Inventory} that this SingleInventory opens for handlers.
     *
     * @return The internal Inventory.
     */
    public Inventory getInventory() {
        return inventory;
    }

    @Override
    public SingleInventory setName(String name) {
        this.inventory = Bukkit.createInventory(null, this.getSize(), Colors.color(name));

        for(Map.Entry<Integer, ItemBuilder> entry : this.getItems().entrySet()) {
            this.updateInventory(entry.getKey(), entry.getValue().getItem());
        }

        return super.setName(name);
    }

    @Override
    public SingleInventory setSize(int size) {
        this.inventory = Bukkit.createInventory(null, size, Colors.color(this.getName()));

        for(Map.Entry<Integer, ItemBuilder> entry : this.getItems().entrySet()) {
            this.updateInventory(entry.getKey(), entry.getValue().getItem());
        }

        return super.setSize(size);
    }

    @Override
    public SingleInventory open(Player player) {
        player.openInventory(this.getInventory());
        return super.open(player);
    }

    @Override
    public void updateInventory(int slot, ItemStack newItem) {
        this.getInventory().setItem(slot, newItem);
    }

    @Override
    public void updateBuilder(int slot, ItemStack newItem) {
        this.setItem(slot, new ItemBuilder(newItem), false);
    }

    @Override
    public boolean handlesInventory(Inventory inventory) {
        return inventory == this.getInventory();
    }
}
