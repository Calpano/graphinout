package com.graphinout.reader.neo4j;

import com.graphinout.base.cj.document.CjDocuments;
import com.graphinout.base.cj.document.ICjDocument;
import com.graphinout.base.cj.document.ICjEdge;
import com.graphinout.base.cj.document.ICjGraph;
import com.graphinout.base.cj.document.ICjLabelEntry;
import com.graphinout.base.cj.document.ICjNode;
import com.graphinout.base.cj.stream.CjStream2CjWriter;
import com.graphinout.base.cj.stream.DelegatingCjStream;
import com.graphinout.base.cj.stream.ICjStream;
import com.graphinout.base.cj.stream.ValidatingCjStream;
import com.graphinout.base.cj.writer.CjWriter2CjDocumentWriter;
import com.graphinout.base.cj.writer.CjWriter2CjStream;
import com.graphinout.base.input.SingleInputSource;
import com.graphinout.base.output.InMemoryOutputSink;
import com.graphinout.foundation.pure.input.ContentError;
import com.graphinout.reader.cj.ConnectedJsonReader;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.google.common.truth.Truth.assertThat;
import static com.google.common.truth.Truth.assertWithMessage;

/** Neo4j APOC JSON <-> CJ: the corpus, one case per mapping rule and per previously broken input. */
class Neo4jMappingTest {

    private final List<ContentError> errors = new ArrayList<>();
    private static final ObjectMapper JSON = new ObjectMapper();

    // ---- helpers ------------------------------------------------------------------------------------------------------

    /** Reads through a validating CJ stream, so the output must also be valid CJ (unique ids, resolvable endpoints). */
    private ICjDocument read(String neo4j) throws IOException {
        Neo4jReader reader = new Neo4jReader();
        reader.setContentErrorHandler(errors::add);
        CjWriter2CjDocumentWriter w = new CjWriter2CjDocumentWriter();
        ICjStream stream = new DelegatingCjStream(new ValidatingCjStream(), new CjStream2CjWriter(w, true));
        reader.read(SingleInputSource.of("test.json", neo4j), stream);
        return w.resultDoc();
    }

    private static ICjGraph graph(ICjDocument doc) {
        return doc.graphs().findFirst().orElseThrow();
    }

    private static ICjNode node(ICjDocument doc, String id) {
        return graph(doc).nodes().filter(n -> id.equals(n.id())).findFirst().orElseThrow();
    }

    private static ICjEdge onlyEdge(ICjDocument doc) {
        List<ICjEdge> edges = graph(doc).edges().toList();
        assertThat(edges).hasSize(1);
        return edges.getFirst();
    }

    private static List<String> labels(ICjNode n) {
        return n.label() == null ? List.of() : n.label().entries().map(ICjLabelEntry::value).toList();
    }

    private static JsonNode data(ICjNode n) {
        return JSON.readTree(n.data().jsonValue().toJsonString());
    }

    private List<String> messages(ContentError.ErrorLevel level) {
        return errors.stream().filter(e -> e.getLevel() == level).map(ContentError::getMessage).toList();
    }

    private static final String NODES_AB = """
            {"type":"node","id":"a","labels":["P"]}
            {"type":"node","id":"b","labels":["P"]}
            """;

    // ---- corpus -------------------------------------------------------------------------------------------------------

    /** Expected counts were computed independently from the raw JSON (every node/relationship object, anywhere). */
    @ParameterizedTest(name = "{0}")
    @CsvSource({
            "apoc-official/all.json, 3, 1", "apoc-official/all_array.json, 3, 1", "apoc-official/all_fields.json, 3, 1",
            "apoc-official/all_id_as_keys.json, 3, 1", "apoc-official/all_withNodeProps_withRelProps.json, 3, 1",
            "apoc-official/all_withNodeProps_withoutRelProps.json, 3, 1", "apoc-official/all_withoutNodeProps_withRelProps.json, 3, 1",
            "apoc-official/data.json, 3, 1", "apoc-official/data_withNodeProps_withRelProps.json, 3, 1",
            "apoc-official/data_withNodeProps_withoutRelProps.json, 3, 1", "apoc-official/data_withoutNodeProps_withRelProps.json, 3, 1",
            "apoc-official/graph.json, 3, 1", "apoc-official/listPath.json, 2, 1", "apoc-official/MapPath.json, 4, 2",
            "apoc-official/multiLabels.json, 16, 4", "apoc-official/query_nodes_path.json, 2, 1",
            "apoc-wild/brain-annex-schema.json, 69, 65", "apoc-wild/entity-resolution.json, 481, 562", "apoc-wild/lesmis.json, 77, 254",
            "apoc-wild/marie-curie.json, 5, 5", "apoc-wild/memgraph-party-members.json, 149, 137", "apoc-wild/sociotechnical-workflow.json, 28, 54",
            "apoc-wild/speedracer.json, 2, 1", "apoc-wild/stellarium-exploration.json, 10, 9", "apoc-wild/tehran-metro.json, 150, 324",
            "neo4j-sample.json, 2, 1", "neo4j-relationship-with-node-properties.json, 2, 1"})
    void corpusSampleIsReadCompletely(String resource, int nodes, int relationships) throws IOException {
        String content;
        try (InputStream in = Objects.requireNonNull(getClass().getResourceAsStream("/" + resource), resource)) {
            content = new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
        ICjDocument doc = read(content);
        assertWithMessage("nodes").that(graph(doc).nodes().count()).isEqualTo(nodes);
        assertWithMessage("relationships").that(graph(doc).edges().count()).isEqualTo(relationships);
        assertWithMessage("errors").that(messages(ContentError.ErrorLevel.Error)).isEmpty();
    }

    // ---- reading ------------------------------------------------------------------------------------------------------

    @Test
    void relationshipStartIsSourceEndIsTarget() throws IOException {
        ICjEdge e = onlyEdge(read(NODES_AB + """
                {"type":"relationship","id":"r","label":"KNOWS","start":{"id":"a"},"end":{"id":"b"}}"""));
        assertThat(e.source().node()).isEqualTo("a");
        assertThat(e.target().node()).isEqualTo("b");
        assertThat(e.label().entries().map(ICjLabelEntry::value).toList()).containsExactly("KNOWS");
    }

    @Test
    void propertyTypesKeepTheirStructure() throws IOException {
        ICjDocument doc = read("""
                {"type":"node","id":"a","labels":["U"],"properties":{"big":5000000000,"huge":123456789012345678901234567890,
                 "f":33.46789,"inf":1e400,"kids":["Sam","Anna"],"nested":[[1,2],[3]],
                 "place":{"crs":"wgs-84","latitude":13.1,"longitude":33.46789,"height":null},"gone":null,"ok":true}}""");
        JsonNode data = data(node(doc, "a"));
        assertThat(data.get("big").asString()).isEqualTo("5000000000");
        assertThat(data.get("huge").asString()).isEqualTo("123456789012345678901234567890");
        assertThat(data.get("f").decimalValue().toPlainString()).isEqualTo("33.46789");
        assertThat(data.get("inf").decimalValue().compareTo(new java.math.BigDecimal("1e400"))).isEqualTo(0); // no Infinity
        assertThat(data.get("kids").toString()).isEqualTo("[\"Sam\",\"Anna\"]");
        assertThat(data.get("nested").toString()).isEqualTo("[[1,2],[3]]");
        assertThat(data.get("place").get("crs").asString()).isEqualTo("wgs-84");
        assertThat(data.has("gone")).isFalse();
        assertThat(errors).isEmpty();
    }

    @Test
    void jsonAndIdAsKeysWrappersAndArrays() throws IOException {
        assertThat(graph(read("""
                {"nodes":[{"type":"node","id":"a"},{"type":"node","id":"b"}],
                 "rels":[{"type":"relationship","id":"0","label":"R","start":{"id":"a"},"end":{"id":"b"}}]}""")).edges().count()).isEqualTo(1);
        assertThat(graph(read("""
                {"nodes":{"a":{"type":"node","id":"a"},"b":{"type":"node","id":"b"}},
                 "rels":{"0":{"type":"relationship","id":"0","label":"R","start":{"id":"a"},"end":{"id":"b"}}}}""")).nodes().count()).isEqualTo(2);
        assertThat(graph(read("""
                [{"type":"node","id":"a"},
                 {"type":"node","id":"b"}]""")).nodes().count()).isEqualTo(2);
    }

    @Test
    void queryRowsAreSearchedRecursivelyAndRepeatedNodesReadOnce() throws IOException {
        ICjDocument doc = read("""
                {"u":{"type":"node","id":"1","labels":["U"]},"rel":{"type":"relationship","id":"9","label":"R","start":{"id":"1"},"end":{"id":"2"}},"u2":{"type":"node","id":"2"}}
                {"p":{"length":1,"rels":[{"type":"relationship","id":"9","label":"R","start":{"id":"1"},"end":{"id":"2"}}],"nodes":[{"type":"node","id":"1","labels":["U"]},{"type":"node","id":"2"}]}}""");
        assertThat(graph(doc).nodes().count()).isEqualTo(2);
        assertThat(graph(doc).edges().count()).isEqualTo(1);
        assertThat(errors).isEmpty();
    }

    @Test
    void memgraphScalarStartAndEnd() throws IOException {
        ICjEdge e = onlyEdge(read("""
                {"e":{"id":3,"start":1,"end":2,"label":"MEMBER_OF","properties":{},"type":"relationship"},"p":{"id":2,"labels":["Party"],"type":"node"},"u":{"id":1,"labels":["User"],"type":"node"}}"""));
        assertThat(e.source().node()).isEqualTo("1");
        assertThat(e.target().node()).isEqualTo("2");
    }

    @Test
    void relationshipIdsThatCollideWithNodeIdsArePrefixed() throws IOException {
        ICjDocument doc = read("""
                {"type":"node","id":"0"}
                {"type":"node","id":"1"}
                {"type":"relationship","id":"0","label":"R","start":{"id":"0"},"end":{"id":"1"}}""");
        assertThat(onlyEdge(doc).id()).isEqualTo("rel:0");
        assertThat(messages(ContentError.ErrorLevel.Warn)).hasSize(1);
    }

    @Test
    void undeclaredEndpointsBecomeNodesFromTheReference() throws IOException {
        ICjDocument doc = read("""
                {"type":"relationship","id":"r","label":"R","start":{"id":"a","labels":["User"],"properties":{"name":"Adam"}},"end":{"id":"b"}}""");
        assertThat(labels(node(doc, "a"))).containsExactly("User");
        assertThat(data(node(doc, "a")).get("name").asString()).isEqualTo("Adam");
        assertThat(graph(doc).nodes().count()).isEqualTo(2);
        assertThat(messages(ContentError.ErrorLevel.Warn)).hasSize(1);
    }

    @Test
    void malformedLinesAreReportedAndReadingContinues() throws IOException {
        ICjDocument doc = read("""
                {"type":"node","id":"a"}
                this is not json
                {"type":"node","id":"b"
                {"type":"node","id":"c"}""");
        assertThat(graph(doc).nodes().map(ICjNode::id).toList()).containsExactly("a", "c");
        List<String> errs = messages(ContentError.ErrorLevel.Error);
        assertThat(errs).hasSize(2);
        assertThat(errors.stream().filter(e -> e.getLevel() == ContentError.ErrorLevel.Error).map(e -> e.getLocation().line()).toList())
                .containsExactly(2, 3).inOrder();
    }

    @Test
    void prettyPrintedValuesSpanLines() throws IOException {
        ICjDocument doc = read("""
                {
                  "type": "node",
                  "id": "a",
                  "labels": ["X"]
                }
                {"type":"node","id":"b"}""");
        assertThat(graph(doc).nodes().count()).isEqualTo(2);
        assertThat(errors).isEmpty();
    }

    @Test
    void wrongTypesAreReportedNotCrashingOrHanging() throws IOException {
        ICjDocument doc = read("""
                {"type":"node","id":"a","labels":"Person"}
                {"type":"node","id":"b","labels":null,"properties":null}
                {"type":"node","id":"c","labels":["X",1,null],"properties":[1,2]}
                {"type":"node","id":{"x":1},"labels":["Lost"]}
                {"type":"relationship","id":"r1","label":["R"],"start":null,"end":{"id":"a"}}
                {"type":"relationship","id":"r2","label":"R","start":"a","end":"b","properties":"oops"}""");
        assertThat(labels(node(doc, "a"))).containsExactly("Person");
        assertThat(labels(node(doc, "c"))).containsExactly("X");
        assertThat(graph(doc).nodes().filter(n -> n.id() == null).count()).isEqualTo(1); // the object id: reported, not "{"
        assertThat(graph(doc).nodes().noneMatch(n -> "null".equals(n.id()) || "{".equals(n.id()))).isTrue();
        assertThat(onlyEdge(doc).id()).isEqualTo("r2");
        assertThat(messages(ContentError.ErrorLevel.Error)).isNotEmpty();
    }

    @Test
    void duplicateNodeWithDifferentContentIsReported() throws IOException {
        ICjDocument doc = read("""
                {"type":"node","id":"a","labels":["X"]}
                {"type":"node","id":"a","labels":["Y"]}""");
        assertThat(labels(node(doc, "a"))).containsExactly("X");
        assertThat(messages(ContentError.ErrorLevel.Warn)).hasSize(1);
    }

    @Test
    void unknownShapesAreReported() throws IOException {
        read("""
                {"type":"NODE","id":"a"}
                {"type":"path","foo":1}
                42
                {"graphs":[]}""");
        assertThat(messages(ContentError.ErrorLevel.Warn)).hasSize(4); // uppercase type, no node found x2, scalar
    }

    @Test
    void emptyInputGivesAnEmptyGraph() throws IOException {
        ICjDocument doc = read("\n\n");
        assertThat(graph(doc).nodes().count()).isEqualTo(0);
        assertThat(errors).isEmpty();
    }

    // ---- writing ------------------------------------------------------------------------------------------------------

    private List<JsonNode> write(String cjJson) throws IOException {
        CjWriter2CjDocumentWriter w = new CjWriter2CjDocumentWriter();
        new ConnectedJsonReader().read(SingleInputSource.of("in.cj.json", cjJson), new CjStream2CjWriter(w, true));
        Neo4jReader neo4j = new Neo4jReader();
        neo4j.setContentErrorHandler(errors::add);
        InMemoryOutputSink sink = InMemoryOutputSink.create();
        w.resultDoc().fire(new CjWriter2CjStream(neo4j.createCjStream(sink)), false);
        List<JsonNode> lines = new ArrayList<>();
        for (String line : sink.getBufferAsUtf8String().split("\n")) if (!line.isBlank()) lines.add(JSON.readTree(line));
        return lines;
    }

    private static JsonNode rel(List<JsonNode> lines) {
        return lines.stream().filter(l -> l.get("type").asString().equals("relationship")).findFirst().orElseThrow();
    }

    @Test
    void writerUsesApocFieldOrderAndStartEndLabels() throws IOException {
        List<JsonNode> lines = write("""
                {"graphs":[{"nodes":[{"id":"a","label":{"entries":[{"value":"User"}]}},{"id":"b"}],
                  "edges":[{"id":"e","label":{"entries":[{"value":"KNOWS"}]},"endpoints":[{"node":"a","direction":"in"},{"node":"b","direction":"out"}]}]}]}""");
        assertThat(lines.get(0).propertyNames()).containsExactly("type", "id", "labels").inOrder();
        JsonNode rel = rel(lines);
        assertThat(rel.propertyNames()).containsExactly("id", "type", "label", "start", "end").inOrder();
        assertThat(rel.at("/start/id").asString()).isEqualTo("a");
        assertThat(rel.at("/start/labels/0").asString()).isEqualTo("User");
        assertThat(rel.at("/end/id").asString()).isEqualTo("b");
    }

    @Test
    void writerWritesUndirectedAndMixedEdgesWithStartAndEnd() throws IOException {
        JsonNode rel = rel(write("""
                {"graphs":[{"nodes":[{"id":"a"},{"id":"b"}],"edges":[{"endpoints":[{"node":"a"},{"node":"b"}]}]}]}"""));
        assertThat(rel.at("/start/id").asString()).isEqualTo("a");
        assertThat(rel.at("/end/id").asString()).isEqualTo("b");
        assertThat(rel.get("label").asString()).isEqualTo(Neo4jWriter.DEFAULT_RELATIONSHIP_TYPE);
        assertThat(messages(ContentError.ErrorLevel.Warn)).hasSize(2); // undirected, no type
    }

    @Test
    void writerReportsHyperedgesAndSkipsEdgesWithOneEndpointOrToAnEdge() throws IOException {
        List<JsonNode> lines = write("""
                {"graphs":[{"nodes":[{"id":"a"},{"id":"b"},{"id":"c"}],"edges":[
                  {"id":"h","label":{"entries":[{"value":"H"}]},"endpoints":[{"node":"a","direction":"in"},{"node":"c","direction":"in"},{"node":"b","direction":"out"}]},
                  {"id":"one","label":{"entries":[{"value":"O"}]},"endpoints":[{"node":"a","direction":"in"}]},
                  {"id":"meta","label":{"entries":[{"value":"M"}]},"endpoints":[{"node":"a","direction":"in"},{"node":"h","direction":"out"}]}]}]}""");
        assertThat(lines.stream().filter(l -> l.get("type").asString().equals("relationship")).map(l -> l.get("id").asString()).toList())
                .containsExactly("h");
        assertThat(messages(ContentError.ErrorLevel.Error)).hasSize(2);
        assertThat(messages(ContentError.ErrorLevel.Warn)).hasSize(1);
    }

    @Test
    void writerKeepsListsAndPointsAndStringifiesMaps() throws IOException {
        JsonNode node = write("""
                {"graphs":[{"nodes":[{"id":"a","data":{"list":[1,2,3],"place":{"crs":"cartesian","x":1,"y":2},"map":{"k":"v"},"nested":[[1]]}}]}]}""").getFirst();
        assertThat(node.at("/properties/list").toString()).isEqualTo("[1,2,3]");
        assertThat(node.at("/properties/place/crs").asString()).isEqualTo("cartesian");
        assertThat(node.at("/properties/map").asString()).isEqualTo("{\"k\":\"v\"}");
        assertThat(node.at("/properties/nested").asString()).isEqualTo("[[1]]");
        assertThat(messages(ContentError.ErrorLevel.Warn)).hasSize(2);
    }

    @Test
    void writerGeneratesNonCollidingIdsAndReportsDroppedData() throws IOException {
        List<JsonNode> lines = write("""
                {"graphs":[{"nodes":[{"id":"node_0","data":"just a string"},{}]}]}""");
        assertThat(lines.get(1).get("id").asString()).isEqualTo("node_1");
        assertThat(messages(ContentError.ErrorLevel.Warn)).hasSize(2); // generated id, non-object data
    }

    @Test
    void writerPrefersLanguageFreeLabels() throws IOException {
        JsonNode node = write("""
                {"graphs":[{"nodes":[{"id":"a","label":{"entries":[{"value":"Milan"},{"value":"Mailand","language":"de"}]}}]}]}""").getFirst();
        assertThat(node.get("labels").toString()).isEqualTo("[\"Milan\"]");
        assertThat(messages(ContentError.ErrorLevel.Warn)).hasSize(1);
    }

    // ---- round trip ---------------------------------------------------------------------------------------------------

    @Test
    void roundTripKeepsIdsDirectionLabelsAndPropertiesExactly() throws IOException {
        String in = """
                {"type":"node","id":"0","labels":["User"],"properties":{"age":42,"big":5000000000,"kids":["Sam","Anna"],"place":{"crs":"wgs-84","latitude":13.1,"longitude":33.46789}}}
                {"type":"node","id":"1","labels":["User","Admin"],"properties":{"name":"Jim"}}
                {"id":"0","type":"relationship","label":"KNOWS","properties":{"since":1993},"start":{"id":"0","labels":["User"]},"end":{"id":"1","labels":["Admin","User"]}}
                """;
        Neo4jReader reader = new Neo4jReader();
        CjWriter2CjDocumentWriter w = new CjWriter2CjDocumentWriter();
        reader.read(SingleInputSource.of("in.json", in), new CjStream2CjWriter(w, true));
        InMemoryOutputSink sink = InMemoryOutputSink.create();
        w.resultDoc().fire(new CjWriter2CjStream(reader.createCjStream(sink)), false);
        assertThat(normalized(sink.getBufferAsUtf8String())).isEqualTo(normalized(in));
        assertThat(CjDocuments.toJsonString(w.resultDoc())).contains("\"rel:0\"");
    }

    /** One JSON tree per line, sorted; labels sorted (Neo4j and CJ labels are sets). Object equality ignores key order. */
    private static List<String> normalized(String jsonLines) {
        List<String> out = new ArrayList<>();
        for (String line : jsonLines.split("\n")) {
            if (line.isBlank()) continue;
            tools.jackson.databind.node.ObjectNode o = (tools.jackson.databind.node.ObjectNode) JSON.readTree(line);
            sortLabels(o);
            if (o.get("start") instanceof tools.jackson.databind.node.ObjectNode s) sortLabels(s);
            if (o.get("end") instanceof tools.jackson.databind.node.ObjectNode e) sortLabels(e);
            out.add(JSON.writeValueAsString(JSON.treeToValue(o, java.util.TreeMap.class)));
        }
        out.sort(null);
        return out;
    }

    private static void sortLabels(tools.jackson.databind.node.ObjectNode o) {
        if (!(o.get("labels") instanceof tools.jackson.databind.node.ArrayNode labels)) return;
        List<String> sorted = new ArrayList<>();
        labels.forEach(l -> sorted.add(l.asString()));
        sorted.sort(null);
        tools.jackson.databind.node.ArrayNode a = o.putArray("labels");
        sorted.forEach(a::add);
    }
}
