package com.graphinout.foundation.json.util;

import com.graphinout.foundation.json.JsonReaderImpl;
import com.graphinout.foundation.json.writer.impl.ValidatingJsonWriter;
import io.github.classgraph.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

class ValidatingJsonWriterTest {

    @Test
    void test() {
        ValidatingJsonWriter w = new ValidatingJsonWriter();
        w.documentStart();
        w.objectStart();
        w.onKey("foo");
        w.onBoolean(true);
        // expect error IllegalStateException, via Google Truth
        Assertions.assertThrows(IllegalStateException.class, () -> w.onKey("foo"));

        w.objectEnd();
        w.documentEnd();
    }

    @ParameterizedTest(name = "{index}: {0}")
    @MethodSource("com.graphinout.foundation.TestFileProvider#jsonResources")
    @DisplayName("Test JSON-Validation - all files together")
    void test_json_Validate(String displayPath, Resource resource) throws Exception {
        JsonReaderImpl jsonReader = new JsonReaderImpl();
        ValidatingJsonWriter w = new ValidatingJsonWriter();
        String string = resource.getContentAsString();
        jsonReader.readStandardJsonString(string, w);
    }

}
