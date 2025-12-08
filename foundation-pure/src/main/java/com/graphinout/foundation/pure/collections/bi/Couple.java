package com.graphinout.foundation.pure.collections.bi;

import java.util.Objects;

/**
 * An ordered pair (2-tuple) of two elements sharing the same type.
 *
 * @param <T> the type of the elements
 */
public class Couple<T> implements ICouple<T> {

    /**
     * The first element of the couple.
     */
    public final T first;

    /**
     * The second element of the couple.
     */
    public final T second;

    /**
     * Create a new couple of two elements.
     */
    public Couple(T first, T second) {
        this.first = first;
        this.second = second;
    }

    @SuppressWarnings("EqualsDoesntCheckParameterClass")
    @Override
    public boolean equals(Object other) {
        return ICouple.isOrderedEqual(this, other);
    }

    @Override
    public T getFirst() {
        return first;
    }

    @Override
    public T getSecond() {
        return second;
    }

    @Override
    public int hashCode() {
        return Objects.hash(first, second);
    }

    @Override
    public String toString() {
        return "Couple{" + //
                "first=" + first + //
                ", second=" + second + //
                "}";
    }

}
