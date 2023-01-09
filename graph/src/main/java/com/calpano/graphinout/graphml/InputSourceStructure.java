package com.calpano.graphinout.graphml;

/**
 *
 * @author rbaba
 * @param <T> inputFile Structure
 * @param <E> inputFile resource
 */
@FunctionalInterface
public interface InputSourceStructure<T, E> {

    T structure(E inputStrucure);
}
