package com.graphinout.foundation.json.util;

import com.graphinout.foundation.json.JsonType;

import java.util.ArrayList;
import java.util.List;

import static com.graphinout.foundation.json.util.JsonCompactFormatter.COMMA;
import static com.graphinout.foundation.json.util.JsonCompactFormatter.SPACE;

class BlockContainer extends Block {

    final JsonType.ContainerType containerType;
    String open;
    String close;
    List<Block> children = new ArrayList<>();

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

    public void appendOpen(String s) {
        open = open + s;
    }

    @Override
    public void compact() {
    }

    public void prependClose(String s) {
        close = s + close;
    }

    @Override
    public void toWriter(IndentWriter writer, int parentDepth) {
        // OPEN brace
        writer.append(open());

        switch (children.size()) {
            case 0 -> {
                // put exactly nothing
            }
            case 1 -> {
                Block child = children.getFirst();
                if (isArrayWithSingleObjectChild()) {
                    // place open and close braces of array on object next to each other
                    // so we get `[{ "foo": 123 }]`
                    child.toWriter(writer, depth+1);
                } else {
                    writer.append(SPACE);
                    child.toWriter(writer, depth+1);
                    writer.append(SPACE);
                }
            }
            default -> {
                //writer.append(SPACE);
                // put on multiple lines
                for (int i = 0; i < children.size(); i++) {
                    Block child = children.get(i);
                    if (i > 0) {
                        writer.append(COMMA);
                    }
                    writer.newLine(child.depth);
                    child.toWriter(writer, depth+1);
                }
            }
        }

        if(children.size() > 1) {
            writer.newLine(parentDepth);
        }
        // CLOSE brace
        writer.append(close());
    }

    String close() {
        return close;
    }

    @Override
    String firstLine() {
        return children.getFirst().firstLine();
    }

    boolean isArrayWithSingleObjectChild() {
        if (children.size() == 1) {
            return containerType == JsonType.ContainerType.Array //
                    && children.getFirst() instanceof BlockContainer childBlockContainer //
                    && childBlockContainer.containerType == JsonType.ContainerType.Object;
        } else {
            return false;
        }
    }

    String open() {
        return open;
    }

    @Override
    int width() {
        return children.stream().mapToInt(Block::width).max().orElse(0);
    }


}
