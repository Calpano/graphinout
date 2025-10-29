package com.graphinout.foundation.json.value.jackson;

import com.fasterxml.jackson.databind.JsonNode;
import com.graphinout.foundation.json.writer.JsonWriter;

public class Jackson2JsonWriter {

    public static void write(JsonNode jsonNode, JsonWriter jsonWriter) {
        switch (jsonNode.getNodeType()) {
            case BOOLEAN -> jsonWriter.onBoolean(jsonNode.asBoolean());
            case STRING -> jsonWriter.onString(jsonNode.asText());
            case NUMBER -> {
                // depends on number
                if (jsonNode.isBigDecimal()) {
                    jsonWriter.onBigDecimal(jsonNode.decimalValue());
                } else if (jsonNode.isBigInteger()) {
                    jsonWriter.onBigInteger(jsonNode.bigIntegerValue());
                } else if (jsonNode.isDouble()) {
                    jsonWriter.onDouble(jsonNode.asDouble());
                } else if (jsonNode.isFloat()) {
                    jsonWriter.onFloat(jsonNode.floatValue());
                } else if (jsonNode.isLong()) {
                    jsonWriter.onLong(jsonNode.asLong());
                } else if (jsonNode.isInt()) {
                    jsonWriter.onInteger(jsonNode.asInt());
                } else {
                    throw new IllegalStateException("Unexpected value: " + jsonNode.getNodeType());
                }
            }
            case NULL -> jsonWriter.onNull();
            case ARRAY -> {
                jsonWriter.arrayStart();
                for (JsonNode n : jsonNode) {
                    write(n, jsonWriter);
                }
                jsonWriter.arrayEnd();
            }
            case OBJECT -> {
                jsonWriter.objectStart();
                // newer API: .properties().forEach(
                jsonNode.fields().forEachRemaining(e -> {
                    jsonWriter.onKey(e.getKey());
                    write(e.getValue(), jsonWriter);
                });
                jsonWriter.objectEnd();
            }
            case BINARY, POJO, MISSING ->
                    throw new IllegalStateException("Unexpected value: " + jsonNode.getNodeType());
        }
    }

}
