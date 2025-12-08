package com.graphinout.foundation.pure.functional;

/**
 * @param <I> input type
 * @param <O> output type
 * @param <E> error type
 */
@FunctionalInterface
public interface ThrowingFunction<I, O, E extends Throwable> {

    O apply(I input) throws E;

}
