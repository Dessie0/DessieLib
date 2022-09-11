package me.dessie.dessielib.storageapi.data;

import me.dessie.dessielib.annotations.storageapi.RecomposeConstructor;
import me.dessie.dessielib.annotations.storageapi.Stored;

import java.util.Objects;

public class ComplexObject {

    @Stored
    private final BasicObject basicObject;

    @Stored
    private final double num;

    @RecomposeConstructor
    public ComplexObject(BasicObject basicObject, double num) {
        this.basicObject = basicObject;
        this.num = num;
    }

    public BasicObject getBasicObject() {
        return basicObject;
    }

    public double getNum() {
        return num;
    }

    @Override
    public String toString() {
        return "ComplexObject{" +
                "basicObject=" + basicObject +
                ", num=" + num +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ComplexObject that = (ComplexObject) o;
        return Double.compare(that.num, num) == 0 && Objects.equals(basicObject, that.basicObject);
    }

    @Override
    public int hashCode() {
        return Objects.hash(basicObject, num);
    }
}
