package com.graphinout.reader.cj;

import com.graphinout.base.cj.CjAssert;
import com.graphinout.base.cj.CjType;
import com.graphinout.base.cj.element.CjDataSchema;
import com.graphinout.base.cj.element.CjDocuments;
import com.graphinout.base.cj.element.ICjData;
import com.graphinout.base.cj.element.ICjDocument;
import com.graphinout.base.cj.element.ICjHasData;
import com.graphinout.base.cj.stream.impl.Cj2JsonWriter;
import com.graphinout.base.cj.stream.impl.Json2CjWriter;
import com.graphinout.base.graphml.CjGraphmlMapping;
import com.graphinout.base.graphml.GraphmlDataType;
import com.graphinout.foundation.TestFileProvider;
import com.graphinout.foundation.input.SingleInputSourceOfString;
import com.graphinout.foundation.json.path.JsonTypeAnalysisTree;
import com.graphinout.foundation.json.stream.JsonWriter;
import com.graphinout.foundation.json.stream.impl.Json2StringWriter;
import com.graphinout.foundation.json.stream.impl.JsonReaderImpl;
import io.github.classgraph.Resource;
import jdk.jfr.Description;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.util.Map;
import java.util.Objects;

import static com.google.common.truth.Truth.assertThat;

public class CjTest {

    @ParameterizedTest(name = "{index}: {0}")
    @MethodSource("com.graphinout.foundation.TestFileProvider#cjResourcesCanonical")
    @Description("JSON->CjDoc->Analysis")
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


    @ParameterizedTest(name = "{index}: {0}")
    @MethodSource("com.graphinout.foundation.TestFileProvider#cjResourcesCanonical")
    @Description("Test JSON<->CJ<->JSON (all)")
    void test_Json_Cj_Json(String displayName, Resource resource) throws IOException {
        String json = resource.getContentAsString();

        Json2StringWriter json2StringWriter = new Json2StringWriter();
        Cj2JsonWriter cj2JsonWriter = new Cj2JsonWriter(json2StringWriter);
        JsonWriter json2cjWriter = Json2CjWriter.createWritingTo(cj2JsonWriter);

        JsonReaderImpl jsonReader = new JsonReaderImpl();
        SingleInputSourceOfString inputSource = SingleInputSourceOfString.of("test", json);
        jsonReader.read(inputSource, json2cjWriter);
        String resultJson = json2StringWriter.jsonString();

        CjAssert.xAssertThatIsSameCj(resultJson, json, null);
    }

}
