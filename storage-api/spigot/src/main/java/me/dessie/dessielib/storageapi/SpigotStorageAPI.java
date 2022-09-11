package me.dessie.dessielib.storageapi;

import me.dessie.dessielib.core.utils.ClassUtil;
import me.dessie.dessielib.storageapi.api.ITaskHandler;
import me.dessie.dessielib.storageapi.api.StorageAPI;
import me.dessie.dessielib.storageapi.cache.TaskHandler;
import me.dessie.dessielib.storageapi.decomposition.StorageDecomposer;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;

/**
 * Main class for registering StorageAPI for use with Spigot.
 */
public class SpigotStorageAPI extends StorageAPI {

    private final JavaPlugin plugin;
    private final TaskHandler taskHandler;

    private SpigotStorageAPI(JavaPlugin yourPlugin, boolean registerAnnotations) {
        Objects.requireNonNull(yourPlugin, "Plugin cannot be null!");

        this.plugin = yourPlugin;
        this.taskHandler = new TaskHandler(this);

        if(registerAnnotations) {
            registerAnnotatedDecomposers(null);
        }
    }

    /**
     * Register the API to use your plugin. Returns the API instance.
     * @param yourPlugin Your plugin instance.
     *
     * @return The {@link SpigotStorageAPI} instance.
     */
    public static SpigotStorageAPI register(JavaPlugin yourPlugin) {
        return SpigotStorageAPI.register(yourPlugin, true);
    }

    /**
     * Register the API to use your plugin.
     * @param yourPlugin Your plugin instance.
     * @param registerAnnotations If the plugin should register the annotated classes for {@link StorageDecomposer}
     *                            If this is false, they will not be registered and therefore will not be used.
     *
     * @return The {@link SpigotStorageAPI} instance.
     */
    public static SpigotStorageAPI register(JavaPlugin yourPlugin, boolean registerAnnotations) {
        return new SpigotStorageAPI(yourPlugin, registerAnnotations);
    }

    @Override
    public ITaskHandler getTaskHandler() {
        return this.taskHandler;
    }

    /**
     * @return The plugin that registered the StorageAPI.
     */
    public JavaPlugin getPlugin() {
        return plugin;
    }

    /**
     * This method must be called to register any annotated classes for decomposers.
     * If this method is not called, annotated classes will not be generated as {@link StorageDecomposer}s.
     *
     * This method will only attempt to register classes that are contained within the provided package.
     *
     * This method may not work properly if you're running from an IDE environment.
     * You may need to run your JAR externally for all classes to be found.
     * If you need to test via an IDE environment, see {@link #registerAnnotatedDecomposer(Class)}
     *
     * @see SpigotStorageAPI#registerAnnotatedDecomposer(Class)
     * 
     * @param pack The package that a class must be in to attempt to register.
     *             You should generally only register classes in a package that you own.
     *
     *             E.g. me.dessie.dessielib.me.dessie.dessielib.storageapi or com.google.gson
     */
    public void registerAnnotatedDecomposers(String pack) {
        ClassUtil.getClasses(Object.class, this.getPlugin(), pack)
                .stream().filter(clazz -> this.getDecomposer(clazz) == null)
                .forEach(this::registerAnnotatedDecomposer);
    }
}
