package me.dessie.dessielib.storageapi.cache;

import me.dessie.dessielib.storageapi.api.ITaskHandler;

/**
 * Represents an object that has been cached from a StorageContainer
 *
 * This object will only be cached for a certain amount of time before it automatically expires itself.
 */
public class CachedObject {

    private final StorageCache cache;
    private final Object object;
    private final int duration;

    private final Runnable runnable;

    /**
     * @param cache The {@link StorageCache} that cached this object.
     * @param object The object to cache
     * @param duration The duration to cache in seconds.
     */
    CachedObject(StorageCache cache, Object object, int duration) {
        this.cache = cache;
        this.object = object;
        this.duration = duration;

        if(duration > 0) {
            this.runnable = () -> this.getCache().remove(this);
            this.getCache().getContainer().getAPI().getTaskHandler().runTaskLater(runnable, duration);
        } else this.runnable = null;
    }

    /**
     * Returns the {@link StorageCache} that cached this object.
     * @return The StorageCache
     */
    public StorageCache getCache() {
        return cache;
    }

    /**
     * Returns the {@link Runnable} that will be executed when the Timer expires.
     * This runnable can be used to cancel the task by calling {@link ITaskHandler#cancel(Runnable)}
     *
     * @return The Runnable
     */
    public Runnable getRunnable() {
        return runnable;
    }

    /**
     * Returns the amount of time this object will be cached in seconds.
     * @return The duration
     */
    public int getDuration() {
        return duration;
    }

    /**
     * Returns the cached object.
     *
     * @return The cached object.
     */
    public Object getObject() {
        return object;
    }
}
