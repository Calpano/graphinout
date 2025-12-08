package com.graphinout.base.json;

import com.graphinout.base.input.SingleInputSourceOfString;
import com.graphinout.foundation.pure.json.writer.impl.StringBuilderJsonWriter;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

class JsonReaderImplTest {


    @ParameterizedTest
    @MethodSource("com.graphinout.testdata.TestFileProvider#jsonInputSources")
    void test(String name, String content) throws IOException {
        testInput(name, content);
    }

    private void testInput(String name, String content) throws IOException {
        JsonReaderImpl jsonReader = new JsonReaderImpl();
        StringBuilderJsonWriter sink = new StringBuilderJsonWriter();

        SingleInputSourceOfString input = SingleInputSourceOfString.of(name, content);
        jsonReader.read(input, sink);

        String result = sink.json();
        assertEquals(input.content(), result);
    }

}
