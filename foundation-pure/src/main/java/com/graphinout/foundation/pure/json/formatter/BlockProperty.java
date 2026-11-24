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

    /**
     * @param forceMultiLine if true, the value is not put on the key line and is itself rendered multi-line. The same
     *                       happens for keys in {@link FormatterConfig#forceMultiLineKeys()}.
     */
    @Override
    public Tile toTile(FormatterConfig config, boolean forceMultiLine) {
        String keyLine = "\"" + JSON.jsonEscape(key) + "\":";
        int valueTileMaxWidth = config.maxWidth() - keyLine.length()
                // reserve a SPACE
                - 1;
        boolean forceMultiLineValue = forceMultiLine || config.forceMultiLineKeys().contains(key);
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
