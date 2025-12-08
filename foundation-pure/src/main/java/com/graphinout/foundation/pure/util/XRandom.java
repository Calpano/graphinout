package com.graphinout.foundation.pure.util;

/**
 * Replacement for XRandom.random() to be able to use deterministic seeds.
 * <p>
 * By default, a DETERMINISTIC seed of 42 is used.
 * <p>
 * Avoid creating multiple instances to obtain deterministic behavior.
 *
 * @author xamde
 */
public class XRandom {

    private static final java.util.Random random;

    static {
        random = new java.util.Random(42);
    }

    public static void main(final String[] args) {
        System.out.println(random());
    }

    /**
     * Uses {@link XRandom}, not so random
     *
     * @return
     */
    public static boolean nextBoolean() {
        return random.nextBoolean();
    }

    /**
     * Uses {@link XRandom}, not so random
     *
     * @param bound
     * @return
     */
    public static int nextInt(final int bound) {
        return random.nextInt(bound);
    }

    /**
     * Uses {@link XRandom}, not so random
     *
     * @return a "random" double in [0,1]
     */
    public static double random() {
        return random.nextDouble();
    }

    /**
     * Uses {@link XRandom}, not so random
     *
     * @param min
     * @param max
     * @return [min, max)
     */
    public static double randomBetween(final double min, final double max) {
        final double delta = max - min;
        final double rnd = random();
        return min + rnd * delta;
    }

    /**
     * Sets the given seed.
     *
     * @param seed
     */
    public static void setSeed(final long seed) {
        random.setSeed(seed);
    }

    /**
     * Sets a new, random seed
     */
    public static void shuffle() {
        // cheat a little: use a new Random generator just to get a pseudo-random seed
        final long seed = new java.util.Random().nextLong();
        setSeed(seed);
    }

}
