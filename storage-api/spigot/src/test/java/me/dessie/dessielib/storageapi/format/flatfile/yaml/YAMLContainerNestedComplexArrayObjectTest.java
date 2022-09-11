package me.dessie.dessielib.storageapi.format.flatfile.yaml;

import me.dessie.dessielib.storageapi.ContainerTestSpigot;
import me.dessie.dessielib.storageapi.data.*;
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
public class YAMLContainerNestedComplexArrayObjectTest extends ContainerTestSpigot<YAMLContainer> {

    private final NestedComplexArrayObject testingObject;

    public YAMLContainerNestedComplexArrayObjectTest() throws URISyntaxException {
        super(ContainerType.YAML, "testyamlnestedcomplexarrayobject.yml", "yamlnestedcomplexarrayobjectcorrect.yml");

        this.testingObject = new NestedComplexArrayObject(
                Arrays.asList(
                        new NestedComplexObject(new ComplexObject(new BasicObject(1, "I am basic"), -98.24), new BasicObject(15, "I am more basic."), true),
                        new NestedComplexObject(new ComplexObject(new BasicObject(-8, "I am basicer"), 23.76), new BasicObject(19, "I am more basicer."), false),
                        new NestedComplexObject(new ComplexObject(new BasicObject(5532, "I am basicest"), 6937.23), new BasicObject(534, "I am more basicest."), true)),

                Arrays.asList(
                        new ComplexArrayObject(Arrays.asList(new ComplexObject(new BasicObject(90, "First basic"), 98.3), new ComplexObject(new BasicObject(45, "Second basic"), -9.4),
                                new ComplexObject(new BasicObject(90, "Third basic"), 7.5), new ComplexObject(new BasicObject(45, "Fourth basic"), -293),
                                new ComplexObject(new BasicObject(90, "Fifth basic"), .75), new ComplexObject(new BasicObject(78, "Sixth basic"), 9.2)), "Hola!"),

                        new ComplexArrayObject(Arrays.asList(new ComplexObject(new BasicObject(6, "Seventh basic"), 32), new ComplexObject(new BasicObject(98, "Eighth basic"), -9.7),
                                new ComplexObject(new BasicObject(14, "Ninth basic"), -9.341), new ComplexObject(new BasicObject(34, "Tenth basic"), -902),
                                new ComplexObject(new BasicObject(84, "Eleventh basic"), 0.8), new ComplexObject(new BasicObject(1009, "Twelfth basic"), 8.4)), "Hello!"),

                        new ComplexArrayObject(Arrays.asList(new ComplexObject(new BasicObject(3, "Thirteenth basic"), 108.5), new ComplexObject(new BasicObject(1234, "Fourteenth basic"), -3.4),
                                new ComplexObject(new BasicObject(-9, "Fifteenth basic"), -7.5), new ComplexObject(new BasicObject(845, "Sixteenth basic"), -87),
                                new ComplexObject(new BasicObject(0, "Seventeenth basic"), 89.2), new ComplexObject(new BasicObject(11, "Eighteenth basic"), 9.3)), "Bonjour!")), "247");
    }

    @Test
    @Order(1)
    public void testStoreNestedComplexArrayObject() {
        this.getContainer().set("nestedcomplexobjects", this.getTestingObject());
        this.getContainer().set("path.nestedcomplexobjects", this.getTestingObject());

        CompletableFuture<Boolean> future = new CompletableFuture<>();
        this.getContainer().flush().thenRun(() -> {
            future.complete(TestComparator.compareYaml(this.getTestFile(), this.getCorrectFile()));
        });

        Assertions.assertTrue(future.join());
    }

    @Test
    @Order(2)
    public void testRetrieveNestedComplexArrayObject() {
        NestedComplexArrayObject object1 = this.getContainer().retrieve(NestedComplexArrayObject.class, "nestedcomplexobjects");
        NestedComplexArrayObject object2 = this.getContainer().retrieve(NestedComplexArrayObject.class, "path.nestedcomplexobjects");

        Assertions.assertEquals(this.getTestingObject().toString(), object1.toString());
        Assertions.assertEquals(this.getTestingObject().toString(), object2.toString());
    }

    @Test
    @Order(3)
    public void testDeleteNestedComplexArrayObject() {
        CompletableFuture<List<NestedComplexObject>> removeNestedComplex = this.getContainer().removeFromList(NestedComplexObject.class, "nestedcomplexobjects.nestedComplexObjects", new NestedComplexObject(new ComplexObject(new BasicObject(5532, "I am basicest"), 6937.23), new BasicObject(534, "I am more basicest."), true));
        CompletableFuture<List<ComplexArrayObject>> removeComplexArray = this.getContainer().removeFromList(ComplexArrayObject.class, "nestedcomplexobjects.complexArrayObjects", new ComplexArrayObject(Arrays.asList(new ComplexObject(new BasicObject(6, "Seventh basic"), 32), new ComplexObject(new BasicObject(98, "Eighth basic"), -9.7),
                new ComplexObject(new BasicObject(14, "Ninth basic"), -9.341), new ComplexObject(new BasicObject(34, "Tenth basic"), -902),
                new ComplexObject(new BasicObject(84, "Eleventh basic"), 0.8), new ComplexObject(new BasicObject(1009, "Twelfth basic"), 8.4)), "Hello!"));

        this.getContainer().remove("nestedcomplexobjects.str");
        this.getContainer().remove("path");

        CompletableFuture<Boolean> future = new CompletableFuture<>();
        CompletableFuture.allOf(removeComplexArray, removeNestedComplex).thenRun(() -> {
            this.getContainer().flush().thenRun(() -> {
                boolean isPathDeleted = !this.getContainer().getKeys("").contains("path");
                boolean isStringDeleted = !this.getContainer().getKeys("nestedcomplexobjects").contains("str");
                boolean isObjectRemovedFromList1 = this.getContainer().retrieveList(NestedComplexObject.class, "nestedcomplexobjects.nestedComplexObjects").size() == 2;
                boolean isObjectRemovedFromList2 = this.getContainer().retrieveList(ComplexArrayObject.class, "nestedcomplexobjects.complexArrayObjects").size() == 2;

                future.complete(isPathDeleted && isObjectRemovedFromList1 && isObjectRemovedFromList2 && isStringDeleted);
            });
        });

        Assertions.assertTrue(future.join());
    }

    @Override
    public YAMLContainer provideContainer() {
        return new YAMLContainer(this.getAPI(), this.getTestFile(), new StorageSettings().setUsesCache(false));
    }

    public NestedComplexArrayObject getTestingObject() {
        return testingObject;
    }
}
