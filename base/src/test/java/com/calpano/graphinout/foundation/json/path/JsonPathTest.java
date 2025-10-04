package com.calpano.graphinout.foundation.json.path;

import com.calpano.graphinout.foundation.json.stream.impl.JsonReaderImpl;
import com.calpano.graphinout.foundation.json.value.IJsonValue;
import io.github.classgraph.Resource;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;

class JsonPathTest {


    @ParameterizedTest(name = "{0}")
    @MethodSource("com.calpano.graphinout.foundation.TestFileProvider#jsonResources")
    void test(String displayName, Resource resource) throws IOException {
        String json = resource.getContentAsString();
        IJsonValue value = JsonReaderImpl.readToJsonValue(json);

        JsonTypeAnalysisTree tree = new JsonTypeAnalysisTree();

        value.forEachLeaf((path, prim) -> {
            System.out.println(path + " " + prim.toJsonString());
            tree.index(path,prim);
        });

        System.out.println(tree);
    }

}
