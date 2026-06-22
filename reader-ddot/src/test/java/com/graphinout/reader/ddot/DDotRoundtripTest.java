package com.graphinout.reader.ddot;

import com.graphinout.base.cj.document.CjDocuments;
import com.graphinout.base.cj.document.ICjDocument;
import com.graphinout.base.input.SingleInputSource;
import com.graphinout.testdata.TestFileProvider;
import io.github.classgraph.Resource;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.util.stream.Stream;

import static com.google.common.truth.Truth.assertThat;

/**
 * Roundtrip tests for the DDot reader/writer.
 * (1) DDot file -> CjDocument -> DDot string : normalized strings should match.
 * (2) CJ file -> DDot string -> CjDocument -> DDot string : the two DDot strings should match (normalized).
 */
public class DDotRoundtripTest {

    private static Stream<TestFileProvider.TestResource> ddotResources() {
        return TestFileProvider.getAllTestResources().filter(res -> res.resource().getPath().endsWith(".ddot"));
    }

    private static Stream<TestFileProvider.TestResource> cjResources() {
        return TestFileProvider.cjResourcesCanonical();
    }

    @ParameterizedTest(name = "{index}: {0}")
    @MethodSource("cjResources")
    void shouldRoundtripCjToDDotAndBackToCj(String displayPath, Resource textResource) throws IOException {
        String cjJsonIn = textResource.getContentAsString();
        ICjDocument cjDocument1 = CjDocuments.parseCjJsonString(displayPath, cjJsonIn);
        assertThat(cjDocument1).isNotNull();

        // Project arbitrary CJ into the DDot-representable domain, then assert that projection is stable:
        // ddot -> cj -> ddot -> cj keeps the exact same CJ (incl. per-link ,, metadata).
        String ddot1 = new DDotOutput(cjDocument1).toDDot();
        ICjDocument cjA = DDotReader.parseDDotToCjDocument(SingleInputSource.of(displayPath + ".1", ddot1));
        String ddot2 = new DDotOutput(cjA).toDDot();
        ICjDocument cjB = DDotReader.parseDDotToCjDocument(SingleInputSource.of(displayPath + ".2", ddot2));

        String jsonA = CjDocuments.toJsonString(cjA);
        String jsonB = CjDocuments.toJsonString(cjB);
        if (!jsonA.equals(jsonB)) {
            System.out.println("---- CJ Input:\n" + cjJsonIn);
            System.out.println("---- DDot 1:\n" + ddot1);
            System.out.println("---- DDot 2:\n" + ddot2);
        }
        assertThat(jsonB).isEqualTo(jsonA);
    }

    /**
     * Faithful DDot round-trip: {@code ddot -> cj1 -> ddot -> cj2}, asserting {@code cj1} and {@code cj2}
     * are semantically identical (canonical, sorted CJ JSON). This covers <em>everything</em> the DDot
     * model carries — node/edge ids, endpoints, types, labels, node attributes, and crucially the per-link
     * {@code ,,} metadata — so a writer that dropped any of it (e.g. link meta) would fail here.
     */
    @ParameterizedTest(name = "{index}: {0}")
    @MethodSource("ddotResources")
    void shouldRoundtripDDotToCjAndBackToDDot(String displayPath, Resource textResource) throws IOException {
        String content = textResource.getContentAsString();

        ICjDocument cjDoc1 = DDotReader.parseDDotToCjDocument(SingleInputSource.of(displayPath, content));
        assertThat(cjDoc1).isNotNull();

        String ddot2 = new DDotOutput(cjDoc1).toDDot();
        ICjDocument cjDoc2 = DDotReader.parseDDotToCjDocument(SingleInputSource.of(displayPath + ".roundtrip", ddot2));

        String json1 = CjDocuments.toJsonString(cjDoc1);
        String json2 = CjDocuments.toJsonString(cjDoc2);
        if (!json1.equals(json2)) {
            System.err.println("Round-trip mismatch for: " + textResource.getPath());
            System.err.println("---- DDot written from CJ:\n" + ddot2);
            System.err.println("---- CJ 1:\n" + json1);
            System.err.println("---- CJ 2:\n" + json2);
        }
        assertThat(json2).isEqualTo(json1);
    }
}
