package me.dessie.dessielib.storageapi.cache;

import me.dessie.dessielib.storageapi.SpigotStorageAPI;
import me.dessie.dessielib.storageapi.api.ITaskHandler;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;

/**
 * TaskHandler implementation for a spigot version of StorageAPI.
 */
public class TaskHandler implements ITaskHandler {

    private static final Map<Runnable, BukkitTask> tasks = new HashMap<>();
    private final SpigotStorageAPI api;

    /**
     * @param api The SpigotStorageAPI instance to create the handler with.
     */
    public TaskHandler(SpigotStorageAPI api) {
        this.api = api;
    }

    @Override
    public void runTaskAsync(Runnable runnable) {
        Bukkit.getScheduler().runTaskAsynchronously(this.getAPI().getPlugin(), runnable);
    }

    @Override
    public void runTaskLater(Runnable runnable, long delay) {
        getTasks().put(runnable, Bukkit.getScheduler().runTaskLater(this.getAPI().getPlugin(), runnable, delay * 20));
    }

    @Override
    public void runTaskTimer(Runnable runnable, long delay, long period) {
        getTasks().put(runnable, Bukkit.getScheduler().runTaskTimer(this.getAPI().getPlugin(), runnable, delay * 20, period * 20));
    }

    @Override
    public void cancel(Runnable runnable) {
        BukkitTask task = getTasks().get(runnable);
        if(task != null) {
            task.cancel();
        }
    }

    /**
     * Returns the {@link SpigotStorageAPI} instance that was used to create this handler.
     *
     * @return The SpigotStorageAPI instance.
     */
    public SpigotStorageAPI getAPI() {
        return api;
    }

    private static Map<Runnable, BukkitTask> getTasks() {
        return tasks;
    }
}
