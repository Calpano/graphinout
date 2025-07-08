package com.calpano.graphinout.foundation.json.impl;

import com.calpano.graphinout.foundation.json.JsonException;
import com.calpano.graphinout.foundation.json.JsonWriter;
import org.slf4j.Logger;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Stack;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * Reconstructs a JSON syntax string from a {@link JsonWriter}. Reusable.
 */
public class StringBuilderJsonWriter implements JsonWriter {

    enum State {First, Later, Property}

    private static final Logger log = getLogger(StringBuilderJsonWriter.class);
    private final StringBuilder b = new StringBuilder();
    private final Stack<State> stack = new Stack<>();
    private final boolean preserveWhitespace;

    public StringBuilderJsonWriter(boolean preserveWhitespace) {this.preserveWhitespace = preserveWhitespace;}

    @Override
    public void arrayEnd() throws JsonException {
        stack.pop();
        b.append(']');
    }

    @Override
    public void arrayStart() throws JsonException {
        maybeDelimiter();
        stack.push(State.First);
        b.append('[');
    }

    @Override
    public void documentEnd() throws JsonException {
        // no-op
    }

    @Override
    public void documentStart() {
        // no-op
    }

    public String json() {
        return b.toString();
    }

    @Override
    public void objectEnd() throws JsonException {
        stack.pop();
        b.append('}');
    }

    @Override
    public void objectStart() throws JsonException {
        maybeDelimiter();
        stack.push(State.First);
        b.append('{');
    }

    @Override
    public void onBigDecimal(BigDecimal bigDecimal) {
        maybeDelimiter();
        b.append(bigDecimal.toString());
    }

    @Override
    public void onBigInteger(BigInteger bigInteger) {
        maybeDelimiter();
        b.append(bigInteger.toString());
    }

    @Override
    public void onBoolean(boolean b) throws JsonException {
        maybeDelimiter();
        this.b.append(b);
    }

    @Override
    public void onDouble(double d) throws JsonException {
        assert Double.isFinite(d) : "Avoid NaN in JSON";
        maybeDelimiter();
        // JSON syntax for Number
        b.append(d);
    }

    @Override
    public void onFloat(float f) throws JsonException {
        onDouble(f);
    }

    @Override
    public void onInteger(int i) throws JsonException {
        onLong(i);
    }

    @Override
    public void onKey(String key) throws JsonException {
        maybeDelimiter();
        b.append('"').append(key).append('"');
        b.append(':');
        stack.push(State.Property);
    }

    @Override
    public void onLong(long l) throws JsonException {
        maybeDelimiter();
        b.append(l);
    }

    @Override
    public void onNull() throws JsonException {
        maybeDelimiter();
        b.append("null");
    }

    public void reset() {
        b.setLength(0);
        stack.clear();
    }

    @Override
    public void stringCharacters(String s) throws JsonException {
        b.append(s //
                .replace("\\", "\\\\") //
                .replace("\"", "\\\"") //
        );
    }

    @Override
    public void stringEnd() throws JsonException {
        b.append('"');
    }

    @Override
    public void stringStart() throws JsonException {
        maybeDelimiter();
        b.append('"');
    }

    @Override
    public void whitespaceCharacters(String s) throws JsonException {
        if (preserveWhitespace) {
            b.append(s);
        }
    }

    private void maybeDelimiter() {
        if (stack.isEmpty()) return;
        if (stack.peek() == State.Property) {
            // no delimiter
            stack.pop();
        } else if (stack.peek() == State.First) {
            stack.pop();
            stack.push(State.Later);
        } else {
            b.append(',');
        }
    }

}
