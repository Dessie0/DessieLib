package me.dessie.dessielib.storageapi;

import be.seeseemelk.mockbukkit.MockBukkit;
import me.dessie.dessielib.storageapi.api.IStorageAPI;
import me.dessie.dessielib.storageapi.container.StorageContainer;

import java.net.URISyntaxException;

public abstract class ContainerTestSpigot<T extends StorageContainer> extends ContainerTest<T> {

    private static boolean isMocking = false;

    public ContainerTestSpigot(ContainerType type, String testFileName, String correctFileName) throws URISyntaxException {
        super(type, testFileName, correctFileName);
    }

    @Override
    public IStorageAPI provide() {
        if(!isMocking) {
            MockBukkit.mock();
            isMocking = true;
        }

        return SpigotStorageAPI.register(MockBukkit.load(TestPlugin.class), false);
    }
}
