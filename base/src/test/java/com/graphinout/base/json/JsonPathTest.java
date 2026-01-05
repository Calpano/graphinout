package com.graphinout.base.json;

import com.graphinout.foundation.pure.json.document.IJsonValue;
import com.graphinout.foundation.pure.json.path.JsonTypeAnalysisTree;
import io.github.classgraph.Resource;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;

class JsonPathTest {


    @ParameterizedTest(name = "{index}: {0}")
    @MethodSource("com.graphinout.testdata.TestFileProvider#jsonResources")
    void testJsonAnalysis(String displayName, Resource resource) throws IOException {
        if (
            // this file contains no data
                resource.getPath().endsWith("minimal.json") ||
                        // duplicate JSON keys
                        resource.getPath().endsWith("nasty05.json")) {
            return;
        }

        String json = resource.getContentAsString();
        IJsonValue value = JsonReaderImpl.readToJsonValue(json);
        JsonTypeAnalysisTree tree = new JsonTypeAnalysisTree();
        value.forEachLeaf(tree::index);

//        assertThat(tree.rootSteps()).isNotEmpty();
    }


}
