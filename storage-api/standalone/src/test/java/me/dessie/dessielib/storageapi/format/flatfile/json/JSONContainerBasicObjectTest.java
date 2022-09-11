package me.dessie.dessielib.storageapi.format.flatfile.json;

import me.dessie.dessielib.storageapi.ContainerTestCore;
import me.dessie.dessielib.storageapi.data.BasicObject;
import me.dessie.dessielib.storageapi.format.flatfile.JSONContainer;
import me.dessie.dessielib.storageapi.helpers.TestComparator;
import me.dessie.dessielib.storageapi.settings.StorageSettings;
import org.junit.jupiter.api.*;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

//Pretty sure this class is integration testing and not unit testing, but I'm stupid and this is what we're doing so...
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class JSONContainerBasicObjectTest extends ContainerTestCore<JSONContainer> {

    public JSONContainerBasicObjectTest() throws URISyntaxException {
        super(ContainerType.JSON, "testjsonbasicobject.json", "jsonbasicobjectcorrect.json");
    }

    @Test
    @Order(1)
    public void testStoreBasicObject() {
        List<CompletableFuture<Void>> futures = new ArrayList<>();

        futures.add(this.getContainer().store("basicobject", new BasicObject(5, "Hello")));
        futures.add(this.getContainer().store("path.basicobject", new BasicObject(19, "another string!")));

        CompletableFuture<Boolean> future = new CompletableFuture<>();
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[]{})).thenRun(() -> {
            future.complete(TestComparator.compareJson(this.getTestFile(), this.getCorrectFile()));
        });

        Assertions.assertTrue(future.join());
    }

    @Test
    @Order(2)
    public void testRetrieveBasicObject() {
        BasicObject object1 = this.getContainer().retrieve(BasicObject.class, "basicobject");
        BasicObject object2 = this.getContainer().retrieve(BasicObject.class, "path.basicobject");

        Assertions.assertEquals(new BasicObject(5, "Hello").toString(), object1.toString());
        Assertions.assertEquals(new BasicObject(19, "another string!").toString(), object2.toString());
    }

    @Test
    @Order(3)
    public void testDeleteBasicObject() {
        List<CompletableFuture<Void>> futures = new ArrayList<>();

        futures.add(this.getContainer().delete("basicobject.num"));
        futures.add(this.getContainer().delete("path"));

        CompletableFuture<Boolean> future = new CompletableFuture<>();
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[]{})).thenRun(() -> {
            boolean isPathDeleted = !this.getContainer().getKeys("").contains("path");
            boolean isObjectRemoved = this.getContainer().retrieve("basicobject.num") == null;

            future.complete(isPathDeleted && isObjectRemoved);
        });

        Assertions.assertTrue(future.join());
    }

    @Override
    public JSONContainer provideContainer() {
        return new JSONContainer(this.getAPI(), this.getTestFile(), new StorageSettings().setUsesCache(false));
    }
}
