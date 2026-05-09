package com.graphinout.reader.ocif07;

import com.graphinout.foundation.pure.input.ContentError;
import com.graphinout.reader.ocif07.document.impl.OcifDocument;
import com.graphinout.testdata.TestFileProvider;
import io.github.classgraph.Resource;
import jdk.jfr.Description;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static com.google.common.truth.Truth.assertThat;
import static com.google.common.truth.Truth.assertWithMessage;
import static com.graphinout.testdata.TestFileProvider.resources;

/**
 * Parses all OCIF example files into the OcifDocument object model to ensure the DOM parser can handle every sample
 * without errors.
 */
class Json2OcifDocTest {

    public static Stream<TestFileProvider.TestResource> ocifResources() {
        return resources("json/ocif", Set.of(".ocif", ".ocif.json"));
    }

    @ParameterizedTest(name = "{index}: {0}")
    @MethodSource("ocifResources")
    @Description("Parse all OCIF files into an OcifDocument")
    void parse_all_ocif_into_document(String displayName, Resource resource) throws IOException {
        String json = resource.getContentAsString();

        List<ContentError> contentErrors = new ArrayList<>();
        Json2OcifDoc json2OcifDoc = new Json2OcifDoc();
        OcifDocument doc = json2OcifDoc.jsonString2ocifDocument(json, contentErrors::add);
        assertThat(contentErrors.stream().filter(ContentError::isError)).isEmpty();

        // Basic sanity checks
        assertWithMessage("Parsed OcifDocument must not be null").that(doc).isNotNull();
        assertWithMessage("Nodes list must not be null").that(doc.nodes()).isNotNull();
        assertWithMessage("Relations list must not be null").that(doc.relations()).isNotNull();
        assertWithMessage("Resources list must not be null").that(doc.resources()).isNotNull();
        assertWithMessage("Schemas list must not be null").that(doc.schemas()).isNotNull();
        // Canvas extensions list should always be non-null as well
        assertWithMessage("Canvas extensions list must not be null").that(doc.canvasExtensions()).isNotNull();
    }

}
