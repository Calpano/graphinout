package com.graphinout.foundation.json.util;

class BlockValue extends Block {

    String value;

    BlockValue(int depth, String value) {
        super(depth);
        this.value = value;
    }

    @Override
    public void compact() {
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
