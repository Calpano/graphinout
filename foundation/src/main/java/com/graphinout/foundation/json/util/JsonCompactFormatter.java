package com.graphinout.foundation.json.util;

import com.graphinout.foundation.json.JSON;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class JsonCompactFormatter {


    public static final String COMMA = ",";
    public static final String NEWLINE = "\n";
    public static final String SPACE = " ";

    private static final int INDENT_SIZE = 2;
    private final int maxLineLength;
    private final Set<String> forceMultiLineKeys;

    private JsonCompactFormatter(int maxLineLength, Set<String> forceMultiLineKeys) {
        this.maxLineLength = maxLineLength;
        this.forceMultiLineKeys = forceMultiLineKeys;
    }

    public static String formatCompact(Object jaJson) {
        return formatCompact(jaJson, 80, Collections.emptySet());
    }

    public static String formatCompact(Object jaJson, int maxLineLength) {
        return formatCompact(jaJson, maxLineLength, Collections.emptySet());
    }

    public static String formatCompact(Object jaJson, int maxLineLength, Set<String> forceMultiLineKeys) {
        JsonCompactFormatter formatter = new JsonCompactFormatter(maxLineLength, forceMultiLineKeys);
        Block block = valueToBlock(0, jaJson, formatter.config());
        block.compact(maxLineLength);

        return IndentWriter.of(block).resultString();
    }

    /**
     *
     * @param depth
     * @param indent for debug, something else than space can be put here.
     * @return
     */
    static String indent(int depth, @SuppressWarnings("SameParameterValue") String indent) {
        return indent.repeat(depth * INDENT_SIZE);
    }

    static Block listToBlock(int depth, List<Object> jaJason, Config config) {
        BlockContainer blockArray = BlockContainer.createArrayBlock(depth);
        for (Object o : jaJason) {
            Block valueBlock = valueToBlock(depth + 1, o, config);
            blockArray.children.add(valueBlock);
        }
        return blockArray;
    }

    static Block mapToBlock(int depth, Map<String, Object> jaJason, Config config) {
        BlockContainer blockObject = BlockContainer.createObjectBlock(depth);
        for (Iterator<Map.Entry<String, Object>> iterator = jaJason.entrySet().iterator(); iterator.hasNext(); ) {
            Map.Entry<String, Object> entry = iterator.next();
            String key = entry.getKey();
            Object value = entry.getValue();

            Block valueBlock = valueToBlock(depth + 1, value, config);
            BlockProperty propertyBlock = new BlockProperty(depth + 1, key, valueBlock);

            blockObject.children.add(propertyBlock);
        }
        return blockObject;
    }

    static Block primitiveToBlock(int depth, Object jaJason) {
        return switch (jaJason) {
            case null -> new BlockValue(depth, "null");
            case String s -> new BlockValue(depth, "\"" + JSON.jsonEscape(s) + "\"");
            case Number n -> new BlockValue(depth, n.toString());
            case Boolean b -> new BlockValue(depth, b.toString());
            default -> new BlockValue(depth, jaJason.toString());
        };
    }

    @SuppressWarnings("unchecked")
    static Block valueToBlock(int depth, Object jaJson, Config config) {
        if (jaJson instanceof Map<?, ?> map) {
            return mapToBlock(depth, (Map<String, Object>) map, config);
        } else if (jaJson instanceof List<?> list) {
            return listToBlock(depth, (List<Object>) list, config);
        } else {
            return primitiveToBlock(depth, jaJson);
        }
    }

    private Config config() {
        return new Config(maxLineLength, forceMultiLineKeys);
    }


}
