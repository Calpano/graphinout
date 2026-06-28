package com.graphinout.base.gio;

import com.graphinout.base.cj.document.CjDocuments;
import com.graphinout.base.cj.document.ICjDocument;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static com.google.common.truth.Truth.assertThat;

/** {@link GioInputAnalyzer#graphPlausibility} should sink a scavenged graph while leaving clean graphs (incl. URIs) alone. */
class GraphPlausibilityTest {

    private static double plausibility(String cjJson) throws IOException {
        ICjDocument doc = CjDocuments.parseCjJsonString("t", cjJson);
        return GioInputAnalyzer.graphPlausibility(doc);
    }

    @Test
    void cleanConnectedGraphIsPlausible() throws IOException {
        double p = plausibility("""
                {"graphs":[{"nodes":[{"id":"a"},{"id":"b"},{"id":"c"}],
                  "edges":[{"endpoints":[{"direction":"out","node":"a"},{"direction":"in","node":"b"}]},
                           {"endpoints":[{"direction":"out","node":"b"},{"direction":"in","node":"c"}]}]}]}
                """);
        assertThat(p).isGreaterThan(0.9);
    }

    @Test
    void scavengedGraphWithStraySyntaxIsImplausible() throws IOException {
        // node ids as a line-based reader would fabricate from DOT syntax tokens
        double p = plausibility("""
                {"graphs":[{"nodes":[{"id":"a"},{"id":"[label=\\"Foo\\"];"},{"id":"{"},{"id":"--"},
                                     {"id":"[shape=box];"},{"id":"[color=blue];"}]}]}
                """);
        assertThat(p).isLessThan(0.6);
    }

    @Test
    void uriNodeIdsStayPlausible() throws IOException {
        // RDF-style URI ids are punctuation-heavy but legitimate — they must NOT be penalised
        double p = plausibility("""
                {"graphs":[{"nodes":[{"id":"http://example.org/Alice"},{"id":"http://example.org/Bob"}],
                  "edges":[{"endpoints":[{"direction":"out","node":"http://example.org/Alice"},
                                         {"direction":"in","node":"http://example.org/Bob"}]}]}]}
                """);
        assertThat(p).isGreaterThan(0.9);
    }
}
