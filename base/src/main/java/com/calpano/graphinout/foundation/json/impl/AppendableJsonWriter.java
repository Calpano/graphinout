package com.calpano.graphinout.foundation.json.impl;

import com.calpano.graphinout.foundation.json.JsonException;
import com.calpano.graphinout.foundation.json.JsonWriter;
import org.slf4j.Logger;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Stack;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * Reconstructs a JSON syntax string from a {@link JsonWriter}. Reusable.
 */
public abstract class AppendableJsonWriter implements JsonWriter {

    enum State {First, Later, Property}

    private static final Logger log = getLogger(AppendableJsonWriter.class);
    private final Appendable appendable;
    private final Stack<State> stack = new Stack<>();
    private final boolean preserveWhitespace;
    public AppendableJsonWriter(Appendable appendable, boolean preserveWhitespace) {
        this.appendable = appendable;
        this.preserveWhitespace = preserveWhitespace;
    }

    public Appendable appendable() {
        return appendable;
    }

    @Override
    public void arrayEnd() throws JsonException {
        stack.pop();
        append(']');
    }

    @Override
    public void arrayStart() throws JsonException {
        maybeDelimiter();
        stack.push(State.First);
        append('[');
    }

    @Override
    public void documentEnd() throws JsonException {
        // no-op
    }

    @Override
    public void documentStart() {
        // no-op
    }

    @Override
    public void objectEnd() throws JsonException {
        stack.pop();
        append('}');
    }

    @Override
    public void objectStart() throws JsonException {
        maybeDelimiter();
        stack.push(State.First);
        append('{');
    }

    @Override
    public void onBigDecimal(BigDecimal bigDecimal) {
        maybeDelimiter();
        append(bigDecimal.toString());
    }

    @Override
    public void onBigInteger(BigInteger bigInteger) {
        maybeDelimiter();
        append(bigInteger.toString());
    }

    @Override
    public void onBoolean(boolean b) throws JsonException {
        maybeDelimiter();
        this.append(Boolean.toString(b));
    }

    @Override
    public void onDouble(double d) throws JsonException {
        assert Double.isFinite(d) : "Avoid NaN in JSON";
        maybeDelimiter();
        // JSON syntax for Number
        append(Double.toString(d));
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
        append('"');
        append(key);
        append('"');
        append(':');
        stack.push(State.Property);
    }

    @Override
    public void onLong(long l) throws JsonException {
        maybeDelimiter();
        append(Long.toString(l));
    }

    @Override
    public void onNull() throws JsonException {
        maybeDelimiter();
        append("null");
    }

    public void reset() {
        stack.clear();
    }

    // IMPROVE is this a simplistic/wrong approach to escaping?
    @Override
    public void stringCharacters(String s) throws JsonException {
        append(s //
                .replace("\\", "\\\\") //
                .replace("\"", "\\\"") //
        );
    }

    @Override
    public void stringEnd() throws JsonException {
        append('"');
    }

    @Override
    public void stringStart() throws JsonException {
        maybeDelimiter();
        append('"');
    }

    @Override
    public void whitespaceCharacters(String s) throws JsonException {
        if (preserveWhitespace) {
            append(s);
        }
    }

    private void append(String s) {
        try {
            appendable.append(s);
        } catch (IOException e) {
            throw new JsonException(e);
        }
    }

    private void append(char c) {
        try {
            appendable.append(c);
        } catch (IOException e) {
            throw new JsonException(e);
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
            append(',');
        }
    }

}
