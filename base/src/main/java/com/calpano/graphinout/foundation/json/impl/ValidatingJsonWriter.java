package com.calpano.graphinout.foundation.json.impl;

import com.calpano.graphinout.foundation.json.JsonException;
import com.calpano.graphinout.foundation.json.JsonWriter;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Stack;

public class ValidatingJsonWriter implements JsonWriter {

    enum Container {Document, Array, Object, Property, String}

    private final Stack<Container> stack = new Stack<>();

    @Override
    public void arrayEnd() throws JsonException {
        Container top = stack.pop();
        if (top != Container.Array) {
            throw new IllegalStateException("Expected array start on stack, but found " + top);
        }
    }

    @Override
    public void arrayStart() throws JsonException {
        stack.push(Container.Array);
    }

    @Override
    public void documentEnd() {
        Container top = stack.pop();
        if (top != Container.Document) {
            throw new IllegalStateException("Expected document start om stack, but found " + top);
        }

    }

    @Override
    public void documentStart() {
        if (!stack.isEmpty())
            throw new IllegalStateException("Cannot start a document again");
        stack.push(Container.Document);
    }

    @Override
    public void objectEnd() throws JsonException {
        Container top = stack.pop();
        if (top != Container.Object) {
            throw new IllegalStateException("Expected object start on stack, but found " + top);
        }
    }

    @Override
    public void objectStart() throws JsonException {
        stack.push(Container.Object);
    }

    @Override
    public void onBigDecimal(BigDecimal bigDecimal) {
        ensureValue();
    }

    @Override
    public void onBigInteger(BigInteger bigIntegerValue) {
        ensureValue();
    }

    @Override
    public void onBoolean(boolean b) throws JsonException {
        ensureValue();
    }

    @Override
    public void onDouble(double d) throws JsonException {
        ensureValue();
    }

    @Override
    public void onFloat(float f) throws JsonException {
        ensureValue();
    }

    @Override
    public void onInteger(int i) throws JsonException {
        ensureValue();
    }

    @Override
    public void onKey(String key) throws JsonException {
        Container top = stack.peek();
        if (top != Container.Object) {
            throw new IllegalStateException("Expected object on stack, but found " + top);
        }
        stack.push(Container.Property);
    }

    @Override
    public void onLong(long l) throws JsonException {
        ensureValue();
    }

    @Override
    public void onNull() throws JsonException {
        ensureValue();
    }

    @Override
    public void stringCharacters(String s) throws JsonException {
        Container top = stack.peek();
        if (top != Container.String) {
            throw new IllegalStateException("Expected string start on stack, but found " + top);
        }
    }

    @Override
    public void stringEnd() throws JsonException {
        Container top = stack.pop();
        if (top != Container.String) {
            throw new IllegalStateException("Expected string start on stack, but found " + top);
        }
    }

    @Override
    public void stringStart() throws JsonException {
        ensureValue();
        stack.push(Container.String);
    }

    @Override
    public void whitespaceCharacters(String s) throws JsonException {
        // legal at any point
    }

    private void ensureValue() {
        // where is a value legal? in property, in array and as root in document
        Container top = stack.peek();
        if (top != Container.Property && top != Container.Array && top != Container.Document) {
            throw new IllegalStateException("Expected property, array or document on stack, but found " + top);
        }
        if (top == Container.Property) {
            stack.pop(); // Pop the Property state after its value is written
        }
    }

}
