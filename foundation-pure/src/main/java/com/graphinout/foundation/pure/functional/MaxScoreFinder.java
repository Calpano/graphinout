package com.graphinout.foundation.pure.functional;

import java.util.function.BiConsumer;

/**
 * Finds the maximally scored element
 * <p>
 * Memory: O(1)
 * <p>
 * Time: O(n)
 *
 * @param <T>
 * @author xamde
 */
public class MaxScoreFinder<T> implements BiConsumer<T, Double> {

    private T maxElement = null;

    private double maxScore = Double.NEGATIVE_INFINITY;

    public static <T> MaxScoreFinder<T> create() {
        return new MaxScoreFinder<>();
    }

    @Override
    public void accept(final T element, final Double score) {
        index(element, score);
    }

    /**
     * Return the first element with the maximal score or null if no element with score > {@link Double#MIN_VALUE}
     * indexed.
     *
     * @return @Nullable
     */
    public T getMax() {
        return this.maxElement;
    }

    public double getScore() {
        return this.maxScore;
    }

    public void index(final T element, final double score) {
        if (score > this.maxScore) {
            this.maxElement = element;
            this.maxScore = score;
        }
    }

}
