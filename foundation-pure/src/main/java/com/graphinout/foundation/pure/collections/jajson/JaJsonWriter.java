package com.graphinout.foundation.pure.collections.jajson;


import com.graphinout.foundation.pure.text.Texts;
import com.graphinout.foundation.pure.value.BooleanRef;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

@SuppressWarnings({"IfCanBeSwitch", "PatternVariableCanBeUsed"})
public class JaJsonWriter {

    static void writeJson(@Nullable Object value, @NonNull StringBuilder sb) {
        writeJson(value, sb, false);
    }

    /**
     * @param value to write
     * @param sb    to write to
     */
    static void writeJson(@Nullable Object value, @NonNull StringBuilder sb, boolean sortMaps) {
        if (value == null) {
            sb.append("null");
            return;
        } else if (value instanceof Boolean) {
            Boolean b = (Boolean) value;
            sb.append(b ? "true" : "false");
            return;
        } else if (value instanceof Number) {
            Number n = (Number) value;
            // Prefer BigDecimal's canonical toString (which may use exponent)
            // and normalize exponent letter to lowercase for consistency.
            if (n instanceof Double) {
                Double d = (Double) n;
                if (d.isNaN() || d.isInfinite()) {
                    throw new IllegalArgumentException("NaN/Infinity not allowed in JSON");
                }
                // Use lowercase 'e' for exponents to follow common convention
                sb.append(d.toString().replace('E', 'e'));
            } else if (n instanceof Float) {
                Float f = (Float) n;
                if (f.isNaN() || f.isInfinite()) {
                    throw new IllegalArgumentException("NaN/Infinity not allowed in JSON");
                }
                // Use lowercase 'e' for exponents to follow common convention
                sb.append(n.toString().replace('E', 'e'));
            } else if (n instanceof BigDecimal) {
                BigDecimal bd = (BigDecimal) n;
                sb.append(bd.toString().replace('E', 'e'));
            } else {
                sb.append(n);
            }
            return;
        } else if (value instanceof String) {
            String s = (String) value;
            writeString(sb, s);
            return;
        } else if (value instanceof Character) {
            char c = (Character) value;
            writeString(sb, String.valueOf(c));
            return;
        } else if (value instanceof Enum<?>) {
            Enum<?> e = (Enum<?>) value;
            writeString(sb, e.name());
            return;
        }

        if (value.getClass().isArray()) {
            sb.append('[');
            int len = Array.getLength(value);
            for (int i = 0; i < len; i++) {
                if (i > 0) sb.append(',');
                Object element = Array.get(value, i);
                writeJson(element, sb);
            }
            sb.append(']');
            return;
        }

        if (value instanceof List<?>) {
            List<?> list = (List<?>) value;
            sb.append('[');
            for (int i = 0; i < list.size(); i++) {
                if (i > 0) sb.append(',');
                writeJson(list.get(i), sb);
            }
            sb.append(']');
            return;
        }

        if (value instanceof Map<?, ?>) {
            Map<?, ?> map = (Map<?, ?>) value;
            sb.append('{');
            final BooleanRef first = BooleanRef.TRUE();
            Map<?, ?> usedMap = map;
            if (sortMaps) {
                usedMap = new TreeMap<>(map);
            }
            usedMap.forEach((k, v) -> {
                if (!first.value) sb.append(',');
                first.value = false;
                writeString(sb, k == null ? "null" : String.valueOf(k));
                sb.append(':');
                writeJson(v, sb);
            });
            sb.append('}');
            return;
        }

        throw new IllegalArgumentException("Unsupported type for JSON serialization: " + value.getClass());
    }

    private static void writeString(@NonNull StringBuilder sb, @NonNull String s) {
        sb.append('"');
        for (int i = 0; i < s.length(); i++) {
            char ch = s.charAt(i);
            switch (ch) {
                case '"':
                    sb.append("\\\"");
                    break;
                case '\\':
                    sb.append("\\\\");
                    break;
                case '\b':
                    sb.append("\\b");
                    break;
                case '\f':
                    sb.append("\\f");
                    break;
                case '\n':
                    sb.append("\\n");
                    break;
                case '\r':
                    sb.append("\\r");
                    break;
                case '\t':
                    sb.append("\\t");
                    break;
                default:
                    if (ch < 0x20) {
                        // Handle control characters by escaping them as \\uXXXX
                        sb.append(Texts.asUnicodeEscape(ch));
                    } else if (ch >= 0x7f && ch <= 0x9f) {
                        // Handle delete and C1 control characters
                        sb.append(Texts.asUnicodeEscape(ch));
                    } else {
                        sb.append(ch);
                    }
            }
        }
        sb.append('"');
    }

}
