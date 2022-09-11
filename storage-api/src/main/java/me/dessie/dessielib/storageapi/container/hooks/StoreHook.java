package me.dessie.dessielib.storageapi.container.hooks;

import java.util.function.BiConsumer;

/**
 * Hooks into a {@link me.dessie.dessielib.storageapi.container.StorageContainer}
 * to specify how the StorageContainer should store data to the data structure.
 *
 * If you're creating your own StorageContainer implementation, the hook
 * will provide you the path and the Object that the user wishes to store.
 *
 * This hooks {@link BiConsumer} will always be executed asynchronously by the StorageContainer.
 */
public class StoreHook extends StorageHook<StoreHook> {

    private final BiConsumer<String, Object> consumer;

    /**
     * @param consumer How the hook behaves when storing to the structure.
     *                 The {@link BiConsumer} will accept the path and the data that the user wishes to store.
     */
    public StoreHook(BiConsumer<String, Object> consumer) {
        this.consumer = consumer;
    }

    /**
     * @return The behavior {@link BiConsumer} for this hook.
     */
    public BiConsumer<String, Object> getConsumer() {
        return consumer;
    }

    /**
     * Applies a path and object to store to the {@link BiConsumer} of this hook.
     *
     * @param path The path to store to.
     * @param object The Object to store.
     */
    public synchronized void accept(String path, Object object) {
        this.getConsumer().accept(path, object);
    }
}
