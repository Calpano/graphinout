package com.calpano.graphinout.reader.cj.json;

import com.calpano.graphinout.foundation.input.InputSource;
import com.calpano.graphinout.foundation.input.SingleInputSource;
import com.calpano.graphinout.foundation.json.JsonEventStream;
import com.calpano.graphinout.foundation.json.JsonReader;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonToken;

import java.io.IOException;
import java.io.InputStream;

public class JsonReaderImpl implements JsonReader {

    @Override
    public void read(InputSource inputSource, JsonEventStream stream) throws IOException {
        if (inputSource.isMulti()) {
            throw new IllegalArgumentException("Cannot handle multi-sources");
        }
        SingleInputSource singleInputSource = (SingleInputSource) inputSource;
        try (InputStream inputStream = singleInputSource.inputStream()) {
            com.fasterxml.jackson.core.JsonParser parser = new JsonFactory().createParser(inputStream);

            while (parser.nextToken() != null) {
                JsonToken token = parser.currentToken();
                switch (token) {
                    case START_OBJECT:
                        stream.objectStart();
                        break;
                    case END_OBJECT:
                        stream.objectEnd();
                        break;
                    case START_ARRAY:
                        stream.arrayStart();
                        break;
                    case END_ARRAY:
                        stream.arrayEnd();
                        break;
                    case FIELD_NAME:
                        stream.onKey(parser.currentName());
                        break;
                    case VALUE_STRING:
                        stream.onString(parser.getValueAsString());
                        break;
                    case VALUE_NUMBER_INT:
                        // Jackson INT = has no decimals
                        switch (parser.getNumberType()) {
                            case BIG_INTEGER:
                                stream.onBigInteger(parser.getBigIntegerValue());
                                break;
                            case INT:
                                stream.onInteger(parser.getIntValue());
                                break;
                            case LONG:
                                stream.onLong(parser.getLongValue());
                                break;
                        }
                        break;
                    case VALUE_NUMBER_FLOAT:
                        // Jackson FLOAT = has decimals
                        switch (parser.getNumberType()) {
                            case BIG_DECIMAL:
                                stream.onBigDecimal(parser.getDecimalValue());
                                break;
                            case FLOAT:
                                stream.onFloat(parser.getFloatValue());
                                break;
                            case DOUBLE:
                                stream.onDouble(parser.getDoubleValue());
                                break;
                        }
                        break;
                    case VALUE_TRUE:
                        stream.onBoolean(true);
                        break;
                    case VALUE_FALSE:
                        stream.onBoolean(false);
                        break;
                    case VALUE_NULL:
                        stream.onNull();
                        break;
                    default:
                        // Ignore other tokens like NOT_AVAILABLE, etc.
                        break;
                }
            }
        }
    }

}
