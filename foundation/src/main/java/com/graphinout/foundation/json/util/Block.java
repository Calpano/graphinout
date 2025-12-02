package com.graphinout.foundation.json.util;

import java.util.ArrayList;
import java.util.function.Consumer;

public abstract class Block {

    final int depth;

    Block(int depth) {this.depth = depth;}

    public abstract void compact(int maxLineLength);

    public abstract void toFormattedString(int charBudget, Consumer<String> lines);

    public ArrayList<String> toFormattedString(int charBudget) {
        ArrayList<String> list = new ArrayList<>();
        toFormattedString(charBudget, list::add);
        return list;
    }

    @Override
    public String toString() {
        return IndentWriter.of(this).resultString();
    }

    public abstract Tile toTile(int maxLineLength);

    public abstract void toWriter(IndentWriter writer, int parentDepth);

    protected boolean isInline() {
        return true;
    }

    protected boolean isObject() {
        return false;
    }

    abstract String firstLine();

    /** excluding indent */
    abstract int width();

}
