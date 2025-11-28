package com.graphinout.foundation.json.util;

public abstract class Block {

    final int depth;

    Block(int depth) {this.depth = depth;}

    public abstract void compact();

    @Override
    public String toString() {
        return IndentWriter.of(this).resultString();
    }

    public abstract void toWriter(IndentWriter writer, int parentDepth);

    abstract String firstLine();

    /** excluding indent */
    abstract int width();

}
