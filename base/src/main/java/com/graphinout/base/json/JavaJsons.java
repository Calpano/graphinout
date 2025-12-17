package com.graphinout.base.json;

import com.graphinout.foundation.pure.collections.jajson.JaJson;
import com.graphinout.foundation.pure.json.document.IJsonValue;
import com.graphinout.foundation.pure.json.writer.impl.Json2JavaJsonWriter;

import java.io.IOException;

/**
 * Java-based implementation of {@link IJsonValue} API, as opposed ot the Jackson-basewd implementation.
 * Do not confuse with the {@link JaJson} API.
 */
public class JavaJsons {

    public static IJsonValue ofJsonString(String jsonString) {
        // parse
        JsonReaderImpl reader = new JsonReaderImpl();
        Json2JavaJsonWriter w = new Json2JavaJsonWriter();
        try {
            reader.readStandardJsonString(jsonString, w);
            return w.jsonValue();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
