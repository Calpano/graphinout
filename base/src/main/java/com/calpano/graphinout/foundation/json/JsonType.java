package com.calpano.graphinout.foundation.json;

public enum JsonType {

    Document,
    /** any container */
    Container, Object, Array,

    ArrayIndex,

    /** key and value */
    Property, PropertyKey,
    /** any primitive */
    Primitive, Number, Null, Boolean, String

}
