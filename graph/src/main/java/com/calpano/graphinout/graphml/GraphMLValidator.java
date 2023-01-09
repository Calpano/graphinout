package com.calpano.graphinout.graphml;

import com.calpano.graphinout.exception.GioException;

/**
 * @param <V> value
 * @param <T> Type
 * @author rbaba
 */
@FunctionalInterface
public interface GraphMLValidator<V, T> {

    void validate(V value, T Type) throws GioException;
}
