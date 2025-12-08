package com.graphinout.foundation.pure.collections.bi;

import org.jspecify.annotations.NonNull;

import java.util.Objects;

/**
 * An ordered pair (2-tuple) of two elements sharing the same type.
 *
 * @param <T> the type of the elements
 */
public interface ICouple<T> extends IPair<T, T> {

    static <T> boolean isOrderedEqual(ICouple<T> a, Object b) {
        if (a == b) return true;
        if (a == null || b == null) return false;
        if (b instanceof ICouple) {
            ICouple<?> other = (ICouple<?>) b;
            return Objects.equals(a.first(), other.first()) && Objects.equals(a.second(), other.second());
        } else {
            return false;
        }
    }

    /**
     * Two couples are equal if their elements are equal, regardless of their order.
     */
    static <T> boolean isUnorderedEqual(T a1, T b1, T a2, T b2) {
        if (Objects.equals(a1, a2)) {
            return Objects.equals(b1, b2);
        }
        // or twisted?
        if (Objects.equals(a1, b2)) {
            return Objects.equals(a2, b1);
        }
        return false;
    }

    /**
     * Create a new couple of two elements.
     *
     * @param a   the first element
     * @param b   the second element
     * @param <T> the type of the elements
     * @return a new ordered couple
     */
    static @NonNull <T> ICouple<T> of(T a, T b) {
        return new Couple<>(a, b);
    }

    /**
     * @return true if the couple contains the element
     */
    default boolean contains(T a) {
        return a.equals(first()) || a.equals(second());
    }

}
