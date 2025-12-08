package com.graphinout.foundation.pure.functional;

public class TypedArray<T> {

    private final Object[] array;

    public TypedArray(T[] array) {
        this.array = array;
    }

    public static <T> TypedArray<T> of(T[] array) {
        return new TypedArray<>(array);
    }

    public T get(int index) {
        //noinspection unchecked
        return (T) array[index];
    }

    public int length() {
        return array.length;
    }

    public void set(int index, T value) {
        array[index] = value;
    }

    public T[] toArray() {
        //noinspection unchecked
        return (T[]) array;
    }

}
