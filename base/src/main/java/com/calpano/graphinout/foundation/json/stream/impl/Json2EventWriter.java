package com.calpano.graphinout.foundation.json.stream.impl;

import com.calpano.graphinout.foundation.json.JsonEvent;
import com.calpano.graphinout.foundation.json.JsonException;
import com.calpano.graphinout.foundation.json.stream.JsonWriter;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.function.Consumer;

import static com.calpano.graphinout.foundation.json.JsonEvent.Type.ArrayEnd;
import static com.calpano.graphinout.foundation.json.JsonEvent.Type.ArrayStart;
import static com.calpano.graphinout.foundation.json.JsonEvent.Type.DocumentEnd;
import static com.calpano.graphinout.foundation.json.JsonEvent.Type.DocumentStart;
import static com.calpano.graphinout.foundation.json.JsonEvent.Type.ObjectEnd;
import static com.calpano.graphinout.foundation.json.JsonEvent.Type.ObjectStart;
import static com.calpano.graphinout.foundation.json.JsonEvent.Type.PropertyName;
import static com.calpano.graphinout.foundation.json.JsonEvent.Type.Value;

public class Json2EventWriter implements JsonWriter {

    private final Consumer<JsonEvent> eventConsumer;

    public Json2EventWriter(Consumer<JsonEvent> eventConsumer) {this.eventConsumer = eventConsumer;}

    @Override
    public void arrayEnd() throws JsonException {
        eventConsumer.accept(JsonEvent.of(ArrayEnd));
    }

    @Override
    public void arrayStart() throws JsonException {
        eventConsumer.accept(JsonEvent.of(ArrayStart));
    }

    @Override
    public void documentEnd() throws JsonException {
        eventConsumer.accept(JsonEvent.of(DocumentEnd));
    }

    @Override
    public void documentStart() throws JsonException {
        eventConsumer.accept(JsonEvent.of(DocumentStart));
    }

    @Override
    public void objectEnd() throws JsonException {
        eventConsumer.accept(JsonEvent.of(ObjectEnd));
    }

    @Override
    public void objectStart() throws JsonException {
        eventConsumer.accept(JsonEvent.of(ObjectStart));
    }

    @Override
    public void onBigDecimal(BigDecimal bigDecimal) throws JsonException {
        eventConsumer.accept(JsonEvent.of(Value, bigDecimal.toString()));
    }

    @Override
    public void onBigInteger(BigInteger bigInteger) throws JsonException {
        eventConsumer.accept(JsonEvent.of(Value, bigInteger.toString()));
    }

    @Override
    public void onBoolean(boolean b) throws JsonException {
        eventConsumer.accept(JsonEvent.of(Value, b));
    }

    @Override
    public void onDouble(double d) throws JsonException {
        eventConsumer.accept(JsonEvent.of(Value, d));
    }

    @Override
    public void onFloat(float f) throws JsonException {
        eventConsumer.accept(JsonEvent.of(Value, f));
    }

    @Override
    public void onInteger(int i) throws JsonException {
        eventConsumer.accept(JsonEvent.of(Value, i));
    }

    @Override
    public void onKey(String key) throws JsonException {
        eventConsumer.accept(JsonEvent.of(PropertyName, key));
    }

    @Override
    public void onLong(long l) throws JsonException {
        eventConsumer.accept(JsonEvent.of(Value, l));
    }

    @Override
    public void onNull() throws JsonException {
        eventConsumer.accept(JsonEvent.of(Value, null));
    }

    @Override
    public void onString(String s) throws JsonException {
        eventConsumer.accept(JsonEvent.of(Value, s));
    }

}
