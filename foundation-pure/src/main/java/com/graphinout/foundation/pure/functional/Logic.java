package com.graphinout.foundation.pure.functional;

import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class Logic {


    public static boolean ifFalse(BooleanSupplier booleanSupplier, Runnable falseRunnable) {
        boolean b = booleanSupplier.getAsBoolean();
        if (!b) {
            falseRunnable.run();
        }
        return b;
    }

    public static boolean ifTrue(boolean b, Runnable trueRunnable) {
        if (b) {
            trueRunnable.run();
        }
        return b;
    }

    public static boolean ifTrue(BooleanSupplier booleanSupplier, Runnable trueRunnable) {
        return ifTrue(booleanSupplier.getAsBoolean(), trueRunnable);
    }

    public static boolean iff(BooleanSupplier booleanSupplier, BooleanConsumer booleanConsumer) {
        boolean b = booleanSupplier.getAsBoolean();
        booleanConsumer.accept(b);
        return b;
    }

    public static boolean iff(BooleanSupplier booleanSupplier, Runnable trueRunnable, Runnable falseRunnable) {
        boolean b = booleanSupplier.getAsBoolean();
        if (b) {
            trueRunnable.run();
        } else {
            falseRunnable.run();
        }
        return b;
    }

    /**
     * Check a stream for valid elements and consume the first non-matching element.
     *
     * @param stream                   to check
     * @param predicate                to check the elements
     * @param firstNonMatchingConsumer to consume the first non-matching element
     * @param <T>                      type of the stream elements
     * @return true if all elements match the predicate
     */
    public static <T> boolean notAllMatch(Stream<T> stream, Predicate<T> predicate, Consumer<T> firstNonMatchingConsumer) {
        return !stream.allMatch(element -> {
            if (predicate.test(element)) {
                return true;
            } else {
                firstNonMatchingConsumer.accept(element);
                return false;
            }
        });
    }

}
