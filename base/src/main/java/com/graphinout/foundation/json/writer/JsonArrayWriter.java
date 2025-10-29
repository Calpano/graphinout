package com.graphinout.foundation.json.writer;

import com.graphinout.foundation.json.JsonException;

import java.util.function.Consumer;

public interface JsonArrayWriter extends IHasJsonValueWriter {

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
