package com.graphinout.foundation.pure.json.document;

/**
 * A JSON value that contains children (an {@link IJsonObject} or {@link IJsonArray}); exposes a size.
 */
public interface IJsonContainer extends IJsonValue {

    default boolean isEmpty() {
        return size() == 0;
    }

    default boolean isPrimitive() {return false;}

    int size();

}
