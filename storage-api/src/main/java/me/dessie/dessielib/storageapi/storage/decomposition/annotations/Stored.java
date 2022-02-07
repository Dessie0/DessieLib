package me.dessie.dessielib.storageapi.storage.decomposition.annotations;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Stored {
    boolean recompose() default true;
    String storeAs() default "";
}