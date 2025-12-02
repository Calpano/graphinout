package com.graphinout.foundation.json.util;

import com.graphinout.foundation.json.JSON;

import java.util.List;
import java.util.function.Consumer;

import static com.graphinout.foundation.json.util.JsonCompactFormatter.SPACE2;
import static com.graphinout.foundation.json.util.JsonCompactFormatter.SPACE4;
import static com.graphinout.foundation.json.util.JsonCompactFormatter.oneChildLine;


class BlockProperty extends Block {

    String key;
    Block value;

    BlockProperty(int depth, String key, Block value) {
        super(depth);
        this.key = key;
        this.value = value;
    }

    @Override
    public void compact(int maxLineLength) {
        value.compact(maxLineLength);
    }

    @Override
    public void toFormattedString(int charBudget, Consumer<String> lines) {
        String keyLine = "\"" + JSON.jsonEscape(key) + "\":";

        List<String> valueLinesWide = value.toFormattedString(charBudget);
        String oneValueLineWide = oneChildLine(valueLinesWide);
        if (oneValueLineWide.length() <= charBudget) {
            // great maybe we can also add the keyLine
            List<String> valueLinesNarrow = value.toFormattedString(charBudget - (keyLine.length() + 1));
            String oneValueLineNarrow = oneChildLine(valueLinesNarrow);
            if (keyLine.length() + 1 + oneValueLineNarrow.length() <= charBudget) {
                // one property line
                lines.accept(keyLine + " " + oneValueLineNarrow);
            } else {
                // two property lines
                lines.accept(keyLine);
                lines.accept(oneValueLineWide);
            }
        } else {
            // multi-line
            lines.accept(keyLine);
            valueLinesWide.forEach(lines);
        }
    }

    @Override
    public Tile toTile(int maxLineLength) {
        String keyLine = "\"" + JSON.jsonEscape(key) + "\":";
        int valueTileMaxWidth = maxLineLength - keyLine.length()
                // reserve a SPACE
                - 1;
        Tile valueTile = value.toTile(valueTileMaxWidth);
        String valueOneLine = valueTile.toSingleLine(valueTileMaxWidth);
        if (valueOneLine != null) {
            return Tile.of(keyLine + " " + valueOneLine);
        } else {
            valueTile.insertLeft(SPACE2);
            valueTile.insertLineAbove(keyLine);
            return valueTile;
        }
    }

    @Override
    public void toWriter(IndentWriter writer, int parentDepth) {
        writer.append("\"" + JSON.jsonEscape(key) + "\"");
        writer.append(": ");
        value.toWriter(writer, depth);
    }

    @Override
    protected boolean isInline() {
        return value.isInline();
    }

    @Override
    String firstLine() {
        return "\"" + JSON.jsonEscape(key) + "\"" + ": " + value.firstLine();
    }

    @Override
    int width() {
        return Math.max(firstLine().length(), value.width());
    }

}
