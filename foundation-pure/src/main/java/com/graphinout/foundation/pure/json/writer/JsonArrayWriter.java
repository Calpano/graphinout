package com.graphinout.foundation.pure.json.writer;


import com.graphinout.foundation.pure.json.JsonException;

import java.util.function.Consumer;

/**
 * Streaming writer for a JSON array.
 */
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
