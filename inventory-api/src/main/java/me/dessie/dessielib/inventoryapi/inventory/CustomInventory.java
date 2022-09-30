package me.dessie.dessielib.inventoryapi.inventory;

import me.dessie.dessielib.inventoryapi.InventoryAPI;
import me.dessie.dessielib.inventoryapi.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

/**
 * Builder for creating Inventories with InventoryAPI.
 */
public abstract class CustomInventory<T extends CustomInventory<T>> {
    private static final List<CustomInventory<?>> inventories = new ArrayList<>();

    private final List<UUID> holders = new ArrayList<>();
    private final HashMap<Integer, ItemBuilder> items = new HashMap<>();

    private int size;
    private String name;
    private boolean preventClose = false;
    private boolean allowPlayerInventory = false;

    protected BiConsumer<Player, CustomInventory<T>> close;
    protected BiConsumer<Player, CustomInventory<T>> open;

    /**
     * Creates an empty Inventory with a size and title.
     * @param size The size of the Inventory, should be divisible by 9 and no larger than 54.
     * @param name The name of the Inventory.
     */
    public CustomInventory(int size, String name) {
        if(!InventoryAPI.isRegistered()) {
            throw new IllegalStateException("You need to register InventoryAPI before creating InventoryBuilders!");
        }

        this.size = size;
        this.name = name;
    }

    public abstract void updateInventory(int slot, ItemStack newItem);
    public abstract void updateBuilder(int slot, ItemStack newItem);

    /**
     * Checks if the provided inventory is handled by this instance.
     * For instance, if the provided inventory was created by this custom inventory
     * and should therefore change to the Inventory should also change this builder.
     *
     * @param inventory The inventory to check handling for.
     * @return If the provided inventory is updated by this instance.
     */
    public abstract boolean handlesInventory(Inventory inventory);

    /**
     * @return The title of this Inventory
     */
    public String getName() {
        return this.name;
    }

    /**
     * @return The size of this Inventory
     */
    public int getSize() {
        return this.size;
    }

    /**
     * @param slot The Inventory slot
     * @return The {@link ItemBuilder} in the specified slot
     */
    public ItemBuilder getItem(int slot) {
        return this.getItems().get(slot);
    }

    /**
     * @return All Players that currently have this Inventory opened.
     */
    public List<UUID> getHolders() {
        return this.holders;
    }

    /**
     * @return A Map of all {@link ItemBuilder}s and their current slot.
     */
    public HashMap<Integer, ItemBuilder> getItems() {
        return this.items;
    }

    /**
     * @return If the player can close the Inventory
     */
    public boolean isPreventClose() {
        return this.preventClose;
    }

    /**
     * @return If the player can click items in their own inventory (i.e, the bottom inventory)
     */
    public boolean isAllowPlayerInventory() {
        return this.allowPlayerInventory;
    }

    /**
     * Opens an InventoryBuilder for the Player.
     *
     * @param player The player to open this inventory for
     * @return The InventoryBuilder
     */
    @SuppressWarnings("unchecked")
    public T open(Player player) {
        this.executeOpen(player);
        return (T) this;
    }

    /**
     * Forcefully closes the Inventory.
     *
     * @param player The player to close
     * @return The InventoryBuilder
     */
    @SuppressWarnings("unchecked")
    public T close(Player player) {
        this.preventClose = false;
        this.executeClose(player);

        player.closeInventory();
        return (T) this;
    }

    /**
     * Note: The Inventory title will not update unless it is reopened.
     *
     * @param name The new name of this InventoryBuilder
     * @return The InventoryBuilder
     */
    @SuppressWarnings("unchecked")
    public T setName(String name) {
        this.name = name;
        return (T) this;
    }

    /**
     * Note: The Inventory size will not update unless it is reopened.
     *
     * @param size The new size of this InventoryBuilder
     * @return The InventoryBuilder
     */
    @SuppressWarnings("unchecked")
    public T setSize(int size) {
        if(size <= 0 || size % 9 != 0 || size > 54) {
            throw new IllegalArgumentException("Invalid Inventory size!");
        }

        this.size = size;
        return (T) this;
    }

    /**
     * When true, the InventoryBuilder cannot be closed by the player.
     * {@see #close} for manually closing the Inventory.
     *
     * @param preventClose Whether the player can or cannot close the Inventory
     * @return The InventoryBuilder
     */
    @SuppressWarnings("unchecked")
    public T setPreventsClose(boolean preventClose) {
        this.preventClose = preventClose;
        return (T) this;
    }

    /**
     * @param allowPlayerInventory Whether the player can interact with their bottom inventory
     * @return The InventoryBuilder
     */
    @SuppressWarnings("unchecked")
    public T setAllowPlayerInventory(boolean allowPlayerInventory) {
        this.allowPlayerInventory = allowPlayerInventory;
        return (T) this;
    }

    /**
     * Adds the item into the first empty slot into the Inventory.
     * Returns null if the inventory is full.
     *
     * @param item The {@link ItemBuilder} to place into the inventory
     * @return The ItemBuilder that was placed, or null if it could not be placed.
     */
    public ItemBuilder addItem(ItemBuilder item) {
        for(int i = 0; i < this.getSize(); i++) {
            if(this.getItems().get(i) == null) {
                return this.setItem(i, item);
            }
        }

        return null;
    }

    /**
     * Adds the item into the first empty slot into the Inventory.
     * Returns null if the inventory is full.
     *
     * @param item The {@link ItemStack} to place into the inventory
     * @return The generated ItemBuilder that was placed, or null if it could not be placed.
     */
    public ItemBuilder addItem(ItemStack item) {
        return this.addItem(new ItemBuilder(item));
    }

    /**
     * Sets an {@link ItemStack} in a specific slot, regardless of if the slot is empty.
     * Passing null as the item will clear the slot.
     *
     * @param item The {@link ItemStack} to add.
     * @param slot The slot to set the item in
     * @return The placed ItemBuilder.
     */
    public ItemBuilder setItem(int slot, ItemStack item) {
        return this.setItem(slot, new ItemBuilder(item));
    }

    /**
     * Sets an {@link ItemBuilder} in a specific slot, regardless of if the slot is empty.
     * Passing null as the item will clear the slot.
     *
     * @param item The {@link ItemBuilder} to add.
     * @param slot The slot to set the item in
     * @return The placed ItemBuilder.
     */
    public ItemBuilder setItem(int slot, ItemBuilder item) {
        return this.setItem(slot, item, true);
    }

    protected ItemBuilder setItem(int slot, ItemBuilder item, boolean updateInventory) {
        if(item == null || item.getItem().getType() == Material.AIR) {

            //Update the ItemBuilder to reflect the fact it's no longer in this builder.
            if(this.getItems().get(slot) != null) {
                this.getItems().get(slot).getSlots().get(this).remove(Integer.valueOf(slot));
                this.getItems().get(slot).getSlots().keySet().removeIf(inv -> {
                    return inv.getItems().get(slot).getSlots().get(inv).isEmpty();
                });
            }

            //Remove the item from the builder.
            this.getItems().remove(slot);

            if(updateInventory) {
                this.updateInventory(slot, item == null ? null : item.getItem());
            }
            return null;
        } else {
            this.getItems().put(slot, item);

            //Update the item to add it to this Inventory.
            item.getSlots().putIfAbsent(this, new ArrayList<>());
            item.getSlots().get(this).add(slot);

            if(updateInventory) {
                this.updateInventory(slot, item.getItem());
            }
            return item;
        }
    }

    /**
     * Sets an {@link ItemBuilder} in multiple slots, regardless of if the slots are empty.
     *
     * @param item The {@link ItemBuilder} to add.
     * @param slots The slots to place the ItemBuilder into.
     * @return The InventoryBuilder.
     */
    @SuppressWarnings("unchecked")
    public T setItems(ItemBuilder item, Integer... slots) {
        Arrays.stream(slots).forEach(i -> this.setItem(i, item));
        return (T) this;
    }

    /**
     * Sets an {@link ItemStack} in multiple slots, regardless of if the slots are empty.
     *
     * @param item The {@link ItemStack} to add.
     * @param slots The slots to place the ItemBuilder into.
     * @return The InventoryBuilder.
     */
    @SuppressWarnings("unchecked")
    public T setItems(ItemStack item, Integer... slots) {
        Arrays.stream(slots).forEach(i -> this.setItem(i, item));
        return (T) this;
    }

    /**
     * Copies the contents from a {@link Inventory} to this InventoryBuilder.
     * @param inventory The Inventory to copy from
     * @return The InventoryBuilder
     */
    public T setContents(Inventory inventory) {
        return (T) this.setContents(inventory.getContents());
    }

    /**
     * Sets the contents of the InventoryBuilder in order from the provided array.
     * @param items The items to set.
     * @return The InventoryBuilder
     */
    @SuppressWarnings("unchecked")
    public T setContents(ItemStack[] items) {
        for(int i = 0; i < items.length; i++) {
            if(items[i] == null || items[i].getType() == Material.AIR) continue;

            this.setItem(i, items[i]);
        }
        return (T) this;
    }

    /**
     * Sets the contents of the InventoryBuilder from the list.
     * @param items The items to set.
     * @return The InventoryBuilder
     */
    @SuppressWarnings("unchecked")
    public T setContents(List<ItemBuilder> items) {
        for (int i = 0; i < items.size() && i < this.getSize(); i++) {
            this.setItem(i, items.get(i));
        }

        return (T) this;
    }

    /**
     * Copies the contents from a {@link CustomInventory} to this InventoryBuilder.
     * @param builder The InventoryBuilder to copy from
     * @return The InventoryBuilder
     */
    @SuppressWarnings("unchecked")
    public T setContents(CustomInventory<T> builder) {
        this.setContents(new ArrayList<>(builder.getItems().values()));
        return (T) this;
    }

    /**
     * Clears all items from this InventoryBuilder
     * @return The InventoryBuilder
     */
    @SuppressWarnings("unchecked")
    public T clear() {
        for(Map.Entry<Integer, ItemBuilder> entry : this.getItems().entrySet()) {
            entry.getValue().getSlots().remove(this);
            this.updateInventory(entry.getKey(), entry.getValue().getItem());
        }

        this.getItems().clear();
        return (T) this;
    }

    /**
     * Fills all empty slots with the specified {@link ItemBuilder}
     *
     * @param item The item to set
     * @return The InventoryBuilder
     */
    @SuppressWarnings("unchecked")
    //Fills all null slots with the specified ItemBuilder.
    public T fill(ItemBuilder item) {
        for(int i = 0; i < this.getSize(); i++) {
            if(this.getItem(i) != null) continue;

            this.setItem(i, new ItemBuilder(item));
        }
        return (T) this;
    }

    /**
     * Shifts all the items to the top most left position possible.
     *
     * @param byItems If the inventory should be sorted by {@link Material}, alphabetically, and by stack size.
     * @return The InventoryBuilder
     */
    @SuppressWarnings("unchecked")
    public T organize(boolean byItems) {
        //Move all the items to the top left.
        for(int i = 0; i < this.getSize(); i++) {
            if(this.getItem(i) == null) continue;

            for(int j = 0; j < this.getSize(); j++) {
                if(this.getItem(j) == null) {
                    this.setItem(j, this.getItem(i));
                    this.setItem(i, (ItemStack) null);
                    break;
                }
            }
        }

        if(byItems) {
            Comparator<ItemBuilder> nameSort = Comparator.comparing(item -> item.getItem().getType().toString());
            Comparator<ItemBuilder> amountSort = Comparator.comparing(ItemBuilder::getAmount).reversed();

            //Sort by name then by amount
            this.setContents(this.getItems().values().stream().map(ItemBuilder::new)
                    .sorted(nameSort.thenComparing(amountSort))
                    .collect(Collectors.toList()));
        }
        return (T) this;
    }

    /**
     * @see #organize(boolean)
     *
     * @return The organized InventoryBuilder
     */
    public T organize() {
        return organize(false);
    }

    /**
     * Combines all possible ItemStacks together into their maximum stack size.
     * @return The InventoryBuilder
     */
    @SuppressWarnings("unchecked")
    public T condense() {
        for(int i = 0; i < this.getSize(); i++) {
            if(!this.getItems().containsKey(i)) continue;
            ItemBuilder item = this.getItem(i);

            //Work through the list backwards so we condense at the most top left item.
            for(int j = this.getSize() - 1; j > 0 && j >= i; j--) {
                if(!this.getItems().containsKey(j)) continue;
                ItemBuilder condensed = this.getItem(j);

                //Don't combine to itself.
                if(item == condensed) continue;

                if (item.isSimilar(condensed) && item.getAmount() <= item.getItem().getMaxStackSize()) {
                    while(condensed.getAmount() > 0 && item.getAmount() < item.getItem().getMaxStackSize()) {
                        item.setAmount(item.getAmount() + 1);

                        if(condensed.getAmount() - 1 != 0) {
                            condensed.setAmount(condensed.getAmount() - 1);
                        } else {
                            condensed.setAmount(0);
                            this.setItem(j, (ItemStack) null);
                        }
                    }
                }
            }
        }

        return (T) this;
    }

    /**
     * Replace all {@link ItemBuilder}s with another.
     *
     * @param oldItem The ItemBuilder to replace
     * @param replacement The replacement ItemBuilder
     * @return The InventoryBuilder
     */
    @SuppressWarnings("unchecked")
    public T replaceAll(ItemBuilder oldItem, ItemBuilder replacement) {
        for(int i = 0; i < this.size; i++) {
            if(this.getItem(i).isSimilar(oldItem)) {
                this.setItem(i, replacement);
            }
        }
        return (T) this;
    }

    /**
     * Note: The current InventoryBuilder must be atleast 27 slots.
     * @param item The {@link ItemBuilder} to create the border from.
     * @return The InventoryBuilder
     */
    @SuppressWarnings("unchecked")
    public T createBorder(ItemBuilder item) {
        if(this.getSize() < 18) {
            throw new IllegalStateException("Inventory size must be atleast 27 to add borders!");
        }

        for(int size = 0; size < this.getSize() / 9; size++) {
            if(size == 0 || size == (this.getSize() / 9) - 1) {
                for(int i = size * 9; i < (size + 1) * 9; i++) {
                    if (this.getItem(i) == null) this.setItem(i, item);
                }
            } else {
                if (this.getItem(size * 9) == null) {
                    this.setItem(size * 9, item);
                }

                if (this.getItem(size * 9 + 8) == null) {
                    this.setItem(size * 9 + 8, item);
                }
            }
        }

        return (T) this;
    }

    /**
     * Finds the first instance of an {@link ItemBuilder}
     * @param item The ItemBuilder to look for.
     * @return The found ItemBuilder, or null if it doesn't exist within the Inventory.
     */
    public ItemBuilder findFirst(ItemBuilder item) {
        return this.getItems().values().stream().filter(item::isSimilar).findFirst().orElse(null);
    }

    /**
     * Finds the first instance of an {@link ItemBuilder} with the specified {@link ItemStack}
     * @param item The ItemBuilder to look for that has the required ItemStack.
     * @return The found ItemBuilder, or null if it doesn't exist within the Inventory.
     */
    public ItemBuilder findFirst(ItemStack item) {
        return this.findFirst(new ItemBuilder(item));
    }

    /**
     * Finds the first instance of an {@link ItemBuilder} with the specified {@link Material}
     * @param material The ItemBuilder to look for that has the required Material.
     * @return The found ItemBuilder, or null if it doesn't exist within the Inventory.
     */
    public ItemBuilder findFirst(Material material) {
        return this.findFirst(new ItemStack(material));
    }

    /**
     * Called when the Inventory is closed.
     * @param consumer A BiFunction containing the Player that closed the Inventory
     *                 and the InventoryBuilder itself.
     * @return The InventoryBuilder
     */
    @SuppressWarnings("unchecked")
    public T onClose(BiConsumer<Player, CustomInventory<T>> consumer) {
        this.close = consumer;
        return (T) this;
    }

    /**
     * Called when the Inventory is opened.
     * @param consumer A BiFunction containing the Player that opened the Inventory
     *                 and the InventoryBuilder itself.
     * @return The InventoryBuilder
     */
    @SuppressWarnings("unchecked")
    public T onOpen(BiConsumer<Player, CustomInventory<T>> consumer) {
        this.open = consumer;
        return (T) this;
    }

    public void executeOpen(Player player) {
        this.addHolder(player);
        if(this.open != null) {
            this.open.accept(player, this);
        }
    }

    public void executeClose(Player player) {
        this.removeHolder(player);
        if(this.close != null) {
            close.accept(player, this);
        }
    }

    protected void addHolder(Player player) {
        if(!this.getHolders().contains(player.getUniqueId())) {
            this.getHolders().add(player.getUniqueId());
        }

        if(!getInventories().contains(this)) {
            getInventories().add(this);
        }
    }

    protected void removeHolder(Player player) {
        this.getHolders().remove(player.getUniqueId());
        if(this.getHolders().isEmpty()) {
            getInventories().remove(this);
        }
    }

    /**
     * @param player The Player to get
     * @return The currently open InventoryBuilder of the player
     */
    public static CustomInventory<?> getBuilder(Player player) {
        return getInventories().stream().filter(inv -> inv.getHolders().contains(player.getUniqueId())).findFirst().orElse(null);
    }

    /**
     * Returns a list of all currently opened CustomInventories
     *
     * @return All opened CustomInventories
     */
    public static List<CustomInventory<?>> getInventories() {
        return inventories;
    }
}
