package com.graphinout.foundation.pure.json.writer.impl;

import com.graphinout.foundation.pure.json.JsonEvent;
import com.graphinout.foundation.pure.json.writer.JsonWriter;

import java.util.function.Consumer;

public class JsonEvent2Writer implements Consumer<JsonEvent> {

    private final JsonWriter jsonWriter;

    public JsonEvent2Writer(JsonWriter jsonWriter) {this.jsonWriter = jsonWriter;}

    @Override
    public void accept(JsonEvent jsonEvent) {
        switch (jsonEvent.type()) {
            case DocumentStart:
                jsonWriter.documentStart();
                break;
            case DocumentEnd:
                jsonWriter.documentEnd();
                break;
            case ArrayStart:
                jsonWriter.arrayStart();
                break;
            case ArrayEnd:
                jsonWriter.arrayEnd();
                break;
            case ObjectStart:
                jsonWriter.objectStart();
                break;
            case ObjectEnd:
                jsonWriter.objectEnd();
                break;
            case PropertyName:
                jsonWriter.onKey((String) jsonEvent.payload());
                break;
            case Value:
                Object o = jsonEvent.payload();
                if (o == null) {
                    jsonWriter.onNull();
                } else if (o instanceof String) {
                    jsonWriter.onString((String) o);
                } else if (o instanceof Number) {
                    jsonWriter.onNumber((Number) o);
                } else if (o instanceof Boolean) {
                    jsonWriter.onBoolean((Boolean) o);
                } else {
                    throw new IllegalStateException("Unexpected value: " + o.getClass());
                }
                break;
        }
    }

}
