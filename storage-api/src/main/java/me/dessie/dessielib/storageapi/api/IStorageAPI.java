package me.dessie.dessielib.storageapi.api;

import me.dessie.dessielib.storageapi.decomposition.StorageDecomposer;

import java.util.List;
import java.util.Map;

/**
 * Base API implementation for StorageAPI instances.
 */
public interface IStorageAPI {

    /**
     * Registers a provided class to generate a {@link StorageDecomposer} from it's Annotations.
     * If this method is not called, annotated classes will not be generated as StorageDecomposers.
     *
     * @param <T> The type of class
     * @param clazz The class that you wish to register with annotations.
     */
    <T> void registerAnnotatedDecomposer(Class<T> clazz);

    /**
     * Adds a {@link StorageDecomposer} that can be accessed through all StorageContainer instances.
     * These only need to be added once, and a class can only have 1 StorageDecomposer.
     *
     * Attempting to add a second StorageDecomposer for a class will overwrite the first one.
     *
     * @param decomposer The StorageDecomposer to add.
     */
    void addStorageDecomposer(StorageDecomposer<?> decomposer);

    /**
     * Adds an Enum decomposer to support directly storing and obtaining Enums from the container.
     * @param <T> The type of Enum to register
     * @param enumType The Enum class to register.
     */
    <T extends Enum<T>> void addStorageEnum(Class<T> enumType);

    /**
     * Returns a {@link ITaskHandler} for running async tasks and task timers.
     *
     * @return The ITaskHandler instance to use.
     */
    ITaskHandler getTaskHandler();

    /**
     * @return All registered {@link StorageDecomposer}s
     */
    List<StorageDecomposer<?>> getStorageDecomposers();

    /**
     * Returns a {@link StorageDecomposer} from the class instance.
     *
     * @param clazz The class to get the decomposer for.
     * @param <T> The Class type to get the decomposer for.
     * @return The registered StorageDecomposer for the provided class, or null if it doesn't exist.
     */
    <T> StorageDecomposer<T> getDecomposer(Class<T> clazz);

    /**
     * Returns the map of primitive classes to their respected Wrapped classes
     *
     * E.g. int -> Integer
     *
     * @return The map for primitives to wrappers.
     */
    Map<Class<?>, Class<?>> getWrappers();

    /**
     * Returns all supported primitives for StorageContainers.
     * Currently, Integers, Booleans, and Doubles.
     *
     * @return A list of the supported primitive wrapper classes
     */
    List<Class<?>> getSupportedPrimitives();

    /**
     * Returns a default value for the provided primitive wrapper.
     *
     * @param clazz A primitive wrapper class to get the default value for.
     * @return The specified default value for the provided wrapper class.
     */
    Object getDefault(Class<?> clazz);
}
