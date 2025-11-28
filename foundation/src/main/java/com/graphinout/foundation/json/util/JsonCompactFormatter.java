package com.graphinout.foundation.json.util;

import com.graphinout.foundation.json.JSON;
import com.graphinout.foundation.json.JsonType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

public class JsonCompactFormatter {

    abstract static class Block {

        final int depth;

        Block(int depth) {this.depth = depth;}

        abstract void append(String s);

        public abstract void compact();

        abstract String firstLine();

        abstract boolean isOneLiner();

        /** excluding indent */
        abstract int width();

    }

    static class BlockValue extends Block {

        String value;

        BlockValue(int depth, String value) {
            super(depth);
            this.value = value;
        }

        @Override
        void append(String s) {
            value = value + s;
        }

        @Override
        public void compact() {
        }

        @Override
        public String toString() {
            return value;
        }

        @Override
        String firstLine() {
            return value;
        }

        @Override
        boolean isOneLiner() {
            return true;
        }

        @Override
        int width() {
            return value.length();
        }

    }

    static class BlockContainer extends Block {

        final JsonType.ContainerType containerType;
        String open;
        String close;
        List<Block> children = new ArrayList<>();

        public void appendOpen(String s){
            open = open + s;
        }

        public void prependClose(String s){
            close = s + close;
        }

        BlockContainer(int depth, JsonType.ContainerType containerType, String open, String close) {
            super(depth);
            this.containerType = containerType;
            this.open = open;
            this.close = close;
        }

        static BlockContainer createArrayBlock(int depth) {
            return new BlockContainer(depth, JsonType.ContainerType.Array, "[", "]");
        }

        static BlockContainer createObjectBlock(int depth) {
            return new BlockContainer(depth, JsonType.ContainerType.Object, "{", "}");
        }

        @Override
        void append(String s) {
            children.getLast().append(s);
        }

        @Override
        public void compact() {
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();

            sb.append(open() + " ");

            for (int i = 0; i < children.size(); i++) {
                Block child = children.get(i);
                if (i > 0) {
                    sb.append("\n");
                    sb.append(indent(child.depth, " "));
                }
                sb.append(child.toString());
            }

            if (children.size() <= 1) {
                sb.append(" ");
            } else {
                sb.append("\n");
                sb.append(indent(depth, " "));
            }
            sb.append(close());

            return sb.toString();
        }

        String close() {
            return close;
        }

        @Override
        String firstLine() {
            return children.getFirst().firstLine();
        }

        @Override
        boolean isOneLiner() {
            return children.size() <= 1;
        }

        String open() {
            return open;
        }

        @Override
        int width() {
            return children.stream().mapToInt(Block::width).max().orElse(0);
        }

    }


    record Config(int maxLineLength, Set<String> forceMultiLineKeys) {}

    public static final String COMMA = ",";
    private static final int INDENT_SIZE = 2;
    private final int maxLineLength;
    private final Set<String> forceMultiLineKeys;
    private final Stack<JsonType.ContainerType> stack = new Stack<>();

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
        block.compact();
        return block.toString();
    }

    static String indent(int depth, String indent) {
        return indent.repeat(depth * INDENT_SIZE);
    }

    static Block listToBlock(int depth, List<Object> jaJason, Config config) {
        BlockContainer blockArray = BlockContainer.createArrayBlock(depth);
        for (int i = 0; i < jaJason.size(); i++) {
            Object o = jaJason.get(i);
            Block valueBlock = valueToBlock(depth + 1, o, config);
            // Add comma for all but the last element
            if (i < jaJason.size() - 1) {
                valueBlock.append(COMMA);
            }
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

            BlockValue keyBlock = new BlockValue(depth + 1, "\"" + JSON.jsonEscape(key) + "\":");
            Block valueBlock = valueToBlock(depth + 1, value, config);
            // Add comma for all but the last element
            if (iterator.hasNext()) {
                valueBlock.append(COMMA);
            }

            // can we merge them right away?

            String oneLine = keyBlock.value + " " + valueBlock.firstLine();

            int combinedWidth = indent(keyBlock.depth, " ").length() + oneLine.length();
            if (combinedWidth < config.maxLineLength
                    && !config.forceMultiLineKeys.contains(key)) {
                Block property = new BlockValue(depth + 1, oneLine);
                blockObject.children.add(property);
                continue;
            }

            // default
            blockObject.children.add(keyBlock);
            blockObject.children.add(valueBlock);
        }
        return blockObject;
    }

    static Block primitiveToBlock(int depth, Object jaJason, Config config) {
        return switch (jaJason) {
            case null -> new BlockValue(depth, "null");
            case String s -> new BlockValue(depth, "\"" + JSON.jsonEscape(s) + "\"");
            case Number n -> new BlockValue(depth, n.toString());
            case Boolean b -> new BlockValue(depth, b.toString());
            default -> new BlockValue(depth, jaJason.toString());
        };
    }

    static Block valueToBlock(int depth, Object jaJson, Config config) {
        if (jaJson instanceof Map<?, ?> map) {
            return mapToBlock(depth, (Map<String, Object>) map, config);
        } else if (jaJson instanceof List<?> list) {
            return listToBlock(depth, (List<Object>) list, config);
        } else {
            return primitiveToBlock(depth, jaJson, config);
        }
    }

    private Config config() {
        return new Config(maxLineLength, forceMultiLineKeys);
    }


}
