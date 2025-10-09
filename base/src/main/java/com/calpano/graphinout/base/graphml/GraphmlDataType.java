package com.calpano.graphinout.base.graphml;

import com.calpano.graphinout.foundation.json.JsonType;

import javax.annotation.Nullable;

/**
 * Data types for {@link IGraphmlData} values, defined in corresponding {@link IGraphmlKey}.
 */
public enum GraphmlDataType {

    typeBoolean("boolean", boolean.class), //
    typeInt("int", int.class), //
    typeLong("long", long.class), //
    typeFloat("float", float.class), //
    typeDouble("double", double.class), //
    /** fallback/default type */
    typeString("string", String.class);

    public final Class<?> clazz;
    public final String graphmlName;

    GraphmlDataType(String graphmlName, Class<?> clazz) {
        this.graphmlName = graphmlName;
        this.clazz = clazz;
    }

    public static GraphmlDataType fromGraphmlName(String graphmlName) {
        for (GraphmlDataType d : values()) {
            if (d.graphmlName.equals(graphmlName)) {
                return d;
            }
        }
        throw new IllegalArgumentException("Could not interpret '" + graphmlName + "' as GioDataType");
    }

    public static GraphmlDataType fromJavaType(Class<?> type) {
        if (type.equals(Float.class)) {
            return typeFloat;
        } else if (type.equals(Double.class)) {
            return typeDouble;
        } else if (type.equals(Integer.class) || type.equals(Short.class) || type.equals(Byte.class)) {
            return typeInt;
        } else if (type.equals(Long.class)) {
            return typeLong;
        } else if (type.equals(Boolean.class)) {
            return typeBoolean;
        } else if (type.equals(String.class)) {
            return typeString;
        }
        // everything can be stringified, even BigDecimal and BigInteger
        return typeString;
    }

    /** string to enum */
    public static GraphmlDataType fromString(@Nullable String dataType) {
        return switch (dataType) {
            case "boolean" -> typeBoolean;
            case "int" -> typeInt;
            case "long" -> typeLong;
            case "float" -> typeFloat;
            case "double" -> typeDouble;
            case null, default -> typeString;
        };
    }

    public boolean canRepresent(GraphmlDataType other) {
        if (this == other) return true;
        // sub-types
        return switch (this) {
            case typeBoolean -> false;
            case typeDouble -> other == typeFloat || other == typeInt || other == typeLong;
            case typeFloat -> other == typeInt || other == typeLong;
            case typeLong -> other == typeInt;
            case typeInt -> false;
            case typeString -> true;
        };

    }


    public String graphmlName() {
        return graphmlName;
    }

    /** map to JSON type. but never to JsonType.XmlString, although we should, but we never know when */
    public JsonType jsonType() {
        return switch (this) {
            case typeBoolean -> JsonType.Boolean;
            case typeDouble, typeFloat, typeInt, typeLong -> JsonType.Number;
            case typeString -> JsonType.String;
        };
    }


}
