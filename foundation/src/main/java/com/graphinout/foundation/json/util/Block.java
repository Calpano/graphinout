package com.graphinout.foundation.json.util;

public abstract class Block {

    final int depth;

    Block(int depth) {this.depth = depth;}

    public abstract void compact(int maxLineLength);

    @Override
    public String toString() {
        return IndentWriter.of(this).resultString();
    }

    public abstract void toWriter(IndentWriter writer, int parentDepth);

    protected boolean isInline() {
        return true;
    }

    abstract String firstLine();

    /** excluding indent */
    abstract int width();

}
