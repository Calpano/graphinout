package com.calpano.graphinout.foundation.json.stream;

import com.calpano.graphinout.foundation.json.JsonException;

import java.util.function.Consumer;

/** writes properties into an object */
public interface JsonPropertyWriter extends IHasJsonValueWriter {

    /**
     * JSON object property key. Only legal within 'objectStart' and 'objectEnd'
     *
     * @param key the key, without quotes.
     * @throws JsonException if nesting of elements is wrong
     */
    void onKey(String key) throws JsonException;

    default void onPropertyString(String key, String value) {
        onKey(key);
        JsonPrimitiveWriter jsonPrimitiveWriter = jsonValueWriter();
        jsonPrimitiveWriter.onString(value);
    }

    default void onProperty(String key, Consumer<JsonValueWriter> value) {
        onKey(key);
        value.accept(jsonValueWriter());
    }

}
