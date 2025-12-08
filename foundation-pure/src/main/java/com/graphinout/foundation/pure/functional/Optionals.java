package com.graphinout.foundation.pure.functional;

import org.jspecify.annotations.Nullable;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

// TODO to xydra
@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public class Optionals {

    /**
     * @param <T> The shared value type of `a` and `b`.
     * @return a and if not present, then b
     */
    public static <T> java.util.Optional<T> or(java.util.Optional<T> a, Supplier<Optional<T>> b) {
        if (a.isPresent()) return a;
        return b.get();
    }

    /**
     * @param <T> The shared value type of `a` and `b`.
     * @return a and if not present, then b
     */
    public static <T> Optional<T> or(Optional<T> a, Optional<T> b) {
        if (a.isPresent()) return a;
        return b;
    }

    private Optionals() {
    }

    /**
     * @param optional to process
     * @param consumer consumer the optional value, if it is present
     * @param <T>      type of the optional
     * @return if the optional is present
     */
    public static <T> boolean isPresent(Optional<T> optional, Consumer<T> consumer) {
        if (optional.isPresent()) {
            consumer.accept(optional.get());
            return true;
        }
        return false;
    }

    public static <N, T> T mapNullable(N nullableObject, Function<N, T> mapFunction) {
        return nullableObject == null ? null : mapFunction.apply(nullableObject);
    }

    public static <T> boolean mapOrFalse(Optional<T> optional, Predicate<T> testMethod) {
        return optional.isPresent() && testMethod.test(optional.get());
    }

    /**
     * Returns the value of the given nullable object or the value returned by the given supplier if the object is
     * null.
     *
     * @param nullable nullable object.
     * @param orElse   supplier of default value.
     * @param <T>      Return type.
     */
    public static <T> T orElse(@Nullable T nullable, Supplier<T> orElse) {
        return nullable != null ? nullable : orElse.get();
    }

    /**
     * Returns the value of the given nullable object or the given default value if the object is null.
     *
     * @param nullable nullable object.
     * @param orElse   default value.
     * @param <T>      Return type.
     */
    public static <T> T orElse(@Nullable T nullable, T orElse) {
        return nullable != null ? nullable : orElse;
    }

    public static <T> String toString(Optional<T> optional) {
        return optional.map(Object::toString).orElse(null);
    }

}
