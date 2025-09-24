package com.calpano.graphinout.reader.graphml;


import com.calpano.graphinout.base.cj.element.ICjDocument;
import com.calpano.graphinout.base.cj.element.impl.CjDocumentElement;
import com.calpano.graphinout.base.cj.stream.ICjWriter;
import com.calpano.graphinout.base.cj.stream.impl.Cj2ElementsWriter;
import com.calpano.graphinout.base.cj.stream.impl.Cj2JsonWriter;
import com.calpano.graphinout.base.cj.stream.impl.Json2CjWriter;
import com.calpano.graphinout.base.graphml.DelegatingGraphmlWriter;
import com.calpano.graphinout.base.graphml.xml.Graphml2XmlWriter;
import com.calpano.graphinout.base.validation.graphml.ValidatingGraphMlWriter;
import com.calpano.graphinout.foundation.input.SingleInputSourceOfString;
import com.calpano.graphinout.foundation.json.stream.JsonWriter;
import com.calpano.graphinout.foundation.json.stream.impl.JsonReaderImpl;
import com.calpano.graphinout.foundation.json.stream.impl.StringBuilderJsonWriter;
import com.calpano.graphinout.foundation.xml.Xml2StringWriter;
import com.calpano.graphinout.reader.graphml.cj.CjDocument2Graphml;
import com.calpano.graphinout.reader.graphml.cj.Graphml2CjDocument;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

import static com.calpano.graphinout.base.cj.CjFormatter.stripCjHeader;
import static com.calpano.graphinout.foundation.json.impl.JsonFormatter.formatDebug;
import static com.calpano.graphinout.foundation.json.impl.JsonFormatter.removeWhitespace;
import static com.google.common.truth.Truth.assertThat;


public class Cj2GraphmlAndBackTest {

    @ParameterizedTest(name = "{index}: {0}")
    @MethodSource("com.calpano.graphinout.foundation.TestFileProvider#cjFilesCanonical")
    @DisplayName("Test JSON-CJ-Graphml - all files together")
    void test_json_cj_graphml_CanonicalCjFiles(String displayPath, Path path) throws Exception {
        String json_in = FileUtils.readFileToString(path.toFile(), StandardCharsets.UTF_8);
        SingleInputSourceOfString inputSource = SingleInputSourceOfString.of(path.getFileName().toString(), json_in);
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
    @MethodSource("com.calpano.graphinout.foundation.TestFileProvider#cjFilesCanonical")
    @DisplayName("Test JSON-CJ-Graphml-CJ-JSON - all files together")
    void test_json_cj_graphml_cj_json_CanonicalCjFiles(String displayPath, Path path) throws Exception {
        // JSON
        String json_in = FileUtils.readFileToString(path.toFile(), StandardCharsets.UTF_8);
        SingleInputSourceOfString inputSource = SingleInputSourceOfString.of(path.getFileName().toString(), json_in);

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
        String xml = xmlWriter.string();
        System.out.println(xml);
        System.out.flush();
        CjDocumentElement cjDoc2 = graphml2cjDocument.resultDoc();

        // CJ2 --> JSON2
        StringBuilderJsonWriter jsonWriter_out = new StringBuilderJsonWriter();
        ICjWriter cjWriter_out = new Cj2JsonWriter(jsonWriter_out);
        cjDoc2.fire(cjWriter_out);
        String json_out = jsonWriter_out.json();
        assertThat(formatDebug(stripCjHeader(removeWhitespace(json_out)))) //
                .isEqualTo(formatDebug(stripCjHeader(removeWhitespace(json_in))));
    }

}
