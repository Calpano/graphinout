package com.calpano.graphinout.foundation.json.value;

import com.calpano.graphinout.foundation.json.JsonType;
import com.calpano.graphinout.foundation.json.stream.JsonWriter;

public interface IJsonValue {

    default IJsonArray asArray() throws ClassCastException {
        return (IJsonArray) this;
    }

    default IJsonObject asObject() throws ClassCastException {
        return (IJsonObject) this;
    }

    default IJsonPrimitive asPrimitive() throws ClassCastException {
        return (IJsonPrimitive) this;
    }

    /** the underlying implementation */
    Object base();

    IJsonFactory factory();

    boolean isArray();

    boolean isObject();

    boolean isPrimitive();

    JsonType jsonType();

    default void fire(JsonWriter jsonWriter) {
        if (isArray()) {
            jsonWriter.arrayStart();
            asArray().forEach(member-> member.fire(jsonWriter));
            jsonWriter.arrayEnd();
        } else if (isObject()) {
            jsonWriter.objectStart();
            asObject().forEach((key, value) -> {
                jsonWriter.onKey(key);
                value.fire(jsonWriter);
            });
            jsonWriter.objectEnd();
        } else {
            assert isPrimitive();
            IJsonPrimitive p = asPrimitive();
            p.fire(jsonWriter);
        }
    }



}
