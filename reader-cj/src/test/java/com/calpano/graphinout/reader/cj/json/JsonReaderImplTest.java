package com.calpano.graphinout.reader.cj.json;

import com.calpano.graphinout.foundation.input.SingleInputSourceOfString;
import com.calpano.graphinout.foundation.json.impl.StringBuilderJsonEventSink;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.util.List;
import java.util.stream.Stream;

import static com.calpano.graphinout.foundation.input.SingleInputSourceOfString.inputSource;
import static org.junit.jupiter.api.Assertions.assertEquals;

class JsonReaderImplTest {

    static final List<SingleInputSourceOfString> inputs = List.of( //
            inputSource("number", "{\"foo\":42}"),//
            inputSource("string", "{\"foo\":\"bar\"}"),//
            inputSource("boolean-true", "{\"foo\":true}"),//
            inputSource("boolean-false", "{\"foo\":false}"),//
            inputSource("null", "{\"foo\":null}"),//
            inputSource("array-of-numbers", "{\"foo\":[1,2,3]}"),//
            inputSource("nested-object", "{\"foo\":{\"bar\":42}}"),//
            inputSource("empty-array", "[]"), //
            inputSource("empty-object", "{}"),//
            inputSource("mixed-array", "[1,\"two\",true,null,{},[]]"), //
            inputSource("two-properties", "{\"foo\":42,\"bar\":\"baz\"}"));

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
            inputSource("hex-number", "{\"foo\":0xFF}"),
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


    static Stream<SingleInputSourceOfString> inputs() {
        return inputs.stream();
    }

    static Stream<SingleInputSourceOfString> inputs5() {
        return inputsJson5.stream();
    }

    @ParameterizedTest
    @MethodSource("inputs")
    void test(SingleInputSourceOfString input) throws IOException {
        testInput(input);
    }

    @ParameterizedTest
    @MethodSource("inputs5")
    void testJson5(SingleInputSourceOfString input) throws IOException {
        testInput(input);
    }

    private void testInput(SingleInputSourceOfString input) throws IOException {
        JsonReaderImpl jsonReader = new JsonReaderImpl();
        StringBuilderJsonEventSink sink = new StringBuilderJsonEventSink();
        jsonReader.read(input, sink);

        String result = sink.json();
        assertEquals(input.content(), result);
    }

}
