package com.graphinout.reader.graphml;


import com.graphinout.base.TestFileUtil;
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
import com.graphinout.foundation.jajson.JaJson;
import com.graphinout.foundation.json.util.FormatterConfig;
import com.graphinout.foundation.json.util.JsonCompactFormatter;
import com.graphinout.foundation.json.writer.JsonWriter;
import com.graphinout.foundation.json.writer.impl.Json2StringWriter;
import com.graphinout.foundation.json.writer.impl.StringBuilderJsonWriter;
import com.graphinout.foundation.xml.writer.Xml2StringWriter;
import com.graphinout.reader.graphml.cj.CjDocument2Graphml;
import com.graphinout.reader.graphml.cj.Graphml2CjDocument;
import com.graphinout.reader.graphml.cj.Graphml2CjWriter;
import com.graphinout.reader.graphml.validation.ValidatingGraphMlWriter;
import io.github.classgraph.Resource;
import jdk.jfr.Description;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.slf4j.Logger;

import java.io.IOException;
import java.util.Set;

import static com.google.common.truth.Truth.assertThat;
import static com.graphinout.base.TestFileUtil.inputSource;
import static org.slf4j.LoggerFactory.getLogger;


@DisplayName("CJ<->Graphml")
public class Cj2GraphmlAndBackTest {

    private static final Logger log = getLogger(Cj2GraphmlAndBackTest.class);
    private static final String TEST_ID = "Cj2Gml2Cj";

    @Test
    void testNestedGraphs() throws IOException {
        Resource resource = TestFileUtil.resource("json/cj/canonical/nested-graphs.cj.json");
        assertThat(resource).isNotNull();
        String json = resource.getContentAsString();
        SingleInputSourceOfString inputSource = SingleInputSourceOfString.of("test", json);

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
        CjAssert.verifySameCjOrRecord(resource, "Cj2Gml2Cj", jsonOut, json, null);
    }

    @ParameterizedTest(name = "{index}: {0}")
    @MethodSource("com.graphinout.base.TestFileProvider#cjResourcesCanonical")
    @Description("Test JSON->CJ->Graphml->CjStream->CJ->JSON (all)")
    void test_Json_Cj_Graphml_CjStream_Cj_Json(String displayName, Resource resource) throws IOException {
        String jsonInput = resource.getContentAsString();
        SingleInputSourceOfString inputSource = SingleInputSourceOfString.of("test", jsonInput);

        // JSON -> CJ doc
        CjWriter2CjDocumentWriter cj2ElementsWriter = new CjWriter2CjDocumentWriter();
        JsonWriter jsonWriter_in = Json2CjWriter.createWritingTo(cj2ElementsWriter);
        JsonReaderImpl jsonReader = new JsonReaderImpl();
        jsonReader.read(inputSource, jsonWriter_in);
        ICjDocument cjDoc = cj2ElementsWriter.resultDoc();
        if (cjDoc == null) {
            cjDoc = new CjDocumentElement();
        }

        // CJ doc -> GraphML -> CJ
        Json2StringWriter json2StringWriter = new Json2StringWriter();
        Cj2JsonWriter cj2JsonWriter = new Cj2JsonWriter(json2StringWriter);
        Graphml2CjDocument graphml2CjWriter = new Graphml2CjWriter(cj2JsonWriter);
        CjDocument2Graphml cjDocument2Graphml = new CjDocument2Graphml(graphml2CjWriter);
        cjDocument2Graphml.writeDocumentToGraphml(cjDoc);

        String jsonOut = json2StringWriter.jsonString();
        CjAssert.verifySameCjOrRecord(resource, "Cj2Gml2Cj", jsonOut, jsonInput, () -> {
            // format both pretty, then diff
            FormatterConfig config = FormatterConfig.of(60, Set.of("nodes", "edges", "graphs"), true);
            String compactOut = JsonCompactFormatter.formatCompact(JaJson.parse(CjAssert.normalize(jsonOut)), config);
            String compactIn = JsonCompactFormatter.formatCompact(JaJson.parse(CjAssert.normalize(jsonInput)), config);
            assertThat(compactOut).isEqualTo(compactIn);
        });
    }

    @ParameterizedTest(name = "{index}: {0}")
    @MethodSource("com.graphinout.base.TestFileProvider#cjResourcesCanonical")
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
    @MethodSource("com.graphinout.base.TestFileProvider#cjResourcesCanonical")
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
        cjDoc2.fire(cjWriter_out);
        String json_out = jsonWriter_out.json();

        CjAssert.verifySameCjOrRecord(resource, TEST_ID, json_out, json_in, () -> {
            log.info("JSON-in:\n{}", json_in);
            log.info("CJ.JSON:\n{}", CjDocuments.toJsonString(cjDoc));
        });
    }

}
