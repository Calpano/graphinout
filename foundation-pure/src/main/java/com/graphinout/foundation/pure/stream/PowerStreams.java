package com.graphinout.foundation.pure.stream;

import com.graphinout.foundation.pure.annotations.GwtIncompatible;
import com.graphinout.foundation.pure.bridge.JavaPlatform;
import com.graphinout.foundation.pure.functional.ThrowingConsumer;

import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class PowerStreams {

    /**
     * Filters a stream by a given class and casts the elements to that class.
     *
     * @param stream input
     * @param clazz  filter and map
     * @param <I>    input type
     * @param <O>    output type
     * @see #filterMap(Stream, Predicate, Function) for a j2cl version
     */
    @GwtIncompatible("Class.isInstance, cast")
    public static <I, O> Stream<O> filterMap(Stream<I> stream, Class<O> clazz) {
        return stream.filter(clazz::isInstance) //
                .map(o -> JavaPlatform.Class.cast(clazz, o));
    }

    /**
     * A j2cl friendly version of {@link #filterMap(Stream, Class).}
     *
     * @param stream
     * @param filter
     * @param castFun
     * @param <I>
     * @param <O>
     * @return
     */
    public static <I, O> Stream<O> filterMap(Stream<I> stream, Predicate<I> filter, Function<I, O> castFun) {
        return stream.filter(filter).map(castFun);
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
                throwingConsumer.accept(t);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

}
