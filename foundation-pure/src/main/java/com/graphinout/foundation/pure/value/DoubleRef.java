package com.graphinout.foundation.pure.value;

import java.util.function.DoubleConsumer;
import java.util.function.DoubleSupplier;

/**
 * A double by reference, to be used with Java 8 lambdas. org.xydra.index.util.SumDouble
 */
// IMPROVE implement IDoubleVariable
public class DoubleRef implements DoubleConsumer, DoubleSupplier {

    public double value;

    public DoubleRef() {
        this.value = 0;
    }

    public DoubleRef(final double value) {
        this.value = value;
    }

    public static DoubleRef create(final double value) {
        return new DoubleRef(value);
    }

    public static DoubleRef createZero() {
        return create(0.);
    }

    @Override
    public void accept(double value) {
        this.value = value;
    }

    @Override
    public boolean equals(final Object o) {
        return o instanceof DoubleRef && ((DoubleRef) o).value == this.value || o instanceof Double && (Double) o == this.value;
    }

    @Override
    public double getAsDouble() {
        return value;
    }

    @Override
    public int hashCode() {
        return Double.hashCode(this.value);
    }

    @Override
    public String toString() {
        return Double.toString(this.value);
    }

}
