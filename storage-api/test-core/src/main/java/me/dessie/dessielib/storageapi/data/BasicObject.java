package me.dessie.dessielib.storageapi.data;

import me.dessie.dessielib.annotations.storageapi.RecomposeConstructor;
import me.dessie.dessielib.annotations.storageapi.Stored;

import java.util.Objects;

public class BasicObject {

    @Stored
    private final int num;

    @Stored
    private final String str;

    @RecomposeConstructor
    public BasicObject(int num, String str) {
        this.num = num;
        this.str = str;
    }

    public int getNum() {
        return num;
    }

    public String getStr() {
        return str;
    }

    @Override
    public String toString() {
        return "BasicObject{" +
                "num=" + num +
                ", str='" + str + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BasicObject that = (BasicObject) o;
        return num == that.num && Objects.equals(str, that.str);
    }

    @Override
    public int hashCode() {
        return Objects.hash(num, str);
    }
}
