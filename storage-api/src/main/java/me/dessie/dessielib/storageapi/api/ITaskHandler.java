package me.dessie.dessielib.storageapi.api;

/**
 * This class is used for running specific tasks asynchronously and on timers.
 *
 * Whenever something will need one of these cases, it's runnable will be passed
 * to the instance of this class to be executed appropriately.
 *
 */
public interface ITaskHandler {

    /**
     * Runs a {@link Runnable} asynchronously.
     * @param runnable The Runnable to execute
     */
    void runTaskAsync(Runnable runnable);

    /**
     * Runs a {@link Runnable} after a specified delay.
     * @param runnable The Runnable to execute
     * @param delay How long to wait, in seconds before the task is executed.
     */
    void runTaskLater(Runnable runnable, long delay);

    /**
     * Runs a {@link Runnable} after a specified delay, and repeats it infinitely until cancelled at the specified period.
     * @param runnable The Runnable to execute
     * @param delay How long to wait, in seconds before the task is executed.
     * @param period How long to wait between run cycles.
     */
    void runTaskTimer(Runnable runnable, long delay, long period);

    /**
     * Cancels a task that matches a provided runnable.
     * @param runnable The runnable task to cancel.
     */
    void cancel(Runnable runnable);

}
