package com.graphinout.foundation.json.writer.impl;

import com.graphinout.foundation.json.JsonEvent;
import com.graphinout.foundation.json.writer.JsonWriter;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.function.Consumer;

public class JsonEvent2Writer implements Consumer<JsonEvent> {

    private final JsonWriter jsonWriter;

    public JsonEvent2Writer(JsonWriter jsonWriter) {this.jsonWriter = jsonWriter;}

    @Override
    public void accept(JsonEvent jsonEvent) {
        switch (jsonEvent.type()) {
            case DocumentStart -> jsonWriter.documentStart();
            case DocumentEnd -> jsonWriter.documentEnd();
            case ArrayStart -> jsonWriter.arrayStart();
            case ArrayEnd -> jsonWriter.arrayEnd();
            case ObjectStart -> jsonWriter.objectStart();
            case ObjectEnd -> jsonWriter.objectEnd();
            case PropertyName -> jsonWriter.onKey((String) jsonEvent.payload());
            case Value -> {
                Object o = jsonEvent.payload();
                switch (o) {
                    case null -> {
                        jsonWriter.onNull();
                    }
                    case String s -> jsonWriter.onString(s);
                    case BigInteger b -> jsonWriter.onBigInteger(b);
                    case BigDecimal d -> jsonWriter.onBigDecimal(d);
                    case Boolean b -> jsonWriter.onBoolean(b);
                    case Double d -> jsonWriter.onDouble(d);
                    case Long l -> jsonWriter.onLong(l);
                    case Integer i -> jsonWriter.onInteger(i);
                    default -> throw new IllegalStateException("Unexpected value: " + o.getClass());
                }
            }
        }
    }

}
