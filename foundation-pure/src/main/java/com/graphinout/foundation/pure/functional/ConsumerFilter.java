package com.graphinout.foundation.pure.functional;

import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * A {@link java.util.function.Consumer} that, via {@link java.util.function.Predicate}, also signals whether
 *  traversal should continue after each accepted element.
 */
@FunctionalInterface
public interface ConsumerFilter<T> extends Consumer<T>, Predicate<T> {
    static <N> ConsumerFilter<N> consumerFilter(Predicate<N> fun) {
        return fun::test;
    }

    /**
     * Accept all elements
     */
    static <T> ConsumerFilter<T> consumerFilter(Consumer<T> consumer) {
        return t -> {
            consumer.accept(t);
            return true;
        };
    }

    @Override
    default void accept(T t) {
        acceptAndFilter(t);
    }

    /**
     * Same name as {@link BiConsumerFilter#acceptAndFilter(Object, Object)}.
     *
     * @return true if another element should be sent
     */
    boolean acceptAndFilter(T t);

    /**
     * @return true if another element should be sent
     * @deprecated use {@link #acceptAndFilter(Object)}
     */
    @Deprecated
    default boolean acceptAndContinue(T t) {
        return acceptAndFilter(t);
    }

    @Override
    default boolean test(T t) {
        return acceptAndFilter(t);
    }
}
