package com.calpano.graphinout.graphml;

import com.calpano.graphinout.exception.GioException;

/**
 *
 * @author rbaba
 * @param <V> value
 * @param <T> Type
 */
@FunctionalInterface
public interface GraphMLValidator<V, T> {

    void validate(V value, T Type) throws GioException;
}
