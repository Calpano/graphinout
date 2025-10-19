package com.graphinout.base.cj.element;

/**
 * Mutable counterpart of {@link ICjArray} that allows adding {@link ICjElement} items during CJ construction.
 */
public interface ICjArrayMutable extends ICjArray {

    void add(ICjElement element);

}
