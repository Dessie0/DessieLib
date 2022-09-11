package me.dessie.dessielib.storageapi.format.persistent;

import me.dessie.dessielib.storageapi.SpigotStorageAPI;
import me.dessie.dessielib.storageapi.settings.StorageSettings;
import org.bukkit.entity.Entity;
import org.bukkit.persistence.PersistentDataHolder;

import java.util.Objects;

/**
 * A {@link PDContainer} that stores using a {@link Entity}
 */
public class EntityContainer extends PDContainer {

    private final Entity entity;

    /**
     * Creates a container that stores data using a {@link Entity}.
     * This will use the default settings in {@link StorageSettings}.
     *
     * @param api The IStorageAPI instance.
     * @param entity The Entity to store/retrieve data from.
     */
    public EntityContainer(SpigotStorageAPI api, Entity entity) {
        this(api, entity, new StorageSettings());
    }

    /**
     * Creates a container that stores data using a {@link Entity}.
     * This will use the provided settings from {@link StorageSettings}.
     *
     * @param api The IStorageAPI instance.
     * @param entity The Entity to store/retrieve data from.
     * @param settings The StorageSettings for this Container.
     */
    public EntityContainer(SpigotStorageAPI api, Entity entity, StorageSettings settings) {
        super(api, settings);
        Objects.requireNonNull(entity, "Entity cannot be null!");

        this.entity = entity;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PersistentDataHolder getHolder() {
        return this.entity;
    }
}
