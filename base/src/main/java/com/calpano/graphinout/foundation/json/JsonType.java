package com.calpano.graphinout.foundation.json;

import com.calpano.graphinout.foundation.json.value.IJsonXmlString;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Set;

public enum JsonType {

    Document,
    /** any container */
    Container, Object, Array,

    ArrayIndex,

    /** key and value */
    Property, PropertyKey,
    /** any primitive */
    Primitive, Number( //
            Integer.class, int.class, //
            Float.class, float.class, //
            Double.class, double.class, //
            Long.class, long.class, //
            Byte.class, byte.class, //
            Short.class, short.class, //
            Character.class, char.class, //
            Number.class, //
            BigInteger.class,  //
            BigDecimal.class //
    ), Null, Boolean(Boolean.class, boolean.class), String(String.class),

    /** not even null */
    Undefined(),

    XmlString(IJsonXmlString.class);

    public enum ValueType {
        Object, Array, Primitive
    }

    public enum ContainerType {
        Object, Array
    }

    public final Set<Class<?>> javaClasses;

    JsonType(Class<?>... classes) {
        this.javaClasses = Set.of(classes);
    }

    public ValueType valueType() {
        return switch (this) {
            case Object -> ValueType.Object;
            case Array -> ValueType.Array;
            case Number, Null, Boolean, String, XmlString -> ValueType.Primitive;
            case Document, Container, Primitive, Undefined ->
                    throw new IllegalStateException("Abstract JSON type has no valueType");
            case ArrayIndex, Property, PropertyKey ->
                    throw new IllegalStateException("Intermediate JSON type has no valueType");
        };
    }

}
