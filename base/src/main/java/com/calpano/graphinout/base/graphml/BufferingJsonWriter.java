package com.calpano.graphinout.base.graphml;

import com.calpano.graphinout.base.gio.dom.JsonArray;
import com.calpano.graphinout.base.gio.dom.JsonObject;
import com.calpano.graphinout.base.gio.dom.JsonValue;
import com.calpano.graphinout.foundation.json.JsonElementWriter;
import com.calpano.graphinout.foundation.json.JsonException;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Stack;

public class BufferingJsonWriter implements JsonElementWriter {

    static class Property {

        final String key;
        JsonObject jsonObject;

        Property(JsonObject jsonObject, String key) {
            this.jsonObject = jsonObject;
            this.key = key;
        }

        public void add(JsonValue jsonValue) {
            jsonObject.addProperty(key, jsonValue);
        }

    }

    private final Stack<Object> stack = new Stack<>();
    private final StringBuilder stringBuffer = new StringBuilder();
    private JsonValue root;

    @Override
    public void arrayEnd() throws JsonException {
        Object o = stack.pop();
        JsonArray jsonArray = (JsonArray) o;
        addValueToCurrentContainer(jsonArray);
    }

    @Override
    public void arrayStart() throws JsonException {
        stack.push(JsonArray.create());
    }

    @Override
    public void objectEnd() throws JsonException {
        Object o = stack.pop();
        JsonObject jsonObject = (JsonObject) o;
        addValueToCurrentContainer(jsonObject);
    }

    @Override
    public void objectStart() throws JsonException {
        stack.push(JsonObject.create());
        if (root == null) {
            root = (JsonValue) stack.peek();
        }
    }

    @Override
    public void onBigDecimal(BigDecimal bigDecimal) throws JsonException {
        addValueToCurrentContainer(JsonValue.bigDecimalValue(bigDecimal));
    }

    @Override
    public void onBigInteger(BigInteger bigInteger) throws JsonException {
        addValueToCurrentContainer(JsonValue.bigIntegerValue(bigInteger));
    }

    @Override
    public void onBoolean(boolean b) throws JsonException {
        addValueToCurrentContainer(JsonValue.booleanValue(b));
    }

    @Override
    public void onDouble(double d) throws JsonException {
        addValueToCurrentContainer(JsonValue.doubleValue(d));
    }

    @Override
    public void onFloat(float f) throws JsonException {
        addValueToCurrentContainer(JsonValue.floatValue(f));
    }

    @Override
    public void onInteger(int i) throws JsonException {
        addValueToCurrentContainer(JsonValue.intValue(i));
    }

    @Override
    public void onKey(String key) throws JsonException {
        JsonObject jsonObject = (JsonObject) stack.peek();
        Property property = new Property(jsonObject, key);
        stack.push(property);
    }

    @Override
    public void onLong(long l) throws JsonException {
        addValueToCurrentContainer(JsonValue.longValue(l));
    }

    @Override
    public void onNull() throws JsonException {
        addValueToCurrentContainer(JsonValue.nullValue());
    }

    @Override
    public void stringCharacters(String s) throws JsonException {
        stringBuffer.append(s);
    }

    @Override
    public void stringEnd() throws JsonException {
        String s = stringBuffer.toString();
        addValueToCurrentContainer(JsonValue.stringValue(s));
    }

    @Override
    public void stringStart() throws JsonException {
        stringBuffer.setLength(0);
    }

    @Override
    public void whitespaceCharacters(String s) throws JsonException {
        // whitespace is ignored
    }

    private void addValueToCurrentContainer(JsonValue jsonValue) {
        if (root == null) {
            root = jsonValue;
            return;
        }
        Object top = stack.peek();
        if (top instanceof JsonArray) {
            ((JsonArray) top).add(jsonValue);
        } else if (top instanceof Property property) {
            property.add(jsonValue);
            stack.pop();
        }
    }

}
