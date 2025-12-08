package com.graphinout.foundation.pure.json.formatter;

import com.graphinout.foundation.pure.json.JSON;
import com.graphinout.foundation.pure.text.JsonFormatting;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * Translates JSON first into a tree of {@link Block} ({@link BlockProperty}, {@link BlockContainer},
 * {@link BlockValue}) in O(n) and then top-down into a {@link Tile}.
 *
 * @see JsonFormatting Formatting invalid JSON
 */
@SuppressWarnings({"PatternVariableCanBeUsed", "IfCanBeSwitch", "ClassCanBeRecord"})
public class JsonCompactFormatter {

    public static final String COMMA = ",";
    public static final String NEWLINE = "\n";
    public static final String SPACE = " ";
    public static final String SPACE2 = "  ";

    private final FormatterConfig config;

    private JsonCompactFormatter(FormatterConfig config) {
        this.config = config;
    }

    /** Format with a width of 80 */
    public static String formatCompact(Object jaJson) {
        return formatCompact(jaJson, 80, Collections.emptySet());
    }

    public static String formatCompact(Object jaJson, int maxLineLength) {
        return formatCompact(jaJson, maxLineLength, Collections.emptySet());
    }

    public static String formatCompact(Object jaJson, int maxLineLength, Set<String> forceMultiLineKeys) {
        FormatterConfig config = FormatterConfig.of(maxLineLength, forceMultiLineKeys, false);
        return formatCompact(jaJson, config);
    }


    public static String formatCompact(Object jaJson, FormatterConfig config) {
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
        Map<String, Object> usedMap = jaJason;
        if (config.sortMaps()) {
            usedMap = new TreeMap<>(jaJason);
        }
        usedMap.forEach((k, v) -> {
            Block valueBlock = valueToBlock(depth + 1, v, config);
            BlockProperty propertyBlock = new BlockProperty(k, valueBlock);
            blockObject.children.add(propertyBlock);
        });
        return blockObject;
    }

    static Block primitiveToBlock(Object jaJason) {
        if (jaJason == null) {
            return new BlockValue("null");
        }
        if (jaJason instanceof String) {
            String s = (String) jaJason;
            return new BlockValue("\"" + JSON.jsonEscape(s) + "\"");
        } else if (jaJason instanceof Number) {
            Number n = (Number) jaJason;
            return new BlockValue(n.toString());
        } else if (jaJason instanceof Boolean) {
            Boolean b = (Boolean) jaJason;
            return new BlockValue(b.toString());
        }
        return new BlockValue(jaJason.toString());
    }

    @SuppressWarnings("unchecked")
    static Block valueToBlock(int depth, Object jaJson, FormatterConfig config) {
        if (jaJson instanceof Map<?, ?>) {
            Map<String, Object> map = (Map<String, Object>) jaJson;
            return mapToBlock(depth, map, config);
        } else if (jaJson instanceof List<?>) {
            List<Object> list = (List<Object>) jaJson;
            return listToBlock(depth, list, config);
        } else {
            return primitiveToBlock(jaJson);
        }
    }

    public FormatterConfig config() {
        return config;
    }

}
