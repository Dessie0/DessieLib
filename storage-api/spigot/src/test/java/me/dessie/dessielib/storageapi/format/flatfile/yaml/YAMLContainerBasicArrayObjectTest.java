package me.dessie.dessielib.storageapi.format.flatfile.yaml;

import me.dessie.dessielib.storageapi.ContainerTestSpigot;
import me.dessie.dessielib.storageapi.data.BasicArrayObject;
import me.dessie.dessielib.storageapi.data.BasicObject;
import me.dessie.dessielib.storageapi.format.flatfile.YAMLContainer;
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
public class YAMLContainerBasicArrayObjectTest extends ContainerTestSpigot<YAMLContainer> {

    public YAMLContainerBasicArrayObjectTest() throws URISyntaxException {
        super(ContainerType.YAML, "testyamlbasicarrayobject.yml", "yamlbasicarrayobjectcorrect.yml");
    }

    @Test
    @Order(1)
    public void testStoreBasicArrayObject() {
        this.getContainer().set("basicobjects", new BasicArrayObject(Arrays.asList(
                new BasicObject(2, "Hello"),
                new BasicObject(8, "Hello again"),
                new BasicObject(10, "Hey!"))));

        this.getContainer().set("path.basicobjects", new BasicArrayObject(Arrays.asList(
                new BasicObject(-9274, "A nested String"),
                new BasicObject(67, "A second nested String"),
                new BasicObject(0, "yAY!"))));

        CompletableFuture<Boolean> future = new CompletableFuture<>();
        this.getContainer().flush().thenRun(() -> {
            future.complete(TestComparator.compareYaml(this.getTestFile(), this.getCorrectFile()));
        });

        Assertions.assertTrue(future.join());
    }

    @Test
    @Order(2)
    public void testRetrieveBasicArrayObject() {
        BasicArrayObject object1 = this.getContainer().retrieve(BasicArrayObject.class, "basicobjects");
        BasicArrayObject object2 = this.getContainer().retrieve(BasicArrayObject.class, "path.basicobjects");

        Assertions.assertEquals(new BasicArrayObject(Arrays.asList(
                new BasicObject(2, "Hello"),
                new BasicObject(8, "Hello again"),
                new BasicObject(10, "Hey!"))).toString(), object1.toString());

        Assertions.assertEquals(new BasicArrayObject(Arrays.asList(
                new BasicObject(-9274, "A nested String"),
                new BasicObject(67, "A second nested String"),
                new BasicObject(0, "yAY!"))).toString(), object2.toString());
    }

    @Test
    @Order(3)
    public void testDeleteBasicArrayObject() {
        CompletableFuture<List<BasicObject>> removeFuture = this.getContainer().removeFromList(BasicObject.class, "basicobjects.basicList", new BasicObject(2, "Hello"));
        this.getContainer().remove("path");

        CompletableFuture<Boolean> future = new CompletableFuture<>();
        removeFuture.thenRun(() -> {
            this.getContainer().flush().thenRun(() -> {
                boolean isPathDeleted = !this.getContainer().getKeys("").contains("path");
                boolean isObjectRemovedFromList = this.getContainer().retrieveList(BasicObject.class, "basicobjects.basicList").size() == 2;

                future.complete(isPathDeleted && isObjectRemovedFromList);
            });
        });

        Assertions.assertTrue(future.join());
    }

    @Override
    public YAMLContainer provideContainer() {
        return new YAMLContainer(this.getAPI(), this.getTestFile(), new StorageSettings().setUsesCache(false));
    }
}
