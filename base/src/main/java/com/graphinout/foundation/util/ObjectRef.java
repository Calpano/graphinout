package com.graphinout.foundation.util;

public class ObjectRef<T> {

    public T value;

    public ObjectRef(T value) {
        this.value = value;
    }

    public static <T> ObjectRef<T> objectRef(T value) {
        return new ObjectRef<>(value);
    }

    public static <T> ObjectRef<T> objectRef() {
        return new ObjectRef<>(null);
    }

}
