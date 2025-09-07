package com.calpano.graphinout.foundation.json.stream;

import com.calpano.graphinout.foundation.json.JsonException;

import java.util.function.Consumer;

public interface JsonArrayWriter extends HasJsonValueWriter {

    default void array(Consumer<JsonValueWriter> consumer) {
        jsonValueWriter().arrayStart();
        consumer.accept(this.jsonValueWriter());
        jsonValueWriter().arrayEnd();
    }

    /**
     * JSON Array
     */
    void arrayEnd() throws JsonException;

    /**
     * JSON Array
     */
    void arrayStart() throws JsonException;


}
