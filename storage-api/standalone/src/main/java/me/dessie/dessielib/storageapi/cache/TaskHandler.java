package me.dessie.dessielib.storageapi.cache;

import me.dessie.dessielib.storageapi.api.ITaskHandler;

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CompletableFuture;

/**
 * TaskHandler implementation for a standalone version of StorageAPI.
 */
public class TaskHandler implements ITaskHandler {

    private static final Map<Runnable, TimerTask> tasks = new HashMap<>();

    @Override
    public void runTaskAsync(Runnable runnable) {
        CompletableFuture.runAsync(runnable);
    }

    @Override
    public void runTaskLater(Runnable runnable, long delay) {
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                runnable.run();
                getTasks().remove(runnable);
            }
        };

        getTasks().put(runnable, task);
        new Timer().schedule(task, delay * 1000);
    }

    @Override
    public void runTaskTimer(Runnable runnable, long delay, long period) {
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                runnable.run();
            }
        };

        getTasks().put(runnable, task);
        new Timer().schedule(task, delay * 1000, period * 1000);
    }

    @Override
    public void cancel(Runnable runnable) {
        TimerTask task = getTasks().get(runnable);

        if(task != null) {
            task.cancel();
            getTasks().remove(runnable);
        }
    }

    private static Map<Runnable, TimerTask> getTasks() {
        return tasks;
    }
}
