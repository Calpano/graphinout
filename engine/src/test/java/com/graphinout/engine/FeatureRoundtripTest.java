package com.graphinout.engine;

import com.graphinout.base.cj.document.CjDocument2CjStream;
import com.graphinout.base.cj.document.CjDocuments;
import com.graphinout.base.cj.document.ICjDocument;
import com.graphinout.base.cj.stream.CjStream2CjWriter;
import com.graphinout.base.cj.stream.ICjStream;
import com.graphinout.base.cj.writer.CjWriter2CjDocumentWriter;
import com.graphinout.base.gio.GioReader;
import com.graphinout.base.gio.GioWriter;
import com.graphinout.base.input.SingleInputSource;
import com.graphinout.base.output.InMemoryOutputSink;
import com.graphinout.testdata.TestFileUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.slf4j.Logger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import static com.google.common.truth.Truth.assertThat;
import static org.slf4j.LoggerFactory.getLogger;

/**
 * Feature-conformance round-trip suite.
 *
 * <p>For every graph format that has both a reader and a writer, and that the
 * <a href="https://github.com/calpano/graph-format-registry">graph-format-registry</a> says <em>supports</em> a given
 * model feature, this test takes the synthetic per-feature CJ file, round-trips it
 * {@code CJ -> format -> CJ}, and asserts the feature is still structurally present afterwards. A failure means the
 * format's claim in the registry does not hold for our reader/writer pair (a punch-list item).
 *
 * <p>The claim matrix below is a snapshot of the {@code ..supports..} / {@code ..lacks..} triples in
 * {@code graph-format-registry/formats/<family>/<format-id>.adoc}. Full ({@code ..supports..}) claims are hard-asserted;
 * partial ({@code ,, ..status.. partial}) claims are exercised by {@link #partialSupportReport()} for visibility only.
 * Format ids are the engine {@code fileFormat().id()} values; the registry slug each maps to is noted inline.
 */
class FeatureRoundtripTest {

    private static final Logger log = getLogger(FeatureRoundtripTest.class);
    private static final GioEngineCore CORE = new GioEngineCore();

    /** One format's feature claims (engine format id + registry-sourced full/partial support slugs). */
    private record Claims(String formatId, Set<String> full, Set<String> partial) {}

    private static Set<String> of(String... slugs) {
        // insertion-ordered so parameterized-test indices and reports are deterministic across runs
        return new LinkedHashSet<>(List.of(slugs));
    }

    private static final List<Claims> CLAIMS = List.of(
            // connected-json-8.0.0 — the pivot format; supports everything except edges-on-edges.
            new Claims("connected-json", of(
                    "multiple-graphs-per-document", "nodes", "undirected-edges", "directed-edges", "hyperedges",
                    "mixed-directionality-edges", "nested-graphs-in-nodes", "nested-graphs-in-edges",
                    "nested-graphs-in-graphs", "node-labels", "edge-labels", "attributes-on-nodes",
                    "attributes-on-edges", "attributes-on-graphs", "typed-edges"), of()),
            // gexf-1.3
            new Claims("gexf", of(
                    "nodes", "undirected-edges", "directed-edges", "mixed-directionality-edges",
                    "nested-graphs-in-nodes", "node-labels", "edge-labels", "attributes-on-nodes",
                    "attributes-on-edges", "attributes-on-graphs", "typed-edges"), of()),
            // gml
            new Claims("gml", of(
                    "nodes", "undirected-edges", "directed-edges", "multiple-graphs-per-document",
                    "nested-graphs-in-nodes", "nested-graphs-in-graphs", "node-labels", "edge-labels",
                    "attributes-on-nodes", "attributes-on-edges", "attributes-on-graphs"), of("typed-edges")),
            // graphml-1.1
            new Claims("graphml", of(
                    "nodes", "undirected-edges", "directed-edges", "hyperedges", "mixed-directionality-edges",
                    "multiple-graphs-per-document", "nested-graphs-in-nodes", "nested-graphs-in-edges",
                    "nested-graphs-in-graphs", "attributes-on-nodes", "attributes-on-edges", "attributes-on-graphs"),
                    of("node-labels", "edge-labels", "typed-edges")),
            // dot
            new Claims("dot", of(
                    "nodes", "undirected-edges", "directed-edges", "nested-graphs-in-nodes", "nested-graphs-in-graphs",
                    "node-labels", "edge-labels", "attributes-on-nodes", "attributes-on-edges",
                    "attributes-on-graphs"), of()),
            // tgf — directed only: a TGF `source target` line is read source -> target.
            new Claims("tgf", of("nodes", "directed-edges", "node-labels", "edge-labels"), of()),
            // d2
            new Claims("d2", of(
                    "nodes", "undirected-edges", "directed-edges", "nested-graphs-in-nodes", "node-labels",
                    "edge-labels", "attributes-on-nodes", "attributes-on-edges"), of()),
            // ddot
            new Claims("ddot", of("nodes", "directed-edges", "node-labels", "edge-labels", "attributes-on-nodes",
                    "typed-edges"), of()),
            // ocif-0.6
            new Claims("ocif", of(
                    "nodes", "undirected-edges", "directed-edges", "multiple-graphs-per-document",
                    "nested-graphs-in-nodes", "node-labels", "edge-labels", "attributes-on-nodes",
                    "attributes-on-edges", "attributes-on-graphs"), of("typed-edges")),
            // plantuml-class-diagram
            new Claims("plantuml", of(
                    "nodes", "undirected-edges", "directed-edges", "mixed-directionality-edges",
                    "multiple-graphs-per-document", "nested-graphs-in-graphs", "node-labels", "edge-labels",
                    "attributes-on-nodes", "typed-edges"), of("attributes-on-edges")),
            // mermaid-flowchart
            new Claims("mermaid", of(
                    "nodes", "undirected-edges", "directed-edges", "mixed-directionality-edges",
                    "nested-graphs-in-nodes", "node-labels", "edge-labels"), of("attributes-on-nodes")),
            // adjlist — a line carries no direction marker, so it is read as directed (source -> neighbours);
            // undirected/mixed are not representable (registry: ..lacks.. undirected-edges).
            new Claims("adjlist", of("directed-edges"), of("nodes", "attributes-on-edges")),
            // edge-list
            new Claims("edge-list", of("nodes", "directed-edges"), of()));

    // -------------------------------------------------------------------------------------------------------------
    // Task 3: each synthetic CJ file actually exercises its feature (validated by self-parse).
    // -------------------------------------------------------------------------------------------------------------

    @ParameterizedTest(name = "{0}")
    @EnumSource(CjFeature.class)
    void featureFileExhibitsItsFeature(CjFeature feature) throws IOException {
        ICjDocument doc = readCjResource(feature.resourcePath());
        assertThat(feature.isPresentIn(doc)).isTrue();
    }

    // -------------------------------------------------------------------------------------------------------------
    // Task 4: a claimed feature survives CJ -> format -> CJ.
    // -------------------------------------------------------------------------------------------------------------

    static Stream<Arguments> fullSupportCombos() {
        List<Arguments> combos = new ArrayList<>();
        for (Claims c : CLAIMS) {
            for (String slug : c.full()) {
                combos.add(Arguments.of(c.formatId(), slug));
            }
        }
        return combos.stream();
    }

    @ParameterizedTest(name = "{0} preserves {1}")
    @MethodSource("fullSupportCombos")
    void claimedFeatureSurvivesRoundtrip(String formatId, String featureSlug) throws IOException {
        CjFeature feature = CjFeature.bySlug(featureSlug);
        GioReader reader = readerFor(formatId).orElseThrow(() ->
                new AssertionError("No reader registered for format id '" + formatId + "' (claim matrix drift)"));
        GioWriter writer = writerFor(formatId).orElseThrow(() ->
                new AssertionError("No writer registered for format id '" + formatId + "' (claim matrix drift)"));

        ICjDocument roundTripped = roundTrip(reader, writer, feature);
        assertThat(feature.isPresentIn(roundTripped)).isTrue();
    }

    /** Full matrix (full + partial claims) printed as a readable report; never fails the build. */
    @Test
    void roundtripMatrixReport() throws IOException {
        StringBuilder report = new StringBuilder("\nFeature round-trip matrix (CJ -> format -> CJ):\n");
        report.append(String.format("  %-14s %-28s %-8s %s%n", "format", "feature", "claim", "result"));
        for (Claims c : CLAIMS) {
            Optional<GioReader> reader = readerFor(c.formatId());
            Optional<GioWriter> writer = writerFor(c.formatId());
            for (String slug : c.full()) {
                report.append(row(c.formatId(), slug, "full", reader, writer));
            }
            for (String slug : c.partial()) {
                report.append(row(c.formatId(), slug, "partial", reader, writer));
            }
        }
        log.info(report.toString());
    }

    private String row(String formatId, String slug, String claim, Optional<GioReader> reader,
                       Optional<GioWriter> writer) {
        CjFeature feature = CjFeature.bySlug(slug);
        String status;
        if (reader.isEmpty() || writer.isEmpty()) {
            status = "SKIP (no reader/writer)";
        } else {
            try {
                status = feature.isPresentIn(roundTrip(reader.get(), writer.get(), feature)) ? "preserved" : "LOST";
            } catch (Exception e) {
                status = "ERROR (" + e.getClass().getSimpleName() + ")";
            }
        }
        return String.format("  %-14s %-28s %-8s %s%n", formatId, slug, claim, status);
    }

    // -------------------------------------------------------------------------------------------------------------
    // helpers
    // -------------------------------------------------------------------------------------------------------------

    /** CJ doc -> {@code format} (via writer) -> CJ doc (via reader). */
    private ICjDocument roundTrip(GioReader reader, GioWriter writer, CjFeature feature) throws IOException {
        ICjDocument cjIn = readCjResource(feature.resourcePath());

        // CJ -> format text
        InMemoryOutputSink sink = new InMemoryOutputSink();
        ICjStream writeStream = writer.createCjStream(sink);
        CjDocument2CjStream.toCjStream(cjIn, writeStream);
        String formatText = sink.getBufferAsUtf8String();

        // format text -> CJ
        CjWriter2CjDocumentWriter docWriter = new CjWriter2CjDocumentWriter();
        reader.read(SingleInputSource.of(feature.slug() + "-input", formatText),
                new CjStream2CjWriter(docWriter, true));
        return docWriter.resultDoc();
    }

    private static ICjDocument readCjResource(String resourcePath) throws IOException {
        String json = TestFileUtil.resource(resourcePath).getContentAsString();
        return CjDocuments.parseCjJsonString(resourcePath, json);
    }

    private static Optional<GioReader> readerFor(String formatId) {
        return CORE.readers().stream().filter(r -> formatId.equals(r.fileFormat().id())).findFirst();
    }

    private static Optional<GioWriter> writerFor(String formatId) {
        return CORE.writers().stream().filter(w -> formatId.equals(w.fileFormat().id())).findFirst();
    }
}
