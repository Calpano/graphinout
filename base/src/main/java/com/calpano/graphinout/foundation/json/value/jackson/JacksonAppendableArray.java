package com.calpano.graphinout.foundation.json.value.jackson;

import com.calpano.graphinout.foundation.json.value.IAppendableJsonArray;
import com.calpano.graphinout.foundation.json.value.IJsonFactory;
import com.calpano.graphinout.foundation.json.value.IJsonValue;
import com.fasterxml.jackson.databind.node.ArrayNode;

public class JacksonAppendableArray implements IAppendableJsonArray {

    private final ArrayNode arrayNode;

    public JacksonAppendableArray(ArrayNode arrayNode) {this.arrayNode = arrayNode;}

    public static IAppendableJsonArray of(ArrayNode jsonNodes) {
        return new JacksonAppendableArray(jsonNodes);
    }

    @Override
    public void add(IJsonValue jsonValue) {
        arrayNode.add(JacksonValues.jacksonValue(jsonValue));
    }

    public ArrayNode arrayNode() {
        return arrayNode;
    }

    @Override
    public Object base() {
        return arrayNode;
    }

    @Override
    public IJsonFactory factory() {
        return JacksonFactory.INSTANCE;
    }

    @Override
    public IJsonValue get(int index) {
        return JacksonValues.ofNullable(arrayNode.get(index));
    }

    @Override
    public int size() {
        return arrayNode.size();
    }

}
