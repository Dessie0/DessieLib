package me.dessie.dessielib.storageapi.format.flatfile.yaml;

import me.dessie.dessielib.storageapi.ContainerTestSpigot;
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
public class YAMLContainerPrimitiveArrayTest extends ContainerTestSpigot<YAMLContainer> {
    public YAMLContainerPrimitiveArrayTest() throws URISyntaxException {
        super(ContainerType.YAML, "testyamlprimitivearray.yml", "yamlprimitivearraycorrect.yml");
    }

    @Test
    @Order(1)
    public void testStorePrimitiveArray() {
        this.getContainer().set("integers", Arrays.asList(4, 2, 7));
        this.getContainer().set("path.integers", Arrays.asList(6, 9, -10));
        this.getContainer().set("booleans", Arrays.asList(true, false, false));
        this.getContainer().set("path.booleans", Arrays.asList(false, true));
        this.getContainer().set("strings", Arrays.asList("Hello", "how", "are", "you"));
        this.getContainer().set("path.strings", List.of("well"));
        this.getContainer().set("doubles", Arrays.asList(753.3, 90.3, -87.1));
        this.getContainer().set("path.doubles", Arrays.asList(0.0, 4.3));

        CompletableFuture<Boolean> future = new CompletableFuture<>();
        this.getContainer().flush().thenRun(() -> {
            future.complete(TestComparator.compareYaml(this.getTestFile(), this.getCorrectFile()));
        });

        Assertions.assertTrue(future.join());
    }

    @Test
    @Order(2)
    public void testRetrievePrimitiveArray() {
        List<Integer> integers = this.getContainer().retrieveList(Integer.class, "integers");
        List<Integer> pathIntegers = this.getContainer().retrieveList(Integer.class, "path.integers");
        List<Boolean> booleans = this.getContainer().retrieveList(Boolean.class, "booleans");
        List<Boolean> pathBooleans = this.getContainer().retrieveList(Boolean.class, "path.booleans");
        List<String> strings = this.getContainer().retrieveList(String.class, "strings");
        List<String> pathStrings = this.getContainer().retrieveList(String.class, "path.strings");
        List<Double> doubles = this.getContainer().retrieveList(Double.class, "doubles");
        List<Double> pathDoubles = this.getContainer().retrieveList(Double.class, "path.doubles");

        Assertions.assertEquals(integers, Arrays.asList(4, 2, 7));
        Assertions.assertEquals(pathIntegers, Arrays.asList(6, 9, -10));
        Assertions.assertEquals(booleans, Arrays.asList(true, false, false));
        Assertions.assertEquals(pathBooleans, Arrays.asList(false, true));
        Assertions.assertEquals(strings, Arrays.asList("Hello", "how", "are", "you"));
        Assertions.assertEquals(pathStrings, List.of("well"));
        Assertions.assertEquals(doubles, Arrays.asList(753.3, 90.3, -87.1));
        Assertions.assertEquals(pathDoubles, Arrays.asList(0.0, 4.3));
    }

    @Test
    @Order(3)
    public void testDeletePrimitiveArray() {
        CompletableFuture<List<Integer>> intRemove = this.getContainer().removeFromList(Integer.class, "integers", 4);
        CompletableFuture<List<Boolean>> boolRemove = this.getContainer().removeFromList(Boolean.class, "booleans", false);
        CompletableFuture<List<String>> stringRemove = this.getContainer().removeFromList(String.class, "strings", "are");
        CompletableFuture<List<Double>> doubleRemove = this.getContainer().removeFromList(Double.class, "doubles", -87.1);

        this.getContainer().remove("path");

        CompletableFuture<Boolean> future = new CompletableFuture<>();
        CompletableFuture.allOf(intRemove, boolRemove, stringRemove, doubleRemove).thenRun(() -> {
            this.getContainer().flush().thenRun(() -> {
                boolean isPathDeleted = !this.getContainer().getKeys("").contains("path");
                boolean isObjectRemovedFromList1 = this.getContainer().retrieveList(Integer.class, "integers").size() == 2;
                boolean isObjectRemovedFromList2 = this.getContainer().retrieveList(Boolean.class, "booleans").size() == 1;
                boolean isObjectRemovedFromList3 = this.getContainer().retrieveList(String.class, "strings").size() == 3;
                boolean isObjectRemovedFromList4 = this.getContainer().retrieveList(Double.class, "doubles").size() == 2;

                future.complete(isPathDeleted && isObjectRemovedFromList1 && isObjectRemovedFromList2 && isObjectRemovedFromList3 && isObjectRemovedFromList4);
            });
        });

        Assertions.assertTrue(future.join());
    }

    @Override
    public YAMLContainer provideContainer() {
        return new YAMLContainer(this.getAPI(), this.getTestFile(), new StorageSettings().setUsesCache(false));
    }
}
