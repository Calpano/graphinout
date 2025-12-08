package com.graphinout.foundation.pure.functional;

import com.graphinout.foundation.pure.value.DoubleRef;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.DoubleConsumer;
import java.util.function.Function;

/**
 * A collection of commonly used {@link Consumer} implementations.
 */
public class Consumers {

    public static class CountingConsumer<T> implements Consumer<T> {

        public int count = 0;

        @Override
        public void accept(final T t) {
            this.count++;
        }

        public int getCount() {
            return this.count;
        }

    }

    public static class ListingConsumer<T> implements Consumer<T> {

        public List<T> list = new LinkedList<>();

        @Override
        public void accept(final T t) {
            this.list.add(t);
        }

        public int getCount() {
            return this.list.size();
        }

        public List<T> getList() {
            return this.list;
        }

    }

    private Consumers() {
    }

    public static <C extends Collection<Double>> C collectDoublesInto(Consumer<DoubleConsumer> itemSource, C collection) {
        itemSource.accept(collection::add);
        return collection;
    }

    public static List<Double> collectDoublesIntoList(Consumer<DoubleConsumer> itemSource) {
        return collectDoublesInto(itemSource, new ArrayList<>());
    }

    /**
     * Collect objects that are iteratively consumed into a provided {@link Collection}. The item source provides (U, V)
     * tuples that are transformed into the entry type E using a mapping function.
     *
     * @param itemSource An item source that provides its items to a {@link BiConsumer}.
     * @param mapping    A mapping {@link BiFunction} that transforms (U, V) tuples into the collection entry type E.
     * @param collection A collection to add the items to.
     * @param <E>        The collection's entry type.
     * @param <C>        The collection's type having E as entry type.
     * @param <U>        The first parameter type of the item source's BiConsumer.
     * @param <V>        The second parameter type of the item source's BiConsumer.
     * @return The collection containing the items.
     */
    public static <E, C extends Collection<E>, U, V> C collectInto(Consumer<BiConsumer<U, V>> itemSource, BiFunction<U, V, E> mapping, C collection) {
        itemSource.accept((u, v) -> collection.add(mapping.apply(u, v)));
        return collection;
    }

    /**
     * Collect objects that are iteratively consumed into a provided {@link Collection}.
     *
     * @param itemSource An item source that provides its items to a consumer.
     * @param collection A collection to add the items to.
     * @param <E>        The collection's entry type.
     * @param <C>        The collection's type having E as entry type.
     * @return The collection containing the items.
     */
    public static <E, C extends Collection<E>> C collectInto(Consumer<Consumer<E>> itemSource, C collection) {
        itemSource.accept(collection::add);
        return collection;
    }

    /**
     * Collect objects that are iteratively consumed into a new {@link ArrayList}.
     *
     * @param itemSource An item source that provides its items to a consumer.
     * @param <E>        The collection's entry type.
     * @return The ArrayList containing the items.
     */
    public static <E> List<E> collectIntoList(Consumer<Consumer<E>> itemSource) {
        return collectInto(itemSource, new ArrayList<>());
    }

    public static <T> CountingConsumer<T> count() {
        return new CountingConsumer<>();
    }

    public static <T> int count(Consumer<Consumer<T>> consumerConsumer) {
        CountingConsumer<T> c = count();
        consumerConsumer.accept(c);
        return c.count;
    }

    /**
     * Pass the value to the consumer if the consumer is not null.
     */
    public static <T> void ifPresent(@Nullable Consumer<T> consumer, T value) {
        Optional.ofNullable(consumer).ifPresent((c -> c.accept(value)));
    }

    /**
     * Get a {@link Consumer} that ignores its parameters executes the given runnable.
     */
    public static <T> Consumer<T> ignoreParams(Runnable runnable) {
        return ignored -> runnable.run();
    }

    /**
     * A no-operation {@link Consumer} that consumes a value and does nothing.
     *
     * @param ignored Consumed value that is ignored.
     */
    public static <T> void ignoreParamsNoop(T ignored) {
    }

    /**
     * A no-operation {@link BiConsumer} that consumes two values and does nothing.
     *
     * @param ignored1 Consumed value that is ignored.
     * @param ignored2 Consumed value that is ignored.
     */
    public static <T1, T2> void ignoreParamsNoop(T1 ignored1, T2 ignored2) {
    }

    public static <T> ListingConsumer<T> list() {
        return new ListingConsumer<>();
    }

    /**
     * Project the first component of a bi-source.
     *
     * @param biSource           give a method reference here or use explicit types in the method call
     * @param component1consumer consumes component 1 of the bi-source
     * @param <A>                component 1 type
     * @param <B>                component 2 type
     */
    public static <A, B> void project_1(Consumer<BiConsumer<A, B>> biSource, Consumer<A> component1consumer) {
        biSource.accept((a, b) -> component1consumer.accept(a));
    }

    public static <A, B> void project_1_filter(Consumer<BiConsumerFilter<A, B>> biSource, ConsumerFilter<A> component1consumer) {
        biSource.accept((a, b) -> component1consumer.acceptAndContinue(a));
    }

    public static <A, B> void project_1_filter(Consumer<BiConsumerFilter<A, B>> biSource, Consumer<A> component1consumer) {
        biSource.accept((a, b) -> {
            component1consumer.accept(a);
            return true;
        });
    }

    /**
     * Project the second component of a bi-source.
     *
     * @param biSource           give a method reference here or use explicit types in the method call
     * @param component2consumer consumes component 2 of the bi-source
     * @param <A>                component 1 type
     * @param <B>                component 2 type
     */
    public static <A, B> void project_2(Consumer<BiConsumer<A, B>> biSource, Consumer<B> component2consumer) {
        biSource.accept((a, b) -> component2consumer.accept(b));
    }

    /**
     * Project the second component of a bi-source.
     *
     * @param biSource           give a method reference here or use explicit types in the method call
     * @param component2consumer consumes component 2 of the bi-source
     * @param <A>                component 1 type
     * @param <B>                component 2 type
     */
    public static <A, B> void project_2_filter(Consumer<BiConsumerFilter<A, B>> biSource, ConsumerFilter<B> component2consumer) {
        biSource.accept((a, b) -> component2consumer.acceptAndContinue(b));
    }

    public static <A, B> void project_2_filter(Consumer<BiConsumerFilter<A, B>> biSource, Consumer<B> component2consumer) {
        biSource.accept((a, b) -> {
            component2consumer.accept(b);
            return true;
        });
    }

    public static double sumOfDoubles(Consumer<DoubleConsumer> itemSource) {
        DoubleRef sum = new DoubleRef();
        itemSource.accept(value -> sum.value += value);
        return sum.value;
    }

    public static <I, O> Consumer<I> transform( Function<I, O> mapFun, Consumer<O> outConsumer) {
        return i -> outConsumer.accept(mapFun.apply(i));
    }

}
