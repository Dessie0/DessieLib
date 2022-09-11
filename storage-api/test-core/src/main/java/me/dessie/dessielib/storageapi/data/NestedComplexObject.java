package me.dessie.dessielib.storageapi.data;

import me.dessie.dessielib.annotations.storageapi.RecomposeConstructor;
import me.dessie.dessielib.annotations.storageapi.Stored;

import java.util.Objects;

public class NestedComplexObject {

    @Stored
    private final ComplexObject complexObject;

    @Stored
    private final BasicObject basicObject;

    @Stored
    private final boolean bool;

    @RecomposeConstructor
    public NestedComplexObject(ComplexObject complexObject, BasicObject basicObject, boolean bool) {
        this.complexObject = complexObject;
        this.basicObject = basicObject;
        this.bool = bool;
    }

    public BasicObject getBasicObject() {
        return basicObject;
    }

    public ComplexObject getComplexObject() {
        return complexObject;
    }

    public boolean isBool() {
        return bool;
    }

    @Override
    public String toString() {
        return "NestedComplexObject{" +
                "complexObject=" + complexObject +
                ", basicObject=" + basicObject +
                ", bool=" + bool +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NestedComplexObject that = (NestedComplexObject) o;
        return bool == that.bool && Objects.equals(complexObject, that.complexObject) && Objects.equals(basicObject, that.basicObject);
    }

    @Override
    public int hashCode() {
        return Objects.hash(complexObject, basicObject, bool);
    }
}
