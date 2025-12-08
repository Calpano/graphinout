package com.graphinout.foundation.pure.functional;

import java.util.function.Supplier;

public interface IReadableProperty<T> extends Supplier<T> {

    /**
     * @return the value of the property
     */
    T get();

}
