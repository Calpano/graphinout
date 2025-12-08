package com.graphinout.foundation.pure.functional;

public interface QuadIntConsumer extends QuadConsumer<Integer, Integer, Integer, Integer> {

    /**
     * @param a
     * @param b
     * @param c
     * @param d
     */
    void accept(int a, int b, int c, int d);

    @Override
    default void accept(final Integer a, final Integer b, final Integer c, final Integer d) {
        accept((int) a, (int) b, (int) c, (int) d);
    }
}
