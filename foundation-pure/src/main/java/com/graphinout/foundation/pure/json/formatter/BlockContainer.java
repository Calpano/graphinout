package com.graphinout.foundation.pure.json.formatter;

import com.graphinout.foundation.pure.bridge.Java9;

import java.util.ArrayList;
import java.util.List;

import static com.graphinout.foundation.pure.json.formatter.JsonCompactFormatter.COMMA;
import static com.graphinout.foundation.pure.json.formatter.JsonCompactFormatter.SPACE;
import static com.graphinout.foundation.pure.json.formatter.JsonCompactFormatter.SPACE2;

class BlockContainer extends Block {

    /** width needed for '[ ' and ' ]' (same with curly braces) */
    private static final int bracesAndSpaces = 4;
    final List<Block> children = new ArrayList<>();
    private final String open;
    private final String close;

    BlockContainer(String open, String close) {
        this.open = open;
        this.close = close;
    }

    static BlockContainer createArrayBlock() {
        return new BlockContainer("[", "]");
    }

    static BlockContainer createObjectBlock() {
        return new BlockContainer("{", "}");
    }

    public boolean isArray() {
        return open.equals("[");
    }

    public boolean isObject() {
        return open.equals("{");
    }


    @Override
    public Tile toTile(FormatterConfig config, boolean forceMultiLine) {
        if (children.isEmpty()) {
            return Tile.of(open() + close());
        }

        // Try to render the whole block in one line.
        // This works only if (a) each child can be in one line and (b) they fit together.
        int maxChildWidth = config.maxWidth() - bracesAndSpaces;
        int childTilesSumMaxBudget = config.maxWidth() - (bracesAndSpaces
                // Reserve space for COMMA SPACE between each child.
                + ((children.size() - 1) * 2));
        if (forceMultiLine) {
            // give up on inlining
            childTilesSumMaxBudget = -1;
        }

        List<Tile> childTiles = new ArrayList<>();
        for (Block childBlock : children) {
            Tile childTile = childBlock.toTile(config.withMaxWidth(maxChildWidth), false);
            childTiles.add(childTile);
            if (childTile.isFixed()) {
                childTilesSumMaxBudget = -1;
            }
            // only compute singleLine if still possible
            if (childTilesSumMaxBudget >= 0) {
                String singleLine = childTile.toSingleLine(childTilesSumMaxBudget);
                if (singleLine != null) {
                    childTilesSumMaxBudget -= singleLine.length();
                } else {
                    childTilesSumMaxBudget = -1;
                }
            }
        }

        if (childTilesSumMaxBudget >= 0) {
            // it worked
            List<String> childLines = Java9.Stream.toList(childTiles.stream().map(tile -> tile.toSingleLine(maxChildWidth)));
            String oneLine = String.join(COMMA + SPACE, childLines);
            return Tile.of(open() + SPACE + oneLine + SPACE + close());
        } else {
            // Some children are too long on their own OR together they are too long.
            // Still try to render at least each child in one line, if possible.
            Tile tile = Tile.create(forceMultiLine);
            for (int i = 0; i < childTiles.size(); i++) {
                Tile childTile = childTiles.get(i);
                // Try to wrap in a single line.
                if (!childTile.isFixed()) {
                    String singleLine = childTile.toSingleLine(maxChildWidth);
                    if (singleLine != null) {
                        // replace existing childTile with wrapped version
                        childTile = Tile.of(singleLine);
                    }
                }

                // Use tile. First tile gets the braces
                childTile.insertLeft(i == 0 ? open() + SPACE : SPACE2, SPACE2);
                // a COMMA after every non-last child tile
                if (i < childTiles.size() - 1) {
                    childTile.insertLastLineRight(COMMA);
                }
                tile.add(childTile);
            }
            // closing brace on its own line
            tile.addLine(close());
            return tile;
        }
    }

    String close() {
        return close;
    }

    String open() {
        return open;
    }

}
