package me.dessie.dessielib.storageapi.data;

import me.dessie.dessielib.annotations.storageapi.RecomposeConstructor;
import me.dessie.dessielib.annotations.storageapi.Stored;
import me.dessie.dessielib.annotations.storageapi.StoredList;

import java.util.List;

public class NestedComplexArrayObject {

    @StoredList(type = NestedComplexObject.class)
    private final List<NestedComplexObject> nestedComplexObjects;

    @StoredList(type = ComplexArrayObject.class)
    private final List<ComplexArrayObject> complexArrayObjects;

    @Stored
    private final String str;

    @RecomposeConstructor
    public NestedComplexArrayObject(List<NestedComplexObject> nestedComplexObjects, List<ComplexArrayObject> complexArrayObjects, String str) {
        this.complexArrayObjects = complexArrayObjects;
        this.nestedComplexObjects = nestedComplexObjects;
        this.str = str;
    }

    public List<ComplexArrayObject> getComplexArrayObjects() {
        return complexArrayObjects;
    }
    public List<NestedComplexObject> getNestedComplexObjects() {
        return nestedComplexObjects;
    }

    public String getStr() {
        return str;
    }

    @Override
    public String toString() {
        return "NestedComplexArrayObject{" +
                "complexArrayObjects=" + complexArrayObjects +
                ", nestedComplexObjects=" + nestedComplexObjects +
                ", str='" + str + '\'' +
                '}';
    }
}
