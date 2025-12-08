package com.graphinout.foundation.pure.value;

/**
 * An int by reference, to be used with Java 8 lambdas
 */
public class LongRef {

    public long value;

    public LongRef() {
        this.value = 0;
    }

    public LongRef(final long value) {
        this.value = value;
    }

    public static void main(final String[] args) {
        System.out.println(new LongRef(3L).equals(3L));
    }

    @Override
    public boolean equals(final Object o) {
        return o instanceof LongRef && ((LongRef) o).value == this.value
                || o instanceof Long && (Long) o == this.value;
    }

    @Override
    public int hashCode() {
        return Long.hashCode(this.value);
    }

    @Override
    public String toString() {
        return Long.toString(this.value);
    }

}
