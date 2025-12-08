package com.graphinout.foundation.pure.collections;


import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.ObjIntConsumer;

public class IteratorsFoundation {

    public static <E> void addAll(final Iterable<E> sourceIterable, final Collection<E> targetCollection) {
        for (final E e : sourceIterable) {
            targetCollection.add(e);
        }
    }

    /**
     * @param <T>        type of both
     * @param <C>        a collection type of T
     * @param it         iterator to add elements from
     * @param collection to which elements are added
     * @return as a convenience, the supplied collection
     */
    public static <C extends Collection<T>, T> C addAll(@NonNull final Iterator<? extends T> it, final C collection) {
        while (it.hasNext()) {
            final T t = it.next();
            collection.add(t);
        }
        return collection;
    }

    public static <E> void forEach(final Iterator<E> it, final Consumer<E> consumer) {
        while (it.hasNext()) {
            final E e = it.next();
            consumer.accept(e);
        }
    }

    public static <E> void forEach(final Iterator<E> it, final ObjIntConsumer<E> element_index) {
        int i = 0;
        while (it.hasNext()) {
            final E e = it.next();
            element_index.accept(e, i++);
        }
    }

    /**
     * A Java HashSet with all elements from iterator
     *
     * @param it input
     * @return a HashSet
     */
    @SuppressWarnings("squid:S1319")
    public static <T> HashSet<T> toSet(final Iterator<? extends T> it) {
        final HashSet<T> set = new HashSet<>();
        addAll(it, set);
        return set;
    }

    public static <T> List<T> toLinkedList(final Iterator<T> it) {
        final List<T> list = new LinkedList<>();
        addAll(it, list);
        return list;
    }

    /**
     * @return a LinkedList
     */
    public static <T> List<T> toList(final Iterable<? extends T> it) {
        final LinkedList<T> list = new LinkedList<>();
        addAll(it.iterator(), list);
        return list;
    }


    /**
     * @param it input
     * @param initialSize for the list
     * @return a list of given size with all iterator elements added -- which may make the list even bigger
     */
    public static <E> List<E> toListOfSize(final Iterator<E> it, final int initialSize) {
        final ArrayList<E> list = new ArrayList<>(initialSize);
        addAll(it, list);
        return list;
    }

}
