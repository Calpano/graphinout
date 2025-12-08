package com.graphinout.foundation.pure.json.document;

/** Mutable */
public interface IJsonArrayMutable extends IJsonArrayAppendable, IJsonValueMutable {

    void remove(int index) throws ArrayIndexOutOfBoundsException;

    void set(int index, IJsonValue jsonValue) throws ArrayIndexOutOfBoundsException;

}
