package me.dessie.dessielib.storageapi;

import me.dessie.dessielib.storageapi.api.IStorageAPI;
import me.dessie.dessielib.storageapi.container.StorageContainer;
import me.dessie.dessielib.storageapi.data.*;
import me.dessie.dessielib.storageapi.helpers.ContainerParameterResolver;
import me.dessie.dessielib.storageapi.helpers.TestComparator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIf;
import org.junit.jupiter.api.extension.ExtendWith;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.net.URISyntaxException;

@ExtendWith(ContainerParameterResolver.class)
public abstract class ContainerTest<T extends StorageContainer> implements IStorageProvider<T> {

    private final File testFileName;
    private final File correctFileName;
    private final IStorageAPI api;
    private final T container;

    public ContainerTest(ContainerType type, String testFileName, String correctFileName) throws URISyntaxException {
        this.api = this.provide();

        if (this.getAPI() == null) {
            throw new IllegalArgumentException("StorageAPI cannot be null. Please override the provide() method.");
        }

        this.getAPI().registerAnnotatedDecomposer(BasicObject.class);
        this.getAPI().registerAnnotatedDecomposer(BasicArrayObject.class);
        this.getAPI().registerAnnotatedDecomposer(ComplexObject.class);
        this.getAPI().registerAnnotatedDecomposer(ComplexArrayObject.class);
        this.getAPI().registerAnnotatedDecomposer(NestedComplexObject.class);
        this.getAPI().registerAnnotatedDecomposer(NestedComplexArrayObject.class);

        if(type == null) {
            this.testFileName = null;
            this.correctFileName = null;
            this.container = null;
            return;
        }

        this.testFileName = new File("tests/" + type.getType() + "/" + testFileName);
        this.correctFileName = new File(ContainerTest.class.getResource("/format/flatfile/" + type.getType() + "/correct/" + correctFileName).toURI());
        this.container = this.provideContainer();

        if(this.getTestFile().exists()) {
            try {
                PrintWriter writer = new PrintWriter(this.getTestFile());
                writer.print("");
                writer.close();
            } catch (FileNotFoundException e) { e.printStackTrace(); }
        }
    }

    @Test
    @Order(4)
    @DisabledIf("isSuperClass")
    public void validate() {
        if(this.getTestFile() == null) {
            Assertions.assertTrue(true);
            return;
        }

        Assertions.assertTrue(TestComparator.isValid(this.getTestFile()));
    }

    //Makes the validate test only run for sub-classes of this class, and not for this class.
    private boolean isSuperClass() {
        return this.getClass().getSimpleName().equals("ContainerTest");
    }

    public File getTestFile() {
        return testFileName;
    }
    public File getCorrectFile() {
        return correctFileName;
    }
    public IStorageAPI getAPI() {
        return api;
    }

    public T getContainer() {
        return container;
    }

    public enum ContainerType {
        JSON("json"),
        YAML("yaml");

        private final String type;
        ContainerType(String type) {
            this.type = type;
        }

        public String getType() {
            return type;
        }
    }
}
