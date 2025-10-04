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

    public String graphmlName() {
        return graphmlName;
    }

    public JsonType jsonType() {
        return switch (this) {
            case typeBoolean -> JsonType.Boolean;
            case typeDouble, typeFloat, typeInt, typeLong -> JsonType.Number;
            case typeString -> JsonType.String;
        };
    }


}
