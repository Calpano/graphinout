package com.graphinout.foundation.util;

public class BooleanRef {

    public boolean value;

    public BooleanRef(boolean value) {
        this.value = value;
    }

    public static BooleanRef booleanRef(boolean value) {
        return new BooleanRef(value);
    }

    public boolean get() {
        return value;
    }

    public void set(boolean value) {
        this.value = value;
    }

}
