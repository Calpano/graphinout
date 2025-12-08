package com.graphinout.foundation.pure.functional;

import java.util.function.Consumer;

/**
 * Represents an operation that accepts two input arguments and returns true or false.
 *
 * <p>
 * This is a functional interface whose functional method is {@link #acceptAndFilter(Object, Object)}.
 *
 * @param <K> the type of the first argument to the operation
 * @param <L> the type of the second argument to the operation
 * @see Consumer
 */
@FunctionalInterface
public interface BiConsumerFilter<K, L> {

    /**
     * Performs this operation on the given two arguments.
     *
     * @param k first input argument
     * @param l second input argument
     * @return true, e.g. to keep triple, false to delete; OR depending on context: true to continue consuming results,
     * false to stop
     */
    boolean acceptAndFilter(K k, L l);

}
