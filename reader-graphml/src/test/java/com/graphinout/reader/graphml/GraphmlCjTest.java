package com.graphinout.reader.graphml;

import com.graphinout.base.cj.CjType;
import com.graphinout.base.cj.element.CjDataSchema;
import com.graphinout.base.cj.element.CjDocuments;
import com.graphinout.base.cj.element.ICjDocument;
import com.graphinout.reader.graphml.cj.CjGraphmlMapping;
import com.graphinout.reader.graphml.elements.GraphmlDataType;
import com.graphinout.foundation.TestFileProvider;
import com.graphinout.foundation.json.path.JsonTypeAnalysisTree;
import com.graphinout.reader.cj.ConnectedJsonReader;
import jdk.jfr.Description;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Map;

import static com.google.common.truth.Truth.assertThat;

public class GraphmlCjTest {

    @Test
    @Description("1 file")
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
