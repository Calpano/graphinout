package com.calpano.graphinout.foundation.json.stream.impl;

import com.calpano.graphinout.foundation.json.JsonException;
import com.calpano.graphinout.foundation.json.stream.JsonWriter;
import com.calpano.graphinout.foundation.json.value.IAppendableJsonArray;
import com.calpano.graphinout.foundation.json.value.IAppendableJsonObject;
import com.calpano.graphinout.foundation.json.value.IJsonValue;
import com.calpano.graphinout.foundation.json.value.java.JavaJsonAppendableArray;
import com.calpano.graphinout.foundation.json.value.java.JavaJsonAppendableObject;
import com.calpano.graphinout.foundation.json.value.java.JavaJsonPrimitive;

import javax.annotation.Nullable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Stack;

/**
 * Base class for collecting all JSON calls into a string. Impl uses {@link #jsonValue()} and {@link #reset()}.
 */
public class Json2JavaJsonWriter implements JsonWriter {

    private final Stack<Object> stack = new Stack<>();
    private IJsonValue root = null;

    @Override
    public void arrayEnd() throws JsonException {
        pop(IAppendableJsonArray.class);
    }

    @Override
    public void arrayStart() throws JsonException {
        JavaJsonAppendableArray array = new JavaJsonAppendableArray();
        attach(array);
        stack.push(array);
    }

    @Override
    public void documentEnd() throws JsonException {
        // do nothing
    }

    @Override
    public void documentStart() throws JsonException {
        // do nothing
    }

    /** get the result */
    public @Nullable IJsonValue jsonValue() {
        return root;
    }

    @Override
    public void objectEnd() throws JsonException {
        pop(IAppendableJsonObject.class);
    }

    @Override
    public void objectStart() throws JsonException {
        IAppendableJsonObject object = new JavaJsonAppendableObject();
        attach(object);
        stack.push(object);
    }

    @Override
    public void onBigDecimal(BigDecimal bigDecimal) throws JsonException {
        attach(JavaJsonPrimitive.of(bigDecimal));
    }

    @Override
    public void onBigInteger(BigInteger bigInteger) throws JsonException {
        attach(JavaJsonPrimitive.of(bigInteger));
    }

    @Override
    public void onBoolean(boolean b) throws JsonException {
        attach(JavaJsonPrimitive.of(b));
    }

    @Override
    public void onDouble(double d) throws JsonException {
        attach(JavaJsonPrimitive.of(d));
    }

    @Override
    public void onFloat(float f) throws JsonException {
        attach(JavaJsonPrimitive.of(f));
    }

    @Override
    public void onInteger(int i) throws JsonException {
        attach(JavaJsonPrimitive.of(i));
    }

    @Override
    public void onKey(String key) throws JsonException {
        assert stack.peek() instanceof IAppendableJsonObject;
        stack.push(key);
    }

    @Override
    public void onLong(long l) throws JsonException {
        attach(JavaJsonPrimitive.of(l));
    }

    @Override
    public void onNull() throws JsonException {
        attach(JavaJsonPrimitive.NULL);
    }

    @Override
    public void onString(String s) throws JsonException {
        attach(JavaJsonPrimitive.of(s));
    }

    public void reset() {
        root = null;
        stack.clear();
    }

    private void attach(IJsonValue value) {
        if (root == null) {
            root = value;
        } else {
            Object o = stack.peek();
            if (o instanceof IAppendableJsonArray parent) {
                parent.add(value);
            } else {
                String key = pop(String.class);
                IAppendableJsonObject obj = (IAppendableJsonObject) stack.peek();
                obj.addProperty(key, value);
            }
        }
    }

    private <T> T pop(Class<T> clazz) {
        assert !stack.isEmpty() : "Expected " + clazz + " but stack is empty";
        Object o = stack.pop();
        if (!clazz.isInstance(o)) {
            throw new IllegalStateException("Unexpected type: " + o.getClass());
        }
        return clazz.cast(o);
    }

}
