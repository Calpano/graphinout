package com.graphinout.foundation.pure.functional;

import com.graphinout.foundation.pure.annotations.quality.QualityUnchecked;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static com.graphinout.foundation.pure.functional.IOrElse.DONT_EXECUTE_ELSE;
import static com.graphinout.foundation.pure.functional.IOrElse.EXECUTE_ELSE;

/**
 * Utility methods for handling null values and performing operations conditionally.
 * <h3>Conventions</h3>
 * For transforming methods, {@code I} is the first input type and {@code O} is the last output type. If only one type
 * is used, that type is {@code T}.
 */
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
     * @param <T>        the return type of the supplier functions
     * @return the first non-null value returned by the supplier functions, or null if all of them return null
     */
    @SafeVarargs
    public static <T> @Nullable T firstNonNull(Supplier<@Nullable T>... candidates) {
        return Arrays.stream(candidates).map(Supplier::get).filter(Objects::nonNull).findFirst().orElse(null);
    }

    /**
     * Returns the first non-null value from the given array of values.
     *
     * @param candidates the values to check
     * @param <T>        the type of the values
     * @return the first non-null value from the given array, or null if all of them are null
     */
    @SafeVarargs
    public static <T> @Nullable T firstNonNull(@Nullable T... candidates) {
        return Arrays.stream(candidates).filter(Objects::nonNull).findFirst().orElse(null);
    }

    /**
     * If CONSUMER is present (not null), let it accept the given value (which itself may or may not be null, that is a
     * different story).
     */
    public static <T> void ifConsumerPresentAccept(@Nullable Consumer<T> nullableConsumer, @Nullable T value) {
        if (nullableConsumer != null) nullableConsumer.accept(value);
    }

    /**
     * Alias for {@link #ifPresentAccept(Object, Function, Consumer)}
     */
    public static <I, O> void ifPresent(@Nullable I value, //
                                        @NonNull Function<@NonNull I, @Nullable O> mapFun, //
                                        @NonNull Consumer<@NonNull O> consumer) {
        ifPresentAccept(value, mapFun, consumer);
    }

    /**
     * If the given value is not null, call the given consumer with it.
     */
    public static <T> void ifPresent(@Nullable T value, @NonNull Consumer<@NonNull T> consumer) {
        if (value == null) return;
        consumer.accept(value);
    }

    /**
     * If the given value is not null, call the given consumer with it.
     *
     * @param <T> The type of the value
     * @return an {@link IOrElse} which is quite similar to a Java {@link Optional}.
     */
    public static <T> @NonNull IOrElse ifPresentAccept(@Nullable T value, @NonNull Consumer<@NonNull T> consumer) {
        if (value != null) {
            consumer.accept(value);
            return DONT_EXECUTE_ELSE;
        } else {
            return EXECUTE_ELSE;
        }
    }

    /**
     * If the given value is not null, let the mapFun transform it, and if that result is also not null, call the given
     * consumer with it.
     */
    public static <I, O> void ifPresentAccept(@Nullable I value, //
                                              @NonNull Function<@NonNull I, @Nullable O> mapFun, //
                                              @NonNull Consumer<@NonNull O> consumer) {
        if (value == null) return;
        O r = mapFun.apply(value);
        if (r == null) return;
        consumer.accept(r);
    }

    /**
     * If the given value is not null, let the mapFun1 transform it, and if that result is also not null, let mapFun2
     * transform it, and if that result is also not null, call the given consumer with it.
     */
    public static <I, J, O> void ifPresentAccept(@Nullable I value, //
                                                 @NonNull Function<@NonNull I, @Nullable J> mapFun1, @NonNull Function<@NonNull J, @Nullable O> mapFun2, @NonNull Consumer<@NonNull O> consumer) {
        if (value == null) return;
        J u = mapFun1.apply(value);
        if (u == null) return;
        O r = mapFun2.apply(u);
        if (r == null) return;
        consumer.accept(r);
    }

    /**
     * Map 3 times, before consuming, if non-null
     */
    public static <I, J, K, O> void ifPresentAccept(@Nullable I value, //
                                                    @NonNull Function<@NonNull I, @Nullable J> mapFun1, //
                                                    @NonNull Function<@NonNull J, @Nullable K> mapFun2, //
                                                    @NonNull Function<@NonNull K, @Nullable O> mapFun3, //
                                                    @NonNull Consumer<@NonNull O> consumer) {
        if (value == null) return;
        J u = mapFun1.apply(value);
        if (u == null) return;
        K r = mapFun2.apply(u);
        if (r == null) return;
        O s = mapFun3.apply(r);
        if (s == null) return;
        consumer.accept(s);
    }

    /**
     * If the given value is not null, call the given consumer with it.
     *
     * @param <T> The type of the value
     */
    public static <T, E extends Throwable> void ifPresentAcceptThrowing(@Nullable T value, //
                                                                        @NonNull ThrowingConsumer<@NonNull T, E> consumer) throws E {
        if (value != null) {
            consumer.accept(value);
        }
    }

    /**
     * If the given value is not null, apply the given function to it and return the result.
     *
     * @param <I> The type of the value
     */
    public static <I, O> @Nullable O ifPresentApply(@Nullable I value, @NonNull Function<@NonNull I, @Nullable O> mapFun) {
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

    public static <T> boolean isNotNullAnd(@Nullable T input, @NonNull Predicate<@NonNull T> testNonNull) {
        return input != null && testNonNull.test(input);
    }

    public static <T> Predicate<@Nullable T> isNotNullAnd(@NonNull Predicate<@NonNull T> testNonNull) {
        return input -> input != null && testNonNull.test(input);
    }

    @QualityUnchecked
    public static <I, O> Predicate<@Nullable I> isNotNullAnd(Function<@NonNull I, @Nullable O> mapToNullFun, //
                                                             @NonNull Predicate<@NonNull O> testNonNull) {
        return input -> {
            if (input == null) return false;
            O r = mapToNullFun.apply(input);
            if (r == null) return false;
            return testNonNull.test(r);
        };
    }

    public static <T> boolean isNullOr(@Nullable T input, @NonNull Predicate<@NonNull T> testNonNull) {
        return input == null || testNonNull.test(input);
    }

    public static <T> Predicate<@Nullable T> isNullOr(@NonNull Predicate<@NonNull T> testNonNull) {
        return input -> input == null || testNonNull.test(input);
    }

    /**
     * Try to map input via mapFun1 and mapFun2, returning defaultValue if any step (input or mapFun1) fails. Like
     * {@link #mapOrDefault(Object, Function, Function, Object)} but non-null results.
     */
    public static <I, J, O> @NonNull O mapNonNull(@Nullable I input, //
                                                  @NonNull Function<@NonNull I, @Nullable J> mapFun1, //
                                                  @NonNull Function<@NonNull J, @NonNull O> mapFun2, //
                                                  @NonNull O defaultValue) {
        if (input == null) return defaultValue;
        J s = mapFun1.apply(input);
        if (s == null) return defaultValue;
        return mapFun2.apply(s);
    }

    /**
     * Try to map input via mapFun1, returning defaultValue if input is null. Like
     * {@link #mapOrDefault(Object, Function, Object)} but non-null results.
     */
    public static <I, J, O> @NonNull O mapNonNull(@Nullable I input, //
                                                  @NonNull Function<@NonNull I, @NonNull O> mapFun, //
                                                  @NonNull O defaultValue) {
        if (input == null) return defaultValue;
        return mapFun.apply(input);
    }

    public static <I, O> @Nullable O mapOrDefault(@Nullable I input, //
                                                  @NonNull Function<@NonNull I, @Nullable O> mapFun, @Nullable O defaultValue) {
        return Optional.ofNullable(input).map(mapFun).orElse(defaultValue);
    }

    public static <I, J, O> @Nullable O mapOrDefault(@Nullable I input, //
                                                     @NonNull Function<@NonNull I, @Nullable J> mapFun1, //
                                                     @NonNull Function<@NonNull J, @Nullable O> mapFun2, //
                                                     @Nullable O defaultValue) {
        if (input == null) return defaultValue;
        J s = mapFun1.apply(input);
        if (s == null) return defaultValue;
        O r = mapFun2.apply(s);
        if (r == null) return defaultValue;
        return r;
    }

    public static <I, J, K, O> @Nullable O mapOrDefault(@Nullable I input, //
                                                        @NonNull Function<I, J> mapFun1,//
                                                        @NonNull Function<J, K> mapFun2, //
                                                        @NonNull Function<K, O> mapFun3, //
                                                        @Nullable O defaultValue) {
        if (input == null) return defaultValue;
        J u = mapFun1.apply(input);
        if (u == null) return defaultValue;
        K v = mapFun2.apply(u);
        if (v == null) return defaultValue;
        O r = mapFun3.apply(v);
        if (r == null) return defaultValue;
        return r;
    }

    public static <T> boolean mapOrFalse(@Nullable T input, @NonNull Predicate<@NonNull T> mapFun) {
        return input != null && mapFun.test(input);
    }

    /**
     *
     * @param input                if null, the defaultValueSupplier is called
     * @param mapFun               if it returns null, ALSO the defaultValueSupplier is called
     * @param defaultValueSupplier
     * @param <I>
     * @param <O>
     * @return
     */
    public static <I, O> @Nullable O mapOrGetDefault(@Nullable I input, //
                                                     @NonNull Function<@NonNull I, @Nullable O> mapFun, @NonNull Supplier<@Nullable O> defaultValueSupplier) {
        return Optional.ofNullable(input).map(mapFun).orElse(defaultValueSupplier.get());
    }

    /**
     * Like {@link #mapOrDefault(Object, Function, Function, Object)} with a {@code null} default value.
     */
    public static <I, J, O> @Nullable O mapOrNull(@Nullable I input, @NonNull Function<@NonNull I, @Nullable J> mapFun1, @NonNull Function<@NonNull J, @Nullable O> mapFun2) {
        return mapOrDefault(input, mapFun1, mapFun2, null);
    }

    /**
     * Like {@link #mapOrDefault(Object, Function, Object)} with a {@code null} default value.
     */
    public static <I, O> @Nullable O mapOrNull(@Nullable I input, @NonNull Function<@NonNull I, @Nullable O> mapFun) {
        return mapOrDefault(input, mapFun, null);
    }


    /**
     * Throws AssertionError if input is null. Otherwise, returns mapped input.
     *
     * @throws AssertionError if input is null
     */
    public static <I, O> @Nullable O mapOrThrow(@Nullable I input, //
                                                @NonNull Function<@NonNull I, @Nullable O> mapFun) throws AssertionError {
        if (input == null) throw new AssertionError("Expected non-null here");
        return mapFun.apply(input);
    }


    /** TODO Does this work in J2CL? */
    @QualityUnchecked
    public static <I, O, E extends Throwable> @Nullable O mapOrThrow(@Nullable I input, //
                                                                     @NonNull Function<@NonNull I, @Nullable O> mapFun, //
                                                                     @NonNull Supplier<@NonNull E> exceptionSupplier) throws E {
        if (input == null) throw exceptionSupplier.get();
        return mapFun.apply(input);
    }

    // HERE

    /**
     * If the given input is null, return the default value. Otherwise, return the input.
     */
    public static <T> @NonNull T nonNull(@Nullable T nullable, @NonNull T defaultValue) {
        return nonNullOrDefault(nullable, defaultValue);
    }

    /**
     * Like {@link #mapOrDefault(Object, Function, Object)}, but with a non-null default value.
     */
    public static <I, O> @NonNull O nonNull(@Nullable I nullable, @NonNull Function<@NonNull I, O> mapFun, @NonNull O defaultValue) {
        return mapOrDefault(nullable, mapFun, defaultValue);
    }

    /**
     * Like {@link #nonNullOrGetDefault(Object, Supplier)}, but with a non-null default value.
     *
     * @param nullable
     * @param defaultValueSupplier
     * @param <T>
     * @return
     */
    public static <T> @NonNull T nonNull(@Nullable T nullable, @NonNull Supplier<@NonNull T> defaultValueSupplier) {
        return nullable != null ? nullable : defaultValueSupplier.get();
    }

    /**
     * If a predicate is given, it is evaluated on the given object. If the predicate is null, this method evaluates to
     * false.
     */
    public static <T> boolean nonNullAndTest(@Nullable Predicate<@Nullable T> predicateOrNull, @Nullable T t) {
        return predicateOrNull != null && predicateOrNull.test(t);
    }


    /**
     * In most cases, better use {@link #nonNull(Object, Function, Object)}
     *
     * @param mapFun       may return null, too
     * @param defaultValue can be null, too
     */
    public static <T, R> @Nullable R nonNullOrDefault(@Nullable T nullable, //
                                                      @NonNull Function<@NonNull T, @Nullable R> mapFun, //
                                                      @Nullable R defaultValue) {
        return nullable != null ? mapFun.apply(nullable) : defaultValue;
    }

    /**
     * In most cases, better use {@link #nonNull(Object, Object)}
     *
     * @param defaultValue can be null, too
     */
    public static <T> @Nullable T nonNullOrDefault(@Nullable T nullable, @Nullable T defaultValue) {
        return nullable != null ? nullable : defaultValue;
    }


    /**
     * In most cases, better use {@link #nonNull(Object, Supplier)}
     *
     * @param defaultValueSupplier may return null, too
     * @deprecated use {@link #nonNullOrGetDefault(Object, Supplier)}
     */
    @Deprecated
    public static <T> @Nullable T nonNullOrDefault(@Nullable T nullable, @NonNull Supplier<@Nullable T> defaultValueSupplier) {
        return nonNullOrGetDefault(nullable, defaultValueSupplier);
    }

    /**
     * In most cases, better use {@link #nonNull(Object, Supplier)}
     *
     * @param defaultValueSupplier may return null, too
     */
    public static <T> @Nullable T nonNullOrGetDefault(@Nullable T nullable, @NonNull Supplier<@Nullable T> defaultValueSupplier) {
        return nullable != null ? nullable : defaultValueSupplier.get();
    }

    public static @NonNull String nonNullOrEmpty(@Nullable String nullable) {
        return nullable != null ? nullable : "";
    }

    /**
     * In most cases, better use {@link #nonNull(Object, Function, Object)}
     *
     * @param mapFun               may return null, too
     * @param defaultValueSupplier may return null, too
     */
    public static <T, R> @Nullable R nonNullOrGetDefault(@Nullable T nullable, //
                                                         @NonNull Function<@NonNull T, @Nullable R> mapFun, //
                                                         @NonNull Supplier<@Nullable R> defaultValueSupplier) {
        return nullable != null ? mapFun.apply(nullable) : defaultValueSupplier.get();
    }

    public static <T> @NonNull T nonNullOrThrow(@Nullable T nullable) throws AssertionError {
        return nonNullOrThrow(nullable, () -> new AssertionError("Expected non-null here"));
    }

    public static <T, E extends Throwable> @NonNull T nonNullOrThrow(@Nullable T nullable, //
                                                                     @NonNull Supplier<@NonNull E> exceptionSupplier) throws E {
        if (nullable == null) throw exceptionSupplier.get();
        return nullable;
    }

    public static <T> @NonNull Stream<@Nullable T> streamOf(@Nullable T nullable) {
        if (nullable == null) return Stream.empty();
        return Stream.of(nullable);
    }

    /** @return either a Stream.of(value) or an empty Stream */
    public static <T> @NonNull Stream<@Nullable T> streamOfOneOrEmpty(@Nullable T value) {
        return value == null ? Stream.empty() : Stream.of(value);
    }

    public static <I, O> @NonNull Stream<@NonNull O> streamOfOneOrEmpty(@Nullable I value, //
                                                                        @NonNull Function<@Nullable I, @NonNull O> mapFun) {
        if (value == null) return Stream.empty();
        return Stream.of(mapFun.apply(value));
    }

}
