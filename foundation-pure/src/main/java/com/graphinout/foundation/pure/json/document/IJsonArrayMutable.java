package com.graphinout.foundation.pure.json.document;

/** Mutable */
public interface IJsonArrayMutable extends IJsonArrayAppendable, IJsonValueMutable {

    default IJsonArrayMutable add(double[] values) {
        for (double v : values) {
            add(factory().createDouble(v));
        }
        return this;
    }

    void remove(int index) throws ArrayIndexOutOfBoundsException;

    void set(int index, IJsonValue jsonValue) throws ArrayIndexOutOfBoundsException;

}
