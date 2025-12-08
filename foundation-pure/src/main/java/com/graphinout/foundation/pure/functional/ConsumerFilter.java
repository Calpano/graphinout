package com.graphinout.foundation.pure.functional;

import java.util.function.Consumer;
import java.util.function.Predicate;

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
        acceptAndContinue(t);
    }

    /**
     * @return true if another element should be sent
     */
    // TODO rename to acceptAndFilter like in BiConsumerFilter
    boolean acceptAndContinue(T t);

    @Override
    default boolean test(T t) {
        return acceptAndContinue(t);
    }
}
