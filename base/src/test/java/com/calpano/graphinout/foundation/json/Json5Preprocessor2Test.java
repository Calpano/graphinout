package com.calpano.graphinout.foundation.json;

import com.calpano.graphinout.foundation.input.SingleInputSourceOfString;
import com.calpano.graphinout.foundation.json.json5.Json5Preprocessor;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.StreamReadFeature;
import com.fasterxml.jackson.core.json.JsonReadFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.util.List;
import java.util.stream.Stream;

import static com.calpano.graphinout.foundation.input.SingleInputSourceOfString.inputSource;

class Json5Preprocessor2Test {

    /** Testing JSON 5 extensions */
    static final List<SingleInputSourceOfString> inputsJson5 = List.of(
            // == Objects
            // test: Object keys may be an ECMAScript 5.1 IdentifierName.
            inputSource("object-identifier-name", "{foo:42}"),
            // test: Objects may have a single trailing comma.
            inputSource("object-trailing-comma", "{\"foo\":42,}"),
            // == Arrays
            // test: Arrays may have a single trailing comma.
            inputSource("array-trailing-comma", "[1,2,3,]"),
            // == Strings
            // test: Strings may be single quoted.
            inputSource("single-quoted-string", "{'foo':'bar'}"),
            // test: Strings may span multiple lines by escaping new line characters.
            inputSource("multiline-string", "{\"foo\":\"line1\\\nline2\"}"),
            // test: Strings may include character escapes.
            inputSource("escaped-chars", "{\"foo\":\"\\u0041\\t\\r\\n\"}"),
            // == Numbers
            // test: Numbers may be hexadecimal.
            // TODO not yet suported:
            // inputSource("hex-number", "{\"foo\":0xFF}"),
            // test: Numbers may have a leading or trailing decimal point.
            inputSource("decimal-point", "{\"foo\":.5,\"bar\":5.}"),
            // test: Numbers may be IEEE 754 positive infinity, negative infinity, and NaN.
            inputSource("special-numbers", "{\"foo\":Infinity,\"bar\":-Infinity,\"baz\":NaN}"),
            // test: Numbers may begin with an explicit plus sign.
            inputSource("plus-sign", "{\"foo\":+42}"),
            // ==  Comments
            // test: Single and multi-line comments are allowed.
            inputSource("comments", "{\"foo\":42} // comment\n/* multi\nline */"),
            // == White Space
            // test: Additional white space characters are allowed.
            inputSource("whitespace", "{\t\"foo\"\t:\t42\t}\n"));

    static Stream<Arguments> inputs5() {
        return inputsJson5.stream().map(is->Arguments.of(is.name(),is));
    }

    @ParameterizedTest
    @MethodSource("inputs5")
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
