package com.graphinout.foundation.pure.functional;

import java.util.function.Supplier;

/**
 * A readable property: a typed {@link java.util.function.Supplier} of a single value.
 */
public interface IReadableProperty<T> extends Supplier<T> {

    /**
     * @return the value of the property
     */
    T get();

}
