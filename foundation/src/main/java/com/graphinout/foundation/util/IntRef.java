package com.graphinout.foundation.util;

public class IntRef {

    public int value;

    public IntRef(int value) {
        this.value = value;
    }

    public static IntRef intRef(int value) {
        return new IntRef(value);
    }

}
