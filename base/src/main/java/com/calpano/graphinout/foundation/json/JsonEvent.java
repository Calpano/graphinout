package com.calpano.graphinout.foundation.json;

import javax.annotation.Nullable;

import static com.calpano.graphinout.foundation.json.JsonEvent.Type.ArrayEnd;
import static com.calpano.graphinout.foundation.json.JsonEvent.Type.ArrayStart;
import static com.calpano.graphinout.foundation.json.JsonEvent.Type.DocumentEnd;
import static com.calpano.graphinout.foundation.json.JsonEvent.Type.DocumentStart;
import static com.calpano.graphinout.foundation.json.JsonEvent.Type.ObjectEnd;
import static com.calpano.graphinout.foundation.json.JsonEvent.Type.ObjectStart;
import static com.calpano.graphinout.foundation.json.JsonEvent.Type.Value;

public interface JsonEvent {

    enum Type {
        DocumentStart, DocumentEnd, ArrayStart, ArrayEnd, ObjectStart, ObjectEnd, PropertyName, Value;
    }

    static JsonEvent of(Type type) {
        return of(type, null);
    }

    static JsonEvent of(Type type, @Nullable Object payload) {
        return new JsonEvent() {
            @Override
            public Object payload() {
                return payload;
            }

            public String toString() {
                return type.name() + (payload == null ? "" : ":" + payload);
            }

            @Override
            public Type type() {
                return type;
            }

        };
    }

    default boolean isContainer() {
        return type() == ArrayStart || type() == ObjectStart || type() == ArrayEnd || type() == ObjectEnd;
    }

    default boolean isEnd() {
        return type() == ArrayEnd || type() == ObjectEnd || type() == DocumentEnd;
    }

    default boolean isStart() {
        return type() == ArrayStart || type() == ObjectStart || type() == DocumentStart;
    }

    default boolean isValue() {
        return type() == Value;
    }

    Object payload();

    Type type();


}
