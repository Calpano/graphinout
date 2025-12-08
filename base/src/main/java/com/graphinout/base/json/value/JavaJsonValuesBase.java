package com.graphinout.base.json.value;

import com.graphinout.base.json.JsonReaderImpl;
import com.graphinout.foundation.pure.json.document.IJsonValue;
import com.graphinout.foundation.pure.json.writer.impl.Json2JavaJsonWriter;

import java.io.IOException;

public class JavaJsonValuesBase {

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
