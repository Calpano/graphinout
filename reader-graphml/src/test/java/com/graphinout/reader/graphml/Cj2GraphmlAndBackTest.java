package com.graphinout.reader.graphml;


import com.graphinout.base.cj.CjAssert;
import com.graphinout.base.cj.document.CjDocuments;
import com.graphinout.base.cj.document.ICjDocument;
import com.graphinout.base.cj.document.impl.CjDocumentElement;
import com.graphinout.base.cj.writer.Cj2JsonWriter;
import com.graphinout.base.cj.writer.CjWriter2CjDocumentWriter;
import com.graphinout.base.cj.writer.ICjWriter;
import com.graphinout.base.cj.writer.Json2CjWriter;
import com.graphinout.base.input.SingleInputSourceOfString;
import com.graphinout.base.json.JsonReaderImpl;
import com.graphinout.foundation.pure.json.writer.JsonWriter;
import com.graphinout.foundation.pure.json.writer.impl.Json2StringWriter;
import com.graphinout.foundation.pure.json.writer.impl.StringBuilderJsonWriter;
import com.graphinout.foundation.pure.xml.writer.Xml2StringWriter;
import com.graphinout.reader.graphml.cj.CjDocument2Graphml;
import com.graphinout.reader.graphml.cj.Graphml2CjDocument;
import com.graphinout.reader.graphml.cj.Graphml2CjWriter;
import com.graphinout.reader.graphml.validation.ValidatingGraphMlWriter;
import com.graphinout.testdata.TestFileUtil;
import io.github.classgraph.Resource;
import jdk.jfr.Description;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.slf4j.Logger;

import java.io.IOException;

import static com.google.common.truth.Truth.assertThat;
import static com.graphinout.base.TestFileUtil2.inputSource;
import static org.slf4j.LoggerFactory.getLogger;


@DisplayName("CJ<->Graphml")
public class Cj2GraphmlAndBackTest {

    private static final Logger log = getLogger(Cj2GraphmlAndBackTest.class);
    private static final String TEST_ID = "Cj2Gml2Cj";

    @Test
    void testNestedGraphs() throws IOException {
        Resource resource = TestFileUtil.resource("json/cj_7_0_0/nested-graphs.cj.json");
        assertThat(resource).isNotNull();
        String jsonIn = resource.getContentAsString();
        SingleInputSourceOfString inputSource = SingleInputSourceOfString.of("test", jsonIn);

        // JSON -> CJ doc
        CjWriter2CjDocumentWriter cj2ElementsWriter = new CjWriter2CjDocumentWriter();
        JsonWriter jsonWriter_in = Json2CjWriter.createWritingTo(cj2ElementsWriter);
        JsonReaderImpl jsonReader = new JsonReaderImpl();
        jsonReader.read(inputSource, jsonWriter_in);
        ICjDocument cjDoc = cj2ElementsWriter.resultDoc();
        if (cjDoc == null) {
            cjDoc = new CjDocumentElement();
        }

        // FIXME debug
        // for debug: write GraphML/XML, too
        {
            Xml2StringWriter xmlWriter = new Xml2StringWriter();
            Graphml2XmlWriter graphml2XmlWriter = new Graphml2XmlWriter(xmlWriter);
            CjDocument2Graphml cjDocument2GraphmlXml = new CjDocument2Graphml(graphml2XmlWriter);
            cjDocument2GraphmlXml.writeDocumentToGraphml(cjDoc);
            log.info("GraphML/XML:\n---------------\n{}\n---------------", xmlWriter.resultString());
        }

        // CJ doc -> GraphML -> CJ
        Json2StringWriter json2StringWriter = new Json2StringWriter();
        Cj2JsonWriter cj2JsonWriter = new Cj2JsonWriter(json2StringWriter);
        Graphml2CjDocument graphml2CjWriter = new Graphml2CjWriter(cj2JsonWriter);
        CjDocument2Graphml cjDocument2GraphmlCj = new CjDocument2Graphml(graphml2CjWriter);
        cjDocument2GraphmlCj.writeDocumentToGraphml(cjDoc);
        String jsonOut = json2StringWriter.jsonString();
        CjAssert.verifySameCjOrRecord(resource, "Cj2Gml2Cj", jsonOut, jsonIn, null);
    }

    /**
     * Issue #137: an edge declared in a subgraph but referencing a node from an ancestor graph must be written into the
     * lowest common ancestor graph, otherwise the GraphML is invalid (the edge would reference a node not visible in or
     * below its graph).
     */
    @Test
    @DisplayName("#137 edge hoisted to lowest common ancestor graph")
    void testEdgeHoistedToCommonAncestorGraph() throws IOException {
        // 'trade_transatlantic' is declared inside subgraph 'europe' but connects germany (in europe) with usa (in the
        // parent graph 'world'). GraphML requires it to be declared in 'world'.
        String jsonIn = """
                { "$schema": "https://j-s-o-n.org/schema/cj-7.0.0.json",
                  "graphs": [ { "id": "world",
                    "nodes": [ { "id": "canada" }, { "id": "usa" } ],
                    "edges": [ { "id": "trade_na", "endpoints": [ { "node": "canada" }, { "node": "usa" } ] } ],
                    "graphs": [ { "id": "europe",
                      "nodes": [ { "id": "france" }, { "id": "germany" } ],
                      "edges": [
                        { "id": "trade_eu", "endpoints": [ { "node": "france" }, { "node": "germany" } ] },
                        { "id": "trade_transatlantic", "endpoints": [ { "node": "germany" }, { "node": "usa" } ] }
                      ] } ] } ] }
                """;
        ICjDocument cjDoc = CjDocuments.parseCjJsonString("issue137", jsonIn);

        Xml2StringWriter xmlWriter = new Xml2StringWriter();
        Graphml2XmlWriter graphml2XmlWriter = new Graphml2XmlWriter(xmlWriter);
        new CjDocument2Graphml(graphml2XmlWriter).writeDocumentToGraphml(cjDoc);
        String xml = xmlWriter.resultString();
        log.info("GraphML/XML:\n---------------\n{}\n---------------", xml);

        int worldGraph = xml.indexOf("\"world\"");
        int transatlanticEdge = xml.indexOf("trade_transatlantic");
        int europeGraph = xml.indexOf("\"europe\"");
        assertThat(worldGraph).isGreaterThan(-1);
        assertThat(transatlanticEdge).isGreaterThan(-1);
        assertThat(europeGraph).isGreaterThan(-1);
        // the edge must be emitted after 'world' opens but before the 'europe' subgraph opens,
        // i.e. as a direct child of 'world' rather than nested inside 'europe'
        assertThat(transatlanticEdge).isGreaterThan(worldGraph);
        assertThat(transatlanticEdge).isLessThan(europeGraph);
    }

    @ParameterizedTest(name = "{index}: {0}")
    @MethodSource("com.graphinout.testdata.TestFileProvider#cjResourcesCanonical")
    @Description("Test JSON->CJ->Graphml->CjStream->CJ->JSON (all)")
    void test_Json_Cj_Graphml_CjStream_Cj_Json(String displayName, Resource resource) throws IOException {
        // JSON -> CJ doc
        String jsonInput = resource.getContentAsString();
        ICjDocument cjDoc = CjDocuments.parseCjJsonString(displayName, jsonInput);

        // CJ doc -> GraphML -> CJ -> JSON
        Json2StringWriter json2StringWriter = new Json2StringWriter();
        Cj2JsonWriter cj2JsonWriter = new Cj2JsonWriter(json2StringWriter);


        Graphml2CjDocument graphml2CjWriter = new Graphml2CjWriter(cj2JsonWriter);
        CjDocument2Graphml.writeToGraphml(cjDoc, graphml2CjWriter);
        String jsonOut = json2StringWriter.jsonString();

        CjAssert.verifySameCjOrRecord(resource, "Cj2Gml2Cj", jsonOut, jsonInput, () -> {
            log.info("----\nInput CJ:\n" + jsonInput);

            Xml2StringWriter xmlWriter = new Xml2StringWriter();
            Graphml2XmlWriter graphml2XmlWriter = new Graphml2XmlWriter(xmlWriter);
            try {
                CjDocument2Graphml.writeToGraphml(cjDoc, graphml2XmlWriter);
                log.info("----\nGraphML:\n" + xmlWriter.resultString());
            } catch (IOException e) {
                log.info("Failed to render intermediate GraphML");
                throw new RuntimeException(e);
            }
            log.info("----\nOutput CJ:\n" + jsonOut);
            CjAssert.xAssertThatIsSameCj(jsonOut, jsonInput, () -> {});
        });
    }

    @ParameterizedTest(name = "{index}: {0}")
    @MethodSource("com.graphinout.testdata.TestFileProvider#cjResourcesCanonical")
    @DisplayName("Run JSON->CJ->Graphml - all files together")
    void test_json_cj_graphml_CanonicalCjFiles(String displayPath, Resource resource) throws Exception {
        String json_in = resource.getContentAsString();
        SingleInputSourceOfString inputSource = inputSource(resource);
        CjWriter2CjDocumentWriter cj2ElementsWriter = new CjWriter2CjDocumentWriter();
        JsonWriter jsonWriter_in = Json2CjWriter.createWritingTo(cj2ElementsWriter);
        JsonReaderImpl jsonReader = new JsonReaderImpl();
        jsonReader.read(inputSource, jsonWriter_in);
        ICjDocument cjDoc = cj2ElementsWriter.resultDoc();

        // CJ -> GraphML
        ValidatingGraphMlWriter graphMlWriter = new ValidatingGraphMlWriter();
        CjDocument2Graphml cjDocument2Graphml = new CjDocument2Graphml(graphMlWriter);
        cjDocument2Graphml.writeDocumentToGraphml(cjDoc);
    }

    @ParameterizedTest(name = "{index}: {0}")
    @MethodSource("com.graphinout.testdata.TestFileProvider#cjResourcesCanonical")
    @DisplayName("Test JSON<->CJ<->Graphml (all)")
    void test_json_cj_graphml_cj_json_CanonicalCjFiles(String displayPath, Resource resource) throws Exception {
        // JSON
        String json_in = resource.getContentAsString();
        SingleInputSourceOfString inputSource = inputSource(resource);

        // JSON -> CJ
        CjWriter2CjDocumentWriter cj2ElementsWriter = new CjWriter2CjDocumentWriter();
        JsonWriter jsonWriter_in = Json2CjWriter.createWritingTo(cj2ElementsWriter);
        JsonReaderImpl.read_(inputSource, jsonWriter_in);
        ICjDocument cjDoc = cj2ElementsWriter.resultDoc();

        // OUTPUT pipeline
        // CJ -> GraphML -> CJ2
        Graphml2CjDocument graphml2cjDocument = new Graphml2CjDocument();

        // TODO remove
        Xml2StringWriter xmlWriter = new Xml2StringWriter();
        Graphml2XmlWriter graphml2XmlWriter = new Graphml2XmlWriter(xmlWriter);
        DelegatingGraphmlWriter graphmlWriter = new DelegatingGraphmlWriter(graphml2XmlWriter, graphml2cjDocument);

        CjDocument2Graphml.writeToGraphml(cjDoc, graphmlWriter);
        String xml = xmlWriter.resultString();
        System.out.println(xml);
        System.out.flush();
        CjDocumentElement cjDoc2 = graphml2cjDocument.resultDoc();

        // CJ2 --> JSON2
        StringBuilderJsonWriter jsonWriter_out = new StringBuilderJsonWriter();
        ICjWriter cjWriter_out = new Cj2JsonWriter(jsonWriter_out);
        cjDoc2.fire(cjWriter_out, false);
        String json_out = jsonWriter_out.json();

        CjAssert.verifySameCjOrRecord(resource, TEST_ID, json_out, json_in, () -> {
            log.info("JSON-in:\n{}", json_in);
            log.info("CJ.JSON:\n{}", CjDocuments.toJsonString(cjDoc));
        });
    }

}
