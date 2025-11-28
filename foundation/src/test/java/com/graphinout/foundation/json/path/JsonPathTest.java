package com.graphinout.foundation.json.path;

import com.graphinout.foundation.json.JsonReaderImpl;
import com.graphinout.foundation.json.value.IJsonValue;
import io.github.classgraph.Resource;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;

import static com.google.common.truth.Truth.assertThat;

class JsonPathTest {


    @ParameterizedTest(name = "{index}: {0}")
    @MethodSource("com.graphinout.foundation.TestFileProvider#jsonResources")
    void testJsonAnalysis(String displayName, Resource resource) throws IOException {
        if(resource.getPath().endsWith("minimal.json")) {
            // this file contains no data
            return;
        }

        String json = resource.getContentAsString();
        IJsonValue value = JsonReaderImpl.readToJsonValue(json);
        JsonTypeAnalysisTree tree = new JsonTypeAnalysisTree();
        value.forEachLeaf(tree::index);

        assertThat(tree.rootSteps()).isNotEmpty();
    }


}
