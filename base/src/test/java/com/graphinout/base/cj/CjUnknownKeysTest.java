package com.graphinout.base.cj;

import com.graphinout.base.cj.document.CjDocuments;
import com.graphinout.base.cj.document.ICjDocument;
import com.graphinout.foundation.pure.input.ContentError;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static com.google.common.truth.Truth.assertThat;

/** Unknown keys are skipped with their whole value, and reported with their path. */
class CjUnknownKeysTest {

    @ParameterizedTest
    @CsvSource(delimiter = '|', textBlock = """
            {"foo":1,"graphs":[{"nodes":[{"id":"n"}]}]}                                      | foo
            {"graphs":[{"baseUri":"x","nodes":[{"id":"n"}]}]}                                | graphs[0].baseUri
            {"graphs":[{"nodes":[{"id":"m"},{"id":"n","foo":1}]}]}                           | graphs[0].nodes[1].foo
            {"graphs":[{"nodes":[{"id":"n"}],"edges":[{"foo":1,"endpoints":[{"node":"n"}]}]}]} | graphs[0].edges[0].foo
            {"graphs":[{"nodes":[{"id":"n"}],"edges":[{"endpoints":[{"node":"n","foo":1}]}]}]} | graphs[0].edges[0].endpoints[0].foo
            {"graphs":[{"nodes":[{"id":"n","label":{"entries":[{"value":"v","foo":1}]}}]}]}  | graphs[0].nodes[0].label.entries[0].foo
            """)
    void warnsWithPath(String json, String expectedPath) throws IOException {
        List<ContentError> errors = new ArrayList<>();
        ICjDocument doc = CjDocuments.parseCjJsonString("test", json, errors::add);
        assertThat(errors).hasSize(1);
        assertThat(errors.get(0).getLevel()).isEqualTo(ContentError.ErrorLevel.Warn);
        assertThat(errors.get(0).getMessage()).contains(" at " + expectedPath + " ");
        assertThat(CjDocuments.toJsonString(doc)).doesNotContain("foo");
    }

    @ParameterizedTest
    @CsvSource(delimiter = '|', textBlock = """
            {"graphs":[{"baseUri":"x","nodes":[{"id":"n","data":{"a":1}}]}]}
            {"graphs":[{"baseUri":"x","nodes":[{"id":"n","data":"s"}]}]}
            {"graphs":[{"nodes":[{"id":"n","foo":1,"data":{"a":1}}]}]}
            {"graphs":[{"nodes":[{"id":"n","foo":{"a":1},"data":{"a":1}}]}]}
            {"graphs":[{"nodes":[{"id":"n","foo":[1,{"b":[2,{}]}],"data":{"a":1}}]}]}
            {"graphs":[{"foo":{"x":{"y":[]}},"nodes":[{"id":"n","data":{"a":1}}]}]}
            """)
    void skipsWholeValueAndKeepsData(String json) throws IOException {
        List<ContentError> errors = new ArrayList<>();
        ICjDocument doc = CjDocuments.parseCjJsonString("test", json, errors::add);
        assertThat(errors).hasSize(1);
        String out = CjDocuments.toJsonString(doc);
        assertThat(out).contains("\"id\":\"n\"");
        assertThat(out).contains("\"data\"");
        assertThat(out).doesNotContain("foo");
        assertThat(out).doesNotContain("baseUri");
    }

    /** Has a graph-level {@code baseUri} (not CJ 8) and node {@code data}; failed with EmptyStackException before. */
    @Test
    void rdfExampleWithBaseUriReads() throws IOException {
        String json = Files.readString(Path.of("../reader-rdf/src/doc/example-cj-with-all-features.cj.json"));
        List<ContentError> errors = new ArrayList<>();
        ICjDocument doc = CjDocuments.parseCjJsonString("example", json, errors::add);
        assertThat(errors.stream().map(ContentError::getMessage).toList()).contains("Unknown property 'baseUri' at graphs[0].baseUri (CJ Graph) — value ignored");
        assertThat(doc.nodesAll().count()).isGreaterThan(0L);
    }
}
