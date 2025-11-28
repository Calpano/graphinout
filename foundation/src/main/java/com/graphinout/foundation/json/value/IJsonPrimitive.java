package com.graphinout.foundation.json.value;


import com.graphinout.foundation.json.JsonType;
import com.graphinout.foundation.json.path.IJsonNavigationPath;
import com.graphinout.foundation.json.writer.JsonWriter;
import org.jspecify.annotations.Nullable;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.function.BiConsumer;

public interface IJsonPrimitive extends IJsonValue {

    default <T> T castTo(Class<T> clazz) {
        return clazz.cast(base());
    }

    default void fire(JsonWriter jsonWriter) {
        switch (jsonType()) {
            case JsonType.Null -> jsonWriter.onNull();
            case JsonType.Boolean -> jsonWriter.onBoolean(castTo(Boolean.class));
            case JsonType.String -> jsonWriter.onString(castTo(String.class));
            case JsonType.XmlString -> castTo(IJsonXmlString.class).fire(jsonWriter);
            case JsonType.Number -> {
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

    default void forEachLeaf(IJsonNavigationPath prefix, BiConsumer<IJsonNavigationPath, IJsonPrimitive> path_primitive) {
        path_primitive.accept(prefix, this);
    }

    default boolean isArray() {return false;}

    default boolean isObject() {return false;}

    default boolean isPrimitive() {return true;}

    default Object toJaJsonPrimitive() {
        return switch (jsonType()) {
            case JsonType.String -> asString();
            case JsonType.XmlString -> castTo(IJsonXmlString.class).toJaJsonMap();
            case JsonType.Boolean -> asBoolean();
            case JsonType.Number -> asNumber();
            case JsonType.Null -> null;
            default -> throw new IllegalStateException("Unexpected value: " + jsonType());
        };
    }

    /** JSON null is Java null */
    default @Nullable String toJavaString() {
        return switch (jsonType()) {
            case JsonType.String -> asString();
            case JsonType.XmlString -> castTo(IJsonXmlString.class).rawXmlString();
            case JsonType.Boolean -> asBoolean().toString();
            case JsonType.Number -> asNumber().toString();
            case JsonType.Null -> null;
            default -> throw new IllegalStateException("Unexpected value: " + jsonType());
        };
    }

}
