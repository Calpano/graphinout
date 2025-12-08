package com.graphinout.foundation.pure.functional;

import java.util.stream.Stream;

public class TypedArray2<T> {

    private final Object[][] array;

    public TypedArray2(T[][] array) {
        this.array = array;
    }

    public TypedArray2(int is, int js) {
        this.array = new Object[is][js];
    }

    public static <T> TypedArray2<T> of(T[][] array) {
        return new TypedArray2<>(array);
    }

    public static <T> TypedArray2<T> of(int is, int js) {
        return new TypedArray2<>(is, js);
    }

    public T get(int i, int j) {
        //noinspection unchecked
        return (T) array[i][j];
    }

    public int length() {
        return array.length;
    }

    public void set(int i, int j, T value) {
        array[i][j] = value;
    }

    /** Stream all values from all rows and all columns, <em>includes nulls</em> */
    public Stream<T> stream() {
        return Stream.of(toArray()).flatMap(Stream::of);
    }

    public T[][] toArray() {
        //noinspection unchecked
        return (T[][]) array;
    }

}
