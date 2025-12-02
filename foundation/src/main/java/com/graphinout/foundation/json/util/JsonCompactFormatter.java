package com.graphinout.foundation.json.util;

import com.graphinout.foundation.json.JSON;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class JsonCompactFormatter {


    public static final String COMMA = ",";
    public static final String NEWLINE = "\n";
    public static final String SPACE = " ";
    public static final String SPACE2 = "  ";
    public static final String SPACE4 = "    ";

    private static final int INDENT_SIZE = 2;
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

    /**
     * @param depth
     * @param indent for debug, something else than space can be put here.
     * @return
     */
    static String indent(int depth, @SuppressWarnings("SameParameterValue") String indent) {
        return indent.repeat(depth * INDENT_SIZE);
    }

    static String indent(int depth) {
        return indent(depth, SPACE);
    }

    static Block listToBlock(int depth, List<Object> jaJason, FormatterConfig config) {
        BlockContainer blockArray = BlockContainer.createArrayBlock(depth);
        for (Object o : jaJason) {
            Block valueBlock = valueToBlock(depth + 1, o, config);
            blockArray.children.add(valueBlock);
        }
        return blockArray;
    }

    static Block mapToBlock(int depth, Map<String, Object> jaJason, FormatterConfig config) {
        BlockContainer blockObject = BlockContainer.createObjectBlock(depth);
        for (Map.Entry<String, Object> entry : jaJason.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();

            Block valueBlock = valueToBlock(depth + 1, value, config);
            BlockProperty propertyBlock = new BlockProperty(depth + 1, key, valueBlock);

            blockObject.children.add(propertyBlock);
        }
        return blockObject;
    }

    public static String oneChildLine(List<String> lines) {
        return String.join("\n", lines);
    }

    public static String oneLargeLine(List<List<String>> lines, String joiner) {
        StringBuilder b = new StringBuilder();
        for (int i = 0; i < lines.size(); i++) {
            List<String> childLines = lines.get(i);
            b.append(oneChildLine(childLines));
            if (i < lines.size() - 1) {
                b.append(joiner);
            }
        }
        return b.toString();
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
    static Block valueToBlock(int depth, Object jaJson, FormatterConfig config) {
        if (jaJson instanceof Map<?, ?> map) {
            return mapToBlock(depth, (Map<String, Object>) map, config);
        } else if (jaJson instanceof List<?> list) {
            return listToBlock(depth, (List<Object>) list, config);
        } else {
            return primitiveToBlock(depth, jaJson);
        }
    }

    private FormatterConfig config() {
        return config;
    }

}
