package me.dessie.dessielib.storageapi.storage.format.flatfile;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import me.dessie.dessielib.storageapi.storage.container.ArrayContainer;
import me.dessie.dessielib.storageapi.storage.container.StorageContainer;
import me.dessie.dessielib.storageapi.storage.container.hooks.DeleteHook;
import me.dessie.dessielib.storageapi.storage.container.hooks.RetrieveHook;
import me.dessie.dessielib.storageapi.storage.container.hooks.StoreHook;
import me.dessie.dessielib.storageapi.storage.container.settings.StorageSettings;
import org.bukkit.Bukkit;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class JSONContainer extends StorageContainer implements ArrayContainer {

    private final File json;
    private JsonObject object;

    private final Gson gson = new Gson().newBuilder().setPrettyPrinting().create();

    public JSONContainer(File jsonFile) {
        this(jsonFile, new StorageSettings());
    }

    public JSONContainer(File jsonFile, StorageSettings settings) {
        super(settings);
        this.json = jsonFile;

        try {
            if(!this.getJson().exists() && !this.getJson().createNewFile()) {
                Bukkit.getLogger().severe("Unable to create JSON file " + this.getJson().getName());
            }

            //If it's empty and exists, setup the basic object structure.
            if(this.getJson().exists() && this.getJson().length() == 0) {
                FileWriter writer = new FileWriter(this.getJson());
                writer.write("{}");
                writer.close();
            }

            this.object = JsonParser.parseReader(new FileReader(this.getJson())).getAsJsonObject();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public File getJson() {
        return json;
    }


    @Override
    protected StoreHook storeHook() {
        return (StoreHook) new StoreHook((path, data) -> {
            JsonObject current = this.getObject();

            String[] tree = path.split("\\.");
            for(int i = 0; i < tree.length; i++) {
                if(i == tree.length - 1) {
                    current.add(tree[i], this.getGson().toJsonTree(data));
                    break;
                }

                if(!current.has(tree[i])) {
                    current.add(tree[i], new JsonObject());
                }
                current = current.get(tree[i]).getAsJsonObject();
            }
        }).onComplete(() -> {
            try {
                this.write();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    @Override
    protected DeleteHook deleteHook() {
        return (DeleteHook) new DeleteHook(path -> {
            JsonObject current = this.getObject();
            String[] tree = path.split("\\.");

            for(int i = 0; i < tree.length; i++) {
                if(i == tree.length - 1) {
                    current.remove(tree[i]);
                    break;
                }
                current = current.get(tree[i]).getAsJsonObject();
            }
        }).onComplete(() -> {
            try {
                this.write();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    @Override
    protected RetrieveHook retrieveHook() {
        return new RetrieveHook(path -> {
            String[] tree = path.split("\\.");

            JsonObject current = this.getObject();
            for(int i = 0; i < tree.length; i++) {
                if(i == tree.length - 1) {
                    return this.getGson().fromJson(current.get(tree[i]), Object.class);
                }
                current = current.get(tree[i]).getAsJsonObject();
            }

            return null;
        });
    }

    private void write() throws IOException {
        FileWriter writer = new FileWriter(this.getJson());
        this.getGson().toJson(this.getObject(), writer);
        writer.close();
    }

    public JsonObject getObject() {
        return object;
    }
    public Gson getGson() {
        return gson;
    }
}