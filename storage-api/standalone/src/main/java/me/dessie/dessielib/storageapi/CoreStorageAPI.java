package me.dessie.dessielib.storageapi;

import me.dessie.dessielib.storageapi.api.ITaskHandler;
import me.dessie.dessielib.storageapi.api.StorageAPI;
import me.dessie.dessielib.storageapi.cache.TaskHandler;

/**
 * Main class for registering StorageAPI.
 */
public class CoreStorageAPI extends StorageAPI {

    private final TaskHandler taskHandler;

    private CoreStorageAPI() {
        this.taskHandler = new TaskHandler();
    }

    /**
     * Registers the StorageAPI for use.
     *
     * @return The StorageAPI instance.
     */
    public static CoreStorageAPI register() {
        return new CoreStorageAPI();
    }

    @Override
    public ITaskHandler getTaskHandler() {
        return this.taskHandler;
    }
}
