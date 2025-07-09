package com.calpano.graphinout.base.graphml;

import com.calpano.graphinout.foundation.json.JsonElementWriter;
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

public class BufferingJsonWriter2 implements JsonElementWriter {

    @FunctionalInterface
    interface ValueConsumer extends Consumer<JsonNode> {

        static ValueConsumer of(Consumer<JsonNode> consumer) {
            return consumer::accept;
        }

    }

    @FunctionalInterface
    interface PropertyConsumer extends Consumer<String> {

        static PropertyConsumer of(Consumer<String> consumer) {
            return consumer::accept;
        }

    }

    private final Stack<Object> stack = new Stack<>();
    private final StringBuilder stringBuffer = new StringBuilder();
    private final JsonNodeFactory nf = JsonNodeFactory.instance;
    private JsonNode root;

    @Override
    public void arrayEnd() throws JsonException {
        // pop the consumer
        stack.pop();
    }

    @Override
    public void arrayStart() throws JsonException {
        // create new array
        ArrayNode arrayNode = new ArrayNode(nf);

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
        pop(PropertyConsumer.class);
    }

    @Override
    public void objectStart() throws JsonException {
        // create new object
        ObjectNode objectNode = new ObjectNode(nf);

        attachToTree(objectNode);

        // prepare for properties
        stack.push(PropertyConsumer.of(key -> {
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
        attachToTree(nf.booleanNode(b));
    }

    @Override
    public void onDouble(double d) throws JsonException {
        attachToTree(nf.numberNode(d));
    }

    @Override
    public void onFloat(float f) throws JsonException {
        attachToTree(nf.numberNode(f));
    }

    @Override
    public void onInteger(int i) throws JsonException {
        attachToTree(nf.numberNode(i));
    }

    @Override
    public void onKey(String key) throws JsonException {
        // fetch PropertyConsumer
        PropertyConsumer propertyConsumer = (PropertyConsumer) stack.peek();
        propertyConsumer.accept(key);
    }

    @Override
    public void onLong(long l) throws JsonException {
        attachToTree(nf.numberNode(l));
    }

    @Override
    public void onNull() throws JsonException {
        attachToTree(nf.nullNode());
    }

    @Override
    public void stringCharacters(String s) throws JsonException {
        stringBuffer.append(s);
    }

    @Override
    public void stringEnd() throws JsonException {
        String s = stringBuffer.toString();
        attachToTree(nf.textNode(s));
    }

    @Override
    public void stringStart() throws JsonException {
        stringBuffer.setLength(0);
    }

    @Override
    public void whitespaceCharacters(String s) throws JsonException {
        // whitespace is ignored
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
