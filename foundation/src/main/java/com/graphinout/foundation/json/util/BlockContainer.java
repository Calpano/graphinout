package com.graphinout.foundation.json.util;

import com.graphinout.foundation.util.Symbols;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static com.graphinout.foundation.json.util.JsonCompactFormatter.COMMA;
import static com.graphinout.foundation.json.util.JsonCompactFormatter.SPACE;
import static com.graphinout.foundation.json.util.JsonCompactFormatter.SPACE2;
import static com.graphinout.foundation.json.util.JsonCompactFormatter.indent;
import static com.graphinout.foundation.json.util.JsonCompactFormatter.oneLargeLine;
import static org.slf4j.LoggerFactory.getLogger;

class BlockContainer extends Block {

    private static final Logger log = getLogger(BlockContainer.class);
    /** width needed for '[ ' and ' ]' (same with curly braces) */
    private static final int bracesAndSpaces = 4;
    final List<Block> children = new ArrayList<>();
    private final String open;
    private final String close;
    /** result of {@link #compact(int)} */
    boolean isInline = false;

    BlockContainer(int depth, String open, String close) {
        super(depth);
        this.open = open;
        this.close = close;
    }

    static BlockContainer createArrayBlock(int depth) {
        return new BlockContainer(depth, "[", "]");
    }

    static BlockContainer createObjectBlock(int depth) {
        return new BlockContainer(depth, "{", "}");
    }

    @Override
    public void compact(int maxLineLength) {
        children.forEach(block -> block.compact(maxLineLength));

        if (!children.isEmpty() && children.stream().allMatch(Block::isInline)) {
            // try to inline all children
            if (indent(depth).length() +
                    // plus COMMA SPACE between each
                    children.stream().mapToInt(b -> b.width() + 2).sum() - 2 < maxLineLength) {
                isInline = true;
                log.info("Inline: " + this);
            } else {
                log.info("Not inline: " + this);
            }
        }
    }

    public boolean isArray() {
        return open.equals("[");
    }

    public boolean isObject() {
        return open.equals("{");
    }

    @Override
    @Deprecated
    public void toFormattedString(int maxWidth, Consumer<String> lines) {
        if (children.isEmpty()) {
            lines.accept(open() + close());
            return;
        }

        List<List<String>> allLines = new ArrayList<>();
        children.forEach(childBlock -> {
            List<String> childLines = childBlock.toFormattedString(maxWidth - bracesAndSpaces);
            allLines.add(childLines);
        });
        boolean isSingleLine = false;
        String oneChildrenLine = oneLargeLine(allLines, COMMA + SPACE);
        if (allLines.size() == children.size()) {
            // each child did fit on one line, so concat might work
            if (bracesAndSpaces + oneChildrenLine.length() <= maxWidth) {
                isSingleLine = true;
            }
        }
        if (isSingleLine) {
            // one line with all children
            lines.accept(open() + SPACE + oneChildrenLine + SPACE + close());
        } else {
            // multi-line
            lines.accept(open() + SPACE + allLines.getFirst());
            for (int i = 1; i < allLines.size(); i++) {
                lines.accept(SPACE + SPACE + allLines.get(i));
            }
            lines.accept(close());
        }
    }

    @Override
    public Tile toTile(int maxWidth) {
        if (children.isEmpty()) {
            return Tile.of(open() + close());
        }

        // Try to render the whole block in one line.
        // This works only if (a) each child can be in one line and (b) they fit together.
        int maxChildWidth = maxWidth - bracesAndSpaces;
        int childTilesSumMaxBudget = maxWidth - (bracesAndSpaces
                // Reserve space for COMMA SPACE between each child.
                + ((children.size() - 1) * 2));

        List<Tile> childTiles = new ArrayList<>();
        for (Block childBlock : children) {
            Tile childTile = childBlock.toTile(maxChildWidth);
            childTiles.add(childTile);
            // only compute singleLine if still possible
            if (childTilesSumMaxBudget >= 0) {
                String singleLine = childTile.toSingleLine(childTilesSumMaxBudget);
                if(singleLine != null) {
                    childTilesSumMaxBudget -= singleLine.length();
                } else {
                    childTilesSumMaxBudget = -1;
                }
            }
        }

        if (childTilesSumMaxBudget >= 0) {
            // it worked
            List<String> childLines = childTiles.stream().map(tile -> tile.toSingleLine(maxChildWidth)).toList();
            String oneLine = String.join(COMMA + SPACE, childLines);
            return Tile.of(open() + SPACE + oneLine + SPACE + close());
        } else {
            // some children are too long on their own OR together they are too long
            // TODO still try to render at least each child in one line, if possible
            Tile tile = Tile.create();
            for (int i = 0; i < childTiles.size(); i++) {
                Tile childTile = childTiles.get(i);
                // Try to wrap in a single line.
                String singleLine = childTile.toSingleLine(maxChildWidth);
                if (singleLine != null) {
                    childTile = Tile.of(singleLine);
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
            tile.addLine( close());
            return tile;
        }
    }

    @Override
    public void toWriter(IndentWriter writer, int parentDepth) {
        if (isObject()) {
            toWriterObject(writer);
        } else {
            assert isArray();
            toWriterArray(writer, parentDepth);
        }
    }

    protected boolean isInline() {
        return isInline;
    }

    boolean allChildrenAreObjects() {
        return children.stream().allMatch(Block::isObject);
    }

    String close() {
        return close;
    }

    @Override
    String firstLine() {
        return children.getFirst().firstLine();
    }

    String open() {
        return open;
    }

    @Override
    int width() {
        if (isInline()) {
            return children.stream().mapToInt(Block::width).sum();
        } else {
            return children.stream().mapToInt(Block::width).max().orElse(0);
        }
    }

    /**
     * Caller ensure linebreaks and indents before.
     *
     * @param writer
     * @param parentDepth
     */
    private void toWriterArray(IndentWriter writer, int parentDepth) {
        if (!isInline()) {
            writer.newLine(depth, Symbols.NUMBER_10_DARK);
        }
        writer.append(open());
        // DEBUG infos
        if (isInline) {
            writer.append(Symbols.symbol(Symbols.INLINE_TRUE));
        } else {
            writer.append(Symbols.symbol(Symbols.INLINE_FALSE));
        }

        if (!children.isEmpty()) {
            toWriterArrayBody(writer);

            if (isInline()) {
                writer.append(Symbols.symbol(Symbols.NUMBER_11_DARK) + SPACE);
            } else {
                writer.newLine(parentDepth, Symbols.symbol(Symbols.NUMBER_12_DARK));
            }
        }

        writer.append(close());
    }

    private void toWriterArrayBody(IndentWriter writer) {
        for (int i = 0; i < children.size(); i++) {
            Block child = children.get(i);

            if (isInline()) {
                writer.append(Symbols.symbol(Symbols.NUMBER_1_DARK) + SPACE);
            } else {
                writer.newLine(depth + 1, Symbols.symbol(Symbols.NUMBER_2_DARK));
            }

            child.toWriter(writer, depth + 1);

            if (i < children.size() - 1) {
                writer.append(COMMA);
            }
        }
    }

    private void toWriterObject(IndentWriter writer) {
        if (!isInline()) {
            writer.newLine(depth, Symbols.NUMBER_10);
        }
        writer.append(open());
        if (isInline) {
            writer.append(Symbols.symbol(Symbols.INLINE_TRUE));
        } else {
            writer.append(Symbols.symbol(Symbols.INLINE_FALSE));
        }

        if (!children.isEmpty()) {
            toWriterObjectBody(writer);
            if (isInline()) {
                writer.append(Symbols.symbol(Symbols.NUMBER_11) + SPACE);
            } else {
                writer.newLine(depth, Symbols.symbol(Symbols.NUMBER_12));
            }
        }

        writer.append(close());
    }

    private void toWriterObjectBody(IndentWriter writer) {
        assert !children.isEmpty();

        for (int i = 0; i < children.size(); i++) {
            Block child = children.get(i);
            if (isInline() || i == 0) {
                writer.append(Symbols.symbol(Symbols.NUMBER_1) + SPACE);
            } else {
                writer.newLine(child.depth, Symbols.NUMBER_2);
            }
            child.toWriter(writer, depth + 1);
            if (i < children.size() - 1) {
                writer.append(COMMA);
            }
        }
    }


}
