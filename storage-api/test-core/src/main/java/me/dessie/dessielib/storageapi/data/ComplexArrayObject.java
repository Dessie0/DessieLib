package me.dessie.dessielib.storageapi.data;

import me.dessie.dessielib.annotations.storageapi.RecomposeConstructor;
import me.dessie.dessielib.annotations.storageapi.Stored;
import me.dessie.dessielib.annotations.storageapi.StoredList;

import java.util.List;
import java.util.Objects;

public class ComplexArrayObject {

    @StoredList(type = ComplexObject.class)
    private final List<ComplexObject> complexList;

    @Stored
    private final String str;

    @RecomposeConstructor
    public ComplexArrayObject(List<ComplexObject> complexList, String str) {
        this.complexList = complexList;
        this.str = str;
    }

    public List<ComplexObject> getComplexList() {
        return complexList;
    }

    public String getStr() {
        return str;
    }

    @Override
    public String toString() {
        return "ComplexArrayObject{" +
                "complexList=" + complexList +
                ", str='" + str + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ComplexArrayObject that = (ComplexArrayObject) o;
        return Objects.equals(complexList, that.complexList) && Objects.equals(str, that.str);
    }

    @Override
    public int hashCode() {
        return Objects.hash(complexList, str);
    }
}
