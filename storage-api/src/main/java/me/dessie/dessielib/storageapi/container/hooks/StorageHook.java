package me.dessie.dessielib.storageapi.container.hooks;

import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

/**
 * Provides a generic interface for interacting with a {@link me.dessie.dessielib.storageapi.container.StorageContainer}
 *
 * @see StoreHook
 * @see RetrieveHook
 * @see DeleteHook
 *
 * @param <T> A StorageHook type
 */
public abstract class StorageHook<T extends StorageHook<T>> {
    private Supplier<CompletableFuture<Void>> complete;

    /**
     * Determines a {@link Runnable} that will run when a container call has completed.
     * Usually, this will be saving the file or data structure.
     *
     * @param runnable The runnable
     * @return This StorageHook instance, for chaining purposes.
     */
    @SuppressWarnings("unchecked")
    public T onComplete(Supplier<CompletableFuture<Void>> runnable) {
        this.complete = runnable;
        return (T) this;
    }

    /**
     * Returns the complete runnable
     *
     * @see StorageHook#onComplete(Supplier)
     * @return The complete runnable
     */
    public Supplier<CompletableFuture<Void>> getComplete() {
        return complete;
    }

    /**
     * Runs the complete runnable if it exists
     * @return A CompletableFuture that is completed once the completion code is finished.
     *         Usually this will be a <code>CompletableFuture.completedFuture(null)</code>
     */
    public CompletableFuture<Void> complete() {
        if(this.getComplete() != null) {
            return this.getComplete().get();
        }

        return CompletableFuture.completedFuture(null);
    }
}
