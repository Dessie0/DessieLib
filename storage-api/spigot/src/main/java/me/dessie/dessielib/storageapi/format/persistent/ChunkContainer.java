package me.dessie.dessielib.storageapi.format.persistent;

import me.dessie.dessielib.storageapi.SpigotStorageAPI;
import me.dessie.dessielib.storageapi.settings.StorageSettings;
import org.bukkit.Chunk;
import org.bukkit.persistence.PersistentDataHolder;

import java.util.Objects;

/**
 * A {@link PDContainer} that stores using a {@link Chunk}
 */
public class ChunkContainer extends PDContainer {

    private final Chunk chunk;

    /**
     * Creates a container that stores data using a {@link Chunk}.
     * This will use the default settings in {@link StorageSettings}.
     *
     * @param api The IStorageAPI instance.
     * @param chunk The Chunk to store/retrieve data from.
     */
    public ChunkContainer(SpigotStorageAPI api, Chunk chunk) {
        this(api, chunk, new StorageSettings());
    }

    /**
     * Creates a container that stores data using a {@link Chunk}.
     * This will use the provided settings from {@link StorageSettings}.
     *
     * @param api The IStorageAPI instance.
     * @param chunk The Chunk to store/retrieve data from.
     * @param settings The StorageSettings for this Container.
     */
    public ChunkContainer(SpigotStorageAPI api, Chunk chunk, StorageSettings settings) {
        super(api, settings);
        Objects.requireNonNull(chunk, "Chunk cannot be null!");

        this.chunk = chunk;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PersistentDataHolder getHolder() {
        return this.chunk;
    }
}
