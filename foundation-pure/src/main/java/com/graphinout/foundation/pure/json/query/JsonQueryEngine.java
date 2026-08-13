package com.graphinout.foundation.pure.json.query;

import com.graphinout.foundation.pure.json.document.IJsonArray;
import com.graphinout.foundation.pure.json.document.IJsonValue;
import org.jspecify.annotations.Nullable;

public class JsonQueryEngine {

    private static boolean isAllDigits(String s) {
        if (s.isEmpty()) return false;
        for (int i = 0; i < s.length(); i++) {
            if (!Character.isDigit(s.charAt(i))) return false;
        }
        return true;
    }

    /**
     * Returns {@code true} iff the given {@code value} satisfies every {@link JsonPathCheck} in the query.
     */
    public static boolean matches(JsonQuery query, IJsonValue value) {
        for (JsonPathCheck test : query.pathTests()) {
            if (!matchesTest(test, value)) return false;
        }
        return true;
    }

    /**
     * Recursively walks {@code parts} from {@code index} into {@code current}, then evaluates {@code expect}. A
     * {@code null} current means the path did not resolve to any value.
     */
    private static boolean matchesPath(String[] parts, int index, @Nullable IJsonValue current, IJsonExpect expect) {
        if (index == parts.length) {
            return expect.matches(current);
        }

        if (current == null) {
            // no value to navigate into; remaining path yields absence
            return expect.matches(null);
        }

        String segment = parts[index];

        if (segment.equals("*")) {
            // wildcard: succeed if ANY child satisfies the remainder
            if (current.isObject()) {
                for (String key : current.asObject().keys()) {
                    IJsonValue child = current.asObject().get(key);
                    if (matchesPath(parts, index + 1, child, expect)) return true;
                }
            } else if (current.isArray()) {
                boolean[] found = {false};
                current.asArray().forEach((IJsonValue child) -> {
                    if (!found[0] && matchesPath(parts, index + 1, child, expect)) found[0] = true;
                });
                return found[0];
            }
            return false;
        }

        // Numeric segment → array index
        if (isAllDigits(segment) && current.isArray()) {
            int arrayIndex = Integer.parseInt(segment);
            IJsonArray arr = current.asArray();
            IJsonValue child = arr.hasIndex(arrayIndex) ? arr.get(arrayIndex) : null;
            return matchesPath(parts, index + 1, child, expect);
        }

        // String segment → object property
        if (current.isObject()) {
            IJsonValue child = current.asObject().get(segment);   // null when absent
            return matchesPath(parts, index + 1, child, expect);
        }

        // Current is a primitive or wrong container type → path doesn't resolve
        return expect.matches(null);
    }

    private static boolean matchesTest(JsonPathCheck test, IJsonValue root) {
        String pathStr = test.path().path();
        if (!pathStr.startsWith("/")) throw new IllegalArgumentException("JsonPath must start with '/': " + pathStr);

        String[] parts = pathStr.equals("/") ? new String[0] : pathStr.substring(1).split("/", -1);
        return matchesPath(parts, 0, root, test.expect());
    }

}
