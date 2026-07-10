package com.graphinout.reader.grale;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import com.graphinout.base.cj.document.ICjDocument;
import com.graphinout.base.input.SingleInputSource;
import com.graphinout.base.output.InMemoryOutputSink;
import com.graphinout.testdata.TestFileProvider;
import com.graphinout.testdata.TestFileProvider.TestResource;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.slf4j.LoggerFactory.getLogger;

/**
 * Round-trips the external grale corpus (graph-test-data {@code json/grale/grale-1.0.0/*.json} — chain,
 * tree, diamond, multigraph, compound, grid, bipartite, weighted, random DAGs, ...) through
 * grale &rarr; CJ &rarr; grale and asserts the graph structure is preserved.
 *
 * <p>The corpus lives only in the external graph-test-data checkout, so this test <b>skips</b> (rather
 * than fails) when it is not configured — set {@code -Dgraph.test.data.dir=/path/to/graph-test-data}
 * (or {@code GRAPH_TEST_DATA_DIR}). See {@link com.graphinout.testdata.TestFileProvider}.
 */
class GraleCorpusRoundTripTest {

    private static final Logger log = getLogger(GraleCorpusRoundTripTest.class);
    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    void externalCorpusRoundTripsPreservingStructure() {
        List<TestResource> corpus =
                TestFileProvider.resources("json/grale", Set.of(".json")).toList();

        Assumptions.assumeFalse(corpus.isEmpty(),
                "grale corpus not on classpath; set -Dgraph.test.data.dir to the graph-test-data checkout");

        log.info("Round-tripping {} grale corpus files", corpus.size());
        List<String> failures = new ArrayList<>();

        for (TestResource tr : corpus) {
            String name = tr.asPath();
            try (var r = tr.resource()) {
                String original = r.getContentAsString();
                JsonNode in = mapper.readTree(original);
                JsonNode out = roundTrip(name, original);
                checkStructure(name, in, out);
            } catch (Throwable t) {
                failures.add(name + " -> " + t);
            }
        }

        assertTrue(failures.isEmpty(), () -> "grale round-trip failed for:\n  " + String.join("\n  ", failures));
    }

    /** grale text -> CJ -> grale text. */
    private JsonNode roundTrip(String name, String graleText) throws Exception {
        GraleReader reader = new GraleReader();
        try (SingleInputSource in = SingleInputSource.of(name, graleText)) {
            ICjDocument doc = reader.readToCjDocument(in);
            assertNotNull(doc, "CJ document should not be null");
            InMemoryOutputSink sink = new InMemoryOutputSink();
            reader.writeCjDocument(doc, sink);
            return mapper.readTree(sink.getBufferAsUtf8String());
        }
    }

    private void checkStructure(String name, JsonNode in, JsonNode out) {
        // options preserved
        for (String opt : List.of("directed", "multigraph", "compound")) {
            assertEquals(in.path("options").path(opt).asBoolean(), out.path("options").path(opt).asBoolean(),
                    () -> name + ": options." + opt + " changed");
        }

        // node ids preserved (as a set)
        assertEquals(nodeIds(in), nodeIds(out), () -> name + ": node id set changed");

        // edges preserved as a multiset of v|w|name
        assertEquals(edgeKeys(in), edgeKeys(out), () -> name + ": edge (v,w,name) set changed");

        // compound parents preserved
        assertEquals(parents(in), parents(out), () -> name + ": node parents changed");
    }

    private Set<String> nodeIds(JsonNode g) {
        return stream(g.get("nodes")).map(n -> n.path("v").asString()).collect(Collectors.toCollection(java.util.TreeSet::new));
    }

    /** sorted multiset of "v w name" so multigraph parallel edges with distinct names are kept apart */
    private List<String> edgeKeys(JsonNode g) {
        return stream(g.get("edges"))
                .map(e -> e.path("v").asString() + " " + e.path("w").asString() + " " + e.path("name").asString(""))
                .sorted().toList();
    }

    private TreeMap<String, String> parents(JsonNode g) {
        TreeMap<String, String> m = new TreeMap<>();
        stream(g.get("nodes")).forEach(n -> {
            if (n.has("parent") && !n.get("parent").isNull()) {
                m.put(n.path("v").asString(), n.get("parent").asString());
            }
        });
        return m;
    }

    private static java.util.stream.Stream<JsonNode> stream(JsonNode array) {
        if (array == null || !array.isArray()) return java.util.stream.Stream.empty();
        return StreamSupport.stream(array.spliterator(), false);
    }
}
