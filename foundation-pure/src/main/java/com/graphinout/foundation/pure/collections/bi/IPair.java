package com.graphinout.foundation.pure.collections.bi;

import org.jspecify.annotations.NonNull;

import java.util.Comparator;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

import static com.graphinout.foundation.pure.functional.Nullables.mapOrDefault;


/**
 * A tuple storing two objects of different types. See {@link ICouple} for a tuple storing two objects of the same type.
 * <p>
 * Use org.xydra.index.impl.ImmutableMapEntry as a {@link Map.Entry} implementation.
 *
 * @param <A> first element type
 * @param <B> second element type
 */
public interface IPair<A, B> {

    /**
     * equals implementation for IPair instances, to be used in conjunction with {@link #hashCode(IPair)}.
     */
    static boolean equals(IPair<?, ?> a, Object b) {
        if (a == b) {
            return true;
        }
        if (a == null || b == null) {
            return false;
        }
        // Does not work in GWT: IPair.class.isAssignableFrom(obj.getClass())
        if (b instanceof IPair<?, ?>) {
            IPair<?, ?> other = (IPair<?, ?>) b;
            try {
                if (a.first() == null) {
                    if (other.first() != null) {
                        return false;
                    }
                } else if (!a.first().equals(other.first())) {
                    return false;
                }
                if (a.second() == null) {
                    return other.second() == null;
                } else return a.second().equals(other.second());
            } catch (ClassCastException e) {
                // ok
                return false;
            }
        } else {
            return false;
        }
    }

    /**
     * hashCode implementation for IPair instances, to be used in conjunction with {@link #equals(IPair, Object)}.
     */
    static int hashCode(IPair<?, ?> pair) {
        final int prime = 31;
        int result = 1;
        result = prime * result + mapOrDefault(pair.first(), Object::hashCode, 0);
        result = prime * result + mapOrDefault(pair.second(), Object::hashCode, 0);
        return result;
    }

    static <I, O> IPair<O, O> map(IPair<I, I> pair, Function<I, O> mapper) {
        return IPair.of(Optional.ofNullable(pair.first()).map(mapper).orElse(null), Optional.ofNullable(pair.second()).map(mapper).orElse(null));
    }

    /**
     * Create an ordered pair with the smaller element first. Keep order when elements are equal according to ordering.
     *
     * @param a          first element
     * @param b          second element
     * @param comparator to compare elements
     * @param <T>        element type
     * @return a new IPair instance
     */
    static @NonNull <T> IPair<T, T> of(T a, T b, Comparator<T> comparator) {
        if (comparator.compare(a, b) <= 0) {
            return IPair.of(a, b);
        } else {
            return IPair.of(b, a);
        }
    }

    static @NonNull <A, B> IPair<A, B> of(A a, B b) {
        return new Pair<>(a, b);
    }

    /**
     * @param pair       might get returned
     * @param comparator to compare elements
     * @return pair in ordered sequence, i.e. first <= second
     */
    static <T> IPair<T, T> order(IPair<T, T> pair, Comparator<T> comparator) {
        assert pair != null;
        assert comparator != null;
        if (comparator.compare(pair.first(), pair.second()) <= 0) return pair;
        else return reverse(pair);
    }

    static <T> IPair<T, T> reverse(IPair<T, T> pair) {
        return IPair.of(pair.second(), pair.first());
    }

    static <T> Stream<T> stream(IPair<T, T> pair) {
        if (pair == null) return Stream.empty();
        if (pair.first() == null) {
            if (pair.second() == null) return Stream.empty();
            else return Stream.of(pair.second());
        }
        if (pair.second() == null) return Stream.of(pair.first());
        else return Stream.of(pair.first(), pair.second());
    }

    default A first() {
        return getFirst();
    }

    A getFirst();

    B getSecond();

    default B second() {
        return getSecond();
    }

}
