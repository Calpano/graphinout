package com.graphinout.foundation.pure.json;

import com.graphinout.foundation.pure.json.document.IJsonXmlString;
import com.graphinout.foundation.pure.bridge.Java9;

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
        this.javaClasses = Java9.Set.of(classes);
    }

    public ValueType valueType() {
        switch (this) {
            case Object:
                return ValueType.Object;
            case Array:
                return ValueType.Array;
            case Number:
            case Null:
            case Boolean:
            case String:
            case XmlString:
                return ValueType.Primitive;
            case Document:
            case Container:
            case Primitive:
            case Undefined:
                throw new IllegalStateException("Abstract JSON type has no valueType");
            case ArrayIndex:
            case Property:
            case PropertyKey:
                throw new IllegalStateException("Intermediate JSON type has no valueType");
            default:
                throw new IllegalArgumentException();
        }
    }

}
