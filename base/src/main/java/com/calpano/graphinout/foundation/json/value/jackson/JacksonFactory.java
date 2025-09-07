package com.calpano.graphinout.foundation.json.value.jackson;

import com.calpano.graphinout.foundation.json.value.IAppendableJsonArray;
import com.calpano.graphinout.foundation.json.value.IAppendableJsonObject;
import com.calpano.graphinout.foundation.json.value.IJsonArray;
import com.calpano.graphinout.foundation.json.value.IJsonFactory;
import com.calpano.graphinout.foundation.json.value.IJsonObject;
import com.calpano.graphinout.foundation.json.value.IJsonValue;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;

import java.math.BigDecimal;
import java.math.BigInteger;

public class JacksonFactory implements IJsonFactory {

    public static final JacksonFactory INSTANCE = new JacksonFactory();

    private final JsonNodeFactory nodeFactory = JsonNodeFactory.instance;

    @Override
    public IJsonArray createArray() {
        return createArrayAppendable();
    }

    @Override
    public IAppendableJsonArray createArrayAppendable() {
        return JacksonAppendableArray.of(nodeFactory.arrayNode());
    }

    @Override
    public IJsonValue createBigDecimal(BigDecimal bigDecimal) {
        return JacksonPrimitive.of(nodeFactory.numberNode(bigDecimal));
    }

    @Override
    public IJsonValue createBigInteger(BigInteger bigInteger) {
        return JacksonPrimitive.of(nodeFactory.numberNode(bigInteger));
    }

    @Override
    public IJsonValue createBoolean(boolean b) {
        return JacksonPrimitive.of(nodeFactory.booleanNode(b));
    }

    @Override
    public IJsonValue createDouble(double d) {
        return JacksonPrimitive.of(nodeFactory.numberNode(d));
    }

    @Override
    public IJsonValue createFloat(float f) {
        return JacksonPrimitive.of(nodeFactory.numberNode(f));
    }

    @Override
    public IJsonValue createInteger(int i) {
        return JacksonPrimitive.of(nodeFactory.numberNode(i));
    }

    @Override
    public IJsonValue createLong(long l) {
        return JacksonPrimitive.of(nodeFactory.numberNode(l));
    }

    @Override
    public IJsonValue createNull() {
        return JacksonPrimitive.of(nodeFactory.nullNode());
    }

    @Override
    public IJsonObject createObject() {
        return createObjectAppendable();
    }

    @Override
    public IAppendableJsonObject createObjectAppendable() {
        return JacksonAppendableObject.of(nodeFactory.objectNode());
    }

    @Override
    public IJsonValue createString(String s) {
        return JacksonPrimitive.of(nodeFactory.textNode(s));
    }

}
