package me.dessie.dessielib.storageapi.format.persistent;

import me.dessie.dessielib.storageapi.SpigotStorageAPI;
import me.dessie.dessielib.storageapi.settings.StorageSettings;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataHolder;

import java.util.Objects;

/**
 * A {@link PDContainer} that stores using a {@link ItemStack}'s {@link ItemMeta}
 */
public class ItemStackContainer extends PDContainer {

    private final ItemMeta meta;

    /**
     * Creates a container that stores data using an {@link ItemStack}'s {@link ItemMeta}.
     * This will use the default settings in {@link StorageSettings}.
     *
     * @param api The IStorageAPI instance.
     * @param item The ItemStack to store/retrieve data from.
     */
    public ItemStackContainer(SpigotStorageAPI api, ItemStack item) {
        this(api, item, new StorageSettings());
    }

    /**
     * Creates a container that stores data using an {@link ItemStack}'s {@link ItemMeta}.
     * This will use the provided settings from {@link StorageSettings}.
     *
     * @param api The IStorageAPI instance.
     * @param item The ItemStack to store/retrieve data from.
     * @param settings The StorageSettings for this Container.
     */
    public ItemStackContainer(SpigotStorageAPI api, ItemStack item, StorageSettings settings) {
        super(api, settings);
        Objects.requireNonNull(item, "Item cannot be null!");
        if(item.getItemMeta() == null) throw new IllegalArgumentException("ItemMeta cannot be null!");

        this.meta = item.getItemMeta();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PersistentDataHolder getHolder() {
        return this.meta;
    }
}
