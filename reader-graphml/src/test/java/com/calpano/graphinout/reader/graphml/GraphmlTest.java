package com.calpano.graphinout.reader.graphml;

import com.calpano.graphinout.base.graphml.impl.Graphml2XmlWriter;
import com.calpano.graphinout.foundation.output.InMemoryOutputSink;
import com.calpano.graphinout.foundation.xml.DelegatingXmlWriter;
import com.calpano.graphinout.foundation.xml.Xml2AppendableWriter;
import com.calpano.graphinout.foundation.xml.XmlFormatter;
import com.calpano.graphinout.foundation.xml.XmlTool;
import com.calpano.graphinout.foundation.xml.XmlWriter;
import com.calpano.graphinout.foundation.xml.XmlWriterImpl;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.slf4j.Logger;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

import static com.google.common.truth.Truth.assertThat;
import static org.slf4j.LoggerFactory.getLogger;

public class GraphmlTest {

    private static final Logger log = getLogger(GraphmlTest.class);

    static Stream<Arguments> graphmlFileProvider() throws Exception {
        Path testResourcesPath = Paths.get("src/test/resources/graphin/graphml/synthetic/errors");
        int baseLen = testResourcesPath.toString().length() + 1;
        return Files.walk(testResourcesPath).filter(Files::isRegularFile).filter(p -> {
            String pathName = p.toString().toLowerCase();
            return pathName.endsWith(".graphml") || pathName.endsWith(".graphml.xml");
        }).map(p -> Arguments.of(p.toString().substring(baseLen).replace('\\', '/'), p));
    }

    @ParameterizedTest(name = "{index}: {0}")
    @MethodSource("graphmlFileProvider")
    @DisplayName("Test all GraphML files together")
    void testAllGraphmlFiles(String displayPath, Path xmlFilePath) throws Exception {
        testGraphmlFile(xmlFilePath);
    }

    void testGraphmlFile(Path file) throws Exception {
        if (file.toFile().getName().startsWith("invalid-")) {
            log.debug("Skipping invalid file: {}", file.toAbsolutePath());
            return;
        }

        // read into string
        String xml_in = FileUtils.readFileToString(file.toFile(), StandardCharsets.UTF_8);

        // check if that is valid XML to begin with
        XmlFormatter.normalize(xml_in);

        InMemoryOutputSink outputSink = new InMemoryOutputSink();
        XmlWriter xmlWriter_out =
                new DelegatingXmlWriter(
                        new Xml2AppendableWriter(System.out),
                new XmlWriterImpl(outputSink));
        /* receive GraphMl events -> send XML events */
        Graphml2XmlWriter graphml2xml = new Graphml2XmlWriter(xmlWriter_out);
        /* receive XML events -> send Graphml events  */
        Xml2GraphmlWriter xml2graphml = new Xml2GraphmlWriter(graphml2xml);
        XmlTool.parseAndWriteXml(file.toFile(), xml2graphml);
        String xml_out = outputSink.getBufferAsUtf8String();

        String norm_in = XmlFormatter.normalize(xml_in);
        String norm_out = XmlFormatter.normalize(xml_out);
        assertThat(norm_out).isEqualTo(norm_in);
    }

}

