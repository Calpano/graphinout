package com.graphinout.foundation.json.util;

import java.util.function.Consumer;

class BlockValue extends Block {

    String value;

    BlockValue(int depth, String value) {
        super(depth);
        this.value = value;
    }

    @Override
    public void compact(int maxLineLength) {
    }

    @Override
    public void toFormattedString(int charBudget, Consumer<String> lines) {
        lines.accept(value);
    }

    @Override
    public Tile toTile(int maxLineLength) {
        return Tile.of(value);
    }


    @Override
    public void toWriter(IndentWriter writer, int parentDepth) {
        writer.append(value);
    }

    @Override
    String firstLine() {
        return value;
    }

    @Override
    int width() {
        return value.length();
    }

}
