package com.graphinout.foundation.json.value.jackson;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.ValueNode;
import com.graphinout.foundation.json.value.IJsonValue;
import org.checkerframework.checker.nullness.qual.NonNull;

import javax.annotation.Nullable;

public class JacksonValues {

    public static JsonNode jacksonValue(@NonNull IJsonValue jsonValue) {
        return (JsonNode) jsonValue.base();
    }

    /**
     * @param jsonNode null is null
     */
    public static @Nullable IJsonValue ofNullable(@Nullable JsonNode jsonNode) {
        if (jsonNode == null) {
            return null;
        }
        return ofValue(jsonNode);
    }

    /**
     * @param jsonNode null is JsonNull
     */
    public static IJsonValue ofValue(@Nullable JsonNode jsonNode) {
        if (jsonNode == null) {
            return JacksonFactory.INSTANCE.createNull();
        }
        if (jsonNode.isArray()) {
            return JacksonArrayMutable.of((ArrayNode) jsonNode);
        } else if (jsonNode.isObject()) {
            return JacksonObjectMutable.of((ObjectNode) jsonNode);
        }
        assert jsonNode.isValueNode();
        return JacksonPrimitive.of((ValueNode) jsonNode);
    }

}
