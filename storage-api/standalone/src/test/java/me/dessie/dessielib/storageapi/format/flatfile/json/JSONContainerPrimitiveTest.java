package me.dessie.dessielib.storageapi.format.flatfile.json;

import me.dessie.dessielib.storageapi.ContainerTestCore;
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
public class JSONContainerPrimitiveTest extends ContainerTestCore<JSONContainer> {
    public JSONContainerPrimitiveTest() throws URISyntaxException {
        super(ContainerType.JSON, "testjsonprimitive.json", "jsonprimitivecorrect.json");
    }

    @Test
    @Order(1)
    public void testStorePrimitive() {
        this.getContainer().set("integer", 5);
        this.getContainer().set("path.integer", -8);
        this.getContainer().set("boolean", true);
        this.getContainer().set("path.boolean", false);
        this.getContainer().set("string", "Hello");
        this.getContainer().set("path.string", "how are you");
        this.getContainer().set("double", 3.2);
        this.getContainer().set("path.double", -10.2);

        CompletableFuture<Boolean> future = new CompletableFuture<>();
        this.getContainer().flush().thenRun(() -> {
            future.complete(TestComparator.compareJson(getTestFile(), this.getCorrectFile()));
        });

        Assertions.assertTrue(future.join());
    }

    @Test
    @Order(2)
    public void testRetrievePrimitive() {
        Integer integer = this.getContainer().retrieve("integer");
        Integer pathInteger = this.getContainer().retrieve(Integer.class, "path.integer");

        Boolean bool = this.getContainer().retrieve("boolean");
        Boolean pathBoolean = this.getContainer().retrieve(Boolean.class, "path.boolean");

        String string = this.getContainer().retrieve("string");
        String pathString = this.getContainer().retrieve(String.class, "path.string");

        Double num = this.getContainer().retrieve("double");
        Double pathDouble = this.getContainer().retrieve(Double.class, "path.double");

        Assertions.assertEquals(integer, 5);
        Assertions.assertEquals(pathInteger, -8);
        Assertions.assertEquals(bool, true);
        Assertions.assertEquals(pathBoolean, false);
        Assertions.assertEquals(string, "Hello");
        Assertions.assertEquals(pathString, "how are you");
        Assertions.assertEquals(num, 3.2);
        Assertions.assertEquals(pathDouble, -10.2);
    }

    @Test
    @Order(3)
    public void testDeletePrimitive() {
        List<CompletableFuture<Void>> futures = new ArrayList<>();

        futures.add(this.getContainer().delete("integer"));
        futures.add(this.getContainer().delete("path"));

        CompletableFuture<Boolean> future = new CompletableFuture<>();
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[]{})).thenRun(() -> {
            boolean isPathDeleted = !this.getContainer().getKeys("").contains("path");
            boolean isObjectRemoved = this.getContainer().retrieve("integer") == null;

            future.complete(isPathDeleted && isObjectRemoved);
        });

        Assertions.assertTrue(future.join());
    }

    @Override
    public JSONContainer provideContainer() {
        return new JSONContainer(this.getAPI(), this.getTestFile(), new StorageSettings().setUsesCache(false));
    }
}
