package me.dessie.dessielib.storageapi.container;

import me.dessie.dessielib.storageapi.api.IStorageAPI;
import me.dessie.dessielib.storageapi.cache.CachedObject;
import me.dessie.dessielib.storageapi.cache.StorageCache;
import me.dessie.dessielib.storageapi.container.hooks.*;
import me.dessie.dessielib.storageapi.decomposition.DecomposedObject;
import me.dessie.dessielib.storageapi.decomposition.StorageDecomposer;
import me.dessie.dessielib.storageapi.settings.StorageSettings;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Abstraction class for StorageAPI.
 * Provides tons of generic methods/code that should be able to be applied globally to any storage implementation.
 *
 * This class will automatically use the cache, and limit the flush of the extending containers,
 * as well as provide store, delete, and retrieve methods.
 */
public abstract class StorageContainer {

    private final IStorageAPI api;
    private final StorageCache cache;
    private final StorageSettings settings;

    private final StoreHook storeHook;
    private final DeleteHook deleteHook;
    private final RetrieveHook retrieveHook;
    private final CompleteHook completeHook;

    /**
     * Creates a StorageContainer with a default {@link StorageSettings}.
     *
     * @param api The IStorageAPI instance.
     * 
     * @see StorageSettings#StorageSettings()
     */
    protected StorageContainer(IStorageAPI api) {
        this(api, new StorageSettings());
    }

    /**
     * Creates a StorageContainer with the specified {@link StorageSettings}.
     *
     * @param api The IStorageAPI instance.
     * @param settings The StorageSettings for the container.
     */
    protected StorageContainer(IStorageAPI api, StorageSettings settings) {
        Objects.requireNonNull(api, "API cannot be null!");
        Objects.requireNonNull(settings, "Settings cannot be null!");

        this.api = api;
        this.settings = settings;
        this.cache = new StorageCache(this, this.getSettings().getCacheDuration());

        //Setup the hooks from the extending class.
        this.storeHook = this.storeHook();
        this.deleteHook = this.deleteHook();
        this.retrieveHook = this.retrieveHook();
        this.completeHook = this.completeHook();
    }

    /**
     * Required implementation method, specifies how this StorageContainer's
     * store operations are performed.
     *
     * @return The {@link StoreHook} behavior.
     */
    protected abstract StoreHook storeHook();

    /**
     * Required implementation method, specifies how this StorageContainer's
     * delete operations are performed.
     *
     * @return The {@link DeleteHook} behavior.
     */
    protected abstract DeleteHook deleteHook();

    /**
     * Required implementation method, specifies how this StorageContainer's
     * retrieve operations are performed.
     *
     * @return The {@link RetrieveHook} behavior.
     */
    protected abstract RetrieveHook retrieveHook();

    /**
     * Required implementation method, specifies how store and delete operations are completed.
     * i.e, how to save the File after they've been modified.
     *
     * @return The {@link CompleteHook} behavior.
     */
    protected abstract CompleteHook completeHook();

    /**
     * Returns a list of all sub-paths one level below the provided path.
     *
     * This should return an empty list if the path has no sub-paths.
     *
     * @param path The path to get the keys for.
     * @return The List of keys that are under the provided path.
     */
    public abstract Set<String> getKeys(String path);

    /**
     * Returns a cached object.
     * Objects are only cached after they've initially been retrieved.
     * Therefore, this method will always return null if you haven't retrieved a path yet.
     *
     * @see StorageContainer#retrieve(String)
     * @see StorageContainer#getOrElse(String, Object)
     *
     * @param path The path to get the data from.
     * @param <T> The type to cast to
     * @return The cached object, or null if none exists at the path.
     * @throws ClassCastException If the cached object is not of type T
     */
    @SuppressWarnings("unchecked")
    public <T> T get(String path) throws ClassCastException {
        Objects.requireNonNull(path, "Cannot get from null path!");

        CachedObject cachedObject = this.getCache().get(path);
        return cachedObject == null ? null : (T) cachedObject.getObject();
    }

    /**
     * Returns a cached object, or an alternative value if it doesn't exist.
     * Objects are only cached after they've initially been retrieved.
     *
     * @see StorageContainer#get(String)
     * @see StorageContainer#retrieveOrElse(String, Object)
     *
     * @param path The path to get the data from.
     * @param orElse The object to return if the path does not exist or returns null.
     * @param <T> The type to cast to
     * @return The cached object, or null if none exists at the path.
     * @throws ClassCastException If the cached object is not of type T
     */
    public <T> T getOrElse(String path, T orElse) {
        T obtained = this.get(path);
        return obtained == null ? orElse : obtained;
    }

    /**
     * Sets data into a cache that will eventually be updated into the data structure.
     * Data that is set may be lost in the case of a hard crash.
     *
     * To determine how often this data is pushed to the structure, you will need to set your {@link StorageSettings} with a flush rate.
     *
     * @see StorageContainer#store(String, Object) for storing data within the source.
     *
     * @param path The path of the data.
     * @param data The data to set.
     */
    public synchronized void set(String path, Object data) {
        Objects.requireNonNull(path, "Cannot set to null path!");

        if(!isSupported(data.getClass())) {
            throw new IllegalArgumentException(data.getClass() + " is not a supported storage class. Create a StorageDecomposer to implement behavior!");
        }

        this.cacheStore(path, data);

        this.getCache().getSetCache().put(path, data);
        this.getCache().getRemoveCache().remove(path);
    }

    /**
     * Sets data into a cache that will eventually be updated into the data structure.
     * Data that is set may be lost in the case of a hard crash.
     *
     * To determine how often this data is pushed to the structure, you will need to set your {@link StorageSettings} with a flush rate.
     *
     * @see StorageContainer#storeAll(Map) for storing data within the source.
     *
     * @param data A map with all the paths and objects to set.
     */
    public synchronized void setAll(Map<String, Object> data) {
        data.forEach(this::set);
    }

    /**
     * Puts a path into a cache that will eventually be updated into the data structure to remove them.
     * Data that is removed may be lost in the case of a hard crash.
     *
     * To determine how often this data is pushed to the structure, you will need to set your {@link StorageSettings} with a flush rate.
     *
     * @see StorageContainer#delete(String) for immediately deleting data within the source.
     *
     * @param path The path of the data to remove.
     */
    public synchronized void remove(String path) {
        Objects.requireNonNull(path, "Cannot remove from null path!");

        this.getCache().getRemoveCache().add(path);

        //Don't need to set anything, since now it was removed.
        this.getCache().getSetCache().remove(path);
    }

    /**
     * Puts paths into a cache that will eventually be updated into the data structure to remove them.
     * Data that is removed may be lost in the case of a hard crash.
     *
     * To determine how often this data is pushed to the structure, you will need to set your {@link StorageSettings} with a flush rate.
     *
     * @see StorageContainer#deleteAll(List)  for immediately deleting data within the source.
     *
     * @param paths The path of the data to remove.
     */
    public synchronized void removeAll(List<String> paths) {
        paths.forEach(this::remove);
    }

    /**
     * Stores data to the data structure. This method is executed asynchronously.
     *
     * It is more efficient and better to use {@link StorageContainer#set(String, Object)} and then call {@link StorageContainer#flush()}
     * if you need to save many things immediately.
     *
     * @see StorageContainer#set(String, Object) for caching objects instead of writing directly to the structure.
     *
     * @param path The path to store the data to.
     * @param data The data to store in the file format.
     *
     * @return A {@link CompletableFuture} that will be completed once the async storage has been finalized, but before {@link StorageHook#complete()} is called.
     */
    public CompletableFuture<Void> store(String path, Object data) {
        //Overwrite anything we've already cached to do.
        this.getCache().getRemoveCache().remove(path);
        this.getCache().getSetCache().remove(path);

        return this.storeData(path, data, true)
                .thenCompose(future -> this.getCompleteHook().complete());
    }

    /**
     * Stores data to the data structure. This method is executed asynchronously.
     *
     * @see StorageContainer#setAll(Map) for caching objects instead of writing directly to the structure.
     *
     * @param data A map with all the paths and objects to store.
     * @return A {@link CompletableFuture} that will be completed once all data has been stored.
     *         This will complete before {@link StorageHook#complete()} has been called.
     */
    public CompletableFuture<Void> storeAll(Map<String, Object> data) {
        List<CompletableFuture<Void>> futures = new ArrayList<>();

        for(Map.Entry<String, Object> entry : data.entrySet()) {
            futures.add(this.storeData(entry.getKey(), entry.getValue(), true));

            //Overwrite anything we've already cached to do.
            this.getCache().getRemoveCache().remove(entry.getKey());
            this.getCache().getSetCache().remove(entry.getKey());
        }

        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[] {}))
                .thenCompose(future -> this.getCompleteHook().complete());
    }

    /**
     * Removes a path from the data source. This method is executed asynchronously.
     *
     * It is more efficient and better to use {@link StorageContainer#remove(String)} and then call {@link StorageContainer#flush()}
     * if you need to delete many things immediately.
     *
     * @see StorageContainer#remove(String) for removing data using a cache. (Recommended)
     *
     * @param path The path to remove.
     * @return A {@link CompletableFuture} that will be completed once the async deletion has been finished.
     *         This will complete before {@link StorageHook#complete()} has been called.
     */
    public CompletableFuture<Void> delete(String path) {
        Objects.requireNonNull(path, "Cannot delete from null path!");

        CompletableFuture<Void> future = new CompletableFuture<>();

        this.getAPI().getTaskHandler().runTaskAsync(() -> {
            this.getDeleteHook().accept(path);

            this.getCompleteHook().complete().thenRun(() -> {
                future.complete(null);
            });
        });

        //Overwrite anything we've already cached to do.
        this.getCache().getRemoveCache().remove(path);
        this.getCache().getSetCache().remove(path);

        return future;
    }

    /**
     * Removes multiple paths from the data source. This method is executed asynchronously.
     * Useful if you're removing lots of items and don't want to call {@link DeleteHook#complete()} constantly.
     *
     * @see StorageContainer#removeAll(List) for removing data using a cache. (Recommended)
     *
     * @param paths The paths to remove.
     * @return A {@link CompletableFuture} that will be completed once the async deletion has been finished.
     */
    public CompletableFuture<Void> deleteAll(List<String> paths) {
        Objects.requireNonNull(paths, "Cannot delete from null path!");

        CompletableFuture<Void> future = new CompletableFuture<>();

        this.getAPI().getTaskHandler().runTaskAsync(() -> {
            paths.forEach(p -> {
                this.getDeleteHook().accept(p);
            });

            this.getCompleteHook().complete().thenRun(() -> {
                future.complete(null);
            });
        });

        //This should overwrite anything we've already cached to do.
        this.getCache().getRemoveCache().removeAll(paths);
        this.getCache().getSetCache().keySet().removeIf(paths::contains);

        return future;
    }

    /**
     * Retrieves an object directly from the data source with implicit casting.
     * Note: This method will not recompose {@link StorageDecomposer}s.
     *
     * Note: This method is blocking, and will block until the data structure returns an object.
     * It is highly recommended to only use this method if you know your data structure will not block
     *
     * @see StorageContainer#retrieve(Class, String) for retrieving with explicit casting, or to recompose decomposed objects.
     * @see StorageContainer#retrieveAsync(String) for retrieving data asynchronously.
     *
     * @param <T> The implicit type that will be cast to.
     * @param path The path to retrieve.
     * @return The cast object from the path, or null if it doesn't exist.
     */
    @SuppressWarnings("unchecked")
    public <T> T retrieve(String path) {
        Objects.requireNonNull(path, "Cannot retrieve from null path!");

        if(this.isCached(path)) {
            return this.get(path);
        }

        T obj = (T) this.getRetrieveHook().getFunction().apply(path);
        this.getRetrieveHook().complete();
        this.cacheRetrieve(path, obj);
        return obj;
    }

    /**
     * Retrieves the object directly from the data source with explicit casting.
     * If you want to retrieve a {@link StorageDecomposer}, you will need to use this method and provide the type.
     *
     * Note: This method is blocking, and will block for up to 5 seconds.
     * It is highly recommended to only use this method if you know your data structure will not block
     *
     * @see StorageContainer#retrieve(String) to get the value with implicit casting.
     * @see StorageContainer#retrieveAsync(Class, String) for retrieving data asynchronously.
     *
     * @param <T> The explicit type that will be cast to.
     * @param type The type of Object to get. If this Object has a {@link StorageDecomposer}, it will be used.
     * @param path The path to retrieve.
     * @return The cast object from the path, or null if it doesn't exist.
     */
    @SuppressWarnings("unchecked")
    public <T> T retrieve(Class<T> type, String path) {
        Objects.requireNonNull(path, "Cannot retrieve from null path!");
        Objects.requireNonNull(type, "Type must be provided");

        if(this.isCached(path)) {
            return this.get(path);
        }

        if(!isSupported(type)) {
            throw new IllegalArgumentException(type + " is not a supported storage class. Create a StorageDecomposer to implement behavior!");
        }

        StorageDecomposer<?> decomposer = this.getAPI().getDecomposer(type);

        if (decomposer != null) {
            //Only append %path% if it doesn't already exist in the String.
            //Also needs to be ran async so it doesn't block itself, very smart
            CompletableFuture<CompletableFuture<T>> future = CompletableFuture.supplyAsync(() -> {
                try {
                    return (CompletableFuture<T>) decomposer.applyRecompose(this, path.contains("%path%") ? path : path + ".%path%");
                } catch (ClassCastException e) {
                    throw new ClassCastException("Unable to recompose! This can occur if you're using addRecomposeKey instead of addCompletedRecomposeKey when using retrieve. addRecomposeKey should use retrieveAsync and addCompletedRecomposeKey should use retrieve or a straight object.");
                }
            });

            try {
                //Wait for the asynchronous future to be completed.
                return future.get().get(5, TimeUnit.SECONDS);
            } catch (InterruptedException | ExecutionException | TimeoutException e) {
                e.printStackTrace();
            }
        } else {
            return this.retrieve(path);
        }
        return null;
    }

    /**
     * Retrieves an object directly from the data source with implicit casting.
     * This method is executed asynchronously, and the future will be completed when the data has been returned.
     * Note: This method will not recompose {@link StorageDecomposer}s.
     *
     * @see StorageContainer#retrieveAsync(Class, String) for retrieving with explicit casting, or to recompose decomposed objects.
     *
     * @param <T> The implicit type that will be cast to.
     * @param path The path to retrieve.
     * @return The cast object from the path, or null if it doesn't exist.
     */
    public <T> CompletableFuture<T> retrieveAsync(String path) {
        return CompletableFuture.supplyAsync(() -> this.retrieve(path));
    }

    /**
     * Retrieves the object directly from the data source with explicit casting.
     * If you want to retrieve a {@link StorageDecomposer}, you will need to use this method and provide the type.
     * This method is executed asynchronously, and the future will be completed when the data has been returned.
     *
     * @see StorageContainer#retrieveAsync(String) to get the value with implicit casting.
     *
     * @param <T> The explicit type that will be cast to.
     * @param type The type of Object to get. If this Object has a {@link StorageDecomposer}, it will be used.
     * @param path The path to retrieve.
     * @return The cast object from the path, or null if it doesn't exist.
     */
    public <T> CompletableFuture<T> retrieveAsync(Class<T> type, String path) {
        return CompletableFuture.supplyAsync(() -> this.retrieve(type, path));
    }

    /**
     * Retrieves an object directly from the data source with implicit casting.
     * Note: This method will not recompose {@link StorageDecomposer}s.
     *
     * Note: This method is blocking, and will block until the data structure returns an object.
     * It is highly recommended to only use this method if you know your data structure will not block
     *
     * If the data structure returns null, the provided object will be returned instead.
     *
     * @see StorageContainer#retrieveOrElse(String, Object) for retrieving with explicit casting, or to recompose decomposed objects.
     * @see StorageContainer#retrieveOrElseAsync(String, Object) for retrieving data asynchronously.
     *
     * @param <T> The implicit type that will be cast to.
     * @param path The path to retrieve.
     * @param orElse The object to return if the path does not exist or returns null.
     * @return The cast object from the path, or null if it doesn't exist.
     */
    public <T> T retrieveOrElse(String path, T orElse) {
        T obtained = this.retrieve(path);
        return obtained == null ? orElse : obtained;
    }

    /**
     * Retrieves the object directly from the data source with explicit casting.
     * If you want to retrieve a {@link StorageDecomposer}, you will need to use this method and provide the type.
     *
     * Note: This method is blocking, and will block for up to 5 seconds.
     * It is highly recommended to only use this method if you know your data structure will not block
     *
     * If the data structure returns null, the provided object will be returned instead.
     *
     * @see StorageContainer#retrieveOrElse(String, Object) to get the value with implicit casting.
     * @see StorageContainer#retrieveOrElseAsync(Class, String, Object) for retrieving data asynchronously.
     *
     * @param <T> The explicit type that will be cast to.
     * @param type The type of Object to get. If this Object has a {@link StorageDecomposer}, it will be used.
     * @param path The path to retrieve.
     * @param orElse The object to return if the path does not exist or returns null.
     * @return The cast object from the path, or null if it doesn't exist.
     */
    public <T> T retrieveOrElse(Class<T> type, String path, T orElse) {
        T obtained = this.retrieve(type, path);
        return obtained == null ? orElse : obtained;
    }

    /**
     * Retrieves an object directly from the data source with implicit casting.
     * This method is executed asynchronously, and the future will be completed when the data has been returned.
     * Note: This method will not recompose {@link StorageDecomposer}s.
     *
     * If the data structure returns null, the provided object will be returned instead.
     *
     * @see StorageContainer#retrieveOrElseAsync(Class, String, Object) for retrieving with explicit casting, or to recompose decomposed objects.
     *
     * @param <T> The implicit type that will be cast to.
     * @param path The path to retrieve.
     * @param orElse The object to return if the path does not exist or returns null.
     * @return The cast object from the path, or null if it doesn't exist.
     */
    public <T> CompletableFuture<T> retrieveOrElseAsync(String path, T orElse) {
        CompletableFuture<T> obtained = this.retrieveAsync(path);
        obtained.thenAccept(obj -> {
            obtained.complete(obj == null ? orElse : obj);
        });
        return obtained;
    }

    /**
     * Retrieves the object directly from the data source with explicit casting.
     * If you want to retrieve a {@link StorageDecomposer}, you will need to use this method and provide the type.
     * This method is executed asynchronously, and the future will be completed when the data has been returned.
     *
     * If the data structure returns null, the provided object will be returned instead.
     *
     * @see StorageContainer#retrieveOrElseAsync(String, Object) to get the value with implicit casting.
     *
     * @param <T> The explicit type that will be cast to.
     * @param type The type of Object to get. If this Object has a {@link StorageDecomposer}, it will be used.
     * @param path The path to retrieve.
     * @param orElse The object to return if the path does not exist or returns null.
     * @return The cast object from the path, or null if it doesn't exist.
     */
    public <T> CompletableFuture<T> retrieveOrElseAsync(Class<T> type, String path, T orElse) {
        CompletableFuture<T> obtained = this.retrieveAsync(type, path);
        obtained.thenAccept(obj -> {
            obtained.complete(obj == null ? orElse : obj);
        });
        return obtained;
    }

    /**
     * Caches a retrieved object to the cache.
     *
     * @param path The path to cache to
     * @param object The object to cache
     */
    public void cache(String path, Object object) {
        Objects.requireNonNull(path, "Cannot cache null path!");

        if(!this.getSettings().isUseCache()) return;

        this.getCache().cache(path, object);
    }

    /**
     * Attempts to cache an object, but obeys the {@link StorageSettings#isCachedOnRetrieve()} setting.
     *
     * @param path The path to cache to
     * @param object The object to cache
     */
    public void cacheRetrieve(String path, Object object) {
        if(this.getSettings().isCachedOnRetrieve()) {
            this.cache(path, object);
        }
    }

    /**
     * Attempts to cache an object, but obeys the {@link StorageSettings#isCachedOnStore()} setting.
     *
     * @param path The path to cache to
     * @param object The object to cache
     */
    private void cacheStore(String path, Object object) {
        if(this.getSettings().isCachedOnStore()) {
            this.cache(path, object);
        }
    }

    /**
     * Returns if the provided path is cached.
     *
     * @param path The path to check.
     * @return If the path is cached.
     */
    public boolean isCached(String path) {
        return this.getCache().isCached(path);
    }

    /**
     * Returns the {@link StorageSettings} for this Container.
     *
     * @return The StorageSettings
     */
    public StorageSettings getSettings() {
        return settings;
    }

    /**
     * Returns the {@link StorageCache} that is cached objects.
     * @return The StorageCache
     */
    public StorageCache getCache() {
        return cache;
    }

    /**
     * Returns the {@link IStorageAPI} that was used to create this container.
     * @return The IStorageAPI
     */
    public IStorageAPI getAPI() {
        return api;
    }

    /**
     * Updates the {@link StorageContainer} with the set and remove caches.
     * After flushing, these maps will be cleared.
     *
     * Flushing will not empty the cached data, only the data that needs to be updated to the structure.
     *
     * @see StorageContainer#set(String, Object) for adding objects into the Set cache.
     * @see StorageContainer#remove(String) for adding paths into the Remove cache.
     *
     * @return A future that is completed once the flush has finished.
     */
    public CompletableFuture<Void> flush() {
        return this.getCache().flush();
    }

    /**
     * Clears the cache
     */
    public void clearCache() {
        this.getCache().clearCache();
    }

    /**
     * Returns if a class is supported for storage in this container.
     *
     * All primitives and Strings are automatically supported.
     * Other classes will need to have a {@link StorageDecomposer} to be considered supported.
     *
     * @param clazz The type to check.
     * @return If the specified class is able to be stored.
     */
    public boolean isSupported(Class<?> clazz) {
        if(clazz.isPrimitive()) return true;
        if(clazz == String.class) return true;
        if(this.getAPI().getDecomposer(clazz) != null) return true;
        if(this.getAPI().getSupportedPrimitives().contains(clazz)) return true;

        return false;
    }

    /**
     * Stores data, but does not call {@link StoreHook#complete()} when finished.
     *
     * @param path The path to store the data to.
     * @param data The data to store in the file format.
     */
    private CompletableFuture<Void> storeData(String path, Object data, boolean async) {
        Objects.requireNonNull(path, "Cannot store to null path!");
        CompletableFuture<Void> future = new CompletableFuture<>();

        if (data != null && !this.isSupported(data.getClass())) {
            throw new IllegalArgumentException(data.getClass() + " is not a supported storage class. Create a StorageDecomposer to implement behavior!");
        }

        StorageDecomposer<?> decomposer = data == null ? null : this.getAPI().getDecomposer(data.getClass());
        DecomposedObject object = null;

        //Cache the data.
        if (decomposer != null) {
            String decomposePath = path + ".%path%";
            object = decomposer.applyDecompose(data);
            for (String decomposedPath : object.getDecomposedMap().keySet()) {
                this.cacheStore(decomposePath.replace("%path%", decomposedPath), object.getDecomposedMap().get(decomposedPath));
            }
        } else {
            this.cacheStore(path, data);
        }

        DecomposedObject finalObject = object;
        Runnable runnable = () -> {
            if (decomposer != null) {
                //Add the placeholder for each decomposed path.
                String decomposePath = path + ".%path%";
                for (String decomposedPath : finalObject.getDecomposedMap().keySet()) {
                    String compiledPath = decomposePath.replace("%path%", decomposedPath);
                    Object decomposedObject = finalObject.getDecomposedMap().get(decomposedPath);

                    if(decomposedObject != null) {
                        if(this instanceof ArrayContainer<?> arrayContainer && arrayContainer.isList(decomposedObject)) {
                            this.getStoreHook().accept(compiledPath, arrayContainer.handleList(decomposedObject));
                            continue;
                        } else if(this.getAPI().getDecomposer(decomposedObject.getClass()) != null) {
                            this.storeData(compiledPath, decomposedObject, false);
                            continue;
                        }
                        this.getStoreHook().accept(compiledPath, decomposedObject);
                    }
                }
            } else if (this instanceof ArrayContainer<?> arrayContainer && arrayContainer.isList(data)) {
                this.getStoreHook().accept(path, arrayContainer.handleList(data));
            } else {
                this.getStoreHook().accept(path, data);
            }

            future.complete(null);
        };

        if(async) {
            this.getAPI().getTaskHandler().runTaskAsync(runnable);
        } else {
            runnable.run();
        }

        //No need to update these later, since this should overwrite them.
        this.getCache().getSetCache().remove(path);
        this.getCache().getRemoveCache().remove(path);

        return future;
    }

    private StoreHook getStoreHook() {
        return this.storeHook;
    }
    private DeleteHook getDeleteHook() {
        return deleteHook;
    }
    private RetrieveHook getRetrieveHook() {
        return retrieveHook;
    }
    private CompleteHook getCompleteHook() {
        return completeHook;
    }
}
