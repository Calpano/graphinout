package com.graphinout.foundation.pure.value;

import java.util.function.Supplier;

/**
 * An object by reference, to be used with Java 8 lambdas
 */
public class ObjectRef<T> {

    public T value;

    public ObjectRef(final T value) {
        this.value = value;
    }

    public static <T> ObjectRef<T> createNull() {
        return new ObjectRef<>(null);
    }

    /**
     * Initially <code>null</code>.
     */
    public static <T> ObjectRef<T> empty() {
        return new ObjectRef<>(null);
    }

    public static <T> ObjectRef<T> objectRef(T value) {
        return new ObjectRef<>(value);
    }

    public static <T> ObjectRef<T> objectRef() {
        return new ObjectRef<>(null);
    }

    public static <T> ObjectRef<T> of(T value) {
        return new ObjectRef<>(value);
    }

    public void clear() {
        this.value = null;
    }

    @SuppressWarnings("rawtypes")
    @Override
    public boolean equals(final Object o) {
        return o instanceof ObjectRef && ((ObjectRef) o).value.equals(this.value) || this.value != null && this.value.equals(o);
    }

    public T get() {
        return value;
    }

    public T getOrThrow(Supplier<RuntimeException> exceptionSupplier) {
        if (this.value == null) {
            throw exceptionSupplier.get();
        }
        return this.value;
    }

    @Override
    public int hashCode() {
        return this.value == null ? 0 : this.value.hashCode();
    }

    public boolean isEmpty() {
        return this.value == null;
    }

    public boolean isPresent() {
        return this.value != null;
    }

    public ObjectRef<T> set(T value) {
        this.value = value;
        return this;
    }

    @Override
    public String toString() {
        return this.value == null ? null : this.value.toString();
    }

}
