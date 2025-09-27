package com.calpano.graphinout.foundation.util;

public interface ThrowingConsumer<T, E extends Throwable> {

    void accept(T object) throws E;

}
