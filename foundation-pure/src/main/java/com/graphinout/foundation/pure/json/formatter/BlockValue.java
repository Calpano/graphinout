package com.graphinout.foundation.pure.json.formatter;

class BlockValue extends Block {

    final String value;

    BlockValue(String value) {
        this.value = value;
    }

    @Override
    public Tile toTile(FormatterConfig config, boolean forceMultiLine) {
        return Tile.of(value);
    }


}
