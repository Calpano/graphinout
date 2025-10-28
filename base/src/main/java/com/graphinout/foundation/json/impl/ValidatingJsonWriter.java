package com.graphinout.foundation.json.impl;

import com.graphinout.base.BaseOutput;
import com.graphinout.foundation.json.JsonException;
import com.graphinout.foundation.json.stream.JsonWriter;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.HashSet;
import java.util.Set;
import java.util.Stack;
import java.util.stream.Collectors;

public class ValidatingJsonWriter extends BaseOutput implements JsonWriter {

    enum ContainerType {Document, Array, Object, Property, String}

    interface Container {

        ContainerType type();

    }

    static class ValBase implements Container {

        final ContainerType type;

        ValBase(ContainerType type) {this.type = type;}

        @Override
        public String toString() {
            return "ValBase{" + type + '}';
        }

        @Override
        public ContainerType type() {
            return type;
        }

    }

    static class ValObject implements Container {

        Set<String> usedKeys = new HashSet<>();

        @Override
        public String toString() {
            return "Object{" +
                    "keys=" + usedKeys.stream().map(s -> '"' + s + '"').collect(Collectors.joining(", ")) +
                    '}';
        }

        @Override
        public ContainerType type() {
            return ContainerType.Object;
        }

    }

    private final Stack<Container> stack = new Stack<>();

    static Container container(ContainerType type) {
        return new ValBase(type);
    }

    @Override
    public void arrayEnd() throws JsonException {
        Container top = stack.pop();
        if (top.type() != ContainerType.Array) {
            throw new IllegalStateException("Validation: Expected array start on stack, but found " + top);
        }
    }

    @Override
    public void arrayStart() throws JsonException {
        ensureValue();
        stack.push(container(ContainerType.Array));
    }

    @Override
    public void documentEnd() {
        Container top = stack.pop();
        if (top.type() != ContainerType.Document) {
            throw new IllegalStateException("Validation: Expected document start om stack, but found " + top);
        }

    }

    @Override
    public void documentStart() {
        if (!stack.isEmpty()) throw new IllegalStateException("Validation: Cannot start a document again");
        stack.push(container(ContainerType.Document));
    }

    @Override
    public void objectEnd() throws JsonException {
        Container top = stack.pop();
        if (top.type() != ContainerType.Object) {
            throw new IllegalStateException("Validation: Expected object start on stack, but found " + top);
        }
    }

    @Override
    public void objectStart() throws JsonException {
        ensureValue();
        stack.push(new ValObject());
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
        if (top.type() != ContainerType.Object) {
            throw new IllegalStateException("Validation: Expected object on stack, but found " + top + " stack=" + stack);
        }
        // verify key is not used yet
        ValObject valObject = (ValObject) top;
        if (valObject.usedKeys.contains(key)) {
            throw new IllegalStateException("Validation: Key '" + key + "' already used");
        }
        valObject.usedKeys.add(key);

        stack.push(container(ContainerType.Property));
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
    public void onString(String s) throws JsonException {
        ensureValue();
    }

    private void ensureValue() {
        // where is a value legal? in property, in array and as root in document
        Container top = stack.peek();
        if (top.type() != ContainerType.Property && top.type() != ContainerType.Array && top.type() != ContainerType.Document) {
            throw new IllegalStateException("Validation: Expected property, array or document on stack, but found " + top);
        }
        if (top.type() == ContainerType.Property) {
            stack.pop(); // Pop the Property state after its value is written
        }
    }

}
