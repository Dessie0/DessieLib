package me.dessie.dessielib.storageapi.format.flatfile.json;

import me.dessie.dessielib.storageapi.ContainerTestCore;
import me.dessie.dessielib.storageapi.data.BasicObject;
import me.dessie.dessielib.storageapi.data.ComplexArrayObject;
import me.dessie.dessielib.storageapi.data.ComplexObject;
import me.dessie.dessielib.storageapi.format.flatfile.JSONContainer;
import me.dessie.dessielib.storageapi.helpers.TestComparator;
import me.dessie.dessielib.storageapi.settings.StorageSettings;
import org.junit.jupiter.api.*;

import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

//Pretty sure this class is integration testing and not unit testing, but I'm stupid and this is what we're doing so...
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class JSONContainerComplexArrayObjectTest extends ContainerTestCore<JSONContainer> {
    public JSONContainerComplexArrayObjectTest() throws URISyntaxException {
        super(ContainerType.JSON, "testjsoncomplexarrayobject.json", "jsoncomplexarrayobjectcorrect.json");
    }

    @Test
    @Order(1)
    public void testStoreComplexArrayObject() {
        JSONContainer container = new JSONContainer(this.getAPI(), this.getTestFile(), new StorageSettings().setUsesCache(false));

        container.set("complexobject", new ComplexArrayObject(Arrays.asList(
                new ComplexObject(new BasicObject(1, "First object"), 2.1),
                new ComplexObject(new BasicObject(19, "Second object"), 0.3),
                new ComplexObject(new BasicObject(-76, "Third Object"), -6.7)), "HellO!"));

        container.set("path.complexobject", new ComplexArrayObject(Arrays.asList(
                new ComplexObject(new BasicObject(7, "A pathed first object"), 74.3),
                new ComplexObject(new BasicObject(-201, "A pathed second object"), 1.0),
                new ComplexObject(new BasicObject(-34, "A pathed third Object"), -928.45)), "Another pathed string!"));

        CompletableFuture<Boolean> future = new CompletableFuture<>();
        container.flush().thenRun(() -> {
            future.complete(TestComparator.compareJson(getTestFile(), this.getCorrectFile()));
        });

        Assertions.assertTrue(future.join());
    }

    @Test
    @Order(2)
    public void testRetrieveComplexArrayObject() {
        JSONContainer container = new JSONContainer(this.getAPI(), this.getTestFile(), new StorageSettings().setUsesCache(false));

        ComplexArrayObject object1 = container.retrieve(ComplexArrayObject.class, "complexobject");
        ComplexArrayObject object2 = container.retrieve(ComplexArrayObject.class, "path.complexobject");

        Assertions.assertEquals(new ComplexArrayObject(Arrays.asList(
                new ComplexObject(new BasicObject(1, "First object"), 2.1),
                new ComplexObject(new BasicObject(19, "Second object"), 0.3),
                new ComplexObject(new BasicObject(-76, "Third Object"), -6.7)), "HellO!").toString(), object1.toString());

        Assertions.assertEquals(new ComplexArrayObject(Arrays.asList(
                new ComplexObject(new BasicObject(7, "A pathed first object"), 74.3),
                new ComplexObject(new BasicObject(-201, "A pathed second object"), 1.0),
                new ComplexObject(new BasicObject(-34, "A pathed third Object"), -928.45)), "Another pathed string!").toString(), object2.toString());
    }

    @Test
    @Order(3)
    public void testDeleteComplexArrayObject() {
        JSONContainer container = new JSONContainer(this.getAPI(), this.getTestFile(), new StorageSettings().setUsesCache(false));

        CompletableFuture<List<ComplexObject>> removeFuture = container.removeFromList(ComplexObject.class, "complexobject.complexList", new ComplexObject(new BasicObject(1, "First object"), 2.1));
        container.remove("complexobject.str");
        container.remove("path");

        CompletableFuture<Boolean> future = new CompletableFuture<>();
        removeFuture.thenRun(() -> {
            container.flush().thenRun(() -> {
                boolean isPathDeleted = !container.getKeys("").contains("path");
                boolean isStringDeleted = !container.getKeys("complexobject").contains("str");
                boolean isObjectRemovedFromList = container.retrieveList(ComplexObject.class, "complexobject.complexList").size() == 2;

                future.complete(isPathDeleted && isObjectRemovedFromList && isStringDeleted);
            });
        });

        Assertions.assertTrue(future.join());
    }

    @Override
    public JSONContainer provideContainer() {
        return new JSONContainer(this.getAPI(), this.getTestFile(), new StorageSettings().setUsesCache(false));
    }
}
