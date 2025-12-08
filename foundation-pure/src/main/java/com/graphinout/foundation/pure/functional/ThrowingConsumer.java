package com.graphinout.foundation.pure.functional;

import com.graphinout.foundation.pure.value.ObjectRef;

import java.util.function.Consumer;

/**
 * Similar to e.g. the one provided by JUnit
 */
@FunctionalInterface
public interface ThrowingConsumer<T, E extends Throwable> {

    /**
     * @param throwingConsumer
     * @param classicSource
     * @param <T>
     * @param <E>
     * @throws Throwable
     */
    static <T, E extends Throwable> void letItThrow(ThrowingConsumer<T, E> throwingConsumer, Consumer<Consumer<T>> classicSource) throws Throwable {
        ObjectRef<Throwable> error = new ObjectRef<>(null);
        Consumer<T> classicConsumer = redirectErrors(throwingConsumer, error::set);
        classicSource.accept(classicConsumer);
        if (error.get() != null) throw error.get();
    }

    /**
     * Split a {@link ThrowingConsumer} into a classic {@link Consumer} and an error {@link Consumer}
     *
     * @param throwingConsumer to convert
     * @param errorConsumer    to receive the errors thrown by the throwingConsumer; needs also to handle unplanned
     *                         {@link RuntimeException}
     * @param <T>              consumed element type
     * @param <E>              error type
     * @return a classic consumer
     */
    static <T, E extends Throwable> Consumer<T> redirectErrors(ThrowingConsumer<T, E> throwingConsumer, Consumer<Throwable> errorConsumer) {
        return element -> {
            try {
                throwingConsumer.accept(element);
            } catch (Throwable e) {
                // NOTE: Due to Java Generics, we cannot catch the E type here
                //noinspection unchecked
                errorConsumer.accept((E) e);
            }
        };
    }

    /**
     * This does not make the {@link Exception} go away
     * @throws E if the consumer implementation throws an exception
     */
    void accept(T t) throws E;

    static <T,E extends Throwable> Consumer<T> toNonThrowing( ThrowingConsumer<T,E> throwingConsumer) {
        return t -> {
            try {
                throwingConsumer.accept(t);
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        };
    }

}
