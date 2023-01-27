package com.calpano.graphinout.base.graphml;

/**
 * @param <V> value
 * @param <T> Type
 * @author rbaba
 */
@FunctionalInterface
public interface GraphMLValidator<V, T> {

    void validate(V value, T Type) throws Exception;
}
