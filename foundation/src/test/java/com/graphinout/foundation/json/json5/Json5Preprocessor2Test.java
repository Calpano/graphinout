package com.graphinout.foundation.json.json5;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.StreamReadFeature;
import com.fasterxml.jackson.core.json.JsonReadFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.graphinout.foundation.input.SingleInputSourceOfString;
import com.graphinout.foundation.json5.Json5Preprocessor;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;

class Json5Preprocessor2Test {

    @ParameterizedTest
    @MethodSource("com.graphinout.foundation.TestFileProvider#json5InputSources")
    void testJson5(String name, SingleInputSourceOfString input) throws IOException {
        testInput(input);
    }

    private void testInput(SingleInputSourceOfString input) throws IOException {
        String json = Json5Preprocessor.toJson(input.content());
        // Validate json is valid JSON syntax by parsing it
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.enable(StreamReadFeature.INCLUDE_SOURCE_IN_LOCATION.mappedFeature());
        // enable all json5 features
        objectMapper.enable(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES);
        objectMapper.enable(JsonParser.Feature.ALLOW_COMMENTS);
        objectMapper.enable(JsonParser.Feature.ALLOW_SINGLE_QUOTES);
        objectMapper.enable(JsonReadFeature.ALLOW_TRAILING_COMMA.mappedFeature());
        objectMapper.enable(JsonReadFeature.ALLOW_UNESCAPED_CONTROL_CHARS.mappedFeature());
        objectMapper.enable(JsonReadFeature.ALLOW_LEADING_DECIMAL_POINT_FOR_NUMBERS.mappedFeature());
        objectMapper.enable(JsonReadFeature.ALLOW_LEADING_PLUS_SIGN_FOR_NUMBERS.mappedFeature());
        objectMapper.enable(JsonReadFeature.ALLOW_LEADING_ZEROS_FOR_NUMBERS.mappedFeature());
        objectMapper.enable(JsonReadFeature.ALLOW_BACKSLASH_ESCAPING_ANY_CHARACTER.mappedFeature());
        objectMapper.enable(JsonReadFeature.ALLOW_TRAILING_DECIMAL_POINT_FOR_NUMBERS.mappedFeature());
        objectMapper.enable(JsonReadFeature.ALLOW_NON_NUMERIC_NUMBERS.mappedFeature());
        // TODO allow hex numbers, see https://github.com/FasterXML/jackson-core/issues/707

        objectMapper.readTree(json); // This will throw JsonProcessingException if JSON is invalid
    }

}
