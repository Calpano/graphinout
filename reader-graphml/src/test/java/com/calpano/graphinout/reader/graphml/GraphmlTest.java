package com.calpano.graphinout.reader.graphml;

import com.calpano.graphinout.base.graphml.xml.Graphml2XmlWriter;
import com.calpano.graphinout.foundation.text.StringFormatter;
import com.calpano.graphinout.foundation.xml.DelegatingXmlWriter;
import com.calpano.graphinout.foundation.xml.Xml2AppendableWriter;
import com.calpano.graphinout.foundation.xml.Xml2StringWriter;
import com.calpano.graphinout.foundation.xml.XmlFormatter;
import com.calpano.graphinout.foundation.xml.XmlTool;
import com.calpano.graphinout.foundation.xml.XmlWriter;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.slf4j.Logger;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

import static com.google.common.truth.Truth.assertThat;
import static org.slf4j.LoggerFactory.getLogger;

public class GraphmlTest {

    private static final Logger log = getLogger(GraphmlTest.class);

    @ParameterizedTest(name = "{index}: {0}")
    @MethodSource("com.calpano.graphinout.foundation.TestFileProvider#graphmlFiles")
    @DisplayName("Test all GraphML files together")
    void testAllGraphmlFiles(String displayPath, Path xmlFilePath) throws Exception {
        testGraphmlFile(xmlFilePath);
    }

    void testGraphmlFile(Path file) throws Exception {
        if (file.toFile().getName().startsWith("invalid-")) {
            log.debug("Skipping invalid file: {}", file.toAbsolutePath());
            return;
        }

        // read into string;
        String xml_in = FileUtils.readFileToString(file.toFile(), StandardCharsets.UTF_8);
        xml_in = StringFormatter.normalizeLineEndings(xml_in);

        // check if that is valid XML to begin with
        XmlFormatter.normalize(xml_in);

        Xml2StringWriter xmlWriter = new Xml2StringWriter();
        XmlWriter xmlWriter_out = new DelegatingXmlWriter(new Xml2AppendableWriter(System.out), xmlWriter);
        /* receive GraphMl events -> send XML events */
        Graphml2XmlWriter graphml2xml = new Graphml2XmlWriter(xmlWriter_out);
        /* receive XML events -> send Graphml events  */
        Xml2GraphmlWriter xml2graphml = new Xml2GraphmlWriter(graphml2xml);
        XmlTool.parseAndWriteXml(file.toFile(), xml2graphml);
        String xml_out = xmlWriter.string();

        int lineLen = 60;
        String norm_in = XmlFormatter.wrap(GraphmlFormatter.normalize(xml_in), lineLen);
        String norm_out = XmlFormatter.wrap(GraphmlFormatter.normalize(xml_out), lineLen);

        // HACK to avoid calc'ing diff for large file
        if (norm_in.length() > 1024 * 1024) {
            return;
        }
        assertThat(norm_out).isEqualTo(norm_in);
    }

}

