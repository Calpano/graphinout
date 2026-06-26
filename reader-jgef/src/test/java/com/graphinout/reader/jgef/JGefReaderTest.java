package com.graphinout.reader.jgef;

import com.graphinout.base.cj.analyze.CjAnalysis;
import com.graphinout.base.cj.analyze.CjAnalyzer;
import com.graphinout.base.cj.analyze.CjFeature;
import com.graphinout.base.cj.document.ICjDocument;
import com.graphinout.base.input.SingleInputSource;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static com.google.common.truth.Truth.assertThat;

/** Unit tests for the GEF reader: each GEF leniency must round-trip through the CJ model. */
class JGefReaderTest {

    private static CjAnalysis analyze(String gef) throws IOException {
        ICjDocument doc = new JGefReader().readToCjDocument(SingleInputSource.of("t.gef.json", gef));
        return CjAnalyzer.analyze(doc);
    }

    @Test
    void graphAtRootWithSourceTarget() throws IOException {
        // top-level nodes/edges (no graphs wrapper), numeric ids, source/target shortcut
        CjAnalysis a = analyze("""
                { "nodes": [ { "id": 1 }, { "id": 2 } ],
                  "edges": [ { "source": 1, "target": 2 } ] }
                """);
        assertThat(a.nodeCount()).isEqualTo(2);
        assertThat(a.edgeCount()).isEqualTo(1);
        assertThat(a.features()).contains(CjFeature.DIRECTED_EDGES);
    }

    @Test
    void stringAndMultilangLabelShorthand() throws IOException {
        CjAnalysis a = analyze("""
                { "nodes": [
                    { "id": "a", "label": "simple label" },
                    { "id": "b", "label": [ { "language": "de", "value": "Sonne" },
                                            { "language": "en", "value": "Sun" } ] } ] }
                """);
        assertThat(a.nodeCount()).isEqualTo(2);
        assertThat(a.features()).contains(CjFeature.NODE_LABELS);
    }

    @Test
    void unknownKeysBecomeNodeData() throws IOException {
        CjAnalysis a = analyze("""
                { "nodes": [ { "id": "b", "foo": "bar", "hello": [1, 2, 3] } ] }
                """);
        assertThat(a.nodeCount()).isEqualTo(1);
        assertThat(a.features()).contains(CjFeature.ATTRIBUTES_ON_NODES);
    }

    @Test
    void hyperedgeWithMixedDirections() throws IOException {
        // n-ary endpoints incl. an undirected one — exercises hyperedges + mixed directionality
        CjAnalysis a = analyze("""
                { "nodes": [ { "id": 1 }, { "id": "a" }, { "id": "d" }, { "id": "f" } ],
                  "edges": [ { "endpoints": [
                      { "direction": "in",  "node": 1 },
                      { "direction": "in",  "node": "a" },
                      { "direction": "out", "node": "d" },
                      { "direction": "undir", "node": "f" } ] } ] }
                """);
        assertThat(a.nodeCount()).isEqualTo(4);
        assertThat(a.features()).contains(CjFeature.HYPEREDGES);
    }

    @Test
    void validConnectedJsonStillParses() throws IOException {
        // a strict CJ document (graphs wrapper) must pass through unchanged
        CjAnalysis a = analyze("""
                { "graphs": [ { "id": "g", "nodes": [ { "id": "n1" }, { "id": "n2" } ],
                    "edges": [ { "endpoints": [ { "direction": "out", "node": "n1" },
                                                { "direction": "in", "node": "n2" } ] } ] } ] }
                """);
        assertThat(a.nodeCount()).isEqualTo(2);
        assertThat(a.edgeCount()).isEqualTo(1);
    }
}
