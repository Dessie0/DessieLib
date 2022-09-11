package me.dessie.dessielib.storageapi.container.hooks;

import java.util.concurrent.CompletableFuture;

/**
 * Hooks into a {@link me.dessie.dessielib.storageapi.container.StorageContainer}
 * to specify how the StorageContainer should complete after a {@link StorageHook} or {@link DeleteHook} has been completed.
 *
 * If you're creating your own StorageContainer implementation, this method
 * should generally be your methods to write to a file, or some other completion code.
 */
public class CompleteHook extends StorageHook<CompleteHook> {

    @Override
    public synchronized CompletableFuture<Void> complete() {
        return super.complete();
    }
}
