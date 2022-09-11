package me.dessie.dessielib.storageapi.format.flatfile.json;

import me.dessie.dessielib.storageapi.ContainerTestCore;
import me.dessie.dessielib.storageapi.data.BasicObject;
import me.dessie.dessielib.storageapi.data.ComplexObject;
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
public class JSONContainerComplexObjectTest extends ContainerTestCore<JSONContainer> {

    public JSONContainerComplexObjectTest() throws URISyntaxException {
        super(ContainerType.JSON, "testjsoncomplexobject.json", "jsoncomplexobjectcorrect.json");
    }

    @Test
    @Order(1)
    public void testStoreComplexObject() {
        List<CompletableFuture<Void>> futures = new ArrayList<>();

        futures.add(this.getContainer().store("complexobject", new ComplexObject(new BasicObject(4, "I am a basic object"), 6.7)));
        futures.add(this.getContainer().store("path.complexobject", new ComplexObject(new BasicObject(8, "Another basic object"), 3.984)));

        CompletableFuture<Boolean> future = new CompletableFuture<>();
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[]{})).thenRun(() -> {
            future.complete(TestComparator.compareJson(this.getTestFile(), this.getCorrectFile()));
        });

        Assertions.assertTrue(future.join());
    }

    @Test
    @Order(2)
    public void testRetrieveComplexObject() {
        ComplexObject object1 = this.getContainer().retrieve(ComplexObject.class, "complexobject");
        ComplexObject object2 = this.getContainer().retrieve(ComplexObject.class, "path.complexobject");

        Assertions.assertEquals(new ComplexObject(new BasicObject(4, "I am a basic object"), 6.7).toString(), object1.toString());
        Assertions.assertEquals(new ComplexObject(new BasicObject(8, "Another basic object"), 3.984).toString(), object2.toString());
    }

    @Test
    @Order(3)
    public void testDeleteComplexObject() {
        List<CompletableFuture<Void>> futures = new ArrayList<>();

        futures.add(this.getContainer().delete("complexobject.basicObject"));
        futures.add(this.getContainer().delete("path"));

        CompletableFuture<Boolean> future = new CompletableFuture<>();
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[]{})).thenRun(() -> {
            boolean isPathDeleted = !this.getContainer().getKeys("").contains("path");
            boolean isObjectRemoved = this.getContainer().retrieve("basicobject.basicObject") == null;

            future.complete(isPathDeleted && isObjectRemoved);
        });

        Assertions.assertTrue(future.join());
    }

    @Override
    public JSONContainer provideContainer() {
        return new JSONContainer(this.getAPI(), this.getTestFile(), new StorageSettings().setUsesCache(false));
    }
}
