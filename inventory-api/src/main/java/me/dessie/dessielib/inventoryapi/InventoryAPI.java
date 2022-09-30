package me.dessie.dessielib.inventoryapi;

import me.dessie.dessielib.inventoryapi.inventory.CustomInventory;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Map;

/**
 * Main class for registering InventoryAPI.
 * Also manages the Listener aspect of the API.
 */
public class InventoryAPI implements Listener {

    private static boolean registered = false;
    private static JavaPlugin plugin;

    /**
     * Register the API to use your plugin.
     * @param yourPlugin Your plugin instance.
     */
    public static void register(JavaPlugin yourPlugin) {
        if(isRegistered()) {
            throw new IllegalStateException("Cannot register InventoryAPI in " + yourPlugin.getName() + ". Already registered by " + getPlugin().getName());
        }

        plugin = yourPlugin;
        getPlugin().getServer().getPluginManager().registerEvents(new InventoryAPI(), getPlugin());
        registered = true;
    }

    @EventHandler
    private void onInventoryDrag(InventoryDragEvent event) {
        Player player = (Player) event.getWhoClicked();
        CustomInventory<?> inventory = CustomInventory.getBuilder(player);

        if(inventory != null) {
            for(Map.Entry<Integer, ItemStack> entry : event.getNewItems().entrySet()) {
                if(inventory.handlesInventory(event.getView().getInventory(entry.getKey()))) {
                    inventory.updateBuilder(entry.getKey(), entry.getValue());
                }
            }
        }
    }

    @EventHandler
    private void onInventoryClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        CustomInventory<?> inventory = CustomInventory.getBuilder(player);

        //The inventory they clicked isn't a CustomInventory, so don't attempt to handle it.
        if(inventory == null) return;

        //Return if they didn't click anything.
        if((event.getCurrentItem() == null || event.getCurrentItem().getType() == Material.AIR) && (event.getCursor() == null || event.getCursor().getType() == Material.AIR)) return;

        //Cancel if they have a Builder open, but the Inventory they clicked isn't handled by the builder.
        //I.e, they clicked their inventory.
        if(!inventory.isAllowPlayerInventory() && !inventory.handlesInventory(event.getClickedInventory())) {
            event.setCancelled(true);
            return;
        }

        ItemBuilder clicked = inventory.getItem(event.getSlot());

        //The item could be null, and if it is, just update the inventory to reflect any changes.
        if(clicked == null) {
            //Update the inventory
            if(inventory.handlesInventory(event.getClickedInventory())) {
                inventory.updateBuilder(event.getSlot(), event.getCursor());
            }
            return;
        }

        clicked.executeClick(new ClickResult(player, clicked, event.getClick(), event.getCursor(), inventory));
        clicked.swap();

        if (clicked.isCancel()) {
            event.setCancelled(true);
        } else if (inventory.handlesInventory(event.getClickedInventory())) {
            inventory.updateBuilder(event.getSlot(), event.getCursor());
        }
    }

    @EventHandler
    private void onInventoryClose(InventoryCloseEvent event) {
        Player player = (Player) event.getPlayer();
        CustomInventory<?> inventory = CustomInventory.getBuilder(player);

        if (inventory != null && inventory.handlesInventory(event.getInventory())) {
            if (inventory.isPreventClose()) {
                Bukkit.getScheduler().runTaskLater(getPlugin(), () -> player.openInventory(event.getInventory()), 1);
            } else {
                inventory.executeClose(player);
            }
        }
    }

    /**
     * @return If InventoryAPI has been registered.
     */
    public static boolean isRegistered() {
        return registered;
    }

    /**
     * @return The plugin that registered the InventoryAPI.
     */
    public static JavaPlugin getPlugin() {
        return plugin;
    }
}
