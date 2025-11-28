package com.graphinout.foundation.json.util;

import com.graphinout.foundation.json.JSON;

class BlockProperty extends Block {

    String key;
    Block value;

    BlockProperty(int depth, String key, Block value) {
        super(depth);
        this.key = key;
        this.value = value;
    }

    @Override
    public void compact() {
    }

    @Override
    public void toWriter(IndentWriter writer, int parentDepth) {
        writer.append("\"" + JSON.jsonEscape(key) + "\"");
        writer.append(": ");
        value.toWriter(writer, depth);
    }

    @Override
    String firstLine() {
        return "\"" + JSON.jsonEscape(key) + "\"" + ": " + value.firstLine();
    }

    @Override
    int width() {
        return Math.max(firstLine().length(), value.width());
    }

}
