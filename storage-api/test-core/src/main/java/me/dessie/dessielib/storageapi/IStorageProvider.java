package me.dessie.dessielib.storageapi;

import me.dessie.dessielib.storageapi.api.IStorageAPI;
import me.dessie.dessielib.storageapi.container.StorageContainer;

/**
 * Provides a {@link IStorageAPI} instance and a typed {@link StorageContainer} for testing.
 *
 * @param <T> A type of StorageContainer
 */
public interface IStorageProvider<T extends StorageContainer> {

    /**
     * Provides a IStorageAPI instance.
     * @return An IStorageAPI instance.
     */
    IStorageAPI provide();

    /**
     * Provides a specified {@link StorageContainer}
     * @return A StorageContainer
     */
    T provideContainer();

}
