package com.graphinout.base.cj.util;

import com.graphinout.foundation.pure.collections.jajson.JaJson;
import com.graphinout.foundation.pure.json.formatter.JsonCompactFormatter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * <h3>Keyword-aware Rules</h3>
 * (3) Ids always need to be the first element of an object.
 *
 * @deprecated use {@link JsonCompactFormatter}
 */
@Deprecated
public class CjFormatter {

    private static final int MAX_LINE_LENGTH = 60;
    private static final String INDENT_STRING = "  ";

    /**
     * Wrap valid or invalid Connected JSON (CJ) into multiple lines for easier debugging. Each line has a max length of
     * 60 characters. Quoted strings cannot be wrapped. They are the only exception and might exceed the max line
     * length. Lines are wrapped if the next token would go over the limit.
     *
     * @param jaJson a JaJson value as defined in jajson.adoc and {@link JaJson}.
     */
    public static String formatDebug(Object jaJson) {
        StringBuilder sb = new StringBuilder();
        formatValue(jaJson, sb, 0);
        return sb.toString();
    }

    private static void formatValue(Object value, StringBuilder sb, int indentLevel) {
        if (value == null) {
            sb.append("null");
            return;
        }

        if (!(value instanceof String)) {
            String singleLine = toSingleLineString(value);
            if (singleLine.length() <= MAX_LINE_LENGTH - (indentLevel * INDENT_STRING.length())) {
                sb.append(singleLine);
                return;
            }
        }

        if (value instanceof String) {
            sb.append(JaJson.toString(value));
        } else if (value instanceof Number || value instanceof Boolean) {
            sb.append(value.toString());
        } else if (value instanceof List) {
            formatList((List<?>) value, sb, indentLevel);
        } else if (value instanceof Map) {
            formatMap((Map<?, ?>) value, sb, indentLevel);
        } else {
            sb.append(JaJson.toString(String.valueOf(value)));
        }
    }

    private static String toSingleLineString(Object value) {
        if (value == null) return "null";
        if (value instanceof String) return JaJson.toString(value);
        if (value instanceof Number || value instanceof Boolean) return value.toString();
        if (value instanceof List) {
            List<?> list = (List<?>) value;
            if (list.isEmpty()) return "[]";
            return "[ " + list.stream().map(CjFormatter::toSingleLineString).collect(Collectors.joining(", ")) + " ]";
        }
        if (value instanceof Map) {
            Map<?, ?> map = (Map<?, ?>) value;
            if (map.isEmpty()) return "{}";

            List<Object> keys = new ArrayList<>(map.keySet());
            if (keys.contains("id")) {
                keys.remove("id");
                keys.add(0, "id");
            }

            return "{ " + keys.stream()
                    .map(key -> JaJson.toString(String.valueOf(key)) + ": " + toSingleLineString(map.get(key)))
                    .collect(Collectors.joining(", ")) + " }";
        }
        return JaJson.toString(String.valueOf(value));
    }

    private static void formatList(List<?> list, StringBuilder sb, int indentLevel) {
        if (list.isEmpty()) {
            sb.append("[]");
            return;
        }

        if (list.size() == 1 && list.get(0) instanceof Map) {
            String mapSingleLine = toSingleLineString(list.get(0));
            if (!mapSingleLine.isEmpty() && mapSingleLine.length() > 2) {
                String mapContent = mapSingleLine.substring(2, mapSingleLine.length() - 2);
                String singleLine = "[{ " + mapContent + " }]";
                if (singleLine.length() <= MAX_LINE_LENGTH - (indentLevel * INDENT_STRING.length())) {
                    sb.append(singleLine);
                    return;
                }
            }

            sb.append("[{\n");
            formatMapContent((Map<?, ?>) list.get(0), sb, indentLevel + 1);
            sb.append("\n");
            appendIndent(sb, indentLevel);
            sb.append("}]");
            return;
        }

        sb.append("[\n");
        for (int i = 0; i < list.size(); i++) {
            appendIndent(sb, indentLevel + 1);
            formatValue(list.get(i), sb, indentLevel + 1);
            if (i < list.size() - 1) {
                sb.append(",");
            }
            sb.append("\n");
        }
        appendIndent(sb, indentLevel);
        sb.append("]");
    }

    private static void formatMap(Map<?, ?> map, StringBuilder sb, int indentLevel) {
        if (map.isEmpty()) {
            sb.append("{}");
            return;
        }

        List<Object> keys = new ArrayList<>(map.keySet());
        if (keys.contains("id")) {
            keys.remove("id");
            keys.add(0, "id");
        }

        sb.append("{ ");

        Object firstKey = keys.get(0);
        sb.append(JaJson.toString(String.valueOf(firstKey))).append(": ");
        formatValue(map.get(firstKey), sb, indentLevel + 1);

        for (int i = 1; i < keys.size(); i++) {
            sb.append(",\n");
            appendIndent(sb, indentLevel + 1);
            Object key = keys.get(i);
            sb.append(JaJson.toString(String.valueOf(key))).append(": ");
            formatValue(map.get(key), sb, indentLevel + 1);
        }

        sb.append("\n");
        appendIndent(sb, indentLevel);
        sb.append("}");
    }

    private static void formatMapContent(Map<?, ?> map, StringBuilder sb, int indentLevel) {
        if (map.isEmpty()) {
            return;
        }

        List<Object> keys = new ArrayList<>(map.keySet());
        if (keys.contains("id")) {
            keys.remove("id");
            keys.add(0, "id");
        }

        for (int i = 0; i < keys.size(); i++) {
            appendIndent(sb, indentLevel);
            Object key = keys.get(i);
            sb.append(JaJson.toString(String.valueOf(key))).append(": ");
            formatValue(map.get(key), sb, indentLevel);
            if (i < keys.size() - 1) {
                sb.append(",\n");
            }
        }
    }

    private static void appendIndent(StringBuilder sb, int indentLevel) {
        for (int i = 0; i < indentLevel; i++) {
            sb.append(INDENT_STRING);
        }
    }
}
