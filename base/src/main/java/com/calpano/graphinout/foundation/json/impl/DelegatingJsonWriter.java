package com.calpano.graphinout.foundation.json.impl;

import com.calpano.graphinout.foundation.json.JsonException;
import com.calpano.graphinout.foundation.json.stream.JsonWriter;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

public class DelegatingJsonWriter implements JsonWriter {

    protected final List<JsonWriter> jsonWriters = new ArrayList<>();

    public DelegatingJsonWriter(JsonWriter jsonWriter) {addJsonWriter(jsonWriter);}

    public DelegatingJsonWriter addJsonWriter(JsonWriter jsonWriter) {
        jsonWriters.add(jsonWriter);
        return this;
    }

    @Override
    public void arrayEnd() throws JsonException {
        jsonWriters.forEach(JsonWriter::arrayEnd);
    }

    @Override
    public void arrayStart() throws JsonException {
        jsonWriters.forEach(JsonWriter::arrayStart);
    }

    @Override
    public void documentEnd() throws JsonException {
        jsonWriters.forEach(JsonWriter::documentEnd);
    }

    @Override
    public void documentStart() throws JsonException {
        jsonWriters.forEach(JsonWriter::documentStart);
    }

    @Override
    public void objectEnd() throws JsonException {
        jsonWriters.forEach(JsonWriter::objectEnd);
    }

    @Override
    public void objectStart() throws JsonException {
        jsonWriters.forEach(JsonWriter::objectStart);
    }

    @Override
    public void onBigDecimal(BigDecimal bigDecimal) throws JsonException {
        jsonWriters.forEach(w -> w.onBigDecimal(bigDecimal));
    }

    @Override
    public void onBigInteger(BigInteger bigInteger) throws JsonException {
        jsonWriters.forEach(w -> w.onBigInteger(bigInteger));
    }

    @Override
    public void onBoolean(boolean b) throws JsonException {
        jsonWriters.forEach(w -> w.onBoolean(b));
    }

    @Override
    public void onDouble(double d) throws JsonException {
        jsonWriters.forEach(w -> w.onDouble(d));
    }

    @Override
    public void onFloat(float f) throws JsonException {
        jsonWriters.forEach(w -> w.onFloat(f));
    }

    @Override
    public void onInteger(int i) throws JsonException {
        jsonWriters.forEach(w -> w.onInteger(i));
    }

    @Override
    public void onKey(String key) throws JsonException {
        jsonWriters.forEach(w -> w.onKey(key));
    }

    @Override
    public void onLong(long l) throws JsonException {
        jsonWriters.forEach(w -> w.onLong(l));
    }

    @Override
    public void onNull() throws JsonException {
        jsonWriters.forEach(JsonWriter::onNull);
    }

    @Override
    public void onString(String s) throws JsonException {
        jsonWriters.forEach(w -> w.onString(s));
    }

}
