package com.graphinout.foundation.json.value.jackson;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.graphinout.foundation.json.value.IJsonFactory;
import com.graphinout.foundation.json.value.IJsonObjectMutable;
import com.graphinout.foundation.json.value.IJsonValue;

import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.Set;

public class JacksonObjectMutable implements IJsonObjectMutable {

    private final ObjectNode objectNode;

    public JacksonObjectMutable(ObjectNode objectNode) {this.objectNode = objectNode;}

    public static IJsonObjectMutable of(ObjectNode jsonNodes) {
        return new JacksonObjectMutable(jsonNodes);
    }

    @Override
    public IJsonObjectMutable addProperty(String key, IJsonValue jsonValue) {
        if (objectNode.has(key))
            throw new IllegalStateException("Property '" + key + "' already present with value=" + objectNode.get(key));
        objectNode.set(key, JacksonValues.jacksonValue(jsonValue));
        return this;
    }

    @Override
    public Object base() {
        return objectNode;
    }

    @Override
    public IJsonFactory factory() {
        return JacksonFactory.INSTANCE;
    }

    @Nullable
    @Override
    public IJsonValue get(String key) {
        return JacksonValues.ofNullable(objectNode.get(key));
    }

    @Override
    public Set<String> keys() {
        Set<String> keys = new HashSet<>();
        objectNode.fieldNames().forEachRemaining(keys::add);
        return keys;
    }

    public ObjectNode objectNode() {
        return objectNode;
    }

    @Override
    public IJsonObjectMutable removeProperty(String key) {
        objectNode.remove(key);
        return this;
    }

    @Override
    public int size() {
        return objectNode.size();
    }

}
