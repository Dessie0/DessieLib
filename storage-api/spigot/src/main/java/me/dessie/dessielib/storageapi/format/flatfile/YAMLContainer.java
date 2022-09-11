package me.dessie.dessielib.storageapi.format.flatfile;

import me.dessie.dessielib.storageapi.api.IStorageAPI;
import me.dessie.dessielib.storageapi.container.RetrieveArrayContainer;
import me.dessie.dessielib.storageapi.container.StorageContainer;
import me.dessie.dessielib.storageapi.container.hooks.CompleteHook;
import me.dessie.dessielib.storageapi.container.hooks.DeleteHook;
import me.dessie.dessielib.storageapi.container.hooks.RetrieveHook;
import me.dessie.dessielib.storageapi.container.hooks.StoreHook;
import me.dessie.dessielib.storageapi.settings.StorageSettings;
import me.dessie.dessielib.storageapi.util.Pair;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.serialization.ConfigurationSerializable;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.stream.Stream;

/**
 * A {@link StorageContainer} that stores using YAML format using {@link YamlConfiguration}.
 */
public class YAMLContainer extends RetrieveArrayContainer<List<Object>, ConfigurationSection> {

    private final File yaml;
    private final YamlConfiguration configuration;

    /**
     * Creates a YAMLContainer that can be stored and retrieved from using the provided file.
     * This will use the default settings in {@link StorageSettings}.
     *
     * @param api The IStorageAPI instance.
     * @param yamlFile The YAML {@link File} that will be used for this Container.
     */
    public YAMLContainer(IStorageAPI api, File yamlFile) {
        this(api, yamlFile, new StorageSettings());
    }

    /**
     * Creates a YAMLContainer that can be stored and retrieved from using the provided file.
     * This will use the provided settings from {@link StorageSettings}.
     *
     * @param api The IStorageAPI instance.
     * @param yamlFile The YAML file that will be used for this Container.
     * @param settings The StorageSettings for this Container.
     */
    public YAMLContainer(IStorageAPI api, File yamlFile, StorageSettings settings) {
        super(api, settings);
        this.yaml = yamlFile;
        this.configuration = new YamlConfiguration();

        try {
            //Create the file.
            if(this.getYaml().getParentFile() != null) {
                this.getYaml().getParentFile().mkdirs();
            }
            this.getYaml().createNewFile();

            if(this.getYaml().exists()) {
                this.getConfiguration().load(this.getYaml());
            } else {
                throw new IOException("Unable to create file " + this.getYaml().getName());
            }
        } catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
        }
    }

    /**
     * @return The YAML {@link File} that is being used for the container.
     */
    public File getYaml() {
        return yaml;
    }

    /**
     * @return The {@link YamlConfiguration} instance.
     */
    public YamlConfiguration getConfiguration() {
        return configuration;
    }

    @Override
    protected StoreHook storeHook() {
        return new StoreHook((path, data) -> {
            this.getConfiguration().set(path, data);
        });
    }

    @Override
    protected DeleteHook deleteHook() {
        return new DeleteHook((path) -> {
            this.getConfiguration().set(path, null);
        });
    }

    @Override
    protected RetrieveHook retrieveHook() {
        return new RetrieveHook(path -> this.getConfiguration().get(path));
    }

    @Override
    protected CompleteHook completeHook() {
        return new CompleteHook().onComplete(() -> {
            try {
                this.getConfiguration().save(this.getYaml());
            } catch (IOException | IllegalArgumentException e) {
                e.printStackTrace();
            }

            return CompletableFuture.completedFuture(null);
        });
    }

    @Override
    public Set<String> getKeys(String path) {
        ConfigurationSection section = this.getConfiguration().getConfigurationSection(path);
        return section != null ? section.getKeys(false) : new HashSet<>();
    }

    @Override
    protected List<Object> getStoreListHandler() {
        return new ArrayList<>();
    }

    @Override
    @SuppressWarnings("unchecked")
    protected List<Object> getRetrieveListHandler(String path) {
        if(this.getConfiguration().get(path) != null && !this.getConfiguration().isList(path)) {
            throw new IllegalArgumentException("List not found at path " + path);
        }

        return this.getConfiguration().get(path) != null ? (List<Object>) this.getConfiguration().getList(path) : new ArrayList<>();
    }

    @Override
    protected BiConsumer<List<Object>, List<Pair<String, Object>>> handleListObject() {
        return ((handler, list) -> {
            ConfigurationSection section = new MemoryConfiguration();

            for(Pair<String, Object> pair : list) {
                if(pair.getKey() == null) {
                    handler.add(pair.getValue());
                } else {
                    section.set(pair.getKey(), pair.getValue());
                }
            }

            if(section.getKeys(false).size() != 0) {
                handler.add(section);
            }
        });
    }

    @Override
    protected BiConsumer<List<Object>, ConfigurationSection> add() {
        return List::add;
    }

    @Override
    protected Stream<Object> getHandlerStream(List<Object> handler) {
        return handler.stream();
    }

    @Override
    protected Stream<String> getNestedKeys(ConfigurationSection nested) {
        return nested.getKeys(false).stream().filter(key -> !key.equalsIgnoreCase("=="));
    }

    @Override
    protected boolean isHandler(Object object) {
        return object instanceof List<?>;
    }

    @Override
    protected boolean isNested(Object object) {
        return object instanceof ConfigurationSection;
    }

    @Override
    protected Object getObjectFromNested(ConfigurationSection nested, String key) {
        return nested.get(key);
    }

    @Override
    protected Object getPrimitive(Object object) {
        return object;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isListSupported(Class<?> clazz) {
        return super.isListSupported(clazz) || this.getAPI().getDecomposer(clazz) != null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isSupported(Class<?> clazz) {
        return super.isSupported(clazz) || ConfigurationSerializable.class.isAssignableFrom(clazz);
    }
}
