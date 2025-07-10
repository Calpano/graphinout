package com.calpano.graphinout.base.graphml;

/**
 * @param <T> inputFile Structure
 * @param <E> inputFile resource
 * @author rbaba
 */
@FunctionalInterface
public interface InputSourceStructure<T, E> {

    T structure(E inputStructure);

}
