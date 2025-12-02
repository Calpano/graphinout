package com.graphinout.foundation.jajson;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import javax.annotation.Nonnull;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Implements the Java-JSON from jajson.adoc */
public class JaJson {

    /** Creates a {@link LinkedHashMap} internally to respect insertion order */
    public static JaJsonMapBuilder createMap() {
        return new JaJsonMapBuilder();
    }

    public static boolean isJaJson(Object value) {
        return isJaJsonPrimitive(value) || isJaJsonList(value) || isJaJsonMap(value);
    }

    public static boolean isJaJsonList(Object value) {
        return value instanceof List && ((List<?>) value).stream().allMatch(JaJson::isJaJson);
    }

    public static boolean isJaJsonMap(Object object) {
        return object instanceof Map && ((Map<?, ?>) object).entrySet().stream().allMatch(JaJson::isJaJsonMapEntry);
    }

    public static boolean isJaJsonMapEntry(Object object) {
        return object instanceof Map.Entry<?, ?>  //
                && ((Map.Entry<?, ?>) object).getKey() instanceof String //
                && isJaJson(((Map.Entry<?, ?>) object).getValue()) //
                ;
    }

    public static boolean isJaJsonPrimitive(Object value) {
        return value == null || value instanceof Boolean || value instanceof Number || value instanceof String;
    }

    /**
     * Parse a JSON string into Java structures as defined in jajson.adoc: null -> null, boolean -> Boolean, number ->
     * Integer/Long/Double or BigDecimal (for large numbers), string -> String, array -> List<Object>, object ->
     * Map<String,Object> (LinkedHashMap to preserve order).
     */
    @Nullable
    public static Object parse(@Nullable String json) {
        if (json == null) return null;
        JaJsonParser p = new JaJsonParser(json);
        Object value = p.parseValue();
        p.skipWs();
        if (!p.eof()) {
            throw new IllegalArgumentException("Trailing characters after JSON value at position " + p.pos());
        }
        return value;
    }

    public static @NonNull String toJsonString(Object o) {
        return toString(o);
    }

    @Nonnull
    public static String toString(@Nullable Object javaJson) {
        return toString(javaJson, false);
    }

    @Nonnull
    public static String toString(@Nullable Object javaJson, boolean sortMaps) {
        StringBuilder sb = new StringBuilder();
        JaJsonWriter.writeJson(javaJson, sb, sortMaps);
        return sb.toString();
    }


}
