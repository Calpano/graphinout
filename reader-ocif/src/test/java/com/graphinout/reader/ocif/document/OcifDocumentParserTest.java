package com.graphinout.reader.ocif.document;

import com.graphinout.foundation.TestFileProvider;
import io.github.classgraph.Resource;
import jdk.jfr.Description;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.util.Set;
import java.util.stream.Stream;

import static com.graphinout.foundation.TestFileProvider.resources;

/**
 * Parses all OCIF example files into the OcifDocument object model to ensure the DOM parser
 * can handle every sample without errors.
 */
class OcifDocumentParserTest {

    public static Stream<TestFileProvider.TestResource> ocifResources() {
        return resources("json/ocif", Set.of(".ocif", ".ocif.json"));
    }

    @ParameterizedTest(name = "{index}: {0}")
    @MethodSource("ocifResources")
    @Description("Parse all OCIF files into an OcifDocument")
    void parse_all_ocif_into_document(String displayName, Resource resource) throws IOException {
        String json = resource.getContentAsString();

        OcifDocumentParser parser = new OcifDocumentParser();
        OcifDocument doc = parser.parse(json);

        // Basic sanity checks
        Assertions.assertNotNull(doc, "Parsed OcifDocument must not be null");
        Assertions.assertNotNull(doc.getNodes(), "Nodes list must not be null");
        Assertions.assertNotNull(doc.getRelations(), "Relations list must not be null");
        Assertions.assertNotNull(doc.getResources(), "Resources list must not be null");
        Assertions.assertNotNull(doc.getSchemas(), "Schemas list must not be null");
        // Canvas extensions list should always be non-null as well
        Assertions.assertNotNull(doc.getCanvasExtensions(), "Canvas extensions list must not be null");
    }
}
