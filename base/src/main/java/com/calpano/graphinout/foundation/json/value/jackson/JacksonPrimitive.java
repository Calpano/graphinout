package com.calpano.graphinout.foundation.json.value.jackson;

import com.calpano.graphinout.foundation.json.JsonType;
import com.calpano.graphinout.foundation.json.value.IJsonFactory;
import com.calpano.graphinout.foundation.json.value.IJsonPrimitive;
import com.fasterxml.jackson.databind.node.ValueNode;

public class JacksonPrimitive implements IJsonPrimitive {

    private final ValueNode primitive;

    public JacksonPrimitive(ValueNode primitive) {this.primitive = primitive;}

    public static IJsonPrimitive of(ValueNode valueNode) {
        return new JacksonPrimitive(valueNode);
    }

    @Override
    public Object base() {
        return primitive;
    }

    @SuppressWarnings("unchecked")
    public <T> T castTo(Class<T> clazz) {
        if (clazz.equals(String.class)) {
            return (T) primitive.asText();
        } else if (clazz.equals(Boolean.class)) {
            return (T) Boolean.valueOf(primitive.asBoolean());
        } else if (clazz.equals(Number.class)) {
            return (T) primitive.numberValue();
        } else if (clazz.equals(ValueNode.class)) {
            return (T) primitive;
        }
        throw new IllegalArgumentException("Unsupported type: " + clazz.getName());
    }

    @Override
    public IJsonFactory factory() {
        return JacksonFactory.INSTANCE;
    }

    public JsonType jsonType() {
        return switch (primitive.getNodeType()) {
            case NULL -> JsonType.Null;
            case BOOLEAN -> JsonType.Boolean;
            case NUMBER -> JsonType.Number;
            case STRING -> JsonType.String;
            default -> throw new AssertionError("Unknown node type: " + primitive.getNodeType());
        };
    }

}
