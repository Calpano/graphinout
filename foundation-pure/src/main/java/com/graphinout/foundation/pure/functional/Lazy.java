package com.graphinout.foundation.pure.functional;

import java.util.function.Supplier;

/**
 * A type wrapper for a value that is lazily initialized when first accessed.
 *
 * @param <T> the type of the value
 */
public class Lazy<T> {

    private final Supplier<T> supplier;
    private T value;

    /**
     * Creates a new lazy value with the given supplier.
     */
    public Lazy(Supplier<T> supplier) {
        this.supplier = supplier;
    }

    /**
     * Creates a new lazy value with the given supplier.
     *
     * @param supplier the supplier used to initialize the value
     * @param <T>      the type of the value
     * @return the new lazy value
     */
    public static <T> Lazy<T> lazy(Supplier<T> supplier) {
        return new Lazy<>(supplier);
    }

    /**
     * Returns the value, initializing it if necessary.
     */
    public T get() {
        if (value == null) {
            value = supplier.get();
        }
        return value;
    }

    /**
     * Returns true if the value has been initialized.
     */
    public boolean isInitialized() {
        return value != null;
    }

}
