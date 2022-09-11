package me.dessie.dessielib.storageapi.data;

import me.dessie.dessielib.annotations.storageapi.RecomposeConstructor;
import me.dessie.dessielib.annotations.storageapi.StoredList;

import java.util.List;

public class BasicArrayObject {

    @StoredList(type = BasicObject.class)
    private final List<BasicObject> basicList;

    @RecomposeConstructor
    public BasicArrayObject(List<BasicObject> basicList) {
        this.basicList = basicList;
    }

    public List<BasicObject> getBasicList() {
        return basicList;
    }

    @Override
    public String toString() {
        return "BasicArrayObject{" +
                "basicObjects=" + basicList +
                '}';
    }
}
