package me.dessie.dessielib.storageapi.format.persistent;

import me.dessie.dessielib.storageapi.SpigotStorageAPI;
import me.dessie.dessielib.storageapi.settings.StorageSettings;
import org.bukkit.block.Block;
import org.bukkit.block.TileState;
import org.bukkit.persistence.PersistentDataHolder;

import java.util.Objects;

/**
 * A {@link PDContainer} that stores using a {@link TileState}
 *
 * Note: This container may take a block, but only if it is instance of a TileState.
 * Most blocks do not implement TileState, and therefore cannot have data written to them.
 */
public class TileStateContainer extends PDContainer {

    private final TileState state;

    /**
     * Creates a container that stores data using a {@link TileState}.
     * This will use the default settings in {@link StorageSettings}.
     *
     * @param api The IStorageAPI instance.
     * @param state The TileState to store/retrieve data from.
     */
    public TileStateContainer(SpigotStorageAPI api, TileState state) {
        this(api, state, new StorageSettings());
    }

    /**
     * Creates a container that stores data using a {@link TileState}.
     * This will use the default settings in {@link StorageSettings}.
     *
     * Note: The block passed should implement TileState.
     *
     * @param api The IStorageAPI instance.
     * @param block The TileState block to store/retrieve data from.
     */
    public TileStateContainer(SpigotStorageAPI api, Block block) {
        this(api, block, new StorageSettings());
    }

    /**
     * Creates a container that stores data using a {@link TileState}.
     * This will use the provided settings from {@link StorageSettings}.
     *
     * @param api The IStorageAPI instance.
     * @param state The TileState to store/retrieve data from.
     * @param settings The StorageSettings for this Container.
     */
    public TileStateContainer(SpigotStorageAPI api, TileState state, StorageSettings settings) {
        super(api, settings);
        Objects.requireNonNull(state, "TileState cannot be null!");

        this.state = state;
    }

    /**
     * Creates a container that stores data using a {@link TileState}.
     * This will use the provided settings from {@link StorageSettings}.
     *
     * Note: The block passed should implement TileState.
     *
     * @param api The IStorageAPI instance.
     * @param block The TileState block to store/retrieve data from.
     * @param settings The StorageSettings for this Container.
     */
    public TileStateContainer(SpigotStorageAPI api, Block block, StorageSettings settings) {
        super(api, settings);
        Objects.requireNonNull(block, "Block cannot be null!");

        if(block.getState() instanceof TileState state) {
            this.state = state;
        } else throw new IllegalArgumentException("BlockState is not a TileState!");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PersistentDataHolder getHolder() {
        return this.state;
    }
}
