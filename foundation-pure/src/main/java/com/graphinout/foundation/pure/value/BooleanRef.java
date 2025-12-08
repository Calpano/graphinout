package com.graphinout.foundation.pure.value;

/**
 * An boolean by reference, to be used with Java 8 lambdas
 */
public class BooleanRef {

    public boolean value;

    public BooleanRef() {
        this.value = false;
    }

    public BooleanRef(final boolean value) {
        this.value = value;
    }

    public static BooleanRef FALSE() {
        return new BooleanRef(false);
    }

    public static BooleanRef TRUE() {
        return new BooleanRef(true);
    }

    /**
     * Activates this reference if the given boolean is true. This is '|='.
     *
     * @param b to check
     */
    public void activateIf(boolean b) {
        if (b) setTRUE();
    }

    @Override
    public boolean equals(final Object o) {
        return o instanceof BooleanRef && ((BooleanRef) o).value == this.value || o instanceof Boolean && (Boolean) o == this.value;
    }

    public boolean get() {
        return value;
    }

    @Override
    public int hashCode() {
        return Boolean.hashCode(this.value);
    }

    public boolean isFalse() {
        return !this.value;
    }

    public boolean isTrue() {
        return this.value;
    }

    public void set(boolean b) {
        value = b;
    }

    public void setFALSE() {
        this.value = false;
    }

    public void setTRUE() {
        this.value = true;
    }

    @Override
    public String toString() {
        return Boolean.toString(this.value);
    }

    /** for using in lambdas */
    public boolean value() {
        return this.value;
    }

}
