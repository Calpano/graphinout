package com.graphinout.engine;

import com.graphinout.base.gio.GioInputAnalysis;
import com.graphinout.base.gio.GioInputAnalysis.ConfidenceTier;
import com.graphinout.base.gio.GioInputAnalysis.Outcome;
import com.graphinout.base.gio.GioInputAnalyzer;
import com.graphinout.base.input.SingleInputSource;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static com.google.common.truth.Truth.assertThat;

/**
 * Ranked-confidence detection over the full reader set: probe every reader, rank by what each recovers.
 */
class GioInputAnalyzerTest {

    private static final GioInputAnalyzer ANALYZER = new GioInputAnalyzer(new GioEngineCore().readers());

    private static final String GRAPHML = """
            <?xml version="1.0" encoding="UTF-8"?>
            <graphml xmlns="http://graphml.graphdrawing.org/xmlns">
              <graph id="G" edgedefault="directed">
                <node id="n0"/>
                <node id="n1"/>
                <edge source="n0" target="n1"/>
              </graph>
            </graphml>
            """;

    private static final String CONNECTED_JSON = """
            {"graphs":[{"id":"g","nodes":[{"id":"a"},{"id":"b"}],
              "edges":[{"endpoints":[{"direction":"out","node":"a"},{"direction":"in","node":"b"}]}]}]}
            """;

    @Test
    void detectsGraphmlAndExplainsRejections() throws IOException {
        GioInputAnalysis a = ANALYZER.analyze(SingleInputSource.of("sample.graphml", GRAPHML));

        assertThat(a.best()).isPresent();
        GioInputAnalysis.Candidate best = a.best().orElseThrow();
        assertThat(best.format().id()).isEqualTo("graphml");
        assertThat(best.outcome()).isEqualTo(Outcome.RECOVERED);
        assertThat(best.stats()).isPresent();
        assertThat(best.stats().orElseThrow().nodeCount()).isEqualTo(2);
        assertThat(best.stats().orElseThrow().edgeCount()).isEqualTo(1);
        assertThat(a.tier()).isNotEqualTo(ConfidenceTier.UNKNOWN);

        // the JSON readers cannot parse XML — they are eliminated, and we keep them with a reason
        assertThat(a.candidates().stream().anyMatch(c -> c.outcome() == Outcome.ELIMINATED)).isTrue();
        // candidates are ranked best-first
        assertThat(a.candidates().get(0).confidence()).isAtLeast(a.candidates().get(a.candidates().size() - 1).confidence());
    }

    @Test
    void detectsConnectedJson() throws IOException {
        GioInputAnalysis a = ANALYZER.analyze(SingleInputSource.of("sample.cj.json", CONNECTED_JSON));

        assertThat(a.best()).isPresent();
        GioInputAnalysis.Candidate best = a.best().orElseThrow();
        assertThat(best.format().id()).startsWith("connected-json");
        assertThat(best.outcome()).isEqualTo(Outcome.RECOVERED);
        assertThat(best.stats().orElseThrow().nodeCount()).isEqualTo(2);
    }

    @Test
    void ddotTripleSyntaxBeatsAdjacencyList() throws IOException {
        // ddot's `..` relation token must win over the greedy adjacency-list reader, with no extension hint.
        GioInputAnalysis bare = ANALYZER.analyze(SingleInputSource.of("input", "a .... b\n"));
        assertThat(bare.best()).isPresent();
        assertThat(bare.best().orElseThrow().format().id()).isEqualTo("ddot");

        GioInputAnalysis realistic = ANALYZER.analyze(
                SingleInputSource.of("input", "Alice ..knows.. Bob\nBob ..type.. Person\n"));
        assertThat(realistic.best().map(c -> c.format().id())).hasValue("ddot");
    }

    @Test
    void extensionInformsDetectionOverContentKindGuess() throws IOException {
        // A TriG graph block `{ <s> <p> <o> }` opens with '{', which naively looks like JSON — but the .trig
        // extension must keep it detected as trig, not json-ld.
        String trig = "{<http://a.example/s> <http://a.example/p> (1) .}\n";
        GioInputAnalysis a = ANALYZER.analyze(SingleInputSource.of("graph.trig", trig));
        assertThat(a.best().map(c -> c.format().id())).hasValue("trig");
    }

    @Test
    void detectsFormatByIdentityEvenWhenUnparseable() throws IOException {
        // Not well-formed XML (truncated mid-attribute) so no reader can parse it — but it is unmistakably GraphML
        // (root element + namespace) and must be detected as such, never dropped to <none> or relabelled as gexf.
        String brokenGraphml = """
                <?xml version="1.0"?>
                <graphml xmlns="http://graphml.graphdrawing.org/xmlns">
                  <graph edgedefault="directed
                """;
        GioInputAnalysis a = ANALYZER.analyze(SingleInputSource.of("truncated.graphml", brokenGraphml));
        assertThat(a.best()).isPresent();
        assertThat(a.best().orElseThrow().format().id()).isEqualTo("graphml");
    }

    @Test
    void emptyInputIsUnknown() throws IOException {
        GioInputAnalysis a = ANALYZER.analyze(SingleInputSource.of("mystery.txt", ""));

        assertThat(a.tier()).isEqualTo(ConfidenceTier.UNKNOWN);
        assertThat(a.best()).isEmpty();
        // nothing recovered a real graph: every candidate is trivial, eliminated or skipped
        assertThat(a.candidates().stream().noneMatch(c -> c.outcome() == Outcome.RECOVERED)).isTrue();
    }
}
