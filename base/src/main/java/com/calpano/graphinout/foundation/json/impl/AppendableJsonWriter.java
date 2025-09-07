package com.calpano.graphinout.foundation.json.impl;

import com.calpano.graphinout.foundation.json.JsonException;
import com.calpano.graphinout.foundation.json.stream.JsonWriter;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Stack;

/**
 * Reconstructs a JSON syntax string from a {@link JsonWriter}. Reusable.
 */
public abstract class AppendableJsonWriter implements JsonWriter {

    enum State {First, Later, Property}

    private final Appendable appendable;
    private final Stack<State> stack = new Stack<>();

    public AppendableJsonWriter(Appendable appendable) {
        this.appendable = appendable;
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
        assert !stack.isEmpty() : "stack empty";
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

    // IMPROVE is this a simplistic/wrong approach to escaping?
    @Override
    public void onString(String s) throws JsonException {
        maybeDelimiter();
        append("\"");
        // JSON-escape quote symbols
        append(s //
                .replace("\\", "\\\\") //
                .replace("\"", "\\\"") //
        );
        append("\"");
    }

    public void reset() {
        stack.clear();
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
        switch (stack.peek()) {
            case Property ->
                // no delimiter
                    stack.pop();
            case First -> {
                stack.pop();
                stack.push(State.Later);
            }
            case null, default -> append(',');
        }
    }

}
