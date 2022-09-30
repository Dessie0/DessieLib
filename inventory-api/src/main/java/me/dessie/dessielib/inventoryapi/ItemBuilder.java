package me.dessie.dessielib.inventoryapi;

import me.dessie.dessielib.core.utils.Colors;
import me.dessie.dessielib.enchantmentapi.CEnchantment;
import me.dessie.dessielib.enchantmentapi.CEnchantmentAPI;
import me.dessie.dessielib.enchantmentapi.properties.CEnchantProperties;
import me.dessie.dessielib.inventoryapi.inventory.CustomInventory;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * Builder for creating Items to put into InventoryBuilders.
 */
public class ItemBuilder {

    private final Map<CustomInventory<?>, List<Integer>> slots = new HashMap<>();

    private ItemStack item;
    private boolean cancel;
    private boolean glowing;

    private List<ItemStack> cycle = new ArrayList<>();
    private Consumer<ClickResult> clickConsumer;

    //Index of the cycle, where 0 is the item the builder was created with.
    private int cycleIndex = 0;

    /**
     * @param item The ItemStack to represent
     */
    public ItemBuilder(ItemStack item) {
        if(!InventoryAPI.isRegistered()) {
            throw new IllegalStateException("You need to register InventoryAPI before creating InventoryBuilders!");
        }

        this.item = item;
        this.glowing = false;
    }

    /**
     * Creates a full copy of another ItemBuilder
     *
     * @param itemBuilder The ItemBuilder to copy
     */
    //Makes a copy
    public ItemBuilder(ItemBuilder itemBuilder) {
        this.item = itemBuilder.getItem().clone();
        this.cancel = itemBuilder.isCancel();
        this.glowing = itemBuilder.isGlowing();
        this.cycle = itemBuilder.getCycle();
    }

    /**
     * @return The {@link ItemStack} this ItemBuilder represents
     */
    public ItemStack getItem() {
        return this.item;
    }

    /**
     * @return The Set of {@link CustomInventory}s this ItemBuilder is currently in.
     */
    public Set<CustomInventory<?>> getBuilders() {
        return this.slots.keySet();
    }

    /**
     * @return If the ItemBuilder can be picked up or not
     */
    public boolean isCancel() {
        return this.cancel;
    }

    /**
     * @return If the item is glowing
     */
    public boolean isGlowing() {
        return this.glowing;
    }

    /**
     * @return The {@link ItemStack}s that this ItemBuilder cycles through on clicks
     */
    public List<ItemStack> getCycle() {
        return cycle;
    }

    /**
     * @return The current display name of the {@link ItemStack}
     */
    public String getName() {
        return (this.getItem().getItemMeta() != null && this.getItem().getItemMeta().hasDisplayName()) ? this.getItem().getItemMeta().getDisplayName() : this.getItem().getType().toString();
    }

    /**
     * @return The current Lore of the {@link ItemStack}
     */
    public List<String> getLore() {
        return (this.getItem().getItemMeta() != null && this.getItem().getItemMeta().hasLore()) ? this.getItem().getItemMeta().getLore() : new ArrayList<>();
    }

    /**
     * @return The current stacksize of the {@link ItemStack}
     */
    public int getAmount() {
        return this.item.getAmount();
    }

    /**
     * @return A map of {@link CustomInventory} this ItemBuilder is currently in, and which slots it is in.
     */
    public Map<CustomInventory<?>, List<Integer>> getSlots() {
        return slots;
    }

    /**
     * Sets the current Material of the represented ItemStack
     *
     * @param type The new Material
     * @return The ItemBuilder
     */
    public ItemBuilder setMaterial(Material type) {
        this.getItem().setType(type);
        return this;
    }

    /**
     * @param compare The ItemBuilder to compare against
     * @return If the two ItemBuilders are the same, excluding the stack size.
     */
    public boolean isSimilar(ItemBuilder compare) {
        if(compare == null) return false;

        if (this.getItem().isSimilar(compare.getItem()) && this.isGlowing() == compare.isGlowing()) {
            if (this.isCancel() == compare.isCancel()) {

                //TODO Improve cycle comparison to use isSimilar
                if(this.getCycle() == compare.getCycle()) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * When clicking this item builder, this method will set their cursor item.
     *
     * @param item The Item to set to the cursor on click
     * @return The ItemBuilder
     */
    public ItemBuilder setCursorOnClick(Player player, ItemStack item) {
        player.getOpenInventory().setCursor(item);
        return this;
    }

    /**
     * Sets the {@link ItemStack} that represents this ItemBuilder
     *
     * @param item The ItemBuilder
     * @return The ItemBuilder
     */
    public ItemBuilder setItem(ItemStack item) {
        this.item = item.clone();

        for(Map.Entry<CustomInventory<?>, List<Integer>> slots : this.getSlots().entrySet()) {
            for(Integer slot : slots.getValue()) {
                slots.getKey().updateBuilder(slot, item);
            }
        }

        return this;
    }

    /**
     * Sets the stack size of the {@link ItemStack}
     *
     * @param amount The new stack size
     * @return The ItemBuilder
     */
    public ItemBuilder setAmount(int amount) {
        this.getItem().setAmount(amount);
        return this;
    }

    /**
     * @param name The new {@link ItemStack} name.
     * @return The ItemBuilder
     */
    public ItemBuilder setName(String name) {
        ItemMeta meta = this.getItem().getItemMeta();
        if(meta == null) {
            return this;
        }

        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));
        this.getItem().setItemMeta(meta);
        return this;
    }

    /**
     * @param lore The new {@link ItemStack} lore.
     * @return The ItemBuilder
     */
    public ItemBuilder setLore(String... lore) {
        ItemMeta meta = this.getItem().getItemMeta();
        if(meta == null) {
            return this;
        }

        meta.setLore(Arrays.stream(lore).map(Colors::color).toList());
        this.getItem().setItemMeta(meta);
        return this;
    }

    /**
     * @param enchants The Enchantments to add
     * @return The ItemBuilder
     */
    public ItemBuilder setEnchants(Map<Enchantment, Integer> enchants) {
        this.getItem().addUnsafeEnchantments(enchants);
        return this;
    }

    /**
     *
     * @param flags The ItemFlags to set
     * @return The ItemBuilder
     */
    public ItemBuilder setFlags(ItemFlag... flags) {
        ItemMeta meta = this.getItem().getItemMeta();
        if(meta == null) {
            return this;
        }

        meta.addItemFlags(flags);
        this.getItem().setItemMeta(meta);
        return this;
    }

    /**
     * This method can be used to directly modify an ItemBuilder's ItemStack
     * When methods are not provided. This method will supply you the ItemStack,
     * so you can in-line modify it's properties.
     *
     * @param meta The ItemBuilder's ItemMeta
     * @return The ItemBuilder
     */
    public ItemBuilder modifyItem(BiConsumer<ItemStack, ItemMeta> meta) {
        meta.accept(this.getItem(), this.getItem().getItemMeta());
        return this;
    }

    /**
     * Called when the ItemBuilder is clicked.
     * @param consumer A BiFunction containing the Player that clicked the item
     *                 and the ItemBuilder itself.
     * @return The ItemBuilder
     */
    public ItemBuilder onClick(Consumer<ClickResult> consumer) {
        this.clickConsumer = consumer;
        return this;
    }

    /**
     * Whether the ItemBuilder can be picked up out of the Inventory
     *
     * @return The ItemBuilder
     */
    public ItemBuilder cancel() {
        this.cancel = !this.cancel;
        return this;
    }

    /**
     * When this Item is clicked, the ItemStack will be swapped with
     * the next ItemStack in the cycle list.
     *
     * Once the end of the list is reached, the original item is
     * set, and the cycle continues.
     *
     * @param items The items to cycle through
     * @return The ItemBuilder
     */
    public ItemBuilder cyclesWith(ItemStack... items) {
        List<ItemStack> cycle = new ArrayList<>();
        cycle.add(this.getItem());
        cycle.addAll(Arrays.asList(items));
        this.cycle = cycle;
        return this;
    }

    /**
     * Toggles the Enchantment Glint on the ItemStack.
     * This method requires CEnchantmentAPI to be registered.
     *
     * @return The ItemBuilder
     */
    public ItemBuilder toggleGlow() {
        if(!CEnchantmentAPI.isRegistered()) {
            throw new IllegalStateException("CEnchantmentAPI is not registered. Cannot toggle glowing.");
        }

        if(!this.isGlowing()) {
            if(CEnchantment.getByName("glowing") == null) {
                new CEnchantment("glowing").setEnchantProperties(new CEnchantProperties());
            }
            CEnchantment.getByName("glowing").unsafeEnchant(this.getItem(), 1);
        } else {
            CEnchantment.getByName("glowing").removeEnchantment(this.getItem());
        }

        this.glowing = !this.isGlowing();
        return this;
    }

    void executeClick(ClickResult result) {
        if(clickConsumer == null) return;
        clickConsumer.accept(result);
    }

    /**
     * Builds a skull ItemStack.
     *
     * @param player The player to use
     * @param amount The stack size
     * @param name The name of the item
     * @param lore The lore of the item
     * @return The built ItemStack
     */
    public static ItemStack buildSkull(OfflinePlayer player, int amount, String name, String... lore) {
        ItemStack item = buildItem(Material.PLAYER_HEAD, amount, name, lore);
        SkullMeta meta = (SkullMeta) item.getItemMeta();
        if(meta == null) {
            return item;
        }

        meta.setOwningPlayer(player);
        item.setItemMeta(meta);

        return item;
    }

    /**
     * Builds an ItemStack.
     *
     * @param material The material of the ItemStack
     * @param amount The stack size
     * @param name The name of the item
     * @param lore The lore of the item
     * @return The built ItemStack
     */
    public static ItemStack buildItem(Material material, int amount, @Nullable String name, @Nullable List<String> lore) {
        ItemStack item = new ItemStack(material, amount);
        ItemMeta meta = item.getItemMeta();

        if(meta == null) {
            return item;
        }

        if(name != null) {
            meta.setDisplayName(Colors.color(name));
        }

        if(lore != null) {
            List<String> newLore = new ArrayList<>();
            for(String s : lore) {
                newLore.add(Colors.color(s));
            }
            meta.setLore(newLore);
        }

        item.setItemMeta(meta);
        return item;
    }

    /**
     * Builds an ItemStack.
     *
     * @param material The material of the ItemStack
     * @param amount The stack size
     * @param name The name of the item
     * @param lore The lore of the item
     * @return The built ItemStack
     */
    public static ItemStack buildItem(Material material, int amount, @Nullable String name, String... lore) {
        return buildItem(material, amount, name, Arrays.asList(lore));
    }

    //Internal method.
    //Used for swapping ItemStacks in the Cycle List
    void swap() {
        if(this.getCycle().isEmpty()) return;

        this.cycleIndex = this.cycleIndex + 1 >= this.getCycle().size() ? 0 : cycleIndex + 1;
        this.setItem(this.getCycle().get(this.cycleIndex));
    }
}
