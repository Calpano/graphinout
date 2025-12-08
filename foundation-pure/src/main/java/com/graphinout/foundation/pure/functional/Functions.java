package com.graphinout.foundation.pure.functional;

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.DoubleConsumer;
import java.util.function.Function;
import java.util.function.ObjDoubleConsumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * Contains commonly used {@link Function} definitions.
 * <p>
 * See also {@link Function#identity()}.
 */
public class Functions {

    private Functions() {
    }

    /**
     * Silly util method. Experimental.
     *
     * @param given some object
     * @param when  it meets our test
     * @param then  we use it
     * @param <T>   given type
     */
    public static <T> void given_when_then(T given, Predicate<T> when, Consumer<T> then) {
        if (when.test(given)) {
            then.accept(given);
        }
    }

    /**
     * Get a {@link Function} that ignores its parameters and uses the given supplier to return a result.
     */
    public static <P, R> Function<P, R> ignoreParams(Supplier<R> resultSupplier) {
        return ignore -> resultSupplier.get();
    }

    @SuppressWarnings("unused")
    public static <T, R> R methodDoesNotExist(T t) {
        throw new UnsupportedOperationException("Method does not exist");
    }

    /**
     * Does nothing.
     */
    public static void noop() {
    }

    public static <A, B> BiConsumer<A, B> project_1(Consumer<A> aConsumer) {
        return (a, b) -> aConsumer.accept(a);
    }

    public static <A, B> BiConsumerFilter<A, B> project_1(ConsumerFilter<A> aConsumer) {
        return (a, b) -> aConsumer.acceptAndContinue(a);
    }

    public static <A, B> BiConsumerFilter<A, B> project_1_noFilter(Consumer<A> aConsumer) {
        return (a, b) -> {
            aConsumer.accept(a);
            return true;
        };
    }

    public static <A, B> BiConsumer<A, B> project_2(Consumer<B> bConsumer) {
        return (a, b) -> bConsumer.accept(b);
    }

    public static <A> ObjDoubleConsumer<A> project_2(DoubleConsumer doubleConsumer) {
        return (t, value) -> doubleConsumer.accept(value);
    }

    public static <A, B> BiConsumerFilter<A, B> project_2(ConsumerFilter<B> bConsumer) {
        return (a, b) -> bConsumer.acceptAndContinue(b);
    }

    /**
     * Wrap the given {@link Consumer} into a {@link Function} that returns null.
     *
     * @param consumer The consumer to wrap.
     * @param <P>      The type of the Consumer's parameter.
     * @param <R>      The type of the Function's return value.
     * @return A new {@link Function} that returns null after calling the given consumer.
     */
    public static <P, R> Function<P, R> returnNull(Consumer<P> consumer) {
        return param -> {
            consumer.accept(param);
            return null;
        };
    }


}
