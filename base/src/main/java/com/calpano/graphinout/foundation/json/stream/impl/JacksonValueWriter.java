package com.calpano.graphinout.foundation.json.stream.impl;

import com.calpano.graphinout.foundation.json.JsonException;
import com.calpano.graphinout.foundation.json.stream.JsonValueWriter;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.ValueNode;

import javax.annotation.Nullable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class JacksonValueWriter implements JsonValueWriter {

    static JsonNodeFactory factory = JsonNodeFactory.instance;
    private final List<JsonNode> nodes = new ArrayList<>();
    private @Nullable String key;

    @Override
    public void arrayEnd() throws JsonException {
        pop(ArrayNode.class);
    }

    @Override
    public void arrayStart() throws JsonException {
        push(new ArrayNode(factory));
    }

    @Override
    public JsonValueWriter jsonValueWriter() {
        return new JacksonValueWriter();
    }

    @Override
    public void objectEnd() throws JsonException {
        pop(ObjectNode.class);
    }

    @Override
    public void objectStart() throws JsonException {
        push(new ObjectNode(factory));
    }


    @Override
    public void onBigDecimal(BigDecimal bigDecimal) throws JsonException {
        value(factory.numberNode(bigDecimal));
    }


    @Override
    public void onBigInteger(BigInteger bigInteger) throws JsonException {
        value(factory.numberNode(bigInteger));
    }


    @Override
    public void onBoolean(boolean b) throws JsonException {
        value(factory.booleanNode(b));
    }

    @Override
    public void onDouble(double d) throws JsonException {
        value(factory.numberNode(d));
    }


    @Override
    public void onFloat(float f) throws JsonException {
        value(factory.numberNode(f));
    }


    @Override
    public void onInteger(int i) throws JsonException {
        value(factory.numberNode(i));
    }


    @Override
    public void onKey(String key) throws JsonException {
        assert peek().isObject();
        this.key = key;
    }


    @Override
    public void onLong(long l) throws JsonException {
        value(factory.numberNode(l));
    }


    @Override
    public void onNull() throws JsonException {
        value(factory.nullNode());
    }

    @Override
    public void onString(String s) throws JsonException {
        value(factory.textNode(s));
    }

    public JsonNode result() {
        assert nodes.size() == 1;
        return nodes.getFirst();
    }

    protected JsonNode peek() {
        return nodes.getLast();
    }

    protected JsonNode pop(Class<?> clazz) {
        JsonNode node = pop();
        assert node.getClass().equals(clazz);
        return node;
    }

    protected JsonNode pop() {
        return nodes.removeLast();
    }

    protected void push(JsonNode node) {
        nodes.add(node);
    }

    protected void value(ValueNode value) {
        assert peek().isContainerNode();
        if (peek().isArray()) {
            ArrayNode array = (ArrayNode) peek();
            array.add(value);
        } else {
            ObjectNode object = (ObjectNode) peek();
            object.set(Objects.requireNonNull(key), value);
            key = null;
        }
    }

}
