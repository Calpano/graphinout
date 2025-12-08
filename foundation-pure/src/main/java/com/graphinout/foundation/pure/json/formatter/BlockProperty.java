package com.graphinout.foundation.pure.json.formatter;

import com.graphinout.foundation.pure.json.JSON;

import static com.graphinout.foundation.pure.json.formatter.JsonCompactFormatter.SPACE2;


class BlockProperty extends Block {

    final String key;
    final Block value;

    BlockProperty(String key, Block value) {
        this.key = key;
        this.value = value;
    }

    @Override
    public Tile toTile(FormatterConfig config, boolean forceMultiLine) {
        // TODO respect forceMultiLine for key - value
        String keyLine = "\"" + JSON.jsonEscape(key) + "\":";
        int valueTileMaxWidth = config.maxWidth() - keyLine.length()
                // reserve a SPACE
                - 1;
        boolean forceMultiLineValue = config.forceMultiLineKeys().contains(key);
        if (!forceMultiLineValue) {
            FormatterConfig valueConfig = config.withMaxWidth(valueTileMaxWidth);
            Tile valueTile = value.toTile(valueConfig, false);
            String valueOneLine = valueTile.toSingleLine(valueTileMaxWidth);
            if (valueOneLine != null) {
                return Tile.of(keyLine + " " + valueOneLine);
            }
        }
        // fall-back
        Tile valueTile = value.toTile(config, forceMultiLineValue);
        valueTile.insertLeft(SPACE2);
        valueTile.insertLineAbove(keyLine);
        return valueTile;
    }

}
