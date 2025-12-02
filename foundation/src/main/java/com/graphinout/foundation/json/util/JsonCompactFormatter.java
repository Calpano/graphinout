package com.graphinout.foundation.json.util;

import com.graphinout.foundation.json.JSON;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Translates JSON first into a tree of {@link Block} ({@link BlockProperty}, {@link BlockContainer}, {@link BlockValue}) in O(n) and then top-down into a {@link Tile}.
 */
public class JsonCompactFormatter {

    public static final String COMMA = ",";
    public static final String NEWLINE = "\n";
    public static final String SPACE = " ";
    public static final String SPACE2 = "  ";

    private final FormatterConfig config;

    private JsonCompactFormatter(int maxLineLength, Set<String> forceMultiLineKeys) {
        this.config = FormatterConfig.of(maxLineLength, forceMultiLineKeys);
    }

    /** Format with a width of 80 */
    public static String formatCompact(Object jaJson) {
        return formatCompact(jaJson, 80, Collections.emptySet());
    }

    public static String formatCompact(Object jaJson, int maxLineLength) {
        return formatCompact(jaJson, maxLineLength, Collections.emptySet());
    }

    public static String formatCompact(Object jaJson, int maxLineLength, Set<String> forceMultiLineKeys) {
        JsonCompactFormatter formatter = new JsonCompactFormatter(maxLineLength, forceMultiLineKeys);
        FormatterConfig config = formatter.config();
        Block block = valueToBlock(0, jaJson, config);
        Tile tile = block.toTile(config, false);
        return tile.toString();
    }

    static Block listToBlock(int depth, List<Object> jaJason, FormatterConfig config) {
        BlockContainer blockArray = BlockContainer.createArrayBlock();
        for (Object o : jaJason) {
            Block valueBlock = valueToBlock(depth + 1, o, config);
            blockArray.children.add(valueBlock);
        }
        return blockArray;
    }

    static Block mapToBlock(int depth, Map<String, Object> jaJason, FormatterConfig config) {
        BlockContainer blockObject = BlockContainer.createObjectBlock();
        for (Map.Entry<String, Object> entry : jaJason.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();

            Block valueBlock = valueToBlock(depth + 1, value, config);
            BlockProperty propertyBlock = new BlockProperty(key, valueBlock);

            blockObject.children.add(propertyBlock);
        }
        return blockObject;
    }

    static Block primitiveToBlock(Object jaJason) {
        return switch (jaJason) {
            case null -> new BlockValue("null");
            case String s -> new BlockValue("\"" + JSON.jsonEscape(s) + "\"");
            case Number n -> new BlockValue(n.toString());
            case Boolean b -> new BlockValue(b.toString());
            default -> new BlockValue(jaJason.toString());
        };
    }

    @SuppressWarnings("unchecked")
    static Block valueToBlock(int depth, Object jaJson, FormatterConfig config) {
        if (jaJson instanceof Map<?, ?> map) {
            return mapToBlock(depth, (Map<String, Object>) map, config);
        } else if (jaJson instanceof List<?> list) {
            return listToBlock(depth, (List<Object>) list, config);
        } else {
            return primitiveToBlock(jaJson);
        }
    }

    private FormatterConfig config() {
        return config;
    }

}
