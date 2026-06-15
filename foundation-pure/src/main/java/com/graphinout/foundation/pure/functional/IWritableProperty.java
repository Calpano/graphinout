package com.graphinout.foundation.pure.functional;

import java.util.function.Consumer;

/**
 * A writable property: a typed {@link java.util.function.Consumer} that sets a single value.
 */
public interface IWritableProperty<T> extends Consumer<T> {

    default void accept(T value) {
        set(value);
    }

    /**
     * Sets the value of the property
     *
     * @param value the new value
     */
    void set(T value);

}
