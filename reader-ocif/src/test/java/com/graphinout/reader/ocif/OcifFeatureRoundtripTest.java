package com.graphinout.reader.ocif;

import com.graphinout.base.cj.analyze.CjFeature;
import com.graphinout.base.cj.document.CjDocument2CjStream;
import com.graphinout.base.cj.document.CjDocuments;
import com.graphinout.base.cj.document.ICjDocument;
import com.graphinout.base.cj.stream.CjStream2CjWriter;
import com.graphinout.base.cj.stream.ICjStream;
import com.graphinout.base.cj.writer.CjWriter2CjDocumentWriter;
import com.graphinout.base.input.SingleInputSource;
import com.graphinout.base.output.InMemoryOutputSink;
import com.graphinout.testdata.TestFileUtil;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.IOException;

import static com.google.common.truth.Truth.assertWithMessage;

/**
 * CJ -> OCIF -> CJ keeps the graph features OCIF claims to support. OCIF has no native slot for several of them; they
 * survive via graphinout's own OCIF extensions: {@code @connected-json/structure} (multiple graphs, nested graphs, graph
 * data) and {@code @connected-json/label} (edge labels). The engine's {@code FeatureRoundtripTest} asserts the same
 * claims across all formats.
 */
class OcifFeatureRoundtripTest {

    @ParameterizedTest(name = "ocif preserves {0}")
    @ValueSource(strings = {
            // need @connected-json/structure
            "multiple-graphs-per-document", "nested-graphs-in-nodes", "attributes-on-graphs",
            // needs @connected-json/label
            "edge-labels",
            // native OCIF
            "nodes", "undirected-edges", "directed-edges", "node-labels", "attributes-on-nodes", "attributes-on-edges"})
    void featureSurvivesRoundtrip(String slug) throws IOException {
        CjFeature feature = CjFeature.bySlug(slug);
        String path = "json/connected-json/connected-json-7.0.0/graph-format-features/" + slug + ".cj.json";
        ICjDocument cjIn = CjDocuments.parseCjJsonString(path, TestFileUtil.resource(path).getContentAsString());
        assertWithMessage("fixture %s must exhibit the feature", path).that(feature.isPresentIn(cjIn)).isTrue();

        // CJ -> OCIF text
        InMemoryOutputSink sink = new InMemoryOutputSink();
        ICjStream writeStream = new OcifWriter().createCjStream(sink);
        CjDocument2CjStream.toCjStream(cjIn, writeStream);
        String ocif = sink.getBufferAsUtf8String();

        // OCIF text -> CJ
        CjWriter2CjDocumentWriter docWriter = new CjWriter2CjDocumentWriter();
        new OcifReader().read(SingleInputSource.of(slug + ".ocif.json", ocif), new CjStream2CjWriter(docWriter, true));

        assertWithMessage("%s lost in CJ -> OCIF -> CJ; OCIF was:%n%s", slug, ocif)
                .that(feature.isPresentIn(docWriter.resultDoc())).isTrue();
    }

}
