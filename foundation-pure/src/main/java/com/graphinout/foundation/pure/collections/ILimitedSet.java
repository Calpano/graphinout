package com.graphinout.foundation.pure.collections;

import org.jspecify.annotations.NonNull;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * Factory for {@link java.util.Set}s with a bounded capacity that ignore additions beyond the limit.
 */
public interface ILimitedSet {

    /**
     * Create a new {@link HashSet} with a limited capacity by using the {@link #ofCapacity(Set, int)} method.
     */
    static <E> Set<E> ofCapacity(int capacity) {
        return ofCapacity(new HashSet<>(), capacity);
    }

    /**
     * Wrap a set to limit its capacity by ignoring add operations when the capacity is reached.
     */
    static <E> Set<E> ofCapacity(Set<E> set, int capacity) {
        return new Set<E>() {
            @Override
            public boolean add(E e) {
                if (set.size() < capacity) {
                    return set.add(e);
                }
                return false;
            }

            @Override
            public boolean addAll(@NonNull Collection<? extends E> c) {
                boolean changed = false;
                Iterator<? extends E> iterator = c.iterator();
                while (iterator.hasNext() && set.size() < capacity) {
                    set.add(iterator.next());
                    changed = true;
                }
                return changed;
            }

            @Override
            public void clear() {
                set.clear();
            }

            @Override
            public boolean contains(Object o) {
                return set.contains(o);
            }

            @Override
            public boolean containsAll(@NonNull Collection<?> c) {
                return set.containsAll(c);
            }

            @Override
            public boolean isEmpty() {
                return set.isEmpty();
            }

            @Override
            public @NonNull Iterator<E> iterator() {
                return set.iterator();
            }

            @Override
            public boolean remove(Object o) {
                return set.remove(o);
            }

            @Override
            public boolean removeAll(@NonNull Collection<?> c) {
                return set.removeAll(c);
            }

            @Override
            public boolean retainAll(@NonNull Collection<?> c) {
                return set.retainAll(c);
            }

            @Override
            public int size() {
                return set.size();
            }

            @Override
            public Object @NonNull [] toArray() {
                return set.toArray();
            }

            @Override
            public <T> T @NonNull [] toArray(T @NonNull [] a) {
                return set.toArray(a);
            }
        };
    }

}
