package com.calpano.graphinout.foundation.json.value.java;

import com.calpano.graphinout.foundation.json.stream.impl.Json2StringWriter;
import com.calpano.graphinout.foundation.json.value.IJsonFactory;
import com.calpano.graphinout.foundation.json.value.IJsonObject;
import com.calpano.graphinout.foundation.json.value.IJsonObjectAppendable;
import com.calpano.graphinout.foundation.json.value.IJsonObjectMutable;
import com.calpano.graphinout.foundation.json.value.IJsonValue;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public class JavaJsonObject implements IJsonObjectMutable {

    private final Map<String, IJsonValue> map = new TreeMap<>();

    public static JavaJsonObject copyOf(IJsonObject object) {
        JavaJsonObject copy = new JavaJsonObject();
        object.forEach(copy::addProperty);
        return copy;
    }

    public static IJsonObjectAppendable of(Map<String, IJsonValue> map) {
        return new JavaJsonObject().putAll(map);
    }

    @Override
    public IJsonObjectAppendable addProperty(String key, IJsonValue jsonValue) {
        if (map.containsKey(key))
            throw new IllegalStateException("Property '" + key + "' already present with value=" + map.get(key));

        IJsonValue prev = map.put(key, jsonValue);
        assert prev == null;
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
    public IJsonObjectMutable removeProperty(String key) {
        map.remove(key);
        return this;
    }

    @Override
    public int size() {
        return map.size();
    }

    @Override
    public String toString() {
        Json2StringWriter w = new Json2StringWriter();
        fire(w);
        return w.jsonString();
    }

    private IJsonObjectAppendable putAll(Map<String, IJsonValue> map) {
        this.map.putAll(map);
        return this;
    }

}
