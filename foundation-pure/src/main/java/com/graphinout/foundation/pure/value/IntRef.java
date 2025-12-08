package com.graphinout.foundation.pure.value;

/**
 * An int by reference, to be used with Java 8 lambdas
 */
public class IntRef {

    public int value;

    public IntRef() {
        this.value = 0;
    }

    public IntRef(final int value) {
        this.value = value;
    }

    public static IntRef create(int initialValue) {
        return new IntRef(initialValue);
    }

    public static IntRef createZero() {
        return new IntRef(0);
    }

    public static IntRef intRef(int initialValue) {
        return new IntRef(initialValue);
    }

    /**
     * Adds the given value to the current value and returns the updated value.
     *
     * <p>Equivalent to {@code setAndGet(value + delta)}.
     *
     * @param delta the value to add
     * @return the updated value
     */
    public int addAndGet(int delta) {
        return setAndGet(value + delta);
    }

    /**
     * Decrements the current value and returns the updated value.
     *
     * <p>Equivalent to {@code addAndGet(-1)}.
     *
     * @return the updated value
     */
    public int decrementAndGet() {
        return addAndGet(-1);
    }

    @Override
    public boolean equals(final Object o) {
        return o instanceof IntRef && ((IntRef) o).value == this.value
                || o instanceof Integer && (Integer) o == this.value;
    }

    /**
     * Returns the current value.
     *
     * @return the current value
     */
    public int get() {
        return value;
    }

    /**
     * Adds the given value to the current value and returns the previous value.
     *
     * <p>Equivalent to {@code getAndSet(value + delta)}.
     *
     * @param delta the value to add
     * @return the previous value
     */
    public int getAndAdd(int delta) {
        return getAndSet(value + delta);
    }

    /**
     * Decrements the current value.
     *
     * <p>Equivalent to {@code getAndAdd(-1)}.
     *
     * @return the previous value
     */
    public int getAndDecrement() {
        return getAndAdd(-1);
    }

    /**
     * Increments the current value and returns the previous value.
     *
     * <p>Equivalent to {@code getAndAdd(1)}.
     *
     * @return the previous value
     */
    public int getAndIncrement() {
        return getAndAdd(1);
    }

    /**
     * Sets the value to {@code newValue} and returns the previous value.
     *
     * @param newValue the new value
     * @return the previous value
     */
    public int getAndSet(int newValue) {
        int oldValue = value;
        value = newValue;
        return oldValue;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(this.value);
    }

    /**
     * Increments the current value and returns the updated value.
     *
     * <p>Equivalent to {@code addAndGet(1)}.
     *
     * @return the updated value
     */
    public int incrementAndGet() {
        return addAndGet(1);
    }

    public void incrementOne() {
        this.value++;
    }

    /**
     * Sets the value to {@code newValue}.
     *
     * @param newValue the new value
     */
    public void set(int newValue) {
        value = newValue;
    }

    /**
     * Sets to the given value and returns the updated value.
     *
     * @param newValue the new value
     * @return the updated value
     */
    public int setAndGet(int newValue) {
        set(newValue);
        return value;
    }

    @Override
    public String toString() {
        return Integer.toString(this.value);
    }

}
