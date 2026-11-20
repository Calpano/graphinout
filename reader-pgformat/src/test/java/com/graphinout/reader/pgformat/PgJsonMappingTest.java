package com.graphinout.reader.pgformat;

import com.graphinout.base.cj.document.ICjDocument;
import com.graphinout.base.cj.document.ICjEdge;
import com.graphinout.base.cj.document.ICjEndpoint;
import com.graphinout.base.cj.document.ICjGraph;
import com.graphinout.base.cj.document.ICjLabelEntry;
import com.graphinout.base.cj.document.ICjNode;
import com.graphinout.base.cj.stream.CjStream2CjWriter;
import com.graphinout.base.cj.writer.CjWriter2CjDocumentWriter;
import com.graphinout.base.cj.writer.CjWriter2CjStream;
import com.graphinout.base.input.SingleInputSource;
import com.graphinout.base.output.InMemoryOutputSink;
import com.graphinout.foundation.pure.input.ContentError;
import com.graphinout.reader.cj.ConnectedJsonReader;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

/** PG-JSON <-> CJ mapping, one case per rule and per previously broken input. */
class PgJsonMappingTest {

    private final List<ContentError> errors = new ArrayList<>();

    private static final String AB = """
            {"id":"a","labels":[],"properties":{}},{"id":"b","labels":[],"properties":{}}""";

    // ---- reading -------------------------------------------------------------------------------------------------

    private ICjGraph read(String pgJson) throws IOException {
        PgJsonReader reader = new PgJsonReader();
        reader.setContentErrorHandler(errors::add);
        CjWriter2CjDocumentWriter w = new CjWriter2CjDocumentWriter();
        reader.read(SingleInputSource.of("test.pg.json", pgJson), new CjStream2CjWriter(w, true));
        return w.resultDoc().graphs().findFirst().orElseThrow();
    }

    private static ICjNode node(ICjGraph g, String id) {
        return g.nodes().filter(n -> id.equals(n.id())).findFirst().orElseThrow();
    }

    private static ICjEdge onlyEdge(ICjGraph g) {
        List<ICjEdge> edges = g.edges().toList();
        assertThat(edges).hasSize(1);
        return edges.getFirst();
    }

    private static List<String> labels(ICjNode n) {
        return n.label() == null ? List.of() : n.label().entries().map(ICjLabelEntry::value).toList();
    }

    private static List<String> labels(ICjEdge e) {
        return e.label() == null ? List.of() : e.label().entries().map(ICjLabelEntry::value).toList();
    }

    /** Node data as a Jackson tree: object equality ignores key order (CJ facts are unordered). */
    private static JsonNode data(ICjNode n) {
        return n.data().jsonValue() == null ? null : json(n.data().jsonValue().toJsonString());
    }

    private static JsonNode json(String s) {
        return new ObjectMapper().readTree(s);
    }

    private List<String> messages() {
        return errors.stream().map(e -> e.getLevel() + ": " + e.getMessage()).toList();
    }

    @Test
    void directedEdgeFromIsSourceToIsTarget() throws IOException {
        ICjEdge e = onlyEdge(read("""
                {"nodes":[%s],"edges":[{"from":"a","to":"b","labels":[],"properties":{}}]}""".formatted(AB)));
        // CJ direction is seen from the edge: source = direction "in", target = direction "out"
        assertThat(e.sources().stream().map(ICjEndpoint::node).toList()).containsExactly("a");
        assertThat(e.targets().stream().map(ICjEndpoint::node).toList()).containsExactly("b");
        assertThat(errors).isEmpty();
    }

    @Test
    void undirectedEdge() throws IOException {
        ICjEdge e = onlyEdge(read("""
                {"nodes":[%s],"edges":[{"from":"a","to":"b","undirected":true,"labels":[],"properties":{}}]}""".formatted(AB)));
        assertThat(e.undirectedEndpoints().stream().map(ICjEndpoint::node).toList()).containsExactly("a", "b");
        assertThat(e.sources()).isEmpty();
    }

    @Test
    void allEdgeLabelsAreKept() throws IOException {
        ICjEdge e = onlyEdge(read("""
                {"nodes":[%s],"edges":[{"id":"e1","from":"a","to":"b","labels":["x","y"],"properties":{}}]}""".formatted(AB)));
        assertThat(labels(e)).containsExactly("x", "y").inOrder();
        assertThat(e.id()).isEqualTo("e1");
    }

    @Test
    void allNodeLabelsAreKept() throws IOException {
        ICjGraph g = read("""
                {"nodes":[{"id":"a","labels":["person","student"],"properties":{}}],"edges":[]}""");
        assertThat(labels(node(g, "a"))).containsExactly("person", "student").inOrder();
    }

    @Test
    void singleValuePropertyIsScalarMultiValueIsArray() throws IOException {
        ICjGraph g = read("""
                {"nodes":[{"id":"a","labels":[],"properties":{"name":["Alice"],"tag":["t1","t2","t3"],"ok":[true]}}],"edges":[]}""");
        assertThat(data(node(g, "a"))).isEqualTo(json("{\"name\":\"Alice\",\"tag\":[\"t1\",\"t2\",\"t3\"],\"ok\":true}"));
    }

    @Test
    void largeAndPreciseNumbers() throws IOException {
        ICjGraph g = read("""
                {"nodes":[{"id":"a","labels":[],"properties":{"big":[3000000000],"huge":[123456789012345678901234567890],"f":[1.5]}}],"edges":[]}""");
        String data = data(node(g, "a")).toString();
        assertThat(data).contains("\"big\":3000000000");
        assertThat(data).contains("\"huge\":123456789012345678901234567890");
        assertThat(data).contains("\"f\":1.5");
    }

    @Test
    void edgesBeforeNodes() throws IOException {
        ICjGraph g = read("""
                {"edges":[{"from":"a","to":"b","labels":["r"],"properties":{}}],"nodes":[%s]}""".formatted(AB));
        assertThat(g.nodes().count()).isEqualTo(2);
        assertThat(onlyEdge(g).sources().getFirst().node()).isEqualTo("a");
        assertThat(errors).isEmpty();
    }

    @Test
    void scalarPropertyIsReadAsOneValueAndDoesNotSwallowTheNextProperty() throws IOException {
        ICjGraph g = read("""
                {"nodes":[{"id":"a","labels":[],"properties":{"name":"Alice","after":["x"]}}],"edges":[]}""");
        assertThat(data(node(g, "a"))).isEqualTo(json("{\"name\":\"Alice\",\"after\":\"x\"}"));
        assertThat(messages()).containsExactly("Warn: Property 'name' should be an array of values");
    }

    @Test
    void nestedValuesAreSkippedWithoutLosingTheRest() throws IOException {
        ICjGraph g = read("""
                {"nodes":[{"id":"a","labels":[],"properties":{"o":[{"k":1}],"arr":[[1,2]],"after":["still here"]}}],"edges":[]}""");
        assertThat(data(node(g, "a"))).isEqualTo(json("{\"after\":\"still here\"}"));
        assertThat(errors).hasSize(2);
    }

    @Test
    void nullValuesAreSkipped() throws IOException {
        ICjGraph g = read("""
                {"nodes":[{"id":"a","labels":[],"properties":{"x":[null,"second"]}}],"edges":[]}""");
        assertThat(data(node(g, "a"))).isEqualTo(json("{\"x\":\"second\"}"));
    }

    @Test
    void nullOrMissingNodeIdIsAnErrorNotTheStringNull() throws IOException {
        ICjGraph g = read("""
                {"nodes":[{"id":null,"labels":[],"properties":{}}],"edges":[]}""");
        assertThat(g.nodes().toList().getFirst().id()).isNull();
        assertThat(messages()).containsExactly("Error: Node without 'id' (mandatory)");
    }

    @Test
    void numericIdIsAcceptedWithWarning() throws IOException {
        ICjGraph g = read("""
                {"nodes":[{"id":101,"labels":[],"properties":{}}],"edges":[]}""");
        assertThat(node(g, "101")).isNotNull();
        assertThat(errors).hasSize(1);
    }

    @Test
    void edgeWithoutFromIsSkipped() throws IOException {
        ICjGraph g = read("""
                {"nodes":[%s],"edges":[{"to":"b","labels":[],"properties":{}}]}""".formatted(AB));
        assertThat(g.edges().count()).isEqualTo(0);
        assertThat(messages()).containsExactly("Error: Edge without 'from' (mandatory); edge skipped");
    }

    @Test
    void danglingEdgeAndDuplicateIdAreReported() throws IOException {
        read("""
                {"nodes":[{"id":"a","labels":[],"properties":{}},{"id":"a","labels":[],"properties":{}}],
                 "edges":[{"from":"a","to":"zzz","labels":[],"properties":{}}]}""");
        assertThat(messages()).containsExactly("Warn: Duplicate node id 'a'", "Warn: Edge references unknown node 'zzz'");
    }

    @Test
    void nonBooleanUndirectedIsAnErrorAndReadAsDirected() throws IOException {
        ICjEdge e = onlyEdge(read("""
                {"nodes":[%s],"edges":[{"from":"a","to":"b","undirected":"true","labels":[],"properties":{}}]}""".formatted(AB)));
        assertThat(e.sources()).hasSize(1);
        assertThat(errors).hasSize(1);
    }

    @Test
    void errorsCarryLineAndColumn() throws IOException {
        read("{\"nodes\":[],\n \"edges\":[{\"to\":\"b\",\"labels\":[],\"properties\":{}}]}");
        assertThat(errors.getFirst().getLocation().line()).isEqualTo(2);
    }

    @Test
    void malformedJsonIsAnIOExceptionWithPosition() {
        IOException e = assertThrows(IOException.class, () -> read("{\"nodes\":[{\"id\":\"a\""));
        assertThat(e).hasMessageThat().startsWith("Invalid PG-JSON at line 1");
    }

    @Test
    void nonObjectRootIsRejected() {
        assertThrows(IOException.class, () -> read("[]"));
        assertThrows(IOException.class, () -> read(""));
    }

    // ---- writing -------------------------------------------------------------------------------------------------

    private JsonNode write(String cjJson) throws IOException {
        CjWriter2CjDocumentWriter w = new CjWriter2CjDocumentWriter();
        new ConnectedJsonReader().read(SingleInputSource.of("in.cj.json", cjJson), new CjStream2CjWriter(w, true));
        ICjDocument doc = w.resultDoc();
        PgJsonReader pg = new PgJsonReader();
        pg.setContentErrorHandler(errors::add);
        InMemoryOutputSink sink = InMemoryOutputSink.create();
        doc.fire(new CjWriter2CjStream(pg.createCjStream(sink)), false);
        return new ObjectMapper().readTree(sink.getBufferAsUtf8String());
    }

    @Test
    void writerSourceIsFromTargetIsTo() throws IOException {
        JsonNode pg = write("""
                {"graphs":[{"nodes":[{"id":"a"},{"id":"b"}],"edges":[{"endpoints":[{"node":"a","direction":"in"},{"node":"b","direction":"out"}]}]}]}""");
        assertThat(pg.at("/edges/0/from").asString()).isEqualTo("a");
        assertThat(pg.at("/edges/0/to").asString()).isEqualTo("b");
    }

    @Test
    void writerKeepsEmptyLabelsEmpty() throws IOException {
        JsonNode pg = write("""
                {"graphs":[{"nodes":[{"id":"a"},{"id":"b"}],"edges":[{"endpoints":[{"node":"a"},{"node":"b"}]}]}]}""");
        assertThat(pg.at("/nodes/0/labels").size()).isEqualTo(0);
        assertThat(pg.at("/edges/0/labels").size()).isEqualTo(0);
        assertThat(pg.at("/edges/0/undirected").asBoolean()).isTrue();
    }

    @Test
    void writerMapsArraysToMultipleValues() throws IOException {
        JsonNode pg = write("""
                {"graphs":[{"nodes":[{"id":"a","data":{"tags":["x","y"],"n":1,"addr":{"city":"Berlin"}}}]}]}""");
        assertThat(pg.at("/nodes/0/properties/tags").toString()).isEqualTo("[\"x\",\"y\"]");
        assertThat(pg.at("/nodes/0/properties/n").toString()).isEqualTo("[1]");
        assertThat(pg.at("/nodes/0/properties/addr/0").asString()).isEqualTo("{\"city\":\"Berlin\"}");
        assertThat(messages()).containsExactly("Warn: node a: property 'addr' holds nested JSON; written as a JSON string");
    }

    @Test
    void writerGeneratesNonCollidingNodeIds() throws IOException {
        JsonNode pg = write("""
                {"graphs":[{"nodes":[{"id":"node_0"},{}]}]}""");
        assertThat(pg.at("/nodes/0/id").asString()).isEqualTo("node_0");
        assertThat(pg.at("/nodes/1/id").asString()).isEqualTo("node_1");
    }

    @Test
    void writerSkipsEdgeWithOneEndpointAndReportsIt() throws IOException {
        JsonNode pg = write("""
                {"graphs":[{"nodes":[{"id":"a"}],"edges":[{"endpoints":[{"node":"a","direction":"in"}]}]}]}""");
        assertThat(pg.at("/edges").size()).isEqualTo(0);
        assertThat(errors).hasSize(1);
    }

    @Test
    void writerReportsHyperedgeAndMergedGraphs() throws IOException {
        JsonNode pg = write("""
                {"graphs":[{"nodes":[{"id":"a"},{"id":"b"},{"id":"c"}],
                  "edges":[{"endpoints":[{"node":"a","direction":"in"},{"node":"b","direction":"out"},{"node":"c","direction":"out"}]}]},
                 {"nodes":[{"id":"d"}]}]}""");
        assertThat(pg.at("/edges/0/from").asString()).isEqualTo("a");
        assertThat(pg.at("/nodes").size()).isEqualTo(4);
        assertThat(errors).hasSize(2);
    }

    // ---- round trip ----------------------------------------------------------------------------------------------

    @Test
    void pgRoundTripKeepsDirectionLabelsAndProperties() throws IOException {
        String pgIn = """
                {"nodes":[{"id":"101","labels":["person"],"properties":{"name":["Alice"],"tag":["a","b"]}},
                          {"id":"102","labels":["person","student"],"properties":{"since":[3000000000]}}],
                 "edges":[{"id":"e1","from":"101","to":"102","labels":["likes","knows"],"properties":{"w":[0.5]}},
                          {"from":"102","to":"101","undirected":true,"labels":[],"properties":{}}]}""";
        ObjectMapper om = new ObjectMapper();
        // PG -> CJ document -> PG
        PgJsonReader reader = new PgJsonReader();
        CjWriter2CjDocumentWriter w = new CjWriter2CjDocumentWriter();
        reader.read(SingleInputSource.of("in.pg.json", pgIn), new CjStream2CjWriter(w, true));
        InMemoryOutputSink sink = InMemoryOutputSink.create();
        w.resultDoc().fire(new CjWriter2CjStream(reader.createCjStream(sink)), false);
        JsonNode in = om.readTree(pgIn);
        JsonNode out = om.readTree(sink.getBufferAsUtf8String());
        assertThat(normalized(out.get("nodes"))).isEqualTo(normalized(in.get("nodes")));
        assertThat(normalized(out.get("edges"))).isEqualTo(normalized(in.get("edges")));
    }

    /**
     * CJ treats elements, data keys, labels and the two ends of an undirected edge as unordered, and writes them in
     * canonical order. Compare PG-JSON modulo that order: sort elements, labels and undirected from/to; object
     * equality of Jackson trees already ignores key order.
     */
    private static List<JsonNode> normalized(JsonNode array) {
        List<JsonNode> list = new ArrayList<>();
        for (JsonNode element : array) {
            tools.jackson.databind.node.ObjectNode o = (tools.jackson.databind.node.ObjectNode) element.deepCopy();
            List<String> labels = new ArrayList<>();
            o.get("labels").forEach(l -> labels.add(l.asString()));
            labels.sort(null);
            tools.jackson.databind.node.ArrayNode sortedLabels = o.putArray("labels");
            labels.forEach(sortedLabels::add);
            if (o.path("undirected").asBoolean() && o.get("from").asString().compareTo(o.get("to").asString()) > 0) {
                JsonNode from = o.get("from");
                o.set("from", o.get("to"));
                o.set("to", from);
            }
            list.add(o);
        }
        list.sort(java.util.Comparator.comparing(JsonNode::toString));
        return list;
    }
}
