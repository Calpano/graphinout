package com.graphinout.foundation.pure.functional;

/**
 * Similar to e.g. the one provided by JUnit
 */
@FunctionalInterface
public interface ThrowingSupplier<T> {

    /**
     * @param t consumed
     * @throws Throwable if the supplier implementation throws an exception
     */
    T get() throws Throwable;

}
