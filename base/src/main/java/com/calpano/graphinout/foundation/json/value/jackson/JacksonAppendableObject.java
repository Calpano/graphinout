package com.calpano.graphinout.foundation.json.value.jackson;

import com.calpano.graphinout.foundation.json.value.IJsonFactory;
import com.calpano.graphinout.foundation.json.value.IJsonObjectAppendable;
import com.calpano.graphinout.foundation.json.value.IJsonValue;
import com.fasterxml.jackson.databind.node.ObjectNode;

import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.Set;

public class JacksonAppendableObject implements IJsonObjectAppendable {

    private final ObjectNode objectNode;

    public JacksonAppendableObject(ObjectNode objectNode) {this.objectNode = objectNode;}

    public static IJsonObjectAppendable of(ObjectNode jsonNodes) {
        return new JacksonAppendableObject(jsonNodes);
    }

    @Override
    public IJsonObjectAppendable addProperty(String key, IJsonValue jsonValue) {
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
    public int size() {
        return objectNode.size();
    }

}
