package com.graphinout.reader.graphml;


import com.graphinout.base.cj.CjAssert;
import com.graphinout.base.cj.element.CjDocuments;
import com.graphinout.base.cj.element.ICjDocument;
import com.graphinout.base.cj.element.impl.CjDocumentElement;
import com.graphinout.base.cj.stream.ICjWriter;
import com.graphinout.base.cj.stream.impl.Cj2JsonWriter;
import com.graphinout.base.cj.stream.impl.CjWriter2CjDocumentWriter;
import com.graphinout.base.cj.stream.impl.Json2CjWriter;
import com.graphinout.base.graphml.DelegatingGraphmlWriter;
import com.graphinout.base.graphml.Graphml2XmlWriter;
import com.graphinout.base.validation.graphml.ValidatingGraphMlWriter;
import com.graphinout.foundation.input.SingleInputSourceOfString;
import com.graphinout.foundation.json.stream.JsonWriter;
import com.graphinout.foundation.json.stream.impl.Json2StringWriter;
import com.graphinout.foundation.json.stream.impl.JsonReaderImpl;
import com.graphinout.foundation.json.stream.impl.StringBuilderJsonWriter;
import com.graphinout.foundation.xml.Xml2StringWriter;
import com.graphinout.reader.graphml.cj.CjDocument2Graphml;
import com.graphinout.reader.graphml.cj.Graphml2CjDocument;
import com.graphinout.reader.graphml.cj.Graphml2CjWriter;
import io.github.classgraph.Resource;
import jdk.jfr.Description;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.slf4j.Logger;

import java.io.IOException;

import static com.graphinout.foundation.TestFileUtil.inputSource;
import static org.slf4j.LoggerFactory.getLogger;


@DisplayName("CJ<->Graphml")
public class Cj2GraphmlAndBackTest {

    private static final Logger log = getLogger(Cj2GraphmlAndBackTest.class);
    private static final String TEST_ID = "Cj2Gml2Cj";

    @ParameterizedTest(name = "{index}: {0}")
    @MethodSource("com.graphinout.foundation.TestFileProvider#cjResourcesCanonical")
    @Description("Test JSON->CJ->Graphml->CjStream->CJ->JSON (all)")
    void test_Json_Cj_Graphml_CjStream_Cj_Json(String displayName, Resource resource) throws IOException {
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

        // CJ doc -> GraphML -> CJ
        Json2StringWriter json2StringWriter = new Json2StringWriter();
        Cj2JsonWriter cj2JsonWriter = new Cj2JsonWriter(json2StringWriter);
        Graphml2CjDocument graphml2CjWriter = new Graphml2CjWriter(cj2JsonWriter);
        CjDocument2Graphml cjDocument2Graphml = new CjDocument2Graphml(graphml2CjWriter);
        cjDocument2Graphml.writeDocumentToGraphml(cjDoc);

        CjAssert.verifySameCjOrRecord(resource,"Cj2Gml2Cj", json2StringWriter.jsonString(), json, null);
    }

    @ParameterizedTest(name = "{index}: {0}")
    @MethodSource("com.graphinout.foundation.TestFileProvider#cjResourcesCanonical")
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
    @MethodSource("com.graphinout.foundation.TestFileProvider#cjResourcesCanonical")
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
