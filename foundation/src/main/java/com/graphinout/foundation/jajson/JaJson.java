package com.graphinout.foundation.jajson;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;

/** Implements the Java-JSON from jajson.adoc */
public class JaJson {

    public static JaJsonMapBuilder createMap() {
        return new JaJsonMapBuilder();
    }

    public static boolean isJaJason(Object value) {
        return isJaJsonPrimitive(value) || isJaJsonList(value) || isJaJsonMap(value);
    }

    public static boolean isJaJsonList(Object value) {
        return value instanceof List && ((List<?>) value).stream().allMatch(JaJson::isJaJason);
    }

    public static boolean isJaJsonMap(Object object) {
        return object instanceof Map && ((Map<?, ?>) object).entrySet().stream().allMatch(JaJson::isJaJsonMapEntry);
    }

    public static boolean isJaJsonMapEntry(Object object) {
        return object instanceof Map.Entry<?, ?>  //
                && ((Map.Entry<?, ?>) object).getKey() instanceof String //
                && isJaJason(((Map.Entry<?, ?>) object).getValue()) //
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

    public static @Nullable String toJsonString(Object o) {
        return toString(o);
    }

    @Nonnull
    public static String toString(@Nullable Object javaJson) {
        StringBuilder sb = new StringBuilder();
        JaJsonWriter.writeJson(javaJson, sb);
        return sb.toString();
    }


}
