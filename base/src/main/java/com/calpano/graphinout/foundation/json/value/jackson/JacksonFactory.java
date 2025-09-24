package com.calpano.graphinout.foundation.json.value.jackson;

import com.calpano.graphinout.foundation.json.value.IJsonArray;
import com.calpano.graphinout.foundation.json.value.IJsonArrayAppendable;
import com.calpano.graphinout.foundation.json.value.IJsonFactory;
import com.calpano.graphinout.foundation.json.value.IJsonObject;
import com.calpano.graphinout.foundation.json.value.IJsonObjectAppendable;
import com.calpano.graphinout.foundation.json.value.IJsonPrimitive;
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
    public IJsonArrayAppendable createArrayAppendable() {
        return JacksonAppendableArray.of(nodeFactory.arrayNode());
    }

    @Override
    public IJsonPrimitive createBigDecimal(BigDecimal bigDecimal) {
        return JacksonPrimitive.of(nodeFactory.numberNode(bigDecimal));
    }

    @Override
    public IJsonPrimitive createBigInteger(BigInteger bigInteger) {
        return JacksonPrimitive.of(nodeFactory.numberNode(bigInteger));
    }

    @Override
    public IJsonPrimitive createBoolean(boolean b) {
        return JacksonPrimitive.of(nodeFactory.booleanNode(b));
    }

    @Override
    public IJsonPrimitive createDouble(double d) {
        return JacksonPrimitive.of(nodeFactory.numberNode(d));
    }

    @Override
    public IJsonPrimitive createFloat(float f) {
        return JacksonPrimitive.of(nodeFactory.numberNode(f));
    }

    @Override
    public IJsonPrimitive createInteger(int i) {
        return JacksonPrimitive.of(nodeFactory.numberNode(i));
    }

    @Override
    public IJsonPrimitive createLong(long l) {
        return JacksonPrimitive.of(nodeFactory.numberNode(l));
    }

    @Override
    public IJsonPrimitive createNull() {
        return JacksonPrimitive.of(nodeFactory.nullNode());
    }

    @Override
    public IJsonObject createObject() {
        return createObjectAppendable();
    }

    @Override
    public IJsonObjectAppendable createObjectAppendable() {
        return JacksonAppendableObject.of(nodeFactory.objectNode());
    }

    @Override
    public IJsonPrimitive createString(String s) {
        return JacksonPrimitive.of(nodeFactory.textNode(s));
    }

}
