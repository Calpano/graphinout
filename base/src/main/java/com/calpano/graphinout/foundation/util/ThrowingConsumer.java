package com.calpano.graphinout.foundation.util;

import java.util.function.Consumer;

public interface ThrowingConsumer<T, E extends Throwable> extends Consumer<T> {

    default void accept(T object) {
        try {
            acceptThrowing(object);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    void acceptThrowing(T object) throws E;

}
