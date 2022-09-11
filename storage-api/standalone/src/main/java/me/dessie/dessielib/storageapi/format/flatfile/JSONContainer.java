package me.dessie.dessielib.storageapi.format.flatfile;

import com.google.gson.*;
import com.google.gson.internal.LinkedTreeMap;
import me.dessie.dessielib.storageapi.api.IStorageAPI;
import me.dessie.dessielib.storageapi.container.RetrieveArrayContainer;
import me.dessie.dessielib.storageapi.container.StorageContainer;
import me.dessie.dessielib.storageapi.container.hooks.CompleteHook;
import me.dessie.dessielib.storageapi.container.hooks.DeleteHook;
import me.dessie.dessielib.storageapi.container.hooks.RetrieveHook;
import me.dessie.dessielib.storageapi.container.hooks.StoreHook;
import me.dessie.dessielib.storageapi.settings.StorageSettings;
import me.dessie.dessielib.storageapi.util.JsonObjectBuilder;
import me.dessie.dessielib.storageapi.util.Pair;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.stream.Stream;

/**
 * A {@link StorageContainer} that stores using JSON format using {@link Gson}.
 */
public class JSONContainer extends RetrieveArrayContainer<JsonArray, JsonObject> {

    private final Gson gson = new Gson().newBuilder().setPrettyPrinting().create();
    private final File json;
    private JsonObject object;

    /**
     * Creates a JSONContainer that can be stored and retrieved from using the provided file.
     * This will use the default settings in {@link StorageSettings}.
     *
     * @param api The IStorageAPI instance.
     * @param jsonFile The JSON {@link File} that will be used for this Container.
     */
    public JSONContainer(IStorageAPI api, File jsonFile) {
        this(api, jsonFile, new StorageSettings());
    }

    /**
     * Creates a JSONContainer that can be stored and retrieved from using the provided file.
     * This will use the provided settings from {@link StorageSettings}.
     *
     * @param api The IStorageAPI instance.
     * @param jsonFile The JSON {@link File} that will be used for this Container.
     * @param settings The StorageSettings for this Container.
     */
    public JSONContainer(IStorageAPI api, File jsonFile, StorageSettings settings) {
        super(api, settings);
        this.json = jsonFile;

        try {
            //Create the file.
            if(this.getJson().getParentFile() != null) {
                this.getJson().getParentFile().mkdirs();
            }
            this.getJson().createNewFile();

            //If it's empty and exists, setup the basic object structure.
            if(this.getJson().exists() && this.getJson().length() == 0) {
                FileWriter writer = new FileWriter(this.getJson());
                writer.write("{}");
                writer.close();
            } else if(!this.getJson().exists()) {
                throw new IOException("Unable to find file " + this.getJson().getName());
            }

            this.object = JsonParser.parseReader(new FileReader(this.getJson())).getAsJsonObject();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * @return The JSON {@link File} that is being used for the container.
     */
    public File getJson() {
        return json;
    }

    /**
     * @return The {@link JsonObject} that Gson is using to parse JSON.
     */
    public JsonObject getObject() {
        return object;
    }

    /**
     * @return The Gson instance.
     */
    public Gson getGson() {
        return gson;
    }

    @Override
    protected StoreHook storeHook() {
        return new StoreHook((path, data) -> {
            if(path.contains(".")) {
                this.handleNestedPath(this.getObject(), path, this.getGson().toJsonTree(data));
            } else {
                //Remove it before attempting to add, even though JsonObject is using a Set for it's keys
                //There seems to be a rare issue where duplicate keys can be added. This should prevent that from ever happening.
                this.getObject().remove(path);
                this.getObject().add(path, this.getGson().toJsonTree(data));
            }
        });
    }

    @Override
    protected RetrieveHook retrieveHook() {
        return new RetrieveHook(path -> {
            String[] tree = path.split("\\.");
            JsonObject retrieved = this.getRetrieveElement(path);

            return retrieveCorrectly(retrieved, tree[tree.length - 1]);
        });
    }

    @Override
    protected DeleteHook deleteHook() {
        return new DeleteHook(path -> {
            String[] tree = path.split("\\.");

            JsonObject temp = this.getObject();
            for(int i = 0; i < tree.length - 1; i++) {
                temp = temp.get(tree[i]).getAsJsonObject();
            }

            temp.remove(tree[tree.length - 1]);
        });
    }

    @Override
    protected CompleteHook completeHook() {
        return new CompleteHook().onComplete(() -> {
            this.write();
            return CompletableFuture.completedFuture(null);
        });
    }

    @Override
    public Set<String> getKeys(String path) {
        if(this.getElement(path) instanceof JsonObject object) {
            return object.keySet();
        } else return new HashSet<>();
    }

    @Override
    protected BiConsumer<JsonArray, JsonObject> add() {
        return JsonArray::add;
    }

    @Override
    protected Stream<Object> getHandlerStream(JsonArray handler) {
        Stream.Builder<Object> builder = Stream.builder();
        handler.forEach(builder::add);
        return builder.build();
    }

    @Override
    protected Stream<String> getNestedKeys(JsonObject nested) {
        return nested.keySet().stream();
    }

    @Override
    protected boolean isHandler(Object object) {
        return object instanceof JsonArray;
    }

    @Override
    protected boolean isNested(Object object) {
        return object instanceof JsonObject;
    }

    @Override
    protected Object getObjectFromNested(JsonObject nested, String key) {
        return retrieveCorrectly(nested, key);
    }

    @Override
    protected Object getPrimitive(Object object) {
        Object obj = this.getGson().fromJson((JsonElement) object, Object.class);
        if(obj instanceof Double && !object.toString().contains(".")) {
            return Integer.valueOf(object.toString());
        } else return obj;
    }

    @Override
    protected BiConsumer<JsonArray, List<Pair<String, Object>>> handleListObject() {
        return ((array, list) -> {
            JsonObject object = new JsonObject();

            for(Pair<String, Object> pair : list) {
                //If the path is null, then it's not a decomposer, so we just add that to the array directly.
                if(pair.getKey() == null) {
                    array.add(this.getGson().toJsonTree(pair.getValue()));
                    continue;
                }

                //Handle nested paths.
                if(pair.getKey().contains(".")) {
                    this.handleNestedPath(object, pair.getKey(), pair.getValue());
                } else {
                    object.add(pair.getKey(), this.getGson().toJsonTree(pair.getValue()));
                }
            }

            if(object.keySet().size() != 0) {
                array.add(object);
            }
        });
    }

    @Override
    protected JsonArray getStoreListHandler() {
        return new JsonArray();
    }

    @Override
    protected JsonArray getRetrieveListHandler(String path) {
        if(this.getElement(path) instanceof JsonArray array) {
            return array;
        }
        return new JsonArray();
    }

    private JsonObject handleNestedPath(JsonObject object, String path, Object value) {
        String[] tree = path.split("\\.");

        //Add the final value if it's the last part of the tree
        if (tree.length == 1) {
            object.add(tree[0], this.getGson().toJsonTree(value));
            return object;
        }

        //Get an existing sub-object, or create it if the path doesn't exist.
        JsonElement element = object.get(tree[0]);
        JsonObject subObject = (element instanceof JsonObject temp) ? temp : new JsonObject();

        //Traverse down the tree
        String subPath = path.substring(path.indexOf(".") + 1);

        //Recursively set the objects
        object.add(tree[0], this.handleNestedPath(subObject, subPath, value));

        return object;
    }

    private Object retrieveCorrectly(JsonObject object, String key) {
        Object retrieve = this.getGson().fromJson(object.get(key), Object.class);
        if(retrieve != null && retrieve.getClass() == Double.class && !object.get(key).getAsString().contains(".")) {
            return object.get(key).getAsInt();
        } else if(retrieve instanceof LinkedTreeMap<?,?> || retrieve instanceof ArrayList) {
            return object.get(key);
        } else {
            return retrieve;
        }
    }

    private Map<String, JsonElement> getTree(String path) {
        Map<String, JsonElement> tree = new LinkedHashMap<>();
        JsonElement current = this.getObject();
        String[] branches = path.split("\\.");

        for (String branch : branches) {
            if (!(current instanceof JsonObject object)) continue;

            object.has(branch);
            tree.put(branch, current);
            current = object.get(branch);
        }

        //Add the last branch.
        tree.put(branches[branches.length - 1], current);
        return tree;
    }

    private JsonElement getElement(String path) {
        if(path.equals("")) return this.getObject();

        Map<String, JsonElement> tree = this.getTree(path);
        List<String> keys = new ArrayList<>(tree.keySet());

        return tree.get(keys.get(keys.size() - 1));
    }

    //Always returns a JsonObject instead of a JsonElement
    private JsonObject getRetrieveElement(String path) {
        if(path.equals("")) return this.getObject();

        Map<String, JsonElement> tree = this.getTree(path);
        List<String> keys = new ArrayList<>(tree.keySet());

        //We always want a JsonObject to be returned, and the value will be retrieved from that.
        //We can just use the last value from the keys and tree, since that's the element we're trying to obtain.
        return new JsonObjectBuilder().add(keys.get(keys.size() - 1), tree.get(keys.get(keys.size() - 1))).build();
    }

    private void write() {
        try {
            FileWriter writer = new FileWriter(this.getJson());
            this.getGson().toJson(this.getObject(), writer);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isListSupported(Class<?> clazz) {
        return super.isListSupported(clazz) || this.getAPI().getDecomposer(clazz) != null;
    }
}
