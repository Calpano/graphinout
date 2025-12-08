package com.graphinout.foundation.pure.functional;

import org.jspecify.annotations.NonNull;

import java.util.function.Function;

/**
 * Clone of {@link Function}, but with @NeverNull annotation on return type
 */
@FunctionalInterface
public interface FunctionReturnsNeverNull<T, R> extends Function<T, R> {

    static <T> FunctionReturnsNeverNull<T, T> identity() {
        return t -> t;
    }

    default <V> FunctionReturnsNeverNull<T, V> andThen(
            @NonNull final FunctionReturnsNeverNull<? super R, ? extends V> after) {
        return (final T t) -> after.apply(apply(t));
    }

    @Override
    @NonNull
    R apply(T t);

    default <V> FunctionReturnsNeverNull<V, R> compose(
            @NonNull final FunctionReturnsNeverNull<? super V, ? extends T> before) {
        return (final V v) -> apply(before.apply(v));
    }
}
