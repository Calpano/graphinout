package com.graphinout.foundation.pure.functional;

import java.util.Objects;
import java.util.function.Predicate;

/**
 * Represents a predicate (boolean-valued function) of three arguments.
 *
 * <p>
 * This is a <a href="package-summary.html">functional interface</a> whose functional method is {@link #test(Object, Object, Object)}.
 *
 * @param <K> the type of the first argument to the operation
 * @param <L> the type of the second argument to the operation
 * @param <M> the type of the third argument to the operation
 * @see Predicate
 */
@FunctionalInterface
public interface TriPredicate<K, L, M> {

    /**
     * Returns a composed predicate that represents a short-circuiting logical AND of this predicate and another. When
     * evaluating the composed predicate, if this predicate is {@code false}, then the {@code other} predicate is not
     * evaluated.
     *
     * <p>
     * Any exceptions thrown during evaluation of either predicate are relayed to the caller; if evaluation of this
     * predicate throws an exception, the {@code other} predicate will not be evaluated.
     *
     * @param other a predicate that will be logically-ANDed with this predicate
     * @return a composed predicate that represents the short-circuiting logical AND of this predicate and the
     * {@code other} predicate
     * @throws NullPointerException if other is null
     */
    default TriPredicate<K, L, M> and(final TriPredicate<? super K, ? super L, ? super M> other) {
        Objects.requireNonNull(other);
        return (k, l, m) -> test(k, l, m) && other.test(k, l, m);
    }

    /**
     * Returns a predicate that represents the logical negation of this predicate.
     *
     * @return a predicate that represents the logical negation of this predicate
     */
    default TriPredicate<K, L, M> negate() {
        return (k, l, m) -> !test(k, l, m);
    }

    /**
     * Returns a composed predicate that represents a short-circuiting logical OR of this predicate and another. When
     * evaluating the composed predicate, if this predicate is {@code true}, then the {@code other} predicate is not
     * evaluated.
     *
     * <p>
     * Any exceptions thrown during evaluation of either predicate are relayed to the caller; if evaluation of this
     * predicate throws an exception, the {@code other} predicate will not be evaluated.
     *
     * @param other a predicate that will be logically-ORed with this predicate
     * @return a composed predicate that represents the short-circuiting logical OR of this predicate and the
     * {@code other} predicate
     * @throws NullPointerException if other is null
     */
    default TriPredicate<K, L, M> or(final TriPredicate<? super K, ? super L, ? super M> other) {
        Objects.requireNonNull(other);
        return (k, l, m) -> test(k, l, m) || other.test(k, l, m);
    }

    /**
     * Performs this operation on the given three arguments.
     *
     * @param k first input argument
     * @param l second input argument
     * @param m third input argument
     */
    boolean test(K k, L l, M m);

}
