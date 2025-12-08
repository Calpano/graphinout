package com.graphinout.foundation.pure.collections.bi;

import org.jspecify.annotations.NonNull;

import java.util.Map;

/**
 * A tuple storing two objects.
 * <p>
 * Use org.xydra.index.impl.ImmutableMapEntry as a {@link Map.Entry} implementation.
 *
 * @param <A>
 * @param <B>
 * @author dscharrer
 */
public class Pair<A, B> implements IPair<A, B> {

    private final A first;
    private final B second;

    // for de-serialisation only
    protected Pair() {
        first = null;
        second = null;
    }

    public Pair(final A first, final B second) {
        this.first = first;
        this.second = second;
    }

    public static @NonNull <A, B> Pair<A, B> create(final A a, final B b) {
        return new Pair<>(a, b);
    }

    /**
     * @deprecated Use {@link IPair#of(Object, Object)}
     */
    @Deprecated
    public static <A, B> IPair<A, B> of(A a, B b) {
        return IPair.of(a, b);
    }

    /**
     * Can compare to any {@link IPair}
     */
    @Override
    public boolean equals(final Object obj) {
        return IPair.equals(this, obj);
    }

    @Override
    public A getFirst() {
        return this.first;
    }

    @Override
    public B getSecond() {
        return this.second;
    }

    @Override
    public int hashCode() {
        return IPair.hashCode(this);
    }

    public Pair<B, A> inverse() {
        return new Pair<>(this.second, this.first);
    }

    @Override
    public String toString() {
        return "('" + this.first + "', '" + this.second + "')";
    }

}
