package com.calpano.graphinout.foundation.json.stream;

import com.calpano.graphinout.foundation.json.JsonException;

import java.util.function.Consumer;

public interface JsonObjectWriter extends JsonPropertyWriter {

    default void object(Consumer<JsonPropertyWriter> consumer) {
        objectStart();
        consumer.accept(this);
        objectEnd();
    }

    /**
     * JSON Object
     */
    void objectEnd() throws JsonException;

    /**
     * JSON Object
     */
    void objectStart() throws JsonException;

}
