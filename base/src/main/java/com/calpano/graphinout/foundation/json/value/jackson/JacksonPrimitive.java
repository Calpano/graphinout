package com.calpano.graphinout.foundation.json.value.jackson;

import com.calpano.graphinout.foundation.json.JsonType;
import com.calpano.graphinout.foundation.json.value.IJsonFactory;
import com.calpano.graphinout.foundation.json.value.IJsonPrimitive;
import com.calpano.graphinout.foundation.json.value.IJsonValue;
import com.fasterxml.jackson.databind.node.ValueNode;

public class JacksonPrimitive implements IJsonPrimitive {

    private final ValueNode primitive;

    public JacksonPrimitive(ValueNode primitive) {this.primitive = primitive;}

    public static IJsonValue of(ValueNode valueNode) {
        return new JacksonPrimitive(valueNode);
    }

    @Override
    public Object base() {
        return primitive;
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
