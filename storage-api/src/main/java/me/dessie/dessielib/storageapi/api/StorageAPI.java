package me.dessie.dessielib.storageapi.api;

import me.dessie.dessielib.annotations.storageapi.RecomposeConstructor;
import me.dessie.dessielib.annotations.storageapi.Stored;
import me.dessie.dessielib.annotations.storageapi.StoredList;
import me.dessie.dessielib.storageapi.container.ArrayContainer;
import me.dessie.dessielib.storageapi.decomposition.DecomposedObject;
import me.dessie.dessielib.storageapi.decomposition.StorageDecomposer;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;

/**
 * Abstraction class that generically defines and overrides the majority of the required methods for a StorageAPI implementation.
 */
public abstract class StorageAPI implements IStorageAPI {

    private static final List<Class<?>> supportedPrimitives = List.of(Integer.class, Boolean.class, Double.class);

    //Used for registering annotated
    private static final Map<Class<?>, Class<?>> wrappers = new HashMap<>() {{
        put(int.class, Integer.class);
        put(byte.class, Byte.class);
        put(char.class, Character.class);
        put(boolean.class, Boolean.class);
        put(double.class, Double.class);
        put(float.class, Float.class);
        put(long.class, Long.class);
        put(short.class, Short.class);
        put(void.class, void.class);
    }};

    //Used for obtaining default values of wrapper classes.
    private static final Map<Class<?>, ?> defaults = new HashMap<>() {{
        put(Integer.class, 0);
        put(Byte.class, 0);
        put(Character.class, '\0');
        put(Boolean.class, false);
        put(Double.class, 0d);
        put(Float.class, 0f);
        put(Long.class, 0L);
        put(Short.class, 0);
    }};

    private final List<StorageDecomposer<?>> storageDecomposers = new ArrayList<>();

    @Override
    public List<StorageDecomposer<?>> getStorageDecomposers() {
        return storageDecomposers;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> StorageDecomposer<T> getDecomposer(Class<T> clazz) {
        if(clazz == null) return null;
        return this.getStorageDecomposers().stream()
                .filter(decomposer -> decomposer.getType() == clazz)
                .map(composer -> (StorageDecomposer<T>) composer)
                .findFirst().orElse(null);
    }

    @Override
    public void addStorageDecomposer(StorageDecomposer<?> decomposer) {
        getStorageDecomposers().removeIf(decomp -> decomp.getType() == decomposer.getType());
        getStorageDecomposers().add(decomposer);
    }

    @Override
    public <T extends Enum<T>> void addStorageEnum(Class<T> enumType) {
        this.addStorageDecomposer(new StorageDecomposer<>(enumType, (e, decomposer) -> {
            decomposer.addDecomposedKey("value", e.name());

            return decomposer;
        }, (container, recompose) -> {
            recompose.addRecomposeKey("value", enumType, container::retrieveAsync);

            return recompose.onComplete(completed -> Enum.valueOf(enumType, completed.getCompletedObject("value")));
        }));
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> void registerAnnotatedDecomposer(Class<T> clazz) {
        //Grab all the fields that have either Stored or StoredList annotations.
        List<Field> decomposeFields = Arrays.stream(clazz.getDeclaredFields()).filter(f -> f.isAnnotationPresent(Stored.class) || f.isAnnotationPresent(StoredList.class)).toList();
        List<Field> recomposeFields = Arrays.stream(clazz.getDeclaredFields()).filter(f -> (f.isAnnotationPresent(Stored.class) && f.getAnnotation(Stored.class).recompose()) || (f.isAnnotationPresent(StoredList.class) && f.getAnnotation(StoredList.class).recompose())).toList();
        Constructor<T> constructor = (Constructor<T>) Arrays.stream(clazz.getDeclaredConstructors()).filter(c -> c.isAnnotationPresent(RecomposeConstructor.class)).findFirst().orElse(null);

        //If there's no storage fields, and no recompose constructor, skip the class.
        if (decomposeFields.isEmpty() && constructor == null) return;

        if (constructor == null) {
            this.addStorageDecomposer(new StorageDecomposer<>(clazz, getGenericDecompose(clazz, decomposeFields)));
        } else {
            constructor.setAccessible(true);

            this.addStorageDecomposer(new StorageDecomposer<>(clazz, getGenericDecompose(clazz, decomposeFields), (container, recompose) -> {
                for (Field f : recomposeFields) {
                    String path = f.isAnnotationPresent(Stored.class) && !f.getAnnotation(Stored.class).storeAs().equals("") ? f.getAnnotation(Stored.class).storeAs()
                            : f.isAnnotationPresent(StoredList.class) && !f.getAnnotation(StoredList.class).storeAs().equals("") ? f.getAnnotation(StoredList.class).storeAs() : f.getName();

                    Class<?> type = f.isAnnotationPresent(Stored.class) ? f.getType() : f.getAnnotation(StoredList.class).type();

                    if (f.isAnnotationPresent(Stored.class)) {
                        recompose.addRecomposeKey(path, type, (p) -> {
                            return (CompletableFuture<Object>) container.retrieveAsync(type, p);
                        });
                    } else if (container instanceof ArrayContainer<?> arrayContainer) {
                        recompose.addRecomposeKey(path, type, (p) -> {
                            return (CompletableFuture<Object>) (CompletableFuture<?>) arrayContainer.retrieveListAsync(type, p);
                        });
                    }
                }

                return recompose.onComplete(completed -> {
                    List<Object> args = new ArrayList<>();

                    for (Field f : recomposeFields) {
                        String path = f.isAnnotationPresent(Stored.class) && !f.getAnnotation(Stored.class).storeAs().equals("") ? f.getAnnotation(Stored.class).storeAs()
                                : f.isAnnotationPresent(StoredList.class) && !f.getAnnotation(StoredList.class).storeAs().equals("") ? f.getAnnotation(StoredList.class).storeAs() : f.getName();

                        args.add(completed.getCompletedObject(path));
                    }

                    RecomposeConstructor annotation = constructor.getAnnotation(RecomposeConstructor.class);
                    if (args.size() != constructor.getParameterCount()) {
                        if (annotation.throwError()) {
                            throw new IllegalStateException("Cannot use Annotations to add a Recomposer for " + clazz.getSimpleName() + ". Constructor param count and recompose fields are not the same size.");
                        } else return null;

                    } else {
                        //Check if the args provided and the params needed are the same types.
                        Class<?>[] argsArray = args.stream().map(obj -> obj == null ? null : obj.getClass()).toList().toArray(new Class<?>[0]);
                        Class<?>[] paramArray = Arrays.stream(constructor.getParameters()).map((param -> param.getType().isPrimitive() ? wrappers.get(param.getType()) : param.getType())).toList().toArray(new Class<?>[0]);

                        for (int i = 0; i < argsArray.length; i++) {
                            if ((!annotation.allowNull() && argsArray[i] == null)) {
                                if (annotation.throwError()) {
                                    throw new IllegalStateException("When recomposing " + clazz + ", the parameter " + paramArray[i] + " was found to be null when not allowed.");
                                }
                                return null;
                            } else if (argsArray[i] != null && argsArray[i] != paramArray[i] && !paramArray[i].isAssignableFrom(argsArray[i])) {
                                if (annotation.throwError()) {
                                    throw new IllegalStateException("Cannot use Annotations to add a Recomposer for " + clazz.getSimpleName() + ". Constructor and provided arguments do not match. Expected " + Arrays.toString(paramArray) + " but got " + Arrays.toString(argsArray));
                                } else return null;
                            }
                        }
                    }

                    try {
                        return constructor.newInstance(args.toArray());
                    } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                        e.printStackTrace();
                        return null;
                    }
                });
            }));
        }
    }

    @Override
    public Map<Class<?>, Class<?>> getWrappers() {
        return wrappers;
    }

    @Override
    public List<Class<?>> getSupportedPrimitives() {
        return supportedPrimitives;
    }

    @Override
    public Object getDefault(Class<?> clazz) {
        return defaults.getOrDefault(clazz, null);
    }

    private <T> BiFunction<T, DecomposedObject, DecomposedObject> getGenericDecompose(Class<T> type, List<Field> decomposeFields) {
        return (obj, decomposer) -> {
            for(Field f : decomposeFields) {
                try {
                    f.setAccessible(true);
                    String path = f.isAnnotationPresent(Stored.class) && !f.getAnnotation(Stored.class).storeAs().equals("") ? f.getAnnotation(Stored.class).storeAs()
                            : f.isAnnotationPresent(StoredList.class) && !f.getAnnotation(StoredList.class).storeAs().equals("") ? f.getAnnotation(StoredList.class).storeAs() : f.getName();

                    decomposer.addDecomposedKey(path, f.get(obj));
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
            return decomposer;
        };
    }

}
