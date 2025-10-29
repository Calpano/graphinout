package com.graphinout.foundation.json.writer.impl;

import com.graphinout.foundation.json.writer.JsonWriter;

/**
 * Reconstructs a JSON syntax string from a {@link JsonWriter}. Reusable.
 */
public class StringBuilderJsonWriter extends AppendableJsonWriter implements JsonWriter {


    public StringBuilderJsonWriter() {
        super(new StringBuilder());
    }

    public String json() {
        return appendable().toString();
    }


}
