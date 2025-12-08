package com.graphinout.foundation.pure.functional;

import java.util.Objects;
import java.util.function.Consumer;

/**
 * This type exists only due to performance penalties for auto-boxing. It really is just a faster
 * {@link TriConsumer<Integer,Integer,T>}.
 * <p>
 * Represents an operation that accepts three input arguments and returns no result. This is the three-arity
 * specialization of {@link Consumer}. Unlike most other functional interfaces, {@code TriConsumer} is expected to
 * operate via side effects.
 *
 * <p>
 * This is a functional interface whose functional method is {@link #accept(int, int, Object)}.
 *
 * @param <T> the type of the third argument to the operation
 * @see Consumer
 */
@FunctionalInterface
public interface TriConsumer_int_int_T<T> {

    /**
     * Performs this operation on the given three arguments.
     *
     * @param k first input argument
     * @param l second input argument
     * @param m third input argument
     */
    void accept(int k, int l, T m);

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
    default TriConsumer_int_int_T<T> andThen(final TriConsumer_int_int_T<? super T> after) {
        Objects.requireNonNull(after);

        return (k, l, m) -> {
            accept(k, l, m);
            after.accept(k, l, m);
        };
    }
}
