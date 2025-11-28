package com.graphinout.foundation.json.util;

import static com.graphinout.foundation.json.util.JsonCompactFormatter.NEWLINE;
import static com.graphinout.foundation.json.util.JsonCompactFormatter.SPACE;
import static com.graphinout.foundation.json.util.JsonCompactFormatter.indent;

public class IndentWriter {

    private final StringBuilder b = new StringBuilder();

    public static IndentWriter of(Block block) {
        IndentWriter writer = new IndentWriter();
        block.toWriter(writer, 0);
        return writer;
    }

    public void append(String s) {
        b.append(s);
    }

    public void newLine(int depth) {
        b.append(NEWLINE);
        b.append(indent(depth, SPACE));
    }

    public String resultString() {
        return b.toString();
    }

}
