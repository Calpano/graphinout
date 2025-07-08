package com.calpano.graphinout.base.gio.dom;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

public class JsonObject implements JsonValue {

    private GioType type;
    Map<String, JsonValue> properties = new HashMap<>();

    public static JsonObject create() {
        return new JsonObject();
    }

    public void addProperty(String key, JsonValue jsonValue) {
        properties.put(key, jsonValue);
    }


    @Nullable
    @Override
    public GioType type() {
        return type;
    }

}
