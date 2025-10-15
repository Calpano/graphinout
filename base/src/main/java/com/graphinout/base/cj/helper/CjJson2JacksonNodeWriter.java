package com.graphinout.base.cj.helper;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.ValueNode;
import com.graphinout.foundation.json.JsonException;
import com.graphinout.foundation.json.stream.JsonWriter;

import javax.annotation.Nullable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Stack;

/**
 * Base class for collecting all JSON calls into a string. Impl uses {@link #jsonNode()} and {@link #reset()}.
 */
public class CjJson2JacksonNodeWriter implements JsonWriter {

    private final Stack<Object> stack = new Stack<>();
    private final JsonNodeFactory nf = JsonNodeFactory.instance;
    private JsonNode root = null;

    @Override
    public void arrayEnd() throws JsonException {
        pop(ArrayNode.class);
    }

    @Override
    public void arrayStart() throws JsonException {
        ArrayNode arrayNode = nf.arrayNode();
        attach(arrayNode);
        stack.push(arrayNode);
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
    public @Nullable JsonNode jsonNode() {
        return root;
    }

    @Override
    public void objectEnd() throws JsonException {
        pop(JsonNode.class);
    }

    @Override
    public void objectStart() throws JsonException {
        ObjectNode objectNode = nf.objectNode();
        attach(objectNode);
        stack.push(objectNode);
    }

    @Override
    public void onBigDecimal(BigDecimal bigDecimal) throws JsonException {
        ValueNode node = nf.numberNode(bigDecimal);
        attach(node);
    }

    @Override
    public void onBigInteger(BigInteger bigInteger) throws JsonException {
        ValueNode node = nf.numberNode(bigInteger);
        attach(node);
    }

    @Override
    public void onBoolean(boolean b) throws JsonException {
        ValueNode node = nf.booleanNode(b);
        attach(node);
    }

    @Override
    public void onDouble(double d) throws JsonException {
        ValueNode node = nf.numberNode(d);
        attach(node);
    }

    @Override
    public void onFloat(float f) throws JsonException {
        ValueNode node = nf.numberNode(f);
        attach(node);
    }

    @Override
    public void onInteger(int i) throws JsonException {
        ValueNode node = nf.numberNode(i);
        attach(node);
    }

    @Override
    public void onKey(String key) throws JsonException {
        assert stack.peek() instanceof ObjectNode;
        stack.push(key);
    }

    @Override
    public void onLong(long l) throws JsonException {
        ValueNode node = nf.numberNode(l);
        attach(node);
    }

    @Override
    public void onNull() throws JsonException {
        ValueNode node = nf.nullNode();
        attach(node);
    }

    @Override
    public void onString(String s) throws JsonException {
        ValueNode node = nf.textNode(s);
        attach(node);
    }

    public void reset() {
        root = null;
        stack.clear();
    }

    private void attach(JsonNode value) {
        if (root == null) {
            root = value;
        } else {
            Object o = stack.peek();
            if (o instanceof ArrayNode parent) {
                parent.add(value);
            } else {
                String key = pop(String.class);
                ObjectNode obj = (ObjectNode) stack.peek();
                obj.set(key, value);
            }
        }
    }

    private <T> T pop(Class<T> clazz) {
        Object o = stack.pop();
        if (!clazz.isInstance(o)) {
            throw new IllegalStateException("Unexpected type: " + o.getClass());
        }
        return clazz.cast(o);
    }

}
