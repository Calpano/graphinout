package com.calpano.graphinout.foundation.json.impl;

import com.calpano.graphinout.foundation.json.stream.JsonValueWriter;
import com.calpano.graphinout.foundation.json.JsonException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.BigIntegerNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Stack;
import java.util.function.Consumer;

public class BufferingJsonWriter implements JsonValueWriter {

    @FunctionalInterface
    interface ValueConsumer extends Consumer<JsonNode> {

        static ValueConsumer of(Consumer<JsonNode> consumer) {
            return consumer::accept;
        }

    }

    @FunctionalInterface
    interface ObjectPropertiesConsumer extends Consumer<String> {

        static ObjectPropertiesConsumer of(Consumer<String> consumer) {
            return consumer::accept;
        }

    }

    private final Stack<Object> stack = new Stack<>();
    private final StringBuilder stringBuffer = new StringBuilder();
    private final JsonNodeFactory nodeFactory = JsonNodeFactory.instance;
    private JsonNode root;

    @Override
    public void arrayEnd() throws JsonException {
        // pop the consumer
        pop(ValueConsumer.class);
    }

    @Override
    public void arrayStart() throws JsonException {
        // create new array
        ArrayNode arrayNode = new ArrayNode(nodeFactory);

        attachToTree(arrayNode);

        // prepare for members
        stack.push(ValueConsumer.of(arrayNode::add));
    }

    /** the result */
    public JsonNode jsonNode() {
        return root;
    }

    @Override
    public void objectEnd() throws JsonException {
        pop(ObjectPropertiesConsumer.class);
    }

    @Override
    public void objectStart() throws JsonException {
        // create new object
        ObjectNode objectNode = new ObjectNode(nodeFactory);

        attachToTree(objectNode);

        // prepare for properties
        stack.push(ObjectPropertiesConsumer.of(key -> {
            // prepare for value
            stack.push(ValueConsumer.of(value -> {
                objectNode.set(key, value);
                pop(ValueConsumer.class);
            }));
        }));
    }

    @Override
    public void onBigDecimal(BigDecimal bigDecimal) throws JsonException {
        attachToTree(BigIntegerNode.valueOf(bigDecimal.unscaledValue()));
    }

    @Override
    public void onBigInteger(BigInteger bigInteger) throws JsonException {
        attachToTree(BigIntegerNode.valueOf(bigInteger));
    }

    @Override
    public void onBoolean(boolean b) throws JsonException {
        attachToTree(nodeFactory.booleanNode(b));
    }

    @Override
    public void onDouble(double d) throws JsonException {
        attachToTree(nodeFactory.numberNode(d));
    }

    @Override
    public void onFloat(float f) throws JsonException {
        attachToTree(nodeFactory.numberNode(f));
    }

    @Override
    public void onInteger(int i) throws JsonException {
        attachToTree(nodeFactory.numberNode(i));
    }

    @Override
    public void onKey(String key) throws JsonException {
        // fetch PropertyConsumer
        ObjectPropertiesConsumer propertyConsumer = (ObjectPropertiesConsumer) stack.peek();
        propertyConsumer.accept(key);
    }

    @Override
    public void onLong(long l) throws JsonException {
        attachToTree(nodeFactory.numberNode(l));
    }

    @Override
    public void onNull() throws JsonException {
        attachToTree(nodeFactory.nullNode());
    }

    @Override
    public void onString(String s) throws JsonException {
        stringBuffer.append(s);
    }

    private void attachToTree(JsonNode jsonNode) {
        if (stack.isEmpty()) {
            root = jsonNode;
        } else {
            ValueConsumer consumer = (ValueConsumer) stack.peek();
            consumer.accept(jsonNode);
        }
    }

    private void pop(Class<?> expected) {
        Object o = stack.pop();
        assert o.getClass() == expected;
    }

}
