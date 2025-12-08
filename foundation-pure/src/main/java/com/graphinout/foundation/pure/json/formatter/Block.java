package com.graphinout.foundation.pure.json.formatter;

import com.graphinout.foundation.pure.bridge.Java9;

/** A structural block consisting of other Blocks, finally in {@link BlockValue} there are strings. */
public abstract class Block {

    @Override
    public String toString() {
        Tile tile = toTile(FormatterConfig.of(80, Java9.Set.of(), false), false);
        return tile.toString();
    }

    public abstract Tile toTile(FormatterConfig config, boolean forceMultiLine);

}
