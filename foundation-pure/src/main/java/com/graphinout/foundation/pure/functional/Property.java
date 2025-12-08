package com.graphinout.foundation.pure.functional;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class Property<T> implements IReadableProperty<T>, IWritableProperty<T> {

    private final Supplier<T> getter;
    private final Consumer<T> setter;

    public Property(Supplier<T> dimGetter, Consumer<T> dimSetter) {
        this.getter = dimGetter;
        this.setter = dimSetter;
    }

    public static <T> Property<T> of(Supplier<T> getter, Consumer<T> setter) {
        return new Property<>(getter, setter);
    }

    public static <T> IReadableProperty<T> of(Supplier<T> getter) {
        return getter::get;
    }

    public static <T> IWritableProperty<T> of(Consumer<T> setter) {
        return setter::accept;
    }

    @Override
    public T get() {
        return getter.get();
    }

    @Override
    public void set(T value) {
        setter.accept(value);
    }

}
