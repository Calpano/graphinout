package com.graphinout.foundation.json.stream.impl;

import com.graphinout.foundation.json.stream.JsonWriter;

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
