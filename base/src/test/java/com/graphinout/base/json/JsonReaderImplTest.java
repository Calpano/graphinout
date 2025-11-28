package com.graphinout.base.json;

import com.graphinout.base.input.SingleInputSourceOfString;
import com.graphinout.foundation.json.writer.impl.StringBuilderJsonWriter;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

class JsonReaderImplTest {


    @ParameterizedTest
    @MethodSource("com.graphinout.base.TestFileProvider#jsonInputSources")
    void test(SingleInputSourceOfString input) throws IOException {
        testInput(input);
    }

    private void testInput(SingleInputSourceOfString input) throws IOException {
        JsonReaderImpl jsonReader = new JsonReaderImpl();
        StringBuilderJsonWriter sink = new StringBuilderJsonWriter();
        jsonReader.read(input, sink);

        String result = sink.json();
        assertEquals(input.content(), result);
    }

}
