package com.calpano.graphinout.foundation.json.value.java;

import com.calpano.graphinout.foundation.json.stream.impl.Json2JavaJsonWriter;
import com.calpano.graphinout.foundation.json.stream.impl.JsonReaderImpl;
import com.calpano.graphinout.foundation.json.value.IJsonValue;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public class JavaJsonValues {

    public static boolean isArray(Object javaJson) {
        return javaJson instanceof List;
    }

    public static boolean isObject(Object javaJson) {
        return javaJson instanceof Map;
    }

    /**
     * @param javaJson null is JsonNull
     */
    public static IJsonValue ofJavaValue(@Nullable Object javaJson) {
        if (javaJson == null) {
            return JavaJsonFactory.INSTANCE.createNull();
        }
        if (isArray(javaJson)) {
            return JavaJsonArray.of((List) javaJson);
        } else if (isObject(javaJson)) {
            return JavaJsonObject.of((Map) javaJson);
        }
        return JavaJsonPrimitive.of(javaJson);
    }

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

    /**
     * @param jsonNode null is null
     */
    public static @Nullable IJsonValue ofNullable(@Nullable Object jsonNode) {
        if (jsonNode == null) {
            return null;
        }
        return ofJavaValue(jsonNode);
    }

}
