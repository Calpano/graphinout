package com.graphinout.foundation.pure.functional;

import java.util.function.Consumer;

/**
 * Represents an operation that accepts three input arguments and returns true or false.
 *
 * <p>
 * This is a functional interface whose functional method is {@link #acceptAndFilter(Object, Object, Object)}.
 *
 * @param <K> the type of the first argument to the operation
 * @param <L> the type of the second argument to the operation
 * @param <M> the type of the third argument to the operation
 * @see Consumer
 */
@FunctionalInterface
public interface TriConsumerFilter<K, L, M> {

    /**
     * Performs this operation on the given three arguments.
     *
     * @param k first input argument
     * @param l second input argument
     * @param m third input argument
     * @return true to keep triple, false to delete
     */
    boolean acceptAndFilter(K k, L l, M m);

}
