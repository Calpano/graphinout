package com.calpano.graphinout.foundation.json.value;

import com.calpano.graphinout.foundation.json.stream.JsonWriter;

import java.math.BigDecimal;
import java.math.BigInteger;

public interface IJsonPrimitive extends IJsonValue {

    default void fire(JsonWriter jsonWriter) {
        switch (jsonType()) {
            case Null -> jsonWriter.onNull();
            case Boolean -> jsonWriter.onBoolean((Boolean) base());
            case String -> jsonWriter.onString((String) base());
            case Number -> {
                // TODO use same Number to primitive code as elsewhere
                Object base = base();
                if (base instanceof Long v) {
                    jsonWriter.onLong(v);
                } else if (base instanceof Integer v) {
                    jsonWriter.onInteger(v);
                } else if (base instanceof Double v) {
                    jsonWriter.onDouble(v);
                } else if (base instanceof Float v) {
                    jsonWriter.onFloat(v);
                } else if (base instanceof BigInteger v) {
                    jsonWriter.onBigInteger(v);
                } else if (base instanceof BigDecimal v) {
                    jsonWriter.onBigDecimal(v);
                }
            }
            default -> throw new IllegalStateException("Unexpected value: " + jsonType());
        }
    }

    default boolean isArray() {return false;}

    default boolean isObject() {return false;}

    default boolean isPrimitive() {return true;}

}
