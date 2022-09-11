package me.dessie.dessielib.storageapi.format.flatfile.yaml;

import me.dessie.dessielib.storageapi.ContainerTestSpigot;
import me.dessie.dessielib.storageapi.data.BasicObject;
import me.dessie.dessielib.storageapi.data.ComplexObject;
import me.dessie.dessielib.storageapi.data.NestedComplexObject;
import me.dessie.dessielib.storageapi.format.flatfile.YAMLContainer;
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
public class YAMLContainerNestedComplexObjectTest extends ContainerTestSpigot<YAMLContainer> {

    public YAMLContainerNestedComplexObjectTest() throws URISyntaxException {
        super(ContainerType.YAML, "testyamlnestedcomplexobject.yml", "yamlnestedcomplexobjectcorrect.yml");
    }

    @Test
    @Order(1)
    public void testStoreNestedComplexObject() {
        List<CompletableFuture<Void>> futures = new ArrayList<>();

        futures.add(this.getContainer().store("nestedcomplexobject", new NestedComplexObject(new ComplexObject(new BasicObject(9, "I am basic"), 9.5), new BasicObject(7, "I am more basic."), true)));
        futures.add(this.getContainer().store("path.nestedcomplexobject", new NestedComplexObject(new ComplexObject(new BasicObject(-8, "Golly gosh, I am inside a complex object that is nested inside another object and that entire thing is a path!"), -17.3), new BasicObject(3, "I have less to say."), false)));

        CompletableFuture<Boolean> future = new CompletableFuture<>();
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[]{})).thenRun(() -> {
            future.complete(TestComparator.compareYaml(this.getTestFile(), this.getCorrectFile()));
        });

        Assertions.assertTrue(future.join());
    }

    @Test
    @Order(2)
    public void testRetrieveNestedComplexObject() {
        NestedComplexObject object1 = this.getContainer().retrieve(NestedComplexObject.class, "nestedcomplexobject");
        NestedComplexObject object2 = this.getContainer().retrieve(NestedComplexObject.class, "path.nestedcomplexobject");

        Assertions.assertEquals(new NestedComplexObject(new ComplexObject(new BasicObject(9, "I am basic"), 9.5), new BasicObject(7, "I am more basic."), true).toString(), object1.toString());
        Assertions.assertEquals(new NestedComplexObject(new ComplexObject(new BasicObject(-8, "Golly gosh, I am inside a complex object that is nested inside another object and that entire thing is a path!"), -17.3), new BasicObject(3, "I have less to say."), false).toString(), object2.toString());
    }

    @Test
    @Order(3)
    public void testDeleteNestedComplexObject() {
        List<CompletableFuture<Void>> futures = new ArrayList<>();

        futures.add(this.getContainer().delete("nestedcomplexobject.complexObject.basicObject"));
        futures.add(this.getContainer().delete("path"));

        CompletableFuture<Boolean> future = new CompletableFuture<>();

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[]{})).thenRun(() -> {
            boolean isPathDeleted = !this.getContainer().getKeys("").contains("path");
            boolean isObjectRemoved = this.getContainer().retrieve("nestedcomplexobject.complexObject.basicObject") == null;

            future.complete(isPathDeleted && isObjectRemoved);
        });

        Assertions.assertTrue(future.join());
    }

    @Override
    public YAMLContainer provideContainer() {
        return new YAMLContainer(this.getAPI(), this.getTestFile(), new StorageSettings().setUsesCache(false));
    }
}
