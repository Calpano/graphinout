package com.graphinout.foundation.pure.functional;

/**
 * Function of two primitive {@code double} arguments returning a result of type {@code R}.
 */
@FunctionalInterface
public interface BiDoubleFunction<R> {

    R apply(double a, double b);

}
