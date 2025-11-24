package com.graphinout.foundation.json.value;

public interface IJsonContainer extends IJsonValue {

    default boolean isEmpty() {
        return size() == 0;
    }

    default boolean isPrimitive() {return false;}

    int size();

}
