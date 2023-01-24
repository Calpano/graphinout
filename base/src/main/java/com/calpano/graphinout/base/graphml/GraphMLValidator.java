package com.calpano.graphinout.base.graphml;

import com.calpano.graphinout.base.exception.GraphmlException;

/**
 * @param <V> value
 * @param <T> Type
 * @author rbaba
 */
@FunctionalInterface
public interface GraphMLValidator<V, T> {

    void validate(V value, T Type) throws GraphmlException;
}
