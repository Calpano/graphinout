package com.graphinout.foundation.pure.functional;

import java.util.Objects;
import java.util.function.Consumer;

/**
 * Represents an operation that accepts three input arguments and returns no result. This is the three-arity
 * specialization of {@link Consumer}. Unlike most other functional interfaces, {@code TriConsumer} is expected to
 * operate via side effects.
 *
 * <p>
 * This is a functional interface whose functional method is {@link #accept(Object, Object, Object)}.
 *
 * @param <K> the type of the first argument to the operation
 * @param <L> the type of the second argument to the operation
 * @param <M> the type of the third argument to the operation
 * @see Consumer
 */
@FunctionalInterface
public interface TriConsumer<K, L, M> {

    /**
     * Performs this operation on the given three arguments.
     *
     * @param k first input argument
     * @param l second input argument
     * @param m third input argument
     */
    void accept(K k, L l, M m);

    /**
     * Returns a composed {@code TriConsumer} that performs, in sequence, this operation followed by the {@code after}
     * operation. If performing either operation throws an exception, it is relayed to the caller of the composed
     * operation. If performing this operation throws an exception, the {@code after} operation will not be performed.
     *
     * @param after the operation to perform after this operation
     * @return a composed {@code TriConsumer} that performs in sequence this operation followed by the {@code after}
     * operation
     * @throws NullPointerException if {@code after} is null
     */
    default TriConsumer<K, L, M> andThen(final TriConsumer<? super K, ? super L, ? super M> after) {
        Objects.requireNonNull(after);

        return (k, l, m) -> {
            accept(k, l, m);
            after.accept(k, l, m);
        };
    }
}
