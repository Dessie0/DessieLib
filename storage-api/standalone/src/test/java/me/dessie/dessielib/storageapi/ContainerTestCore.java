package me.dessie.dessielib.storageapi;

import me.dessie.dessielib.storageapi.api.IStorageAPI;
import me.dessie.dessielib.storageapi.container.StorageContainer;

import java.net.URISyntaxException;

public abstract class ContainerTestCore<T extends StorageContainer> extends ContainerTest<T> {
    public ContainerTestCore(ContainerType type, String testFileName, String correctFileName) throws URISyntaxException {
        super(type, testFileName, correctFileName);
    }

    @Override
    public IStorageAPI provide() {
        return CoreStorageAPI.register();
    }
}
