package com.graphinout.foundation.json.stream.impl;

import com.graphinout.foundation.json.JsonException;
import com.graphinout.foundation.json.stream.JsonWriter;

import javax.annotation.Nullable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.function.Consumer;

/**
 * Base class for collecting all JSON calls into a string.
 * Impl uses {@link #jsonString()} and {@link #reset()}.
 */
public class Json2StringWriter implements JsonWriter {

    private final StringBuilderJsonWriter jsonWriter = new StringBuilderJsonWriter();
    private final @Nullable Consumer<String> onDone;

    public Json2StringWriter() {
        this(null);
    }

    public Json2StringWriter(@Nullable Consumer<String> onDone) {
        this.onDone = onDone;
    }

    @Override
    public String toString() {
        return jsonWriter.json();
    }

    @Override
    public void arrayEnd() throws JsonException {
        jsonWriter.arrayEnd();
    }

    @Override
    public void arrayStart() throws JsonException {
        jsonWriter.arrayStart();
    }

    @Override
    public void documentEnd() throws JsonException {
        // do nothing
        if (onDone != null) {
            onDone.accept(jsonWriter.json());
        }
    }

    @Override
    public void documentStart() throws JsonException {
        // do nothing
    }

    /** get the result */
    public String jsonString() {
        return jsonWriter.json();
    }

    @Override
    public void objectEnd() throws JsonException {
        jsonWriter.objectEnd();
    }

    @Override
    public void objectStart() throws JsonException {
        jsonWriter.objectStart();
    }

    @Override
    public void onBigDecimal(BigDecimal bigDecimal) throws JsonException {
        jsonWriter.onBigDecimal(bigDecimal);
    }

    @Override
    public void onBigInteger(BigInteger bigInteger) throws JsonException {
        jsonWriter.onBigInteger(bigInteger);
    }

    @Override
    public void onBoolean(boolean b) throws JsonException {
        jsonWriter.onBoolean(b);
    }

    @Override
    public void onDouble(double d) throws JsonException {
        jsonWriter.onDouble(d);
    }

    @Override
    public void onFloat(float f) throws JsonException {
        jsonWriter.onFloat(f);
    }

    @Override
    public void onInteger(int i) throws JsonException {
        jsonWriter.onInteger(i);
    }

    @Override
    public void onKey(String key) throws JsonException {
        jsonWriter.onKey(key);
    }

    @Override
    public void onLong(long l) throws JsonException {
        jsonWriter.onLong(l);
    }

    @Override
    public void onNull() throws JsonException {
        jsonWriter.onNull();
    }

    @Override
    public void onString(String s) throws JsonException {
        jsonWriter.onString(s);
    }

    public void reset() {
        jsonWriter.reset();
    }

}
