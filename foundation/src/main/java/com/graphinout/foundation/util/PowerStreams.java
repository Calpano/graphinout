package com.graphinout.foundation.util;

import java.util.stream.Stream;

public class PowerStreams {

    /**
     * Filters a stream by a given class and casts the elements to that class.
     *
     * @param stream input
     * @param clazz  filter and map
     * @param <I>    input type
     * @param <O>    output type
     */
    public static <I, O> Stream<O> filterMap(Stream<I> stream, Class<O> clazz) {
        return stream.filter(clazz::isInstance).map(clazz::cast);
    }

    /**
     * @param stream           source
     * @param throwingConsumer action
     * @param <T>              element type
     * @param <E>              wrapped into a {@link RuntimeException}, if it comes.
     */
    public static <T, E extends Exception> void forEach(Stream<T> stream, ThrowingConsumer<T, E> throwingConsumer) {
        stream.forEach(t -> {
            try {
                throwingConsumer.acceptThrowing(t);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

}
