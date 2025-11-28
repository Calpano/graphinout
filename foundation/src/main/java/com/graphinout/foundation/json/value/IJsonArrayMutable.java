package com.graphinout.foundation.json.value;

/** Mutable */
public interface IJsonArrayMutable extends IJsonArrayAppendable, IJsonValueMutable {

    void remove(int index) throws ArrayIndexOutOfBoundsException;

    void set(int index, IJsonValue jsonValue) throws ArrayIndexOutOfBoundsException;

}
