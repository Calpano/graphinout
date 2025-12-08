package com.graphinout.foundation.pure.json.document;

public interface IJsonContainer extends IJsonValue {

    default boolean isEmpty() {
        return size() == 0;
    }

    default boolean isPrimitive() {return false;}

    int size();

}
