package com.calpano.graphinout.foundation.json.value.java;

import com.calpano.graphinout.foundation.json.value.IAppendableJsonArray;
import com.calpano.graphinout.foundation.json.value.IAppendableJsonObject;
import com.calpano.graphinout.foundation.json.value.IJsonArray;
import com.calpano.graphinout.foundation.json.value.IJsonFactory;
import com.calpano.graphinout.foundation.json.value.IJsonObject;
import com.calpano.graphinout.foundation.json.value.IJsonPrimitive;

import java.math.BigDecimal;
import java.math.BigInteger;

public class JavaJsonFactory implements IJsonFactory {

    public static final JavaJsonFactory INSTANCE = new JavaJsonFactory();

    @Override
    public IJsonArray createArray() {
        return createArrayAppendable();
    }

    @Override
    public IAppendableJsonArray createArrayAppendable() {
        return new JavaJsonAppendableArray();
    }

    @Override
    public IJsonPrimitive createBigDecimal(BigDecimal bigDecimal) {
        return JavaJsonPrimitive.of(bigDecimal);
    }

    @Override
    public IJsonPrimitive createBigInteger(BigInteger bigInteger) {
        return JavaJsonPrimitive.of(bigInteger);
    }

    @Override
    public IJsonPrimitive createBoolean(boolean b) {
        return JavaJsonPrimitive.of(b);
    }

    @Override
    public IJsonPrimitive createDouble(double d) {
        return JavaJsonPrimitive.of(d);
    }

    @Override
    public IJsonPrimitive createFloat(float f) {
        return JavaJsonPrimitive.of(f);
    }

    @Override
    public IJsonPrimitive createInteger(int i) {
        return JavaJsonPrimitive.of(i);
    }

    @Override
    public IJsonPrimitive createLong(long l) {
        return JavaJsonPrimitive.of(l);
    }

    @Override
    public IJsonPrimitive createNull() {
        return JavaJsonPrimitive.NULL;
    }

    @Override
    public IJsonObject createObject() {
        return createObjectAppendable();
    }

    @Override
    public IAppendableJsonObject createObjectAppendable() {
        return new JavaJsonAppendableObject();
    }

    @Override
    public IJsonPrimitive createString(String s) {
        return JavaJsonPrimitive.of(s);
    }

}
