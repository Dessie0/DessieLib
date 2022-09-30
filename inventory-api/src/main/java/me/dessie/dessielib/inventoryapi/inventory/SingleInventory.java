package me.dessie.dessielib.inventoryapi.inventory;

import me.dessie.dessielib.core.utils.Colors;
import me.dessie.dessielib.inventoryapi.ItemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

public class SingleInventory extends PaginatedInventory<SingleInventory> {

    private Inventory inventory;

    public SingleInventory(int size, String name) {
        super(size, name);

        this.inventory = Bukkit.createInventory(null, size, Colors.color(name));
    }

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
