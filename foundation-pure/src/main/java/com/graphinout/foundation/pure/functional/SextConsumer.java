package com.graphinout.foundation.pure.functional;

import java.util.function.Consumer;

/**
 * Represents an operation that accepts 6 input arguments and returns no result. This is the 6-arity specialization of
 * {@link Consumer}. Unlike most other functional interfaces, {@code SextConsumer} is expected to operate via side
 * effects AND the return value.
 *
 * <p>
 * This is a functional interface whose functional method is
 * {@link #accept(Object, Object, Object, Object, Object, Object)}.
 *
 * @param <K> the type of the first argument to the operation
 * @param <L> the type of the second argument to the operation
 * @param <M> the type of the third argument to the operation
 * @param <O> the type of the fourth argument to the operation
 * @param <P> the type of the fifth argument to the operation
 * @param <Q> the type of the sixth argument to the operation
 * @see Consumer
 */
@FunctionalInterface
public interface SextConsumer<K, L, M, O, P, Q> {

    /**
     * Performs this operation on the given six arguments.
     *
     * @param k first input argument
     * @param l second input argument
     * @param m third input argument
     * @param o fourth input argument
     * @param p fifth input argument
     * @param q sixth input argument
     */
    void accept(K k, L l, M m, O o, P p, Q q);

}
