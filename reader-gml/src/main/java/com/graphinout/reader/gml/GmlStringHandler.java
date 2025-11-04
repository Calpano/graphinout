package com.graphinout.reader.gml;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * An {@link IGmlHandler} implementation that reconstructs a GML text stream
 * from tokenizer callbacks into a String. Useful for pretty-printing or
 * round-tripping GML inputs without interpreting their semantics.
 */
public class GmlStringHandler implements IGmlHandler {

    private final StringBuilder out = new StringBuilder();

    private final Deque<Boolean> bracketLineOpenStack = new ArrayDeque<>();
    private int indent = 0;
    private boolean lastKeyStartedLine = false;

    @Override
    public void key(String key) {
        // If a bracket line is open at this depth, continue on the same line after "[ "
        if (!bracketLineOpenStack.isEmpty() && Boolean.TRUE.equals(bracketLineOpenStack.peek())) {
            out.append(key);
            lastKeyStartedLine = true;
            return;
        }
        // Otherwise, start a new line with current indentation
        newlineIfNeeded();
        out.append(indent());
        out.append(key);
        lastKeyStartedLine = true;
    }

    @Override
    public void value(String value) {
        // Value formatting: quote non-numeric, non-quoted tokens
        String formatted = formatValue(value);
        if (lastKeyStartedLine) {
            out.append("  ").append(formatted); // double space between key and value like examples
            out.append("\n");
            lastKeyStartedLine = false;
            // If we were in a bracket header line, it ends after first key-value
            if (!bracketLineOpenStack.isEmpty() && Boolean.TRUE.equals(bracketLineOpenStack.peek())) {
                bracketLineOpenStack.pop();
                bracketLineOpenStack.push(Boolean.FALSE);
            }
        } else {
            // Standalone value (rare in GML), still output on its own line
            newlineIfNeeded();
            out.append(indent()).append(formatted).append("\n");
        }
    }

    @Override
    public void open() {
        // If the previous token was a key, end its line before opening
        if (lastKeyStartedLine) {
            out.append("\n");
            lastKeyStartedLine = false;
        }
        // Start a bracket block; the first key/value goes on the same line per examples
        newlineIfNeeded();
        out.append(indent());
        out.append("[ ");
        bracketLineOpenStack.push(Boolean.TRUE);
        indent++;
    }

    @Override
    public void close() {
        // Close any pending bracket header line (no content inside), move to next line
        if (!bracketLineOpenStack.isEmpty() && Boolean.TRUE.equals(bracketLineOpenStack.peek())) {
            // empty block: convert "[ " into "[" line
            // finish current line
            out.append("\n");
            bracketLineOpenStack.pop();
            bracketLineOpenStack.push(Boolean.FALSE);
        }
        if (lastKeyStartedLine) {
            out.append("\n");
            lastKeyStartedLine = false;
        }
        indent--;
        out.append(indent());
        out.append("]\n");
        if (!bracketLineOpenStack.isEmpty()) bracketLineOpenStack.pop();
    }

    public String result() {
        return out.toString();
    }

    @Override
    public String toString() {
        return result();
    }

    private String formatValue(String raw) {
        if (raw == null) return "\"\"";
        String v = raw;
        // If already quoted (starts and ends with quotes), keep as-is
        if (v.length() >= 2 && v.startsWith("\"") && v.endsWith("\"")) return v;
        // Numeric? keep as-is
        if (isNumeric(v)) return v;
        // Otherwise, quote and escape embedded quotes minimally
        String escaped = v.replace("\\", "\\\\").replace("\"", "\\\"");
        return "\"" + escaped + "\"";
    }

    private boolean isNumeric(String s) {
        // Accept integers and decimals with optional sign
        return s.matches("-?\\d+(\\.\\d+)?");
    }

    private void newlineIfNeeded() {
        // No-op placeholder in case future adjustments needed
    }

    private String indent() {
        return "  ".repeat(Math.max(0, indent));
        }
}
