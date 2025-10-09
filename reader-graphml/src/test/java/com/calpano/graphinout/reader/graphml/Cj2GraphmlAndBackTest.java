package com.calpano.graphinout.reader.graphml;


import com.calpano.graphinout.base.cj.CjAssert;
import com.calpano.graphinout.base.cj.element.CjDocuments;
import com.calpano.graphinout.base.cj.element.ICjDocument;
import com.calpano.graphinout.base.cj.element.impl.CjDocumentElement;
import com.calpano.graphinout.base.cj.stream.ICjWriter;
import com.calpano.graphinout.base.cj.stream.impl.Cj2ElementsWriter;
import com.calpano.graphinout.base.cj.stream.impl.Cj2JsonWriter;
import com.calpano.graphinout.base.cj.stream.impl.Json2CjWriter;
import com.calpano.graphinout.base.graphml.DelegatingGraphmlWriter;
import com.calpano.graphinout.base.graphml.Graphml2XmlWriter;
import com.calpano.graphinout.base.validation.graphml.ValidatingGraphMlWriter;
import com.calpano.graphinout.foundation.TestFileUtil;
import com.calpano.graphinout.foundation.input.SingleInputSourceOfString;
import com.calpano.graphinout.foundation.json.stream.JsonWriter;
import com.calpano.graphinout.foundation.json.stream.impl.JsonReaderImpl;
import com.calpano.graphinout.foundation.json.stream.impl.StringBuilderJsonWriter;
import com.calpano.graphinout.foundation.xml.Xml2StringWriter;
import com.calpano.graphinout.reader.graphml.cj.CjDocument2Graphml;
import com.calpano.graphinout.reader.graphml.cj.Graphml2CjDocument;
import io.github.classgraph.Resource;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.slf4j.Logger;

import static com.calpano.graphinout.foundation.TestFileUtil.inputSource;
import static org.slf4j.LoggerFactory.getLogger;


@SuppressWarnings("unused")
public class Cj2GraphmlAndBackTest {

    private static final Logger log = getLogger(Cj2GraphmlAndBackTest.class);

    @ParameterizedTest(name = "{index}: {0}")
    @MethodSource("com.calpano.graphinout.foundation.TestFileProvider#cjResourcesCanonical")
    @DisplayName("Run JSON->CJ->Graphml - all files together")
    void test_json_cj_graphml_CanonicalCjFiles(String displayPath, Resource resource) throws Exception {
        String json_in = resource.getContentAsString();
        SingleInputSourceOfString inputSource = inputSource(resource);
        Cj2ElementsWriter cj2ElementsWriter = new Cj2ElementsWriter();
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
    @MethodSource("com.calpano.graphinout.foundation.TestFileProvider#cjResourcesCanonical")
    @DisplayName("Test JSON<->CJ<->Graphml (all)")
    void test_json_cj_graphml_cj_json_CanonicalCjFiles(String displayPath, Resource resource) throws Exception {
        // JSON
        String json_in = resource.getContentAsString();
        SingleInputSourceOfString inputSource = inputSource(resource);

        // JSON -> CJ
        Cj2ElementsWriter cj2ElementsWriter = new Cj2ElementsWriter();
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

        CjAssert.verifySameCjOrRecord(resource,json_out,json_in, () -> {
            log.info("JSON-in:\n{}", json_in);
            log.info("CJ.JSON:\n{}", CjDocuments.toJsonString(cjDoc));
        });
    }

}
