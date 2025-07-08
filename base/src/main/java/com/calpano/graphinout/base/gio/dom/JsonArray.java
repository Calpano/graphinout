package com.calpano.graphinout.base.gio.dom;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class JsonArray implements JsonValue {

    final List<JsonValue> list = new ArrayList<>();
    private GioType type;

    public static JsonArray create() {
        return new JsonArray();
    }

    public void add(JsonValue jsonValue) {
        list.add(jsonValue);
    }

    @Nullable
    @Override
    public GioType type() {
        return type;
    }

}
