package com.calpano.graphinout.base.input;

/**
 * See also {@link SingleInputSource} and {@link MultiInputSource}
 */
public interface InputSource extends AutoCloseable {

    default boolean isMulti() {
        return !isSingle();
    }

    boolean isSingle();

    String name();


}
