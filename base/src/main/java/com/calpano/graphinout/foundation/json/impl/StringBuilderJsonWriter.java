package com.calpano.graphinout.foundation.json.impl;

import com.calpano.graphinout.foundation.json.JsonWriter;

/**
 * Reconstructs a JSON syntax string from a {@link JsonWriter}. Reusable.
 */
public class StringBuilderJsonWriter extends AppendableJsonWriter implements JsonWriter {


    public StringBuilderJsonWriter(boolean preserveWhitespace) {
        super(new StringBuilder(), preserveWhitespace);
    }

    public String json() {
        return appendable().toString();
    }


}
