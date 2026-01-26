package com.graphinout.reader.cj;

import com.graphinout.base.cj.CjAssert;
import com.graphinout.base.cj.document.CjDataSchema;
import com.graphinout.base.cj.document.CjDocuments;
import com.graphinout.base.cj.document.ICjCoreElement;
import com.graphinout.base.cj.document.ICjData;
import com.graphinout.base.cj.document.ICjDocument;
import com.graphinout.base.cj.document.ICjEdge;
import com.graphinout.base.cj.document.ICjGraph;
import com.graphinout.base.cj.document.ICjHasData;
import com.graphinout.base.cj.document.ICjHasId;
import com.graphinout.base.cj.document.ICjHasUri;
import com.graphinout.base.cj.writer.Cj2JsonWriter;
import com.graphinout.base.cj.writer.Json2CjWriter;
import com.graphinout.base.input.SingleInputSourceOfString;
import com.graphinout.base.json.JsonReaderImpl;
import com.graphinout.foundation.pure.json.writer.JsonWriter;
import com.graphinout.foundation.pure.json.writer.impl.Json2StringWriter;
import com.graphinout.testdata.TestFileUtil;
import io.github.classgraph.Resource;
import jdk.jfr.Description;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.util.Objects;

import static com.google.common.truth.Truth.assertThat;

public class CjTest {

    @Test
    void testBaseUri() throws IOException {
        String resource = "json/cj_7_0_0/baseuris.cj.json";
        Resource res = TestFileUtil.resource(resource);
        assertThat(res).isNotNull();

        String json = res.getContentAsString();
        ICjDocument doc = ConnectedJsonReader.readToDocument(json);

        assertThat(doc.edgesAll().map(ICjHasId::id)).containsExactly("edge-1");
        assertThat(doc.findEdgeById("doi:doc#edge-1")).isNull();
        assertThat(doc.findEdgeById("edge-1")).isNull();
        ICjEdge edge1 = doc.findEdgeById("doi:graph-1#edge-1");
        assertThat(edge1).isNotNull();
        ICjGraph graph1 = edge1.parent();
        assertThat(graph1).isNotNull();
        assertThat(edge1.resolveNodeById("aaa")).isNotNull();
        assertThat(edge1.resolveNodeById("aaa").uri()).isEqualTo("doi:graph-1#aaa");
        assertThat(edge1.resolveNodeById("bbb").uri()).isEqualTo("doi:graph-1#bbb");

        assertThat(doc.nodesAll().map(ICjHasUri::uri)).containsExactly( //
                "doi:graph-1#aaa", // (1)==(3)
                "doi:nested-graph-in-node#bbb", // (2)
                "doi:nested-graph-in-edge#aaa" // (5)
        );
        assertThat(doc.nodesAll().map(ICjCoreElement::unstableId)).containsExactly( //
                "aaa", // (1)==(3)
                "bbb", // (2)
                "aaa" // (5)
        );
        assertThat(doc.edgesAll().map(ICjHasId::id)).containsExactly("edge-1");
        assertThat(edge1.nodesResolved().map(ICjHasUri::uri)).containsExactly("doi:graph-1#aaa", "doi:graph-1#bbb");
        assertThat(doc.nodesAllIncludingImplied().map(ICjHasUri::uri)).containsExactly( //
                "doi:graph-1#aaa", // (1)==(3)
                "doi:nested-graph-in-node#bbb", // (2)
                "doi:graph-1#bbb", // (4)
                "doi:nested-graph-in-edge#aaa" // (5)
        );
    }

    @ParameterizedTest(name = "{index}: {0}")
    @MethodSource("com.graphinout.testdata.TestFileProvider#cjResourcesCanonical")
    @Description("JSON->CjDoc->Analysis")
    void testCjAnalysis(String displayName, Resource resource) throws IOException {
        String json = resource.getContentAsString();
        ICjDocument doc = ConnectedJsonReader.readToDocument(json);
        assertThat(doc).isNotNull();
        long datas = doc.allElements().filter(elem -> elem instanceof ICjHasData) //
                .map(elem -> (ICjHasData) elem)//
                .map(ICjHasData::data)//
                .map(ICjData::jsonValue)//
                .filter(Objects::nonNull)//
                .count();
        if (datas == 0) return;

        CjDataSchema schema = CjDocuments.calcEffectiveSchemaForData(doc);
        assertThat(schema.map()).isNotEmpty();
    }

    @ParameterizedTest(name = "{index}: {0}")
    @MethodSource("com.graphinout.testdata.TestFileProvider#cjResourcesCanonical")
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
