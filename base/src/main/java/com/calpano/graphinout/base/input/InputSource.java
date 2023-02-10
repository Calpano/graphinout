package com.calpano.graphinout.base.input;

public interface InputSource {
    String name();

    boolean isSingle();
    default boolean isMulti() {
        return !isSingle();
    }
}
