package com.graphinout.foundation.json.value.java;

import com.graphinout.foundation.json.value.IJsonArray;
import com.graphinout.foundation.json.value.IJsonArrayAppendable;
import com.graphinout.foundation.json.value.IJsonArrayMutable;
import com.graphinout.foundation.json.value.IJsonFactory;
import com.graphinout.foundation.json.value.IJsonObject;
import com.graphinout.foundation.json.value.IJsonObjectAppendable;
import com.graphinout.foundation.json.value.IJsonObjectMutable;
import com.graphinout.foundation.json.value.IJsonPrimitive;

import java.math.BigDecimal;
import java.math.BigInteger;

public class JavaJsonFactory implements IJsonFactory {

    public static final JavaJsonFactory INSTANCE = new JavaJsonFactory();

    @Override
    public IJsonArray createArray() {
        return createArrayAppendable();
    }

    @Override
    public IJsonArrayAppendable createArrayAppendable() {
        return new JavaJsonArray();
    }

    @Override
    public IJsonArrayMutable createArrayMutable() {
        return new JavaJsonArray();
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
    public IJsonObjectAppendable createObjectAppendable() {
        return new JavaJsonObject();
    }

    @Override
    public IJsonObjectMutable createObjectMutable() {
        return new JavaJsonObject();
    }

    @Override
    public IJsonPrimitive createString(String s) {
        return JavaJsonPrimitive.of(s);
    }

}
