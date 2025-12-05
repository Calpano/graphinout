package com.graphinout.foundation.jajson;

import com.graphinout.foundation.util.BooleanRef;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.Array;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class JaJsonWriter {

    static void writeJson(@Nullable Object value, @Nonnull StringBuilder sb) {
        writeJson(value, sb, false);
    }

    /**
     * @param value to write
     * @param sb    to write to
     */
    static void writeJson(@Nullable Object value, @Nonnull StringBuilder sb, boolean sortMaps) {
        switch (value) {
            case null -> {
                sb.append("null");
                return;
            }
            case Boolean b -> {
                sb.append(b ? "true" : "false");
                return;
            }
            case Number n -> {
                switch (n) {
                    case Double d -> {
                        if (d.isNaN() || d.isInfinite()) {
                            throw new IllegalArgumentException("NaN/Infinity not allowed in JSON");
                        }
                        // Use lowercase 'e' for exponents to follow common convention
                        sb.append(d.toString().replace('E', 'e'));
                    }
                    case Float f -> {
                        if (f.isNaN() || f.isInfinite()) {
                            throw new IllegalArgumentException("NaN/Infinity not allowed in JSON");
                        }
                        // Use lowercase 'e' for exponents to follow common convention
                        sb.append(n.toString().replace('E', 'e'));
                    }
                    case java.math.BigDecimal bd ->
                        // Prefer BigDecimal's canonical toString (which may use exponent)
                        // and normalize exponent letter to lowercase for consistency.
                            sb.append(bd.toString().replace('E', 'e'));
                    default -> sb.append(n);
                }
                return;
            }
            case String s -> {
                writeString(sb, s);
                return;
            }
            case Character c -> {
                writeString(sb, String.valueOf(c));
                return;
            }
            case Enum<?> e -> {
                writeString(sb, e.name());
                return;
            }
            default -> {
            }
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

        if (value instanceof List<?> list) {
            sb.append('[');
            for (int i = 0; i < list.size(); i++) {
                if (i > 0) sb.append(',');
                writeJson(list.get(i), sb);
            }
            sb.append(']');
            return;
        }

        if (value instanceof Map<?, ?> map) {
            sb.append('{');
            final BooleanRef first = BooleanRef.booleanRef(true);
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

    private static void writeString(@Nonnull StringBuilder sb, @Nonnull String s) {
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
                        sb.append(String.format("\\u%04x", (int) ch));
                    } else {
                        sb.append(ch);
                    }
            }
        }
        sb.append('"');
    }

}
