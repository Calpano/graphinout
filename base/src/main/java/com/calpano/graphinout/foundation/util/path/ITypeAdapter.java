package com.calpano.graphinout.foundation.util.path;

/**
 * {@code value -> adapted value}
 *
 * @param <T>
 * @param <A>
 */
@FunctionalInterface
public interface ITypeAdapter<T, A> {

    A toAdapted(T value);

}
