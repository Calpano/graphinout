package com.calpano.graphinout.reader.cj;

import com.calpano.graphinout.base.cj.CjType;
import com.calpano.graphinout.base.cj.element.CjDataSchema;
import com.calpano.graphinout.base.cj.element.CjDocuments;
import com.calpano.graphinout.base.cj.element.ICjData;
import com.calpano.graphinout.base.cj.element.ICjDocument;
import com.calpano.graphinout.base.cj.element.ICjHasData;
import com.calpano.graphinout.base.graphml.CjGraphmlMapping;
import com.calpano.graphinout.base.graphml.GraphmlDataType;
import com.calpano.graphinout.foundation.TestFileProvider;
import com.calpano.graphinout.foundation.json.path.JsonTypeAnalysisTree;
import io.github.classgraph.Resource;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.util.Map;
import java.util.Objects;

import static com.google.common.truth.Truth.assertThat;

public class CjTest {

    @ParameterizedTest(name = "{index}: {0}")
    @MethodSource("com.calpano.graphinout.foundation.TestFileProvider#cjResourcesCanonical")
    void testCjAnalysis(String displayName, Resource resource) throws IOException {
        String json = resource.getContentAsString();
        ICjDocument doc = ConnectedJsonReader.readToDocument(json);
        assertThat(doc).isNotNull();
        long datas = doc.allElements().filter(elem -> elem instanceof ICjHasData) //
                .map(elem -> (ICjHasData) elem)//
                .map(ICjHasData::data)//
                .filter(Objects::nonNull)//
                .map(ICjData::jsonValue)//
                .filter(Objects::nonNull)//
                .count();
        if (datas == 0) return;

        CjDataSchema schema = CjDocuments.calcEffectiveSchemaForData(doc);
        assertThat(schema.map()).isNotEmpty();
    }

    @Test
    void testDataSimple() throws IOException {
        TestFileProvider.TestResource tr = TestFileProvider.resourceByPath("json/cj/canonical/custom-data-simple.cj.json");
        String json = tr.resource().getContentAsString();
        ICjDocument doc = ConnectedJsonReader.readToDocument(json);
        assertThat(doc).isNotNull();
        CjDataSchema schema = CjDocuments.calcEffectiveSchemaForData(doc);
        assertThat(schema.map()).isNotEmpty();

        JsonTypeAnalysisTree edgeTree = schema.map().get(CjType.Edge);
        assertThat(edgeTree).isNotNull();
        Map<String, GraphmlDataType> edgeMap = CjGraphmlMapping.toGraphmlDataTypes(edgeTree);
        assertThat(edgeMap).containsEntry("carrier", GraphmlDataType.typeString);
        assertThat(edgeMap).containsEntry("distance_km", GraphmlDataType.typeInt);

        JsonTypeAnalysisTree nodeTree = schema.map().get(CjType.Node);
        assertThat(nodeTree).isNotNull();
        Map<String, GraphmlDataType> nodeMap = CjGraphmlMapping.toGraphmlDataTypes(nodeTree);
        assertThat(nodeMap).containsEntry("foo", GraphmlDataType.typeString);
        assertThat(nodeMap).containsEntry("sub", GraphmlDataType.typeString);
        // using typeInt would be better, but we have generic JSON Number in between
        assertThat(nodeMap).containsEntry("population", GraphmlDataType.typeInt);

//        schema.map().forEach((type, tree) -> {
//            Map<String, GraphmlDataType> map = CjGraphmlMapping.toGraphmlDataTypes(tree);
//            System.out.println("For "+type+" got map:");
//            map.forEach( (k,v) -> System.out.println("  "+k+" -> "+v));
//        });
    }

}
