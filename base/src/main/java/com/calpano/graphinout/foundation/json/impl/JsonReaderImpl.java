package com.calpano.graphinout.foundation.json.impl;

import com.calpano.graphinout.foundation.input.InputSource;
import com.calpano.graphinout.foundation.input.SingleInputSource;
import com.calpano.graphinout.foundation.json.Json5Preprocessor;
import com.calpano.graphinout.foundation.json.JsonReader;
import com.calpano.graphinout.foundation.json.JsonWriter;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.StreamReadFeature;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;

public class JsonReaderImpl implements JsonReader {

    private static final JsonFactory JSON_FACTORY = new JsonFactory();
    final boolean useBigDecimals;

    static {
        JSON_FACTORY.enable(StreamReadFeature.INCLUDE_SOURCE_IN_LOCATION.mappedFeature());
    }

    public JsonReaderImpl() {
        this(true);
    }

    public JsonReaderImpl(boolean useBigDecimals) {this.useBigDecimals = useBigDecimals;}

    @Override
    public void read(InputSource inputSource, JsonWriter stream) throws IOException {
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
            stream.documentStart();
            JsonToken token = parser.nextToken();
            while (token != null) {
                parseJsonValue(parser, token, stream);
                token = parser.nextToken();
            }
        } catch (Throwable t) {
            log.error("Failed", t);
            throw t;
        } finally {
            stream.documentEnd();
        }
    }

    private void parseJsonValue(JsonParser parser, JsonToken token, JsonWriter stream) throws IOException {
        switch (token) {
            case FIELD_NAME:
                stream.onKey(parser.currentName());
                break;
            case VALUE_NULL:
                stream.onNull();
                break;
            case VALUE_STRING:
                stream.stringStart();
                // This is more efficient than parser.getValueAsString()
                // It avoids allocating a new String for the value by using the parser's internal buffer.
                char[] textChars = parser.getTextCharacters();
                int textOffset = parser.getTextOffset();
                int textLength = parser.getTextLength();
                String s = new String(textChars, textOffset, textLength);
                stream.stringCharacters(s);
                stream.stringEnd();
                break;
            case VALUE_NUMBER_INT:
                if (useBigDecimals) {
                    BigDecimal bigDecimalValue = parser.getDecimalValue();
                    stream.onBigDecimal(bigDecimalValue);
                    return;
                }

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
                if (useBigDecimals) {
                    BigDecimal bigDecimalValue = parser.getDecimalValue();
                    stream.onBigDecimal(bigDecimalValue);
                    return;
                }
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
                break;
            case END_ARRAY:
                stream.arrayEnd();
                break;
            case START_OBJECT:
                stream.objectStart();
                break;
            case END_OBJECT:
                stream.objectEnd();
                break;
            default:
                // For any other tokens, skip or handle as needed
                throw new UnsupportedOperationException("Unsupported token: " + token);
        }
    }

}
