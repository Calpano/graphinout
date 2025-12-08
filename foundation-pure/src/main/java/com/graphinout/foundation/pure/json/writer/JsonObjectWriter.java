package com.graphinout.foundation.pure.json.writer;


import com.graphinout.foundation.pure.json.JsonException;

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
