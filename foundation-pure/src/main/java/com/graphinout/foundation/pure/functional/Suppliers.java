package com.graphinout.foundation.pure.functional;

import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

/**
 * Contains commonly used {@link Supplier} definitions and utilities.
 */
public class Suppliers {

    private Suppliers() {
    }

    /**
     * Return a conjunction of the given {@link BooleanSupplier}s.
     *
     * @return A new {@link BooleanSupplier} that evaluates to true only if both parameters evaluate to true.
     */
    public static BooleanSupplier and(BooleanSupplier a, BooleanSupplier b) {
        return () -> a.getAsBoolean() && b.getAsBoolean();
    }

    /**
     * Always returns false.
     */
    @SuppressWarnings("squid:S3400")
    public static boolean false_() {
        return false;
    }

    /**
     * Return the inverse of the given {@link BooleanSupplier}.
     *
     * @return A new {@link BooleanSupplier} that evaluates to true only if the given supplier evaluates to false.
     */
    public static BooleanSupplier not(BooleanSupplier supplier) {
        return () -> !supplier.getAsBoolean();
    }

    /**
     * Return an inclusive disjunction of the given {@link BooleanSupplier}s.
     *
     * @return A new {@link BooleanSupplier} that evaluates to true only if one or both parameters evaluate to true.
     */
    public static BooleanSupplier or(BooleanSupplier a, BooleanSupplier b) {
        return () -> a.getAsBoolean() || b.getAsBoolean();
    }

    public static <R> Supplier<R> supplyNull() {
        return () -> null;
    }

    /**
     * Wrap the given {@link Runnable} to supply a null value of the given result type.
     *
     * @param runnable The runnable to wrap.
     * @param <R>      The return type of the supplier.
     */
    public static <R> Supplier<R> supplyNull(Runnable runnable) {
        return () -> {
            runnable.run();
            return null;
        };
    }

    /**
     * Always returns true.
     */
    @SuppressWarnings("squid:S3400")
    public static boolean true_() {
        return true;
    }

}
