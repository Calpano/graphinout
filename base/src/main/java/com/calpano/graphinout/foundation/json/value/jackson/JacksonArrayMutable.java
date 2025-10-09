package com.calpano.graphinout.foundation.json.value.jackson;

import com.calpano.graphinout.foundation.json.value.IJsonArrayMutable;
import com.calpano.graphinout.foundation.json.value.IJsonFactory;
import com.calpano.graphinout.foundation.json.value.IJsonValue;
import com.fasterxml.jackson.databind.node.ArrayNode;

public class JacksonArrayMutable implements IJsonArrayMutable {

    private final ArrayNode arrayNode;

    public JacksonArrayMutable(ArrayNode arrayNode) {this.arrayNode = arrayNode;}

    public static IJsonArrayMutable of(ArrayNode jsonNodes) {
        return new JacksonArrayMutable(jsonNodes);
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
    public void remove(int index) throws ArrayIndexOutOfBoundsException {
        arrayNode.remove(index);
    }

    @Override
    public void set(int index, IJsonValue jsonValue) throws ArrayIndexOutOfBoundsException {
        arrayNode.set(index, JacksonValues.jacksonValue(jsonValue));
    }

    @Override
    public int size() {
        return arrayNode.size();
    }

}
