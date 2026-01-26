package com.graphinout.base.input;

/**
 * See also {@link SingleInputSource} and {@link MultiInputSource}
 */
public interface InputSource extends AutoCloseable {

    default MultiInputSource asMulti() throws IllegalStateException {
        if (isSingle()) throw new IllegalStateException("Not a multi input source");
        return (MultiInputSource) this;
    }

    default ParameterizedInputSource asParameterized() throws IllegalStateException {
        if (this instanceof ParameterizedInputSource parameterizedInputSource) {
            return parameterizedInputSource;
        } else {
            throw new IllegalStateException("Not a single input source");
        }
    }

    default SingleInputSource asSingle() throws IllegalStateException {
        if (!isSingle()) throw new IllegalStateException("Not a single input source");
        return (SingleInputSource) this;
    }

    default boolean isMulti() {
        return !isSingle();
    }

    default boolean isParameterized() {
        return this instanceof ParameterizedInputSource;
    }

    boolean isSingle();

    String name();

}
