package com.graphinout.foundation.pure.json.document;


import com.graphinout.foundation.pure.bridge.JavaPlatform;
import com.graphinout.foundation.pure.json.path.IJsonNavigationPath;
import com.graphinout.foundation.pure.json.writer.JsonWriter;
import org.jspecify.annotations.Nullable;

import java.util.function.BiConsumer;

/**
 * A scalar JSON value: string, number, boolean or null.
 */
public interface IJsonPrimitive extends IJsonValue {

    default double asDouble() {
        if (isNumber()) {
            return asNumber().doubleValue();
        }
        throw new IllegalStateException("Cannot convert " + jsonType() + " to double");
    }

    default <T> T castTo(Class<T> clazz) {
        return JavaPlatform.Class.cast(clazz, base());
    }

    default void fire(JsonWriter jsonWriter) {
        switch (jsonType()) {
            case Null:
                jsonWriter.onNull();
                break;
            case Boolean:
                jsonWriter.onBoolean(castTo(Boolean.class));
                break;
            case String:
                jsonWriter.onString(castTo(String.class));
                break;
            case XmlString:
                castTo(IJsonXmlString.class).fire(jsonWriter);
                break;
            case Number:
                jsonWriter.onNumber((Number) base());
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + jsonType());
        }
    }

    default void forEachLeaf(IJsonNavigationPath prefix, BiConsumer<IJsonNavigationPath, IJsonPrimitive> path_primitive) {
        path_primitive.accept(prefix, this);
    }

    default boolean isArray() {return false;}

    default boolean isObject() {return false;}

    default boolean isPrimitive() {return true;}

    default Object toJaJsonPrimitive() {
        switch (jsonType()) {
            case String:
                return asString();
            case XmlString:
                return castTo(IJsonXmlString.class).toJaJsonMap();
            case Boolean:
                return asBoolean();
            case Number:
                return asNumber();
            case Null:
                return null;
            default:
                throw new IllegalStateException("Unexpected value: " + jsonType());
        }
    }

    /** JSON null is Java null */
    default @Nullable String toJavaString() {
        switch (jsonType()) {
            case String:
                return asString();
            case XmlString:
                return castTo(IJsonXmlString.class).rawXmlString();
            case Boolean:
                return asBoolean().toString();
            case Number:
                return asNumber().toString();
            case Null:
                return null;
            default:
                throw new IllegalStateException("Unexpected value: " + jsonType());
        }
    }

}
