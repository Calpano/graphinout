package com.graphinout.foundation.pure.stream;

import com.graphinout.foundation.pure.annotations.GwtIncompatible;
import com.graphinout.foundation.pure.bridge.Java9;
import com.graphinout.foundation.pure.bridge.JavaPlatform;
import com.graphinout.foundation.pure.functional.ThrowingConsumer;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.List;
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
     * Finds one element in the {@link Stream}, or throws an exception if more than one element is found.
     *
     * @param stream
     * @param <T>
     * @return null if no element is found
     * @throws IllegalStateException if multiple elements are found
     */
    public static <T> @Nullable T findOneOrNull(Stream<T> stream) throws IllegalStateException {
        List<T> list = Java9.Stream.toList(stream.limit(2));
        switch (list.size()) {
            case 0:
                return null;
            case 1:
                return list.get(0);
            default:
                throw new IllegalStateException("Found more than one element.");
        }
    }

    /**
     * Finds one element in the {@link Stream}, or throws an exception.
     *
     * @param stream
     * @param <T>
     * @throws IllegalStateException if none or multiple elements are found
     */
    public static <T> @NonNull T findOne(Stream<T> stream) throws IllegalStateException {
        List<T> list = Java9.Stream.toList(stream.limit(2));
        if (list.size() == 1) {
            return list.get(0);
        }
        throw new IllegalStateException("Found more than one element.");
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
