package com.graphinout.reader.gxl;

import com.graphinout.base.cj.analyze.CjFeature;
import com.graphinout.base.cj.document.CjDocuments;
import com.graphinout.base.cj.document.ICjDocument;
import com.graphinout.base.input.SingleInputSource;
import com.graphinout.foundation.pure.input.ContentError;
import com.graphinout.testdata.TestFileProvider;
import com.graphinout.testdata.TestFileUtil;
import io.github.classgraph.Resource;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static com.google.common.truth.Truth.assertThat;
import static com.google.common.truth.Truth.assertWithMessage;

class GxlRoundtripTest {

    /** Uses every GXL construct the mapping supports. */
    static final String RICH = """
            <?xml version="1.0" encoding="UTF-8"?>
            <gxl xmlns:xlink="http://www.w3.org/1999/xlink">
              <graph id="g" role="main" edgemode="defaultdirected" edgeids="true" hypergraph="true">
                <type xlink:href="schema.gxl#G"/>
                <attr name="version"><int>2</int></attr>
                <node id="a">
                  <type xlink:href="schema.gxl#A"/>
                  <attr name="label"><string>Node A</string></attr>
                  <attr name="weight"><float>2.0</float></attr>
                  <attr name="color"><enum>red</enum></attr>
                  <attr name="doc"><locator xlink:href="http://example.org/a"/></attr>
                  <attr name="tags"><set><string>x</string><string>y</string></set></attr>
                  <attr name="pair"><tup><int>1</int><bool>false</bool></tup></attr>
                  <graph id="inA" edgemode="undirected">
                    <node id="a1"/><node id="a2"/>
                    <edge from="a1" to="a2"/>
                  </graph>
                </node>
                <node id="b"/>
                <node id="c"/>
                <edge id="d" from="a" to="b" fromorder="1" toorder="2">
                  <type xlink:href="schema.gxl#calls"/>
                  <attr name="label"><string>calls</string></attr>
                  <graph id="inD"><node id="d1"/></graph>
                </edge>
                <edge id="u" from="b" to="c" isdirected="false"/>
                <rel id="r">
                  <relend target="a" role="src" direction="out" startorder="1"/>
                  <relend target="b" role="dst" direction="in"/>
                  <relend target="c" direction="none"><attr name="w"><int>3</int></attr></relend>
                </rel>
                <rel id="binaryRel"><relend target="a" direction="out"/><relend target="c" direction="in"/></rel>
              </graph>
              <graph id="second"><node id="z"/></graph>
            </gxl>
            """;

    static Stream<TestFileProvider.TestResource> gxlResources() {
        return TestFileProvider.resources("xml/gxl", Set.of(".gxl"));
    }

    static ICjDocument read(String name, String gxl, List<ContentError> errors) throws IOException {
        GxlReader reader = new GxlReader();
        reader.setContentErrorHandler(errors::add);
        ICjDocument doc = reader.readToCjDocument(SingleInputSource.of(name, gxl));
        assertThat(doc).isNotNull();
        return doc;
    }

    static String write(ICjDocument doc, List<ContentError> warnings) {
        return GxlWriter.toGxl(doc, warnings::add);
    }

    static List<ContentError> errorsOnly(List<ContentError> all) {
        return all.stream().filter(e -> e.getLevel() == ContentError.ErrorLevel.Error).toList();
    }

    /** GXL -> CJ -> GXL -> CJ must give the same CJ. */
    static void assertRoundtrip(String name, String gxl, boolean expectNoWriterWarnings) throws IOException {
        List<ContentError> readErrors = new ArrayList<>();
        ICjDocument cj1 = read(name, gxl, readErrors);
        assertWithMessage("errors reading %s: %s", name, readErrors).that(errorsOnly(readErrors)).isEmpty();

        List<ContentError> writeWarnings = new ArrayList<>();
        String gxl2 = write(cj1, writeWarnings);
        if (expectNoWriterWarnings) {
            assertWithMessage("writer warnings for %s", name).that(writeWarnings).isEmpty();
        }

        List<ContentError> readErrors2 = new ArrayList<>();
        ICjDocument cj2 = read(name + " (rewritten)", gxl2, readErrors2);
        assertWithMessage("errors re-reading %s:%n%s", name, gxl2).that(errorsOnly(readErrors2)).isEmpty();
        // GXL requires graph ids; the writer adds "_graphN" where CJ has none. Accept exactly those, nothing else.
        String json2WithoutGeneratedIds = CjDocuments.toJsonString(cj2).replaceAll("\"id\":\"_graph\\d+\",", "");
        assertWithMessage("%s changed in GXL -> CJ -> GXL -> CJ; GXL was:%n%s", name, gxl2)
                .that(json2WithoutGeneratedIds).isEqualTo(CjDocuments.toJsonString(cj1));

        // and a second cycle changes nothing at all
        String gxl3 = write(cj2, new ArrayList<>());
        assertWithMessage("%s: second GXL write differs from the first", name).that(gxl3).isEqualTo(gxl2);
    }

    @Test
    void richDocument() throws IOException {
        assertRoundtrip("rich.gxl", RICH, true);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("gxlResources")
    @DisplayName("GXL -> CJ -> GXL -> CJ for all GXL test files")
    void corpus(String displayName, Resource resource) throws IOException {
        String gxl = resource.getContentAsString();
        List<ContentError> errors = new ArrayList<>();
        ICjDocument cj = read(resource.getPath(), gxl, errors);
        assertThat(cj.graphs().count()).isGreaterThan(0);
        assertRoundtrip(resource.getPath(), gxl, true);
    }

    @Test
    void writerWarnsAboutWhatGxlCannotHold() throws IOException {
        ICjDocument cj = CjDocuments.parseCjJsonString("cj", """
                {"graphs":[{"id":"g",
                  "nodes":[{"id":"a","ports":[{"id":"p"}]},{"id":"b"}],
                  "edges":[{"endpoints":[{"node":"a","port":"p","direction":"in"},{"node":"b","direction":"out"}]}],
                  "graphs":[{"id":"inner","nodes":[{"id":"x"}]}]}]}
                """);
        List<ContentError> warnings = new ArrayList<>();
        String gxl = write(cj, warnings);
        assertThat(warnings.stream().map(w -> w.message).toList().toString()).contains("GXL has no ports");
        assertThat(warnings.stream().map(w -> w.message).toList().toString()).contains("nested directly in a graph");
        // the output is still valid GXL, with the nested graph inside a new node
        List<ContentError> errors = new ArrayList<>();
        ICjDocument back = read("out.gxl", gxl, errors);
        assertThat(errorsOnly(errors)).isEmpty();
        assertThat(back.graphs().findFirst().orElseThrow().nodes().anyMatch(n -> n.graphs().findAny().isPresent())).isTrue();
    }

    /** CJ -> GXL -> CJ keeps the features the graph-format-registry claims for GXL (where a CJ fixture exists). */
    @ParameterizedTest(name = "gxl preserves {0}")
    @ValueSource(strings = {"nodes", "undirected-edges", "directed-edges", "mixed-directionality-edges", "hyperedges",
            "multiple-graphs-per-document", "nested-graphs-in-nodes", "nested-graphs-in-edges", "node-labels",
            "edge-labels", "attributes-on-nodes", "attributes-on-edges", "attributes-on-graphs", "typed-edges"})
    void featureSurvivesRoundtrip(String slug) throws IOException {
        CjFeature feature = CjFeature.bySlug(slug);
        String path = "json/connected-json/connected-json-7.0.0/graph-format-features/" + slug + ".cj.json";
        ICjDocument cjIn = CjDocuments.parseCjJsonString(path, TestFileUtil.resource(path).getContentAsString());
        assertWithMessage("fixture %s must exhibit the feature", path).that(feature.isPresentIn(cjIn)).isTrue();

        List<ContentError> warnings = new ArrayList<>();
        String gxl = write(cjIn, warnings);
        List<ContentError> errors = new ArrayList<>();
        ICjDocument cjOut = read(slug + ".gxl", gxl, errors);
        assertThat(errorsOnly(errors)).isEmpty();
        assertWithMessage("%s lost in CJ -> GXL -> CJ; GXL was:%n%s", slug, gxl).that(feature.isPresentIn(cjOut)).isTrue();
    }

}
