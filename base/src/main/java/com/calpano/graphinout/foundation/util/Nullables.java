package com.calpano.graphinout.foundation.util;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;


public class Nullables {

    /**
     * @param b a boolean flag that may be null.
     * @return true only of the given flag b is defined and set to true. Otherwise, false.
     */
    public static boolean booleanOrFalse(@Nullable Boolean b) {
        return b != null && b;
    }

    /**
     * Calls the given supplier functions in order until one of them returns a non-null value, then returns that value.
     *
     * @param candidates the supplier functions to call
     * @param <R>        the return type of the supplier functions
     * @return the first non-null value returned by the supplier functions, or null if all of them return null
     */
    @SafeVarargs
    public static <R> @Nullable R firstNonNull(Supplier<@Nullable R>... candidates) {
        return Arrays.stream(candidates).map(Supplier::get).filter(Objects::nonNull).findFirst().orElse(null);
    }

    /**
     * Returns the first non-null value from the given array of values.
     *
     * @param candidates the values to check
     * @param <R>        the type of the values
     * @return the first non-null value from the given array, or null if all of them are null
     */
    @SafeVarargs
    public static <R> @Nullable R firstNonNull(R... candidates) {
        return Arrays.stream(candidates).filter(Objects::nonNull).findFirst().orElse(null);
    }

    /**
     * If CONSUMER is present (not null), let it accept the given value (which itself may or may not be null, that is a
     * different story).
     */
    public static <T> void ifConsumerPresentAccept(@Nullable Consumer<T> nullableConsumer, T value) {
        if (nullableConsumer != null) nullableConsumer.accept(value);
    }

    /**
     * If the given value is not null, call the given consumer with it.
     *
     * @param <T> The type of the value
     */
    public static <T> void ifPresentAccept(@Nullable T value, Consumer<@NonNull T> consumer) {
        if (value != null) {
            consumer.accept(value);
        }
    }

    /**
     * If the given value is not null, call the given consumer with it.
     *
     * @param <T> The type of the value
     */
    public static <T,E extends Throwable> void ifPresentAcceptThrowing(@Nullable T value, ThrowingConsumer<@NonNull T,E> consumer) throws E{
        if (value != null) {
            consumer.acceptThrowing(value);
        }
    }

    public static <T, R> void ifPresentAccept(@Nullable T value, Function<T, R> mapFun, Consumer<R> consumer) {
        if (value == null) return;
        R r = mapFun.apply(value);
        if (r == null) return;
        consumer.accept(r);
    }

    public static <T, U, R> void ifPresentAccept(@Nullable T value, Function<T, U> mapFun1, Function<U, R> mapFun2, Consumer<R> consumer) {
        if (value == null) return;
        U u = mapFun1.apply(value);
        if (u == null) return;
        R r = mapFun2.apply(u);
        if (r == null) return;
        consumer.accept(r);
    }

    /**
     * Map 3 times, before consuming, if non-null
     */
    public static <T, U, R, S> void ifPresentAccept(@Nullable T value, Function<T, U> mapFun1, Function<U, R> mapFun2, Function<R, S> mapFun3, Consumer<S> consumer) {
        if (value == null) return;
        U u = mapFun1.apply(value);
        if (u == null) return;
        R r = mapFun2.apply(u);
        if (r == null) return;
        S s = mapFun3.apply(r);
        consumer.accept(s);
    }

    /**
     * If the given value is not null, apply the given function to it and return the result.
     *
     * @param <T> The type of the value
     */
    public static <T, R> @Nullable R ifPresentApply(@Nullable T value, Function<T, R> mapFun) {
        return mapOrNull(value, mapFun);
    }

    public static <T> boolean isNonNull(@Nullable T value) {
        return value != null;
    }

    public static boolean isNonNull(@Nullable Object... values) {
        for (Object value : values) {
            if (value == null) return false;
        }
        return true;
    }

    public static <T, R> R mapOrDefault(@Nullable T input, Function<T, R> mapFun, R defaultValue) {
        return Optional.ofNullable(input).map(mapFun).orElse(defaultValue);
    }

    public static <T, S, R> R mapOrDefault(@Nullable T input, Function<T, S> mapFun1, Function<S, R> mapFun2, R defaultValue) {
        if (input == null) return defaultValue;
        S s = mapFun1.apply(input);
        if (s == null) return defaultValue;
        R r = mapFun2.apply(s);
        if (r == null) return defaultValue;
        return r;
    }

    public static <T, U, V, R> R mapOrDefault(@Nullable T input, Function<T, U> mapFun1, Function<U, V> mapFun2, Function<V, R> mapFun3, R defaultValue) {
        if (input == null) return defaultValue;
        U u = mapFun1.apply(input);
        if (u == null) return defaultValue;
        V v = mapFun2.apply(u);
        if (v == null) return defaultValue;
        R r = mapFun3.apply(v);
        if (r == null) return defaultValue;
        return r;
    }

    public static <T> boolean mapOrFalse(@Nullable T input, Predicate<T> mapFun) {
        return input != null && mapFun.test(input);
    }

    public static <T, S, R> R mapOrNull(@Nullable T input, Function<T, S> mapFun1, Function<S, R> mapFun2) {
        return mapOrDefault(input, mapFun1, mapFun2, null);
    }

    /**
     * If given input is not null, return a mapped version. If it is null, return null.
     */
    public static <T, R> @Nullable R mapOrNull(@Nullable T input, Function<T, R> mapFun) {
        return Optional.ofNullable(input).map(mapFun).orElse(null);
    }

    /**
     * Throws AssertionError if input is null. Otherwise returns mapped input.
     */
    public static <T, R> R mapOrThrow(@Nullable T input, Function<T, R> mapFun) {
        if (input == null) throw new AssertionError("Expected non-null here");
        return mapFun.apply(input);
    }

    public static <T, R> R mapOrThrow(@Nullable T input, Function<T, R> mapFun, Supplier<Throwable> exceptionSupplier) throws Throwable {
        if (input == null) throw exceptionSupplier.get();
        return mapFun.apply(input);
    }

    /**
     * If predicate is given, it is evaluated on the given object. If predicate is null, this method evaluates to
     * false.
     */
    public static <T> boolean nonNullAndTest(@Nullable Predicate<T> predicateOrNull, T t) {
        return predicateOrNull != null && predicateOrNull.test(t);
    }

    public static <T> @NonNull T nonNullOrDefault(@Nullable T nullable, T defaultValue) {
        return nullable != null ? nullable : defaultValue;
    }

    public static <T> @NonNull T nonNullOrDefault(@Nullable T nullable, Supplier<T> defaultValueSupplier) {
        return nullable != null ? nullable : defaultValueSupplier.get();
    }

    public static @NonNull String nonNullOrEmpty(@Nullable String nullable) {
        return nullable != null ? nullable : "";
    }

    public static <T> @NonNull T nonNullOrThrow(@Nullable T nullable) {
        return nonNullOrThrow(nullable, () -> new AssertionError("Expected non-null here"));
    }

    public static <T, E extends Throwable> @NonNull T nonNullOrThrow(@Nullable T nullable, Supplier<E> exceptionSupplier) throws E {
        if (nullable == null) throw exceptionSupplier.get();
        return nullable;
    }

    public static <T> Stream<T> streamOf(@Nullable T nullable) {
        if (nullable == null) return Stream.empty();
        return Stream.of(nullable);
    }

    /** @return either a Stream.of(value) or an empty Stream */
    public static <T> Stream<T> streamOfOneOrEmpty(@Nullable T value) {
        return value == null ? Stream.empty() : Stream.of(value);
    }

    public static <I, O> Stream<O> streamOfOneOrEmpty(@Nullable I value, Function<I, O> mapFun) {
        return streamOfOneOrEmpty(mapOrNull(value, mapFun));
    }

}
