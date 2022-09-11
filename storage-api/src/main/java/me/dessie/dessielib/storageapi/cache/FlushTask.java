package me.dessie.dessielib.storageapi.cache;

import me.dessie.dessielib.storageapi.container.StorageContainer;
import me.dessie.dessielib.storageapi.settings.StorageSettings;

/**
 * Handles how fast a {@link StorageContainer} can be written to.
 *
 * This task will make sure that the caches are automatically flushed specified period of time.
 * This makes sure that the caches are always up-to-date and
 *
 * @see StorageSettings Use the Settings to change the cooldown and flush rate.
 */
public class FlushTask implements Runnable {

    private final StorageContainer container;
    private final int flushRate;

    private boolean running;

    /**
     * @param container The container to create the FlushTask for.
     */
    public FlushTask(StorageContainer container) {
        this.container = container;
        this.flushRate = this.getContainer().getSettings().getFlushRate();
        this.reset();
    }

    /**
     * Resets the FlushTask's scheduler to it's initial starting value.
     * @see FlushTask#getFlushRate()
     */
    public void reset() {
        if(this.isRunning()) {
            this.getContainer().getAPI().getTaskHandler().cancel(this);
        }

        this.getContainer().getAPI().getTaskHandler().runTaskTimer(this, this.getFlushRate(), this.getFlushRate());
        this.running = true;
    }

    /**
     * Returns the {@link StorageContainer} that is using this task.
     *
     * @return The respective StorageContainer.
     */
    public StorageContainer getContainer() {
        return container;
    }

    /**
     * Returns how often, in seconds, the task will automatically flush the container.
     * This method is a delegate method for {@link StorageSettings#getFlushRate()}, and will be the same result.
     *
     * @return How often in seconds the FlushTask will automatically flush the container
     */
    public int getFlushRate() {
        return flushRate;
    }

    /**
     * Returns if the FlushTask is currently running.
     *
     * @return If the task is currently running.
     */
    public boolean isRunning() {
        return running;
    }

    @Override
    public void run() {
        this.getContainer().flush();
    }
}
