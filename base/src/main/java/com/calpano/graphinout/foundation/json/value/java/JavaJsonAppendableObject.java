package com.calpano.graphinout.foundation.json.value.java;

import com.calpano.graphinout.foundation.json.value.IAppendableJsonObject;
import com.calpano.graphinout.foundation.json.value.IJsonFactory;
import com.calpano.graphinout.foundation.json.value.IJsonValue;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public class JavaJsonAppendableObject implements IAppendableJsonObject {

    private final Map<String, IJsonValue> map = new TreeMap<>();

    public static IAppendableJsonObject of(Map<String, IJsonValue> map) {
        return new JavaJsonAppendableObject().putAll(map);
    }

    @Override
    public IAppendableJsonObject addProperty(String key, IJsonValue jsonValue) {
        map.put(key, jsonValue);
        return this;
    }

    @Override
    public Object base() {
        return map;
    }

    @Override
    public IJsonFactory factory() {
        return JavaJsonFactory.INSTANCE;
    }

    @Nullable
    @Override
    public IJsonValue get(String key) {
        return map.get(key);
    }

    @Override
    public Set<String> keys() {
        return map.keySet();
    }

    @Override
    public int size() {
        return map.size();
    }

    private IAppendableJsonObject putAll(Map<String, IJsonValue> map) {
        this.map.putAll(map);
        return this;
    }

}
