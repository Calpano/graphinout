package com.calpano.graphinout.xml;

import com.calpano.graphinout.exception.GioException;

/**
 *
 * @author rbaba
 * @param <V> value
 * @param <T> Type
 */
@FunctionalInterface
public interface XMLValidator<V, T> {

    void validate(V value, T Type) throws GioException;
}
