package com.graphinout.foundation.json.util;

import java.util.Set;

/** A structural block consisting of other Blocks, finally in {@link BlockValue} there are strings. */
public abstract class Block {

    @Override
    public String toString() {
        Tile tile = toTile(FormatterConfig.of(80, Set.of()), false);
        return tile.toString();
    }

    public abstract Tile toTile(FormatterConfig config, boolean forceMultiLine);

}
