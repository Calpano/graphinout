package com.calpano.graphinout.reader.cj.json;

import com.calpano.graphinout.foundation.input.InputSource;
import com.calpano.graphinout.foundation.input.SingleInputSource;
import com.calpano.graphinout.foundation.json.JsonEventStream;
import com.calpano.graphinout.foundation.json.JsonReader;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;

public class JsonReaderImpl implements JsonReader {

    private static final JsonFactory JSON_FACTORY = new JsonFactory();

    @Override
    public void read(InputSource inputSource, JsonEventStream stream) throws IOException {
        if (inputSource.isMulti()) {
            throw new IllegalArgumentException("Cannot handle multi-sources");
        }
        SingleInputSource singleInputSource = (SingleInputSource) inputSource;

        // Read the entire content as string for JSON5 preprocessing
        String content = IOUtils.toString(singleInputSource.inputStream(), StandardCharsets.UTF_8);

        if (content.isEmpty()) {
            return;
        }

        // Preprocess JSON5 content to make it compatible with standard JSON
        String preprocessedContent = Json5Preprocessor.toJson(content);

        // Create Jackson parser for JSON parsing
        try (JsonParser parser = JSON_FACTORY.createParser(preprocessedContent)) {
            // Parse the JSON content
            JsonToken token = parser.nextToken();
            if (token != null) {
                parseJsonValue(parser, token, stream);
            }
        }
    }

    private void parseJsonValue(JsonParser parser, JsonToken token, JsonEventStream stream) throws IOException {
        switch (token) {
            case VALUE_NULL:
                stream.onNull();
                break;
            case VALUE_STRING:
                stream.onString(parser.getValueAsString());
                break;
            case VALUE_NUMBER_INT:
                // Handle different integer types based on the number size
                try {
                    int intValue = parser.getIntValue();
                    stream.onInteger(intValue);
                } catch (Exception e) {
                    try {
                        long longValue = parser.getLongValue();
                        stream.onLong(longValue);
                    } catch (Exception e2) {
                        BigInteger bigIntValue = parser.getBigIntegerValue();
                        stream.onBigInteger(bigIntValue);
                    }
                }
                break;
            case VALUE_NUMBER_FLOAT:
                // Handle different float types
                try {
                    double doubleValue = parser.getDoubleValue();
                    // Check if it's a special value (Infinity, -Infinity, NaN)
                    if (Double.isInfinite(doubleValue) || Double.isNaN(doubleValue)) {
                        stream.onDouble(doubleValue);
                    } else {
                        // Try to determine if it should be float or double based on precision
                        BigDecimal bigDecimalValue = parser.getDecimalValue();
                        if (bigDecimalValue.scale() <= 7 && bigDecimalValue.precision() <= 7) {
                            stream.onFloat((float) doubleValue);
                        } else {
                            stream.onDouble(doubleValue);
                        }
                    }
                } catch (Exception e) {
                    BigDecimal bigDecimalValue = parser.getDecimalValue();
                    stream.onBigDecimal(bigDecimalValue);
                }
                break;
            case VALUE_TRUE:
            case VALUE_FALSE:
                stream.onBoolean(parser.getBooleanValue());
                break;
            case START_ARRAY:
                stream.arrayStart();
                while ((token = parser.nextToken()) != JsonToken.END_ARRAY) {
                    parseJsonValue(parser, token, stream);
                }
                stream.arrayEnd();
                break;
            case START_OBJECT:
                stream.objectStart();
                while ((token = parser.nextToken()) != JsonToken.END_OBJECT) {
                    if (token == JsonToken.FIELD_NAME) {
                        String fieldName = parser.getCurrentName();
                        stream.onKey(fieldName);
                        token = parser.nextToken(); // Move to the value
                        parseJsonValue(parser, token, stream);
                    }
                }
                stream.objectEnd();
                break;
            default:
                // For any other tokens, skip or handle as needed
                break;
        }
    }

}
