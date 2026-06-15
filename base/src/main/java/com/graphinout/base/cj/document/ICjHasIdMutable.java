package com.graphinout.base.cj.document;

/**
 * Mutable variant of {@link ICjHasId}: allows setting the id. {@code <T>} is the concrete builder type for fluent chaining.
 */
public interface ICjHasIdMutable<T> extends ICjHasId {

    T id(String id);

}
