package com.calpano.graphinout.foundation.util.path;

import java.util.List;
import java.util.function.IntFunction;
import java.util.function.Supplier;

/**
 * An interface that provides a list-like view of an object, abstracting over the underlying data structure.
 * <p>
 * This allows the {@link PathResolver} to navigate objects that are not standard {@link java.util.List}s but can be
 * treated as such.
 */
public interface IListLike {

    /**
     * Creates an {@link IListLike} instance from a size and a function to retrieve elements by index.
     *
     * @param size the number of elements in the list.
     * @param fun  a function that takes an index and returns the element at that position.
     * @return a new {@link IListLike} instance.
     */
    static IListLike of(int size, IntFunction<Object> fun) {
        return new IListLike() {
            @Override
            public Object get(int index) {
                return fun.apply(index);
            }

            @Override
            public int size() {
                return size;
            }
        };
    }

    /**
     * Creates an {@link IListLike} instance from a size supplier and an element retrieval function.
     * <p>
     * This is useful for cases where the size is computed lazily.
     *
     * @param sizeSupplier a supplier for the number of elements.
     * @param fun          a function that takes an index and returns the element at that position.
     * @return a new {@link IListLike} instance.
     */
    static IListLike of(Supplier<Integer> sizeSupplier, IntFunction<Object> fun) {
        return new IListLike() {
            @Override
            public Object get(int index) {
                return fun.apply(index);
            }

            @Override
            public int size() {
                return sizeSupplier.get();
            }
        };
    }

    /**
     * Creates a trivial wrapper for a standard {@link java.util.List}.
     *
     * @param javaList the list to wrap.
     * @return a new {@link IListLike} instance that delegates to the given list.
     */
    static IListLike ofList(List<?> javaList) {
        return of(javaList.size(), javaList::get);
    }

    /**
     * Returns the element at the specified position in this list.
     *
     * @param index index of the element to return.
     * @return the element at the specified position in this list.
     */
    Object get(int index);

    /**
     * @return the number of elements in this list.
     */
    int size();

}
