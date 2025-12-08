package com.graphinout.foundation.pure.functional;

import java.util.Objects;
import java.util.function.Function;

/**
 * Represents a function that accepts five arguments and produces a result. This is the four-arity specialization of
 * {@link Function}.
 *
 * <p>
 * This is a functional interface whose functional method is {@link #apply(Object, Object, Object, Object, Object)}.
 *
 * @param <K> the type of the first argument to the function
 * @param <L> the type of the second argument to the function
 * @param <M> the type of the third argument to the function
 * @param <N> the type of the fourth argument to the function
 * @param <O> the type of the fifth argument to the function
 * @param <R> the type of the result of the function
 * @see Function
 */
@FunctionalInterface
public interface QuinFunction<K, L, M, N, O, R> {

    /**
     * Returns a composed function that first applies this 3-arg-function to its input, and then applies the
     * {@code after} function (normal 1-arg-function) to the result. If evaluation of either function throws an
     * exception, it is relayed to the caller of the composed function.
     *
     * @param <V>   the type of output of the {@code after} function, and of the composed function
     * @param after the function to apply after this function is applied
     * @return a composed function that first applies this function and then applies the {@code after} function
     * @throws NullPointerException if after is null
     */
    default <V> QuinFunction<K, L, M, N, O, V> andThen(final Function<? super R, ? extends V> after) {
        Objects.requireNonNull(after);
        return (final K k, final L l, final M m, final N n, final O o) -> after.apply(apply(k, l, m, n, o));
    }

    /**
     * Applies this function to the given arguments.
     *
     * @param k the first function argument
     * @param l the second function argument
     * @param m the third function argument
     * @param n the fourth function argument
     * @param o the fifth function argument
     * @return the function result
     */
    R apply(final K k, final L l, final M m, N n, O o);

}
