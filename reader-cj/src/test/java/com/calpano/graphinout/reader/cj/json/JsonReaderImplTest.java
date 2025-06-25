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


    static Stream<SingleInputSourceOfString> inputs() {
        return inputs.stream();
    }


    @ParameterizedTest
    @MethodSource("inputs")
    void test(SingleInputSourceOfString input) throws IOException {
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
