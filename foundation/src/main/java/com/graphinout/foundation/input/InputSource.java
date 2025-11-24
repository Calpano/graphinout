package com.graphinout.foundation.input;

/**
 * See also {@link SingleInputSource} and {@link MultiInputSource}
 */
public interface InputSource extends AutoCloseable {

    default MultiInputSource asMulti() throws IllegalStateException {
        if (isSingle()) throw new IllegalStateException("Not a multi input source");
        return (MultiInputSource) this;
    }

    default SingleInputSource asSingle() throws IllegalStateException {
        if (!isSingle()) throw new IllegalStateException("Not a single input source");
        return (SingleInputSource) this;
    }

    default boolean isMulti() {
        return !isSingle();
    }

    boolean isSingle();

    String name();

}
