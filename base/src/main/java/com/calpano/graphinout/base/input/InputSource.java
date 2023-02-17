package com.calpano.graphinout.base.input;

/**
 * See also {@link SingleInputSource} and {@link MultiInputSource}
 */
public interface InputSource {

    default boolean isMulti() {
        return !isSingle();
    }

    boolean isSingle();

    String name();
}
