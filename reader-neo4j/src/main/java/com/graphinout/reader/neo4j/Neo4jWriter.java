package com.graphinout.reader.neo4j;

import com.graphinout.base.cj.document.ICjDocumentChunk;
import com.graphinout.base.cj.document.ICjEdgeChunk;
import com.graphinout.base.cj.document.ICjEndpoint;
import com.graphinout.base.cj.document.ICjGraphChunk;
import com.graphinout.base.cj.document.ICjLabel;
import com.graphinout.base.cj.document.ICjLabelEntry;
import com.graphinout.base.cj.document.ICjNodeChunk;
import com.graphinout.base.cj.factory.BaseCjOutput;
import com.graphinout.base.cj.stream.ICjStream;
import com.graphinout.base.output.OutputSink;
import com.graphinout.foundation.pure.input.ContentError;
import com.graphinout.foundation.pure.json.document.IJsonFactory;
import com.graphinout.foundation.pure.json.document.IJsonObject;
import com.graphinout.foundation.pure.json.document.IJsonValue;
import org.jspecify.annotations.Nullable;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

/**
 * Writes CJ documents to the Neo4j APOC JSON export format (JSON Lines, as {@code apoc.export.json.all} writes it):
 * every node before every relationship, fields in APOC's order, {@code start}/{@code end} with the node's labels.
 * <p>
 * A Neo4j graph is one flat set of nodes and binary, directed relationships with a type. What CJ has beyond that —
 * nested and multiple graphs, hyperedges, undirected edges, edges on edges, label languages, nested JSON data — is
 * mapped as closely as possible and reported to the content error handler.
 */
public class Neo4jWriter extends BaseCjOutput implements ICjStream {

    /** Relationship type used when a CJ edge has neither label nor type (APOC requires one). */
    public static final String DEFAULT_RELATIONSHIP_TYPE = "RELATED_TO";

    private final OutputSink outputSink;
    private final ObjectMapper objectMapper = new ObjectMapper();
    /** buffered: ids are generated and endpoints resolved once all nodes are known */
    private final List<Map<String, Object>> nodes = new ArrayList<>();
    private final List<PendingEdge> edges = new ArrayList<>();
    private final Set<String> edgeIds = new HashSet<>();
    private int graphCount = 0;
    /** report each kind of loss once per document, with a count */
    private final Map<String, Integer> warnings = new LinkedHashMap<>();

    private record PendingEdge(Map<String, Object> rel, String start, String end) {}

    public Neo4jWriter(OutputSink outputSink) {
        this.outputSink = outputSink;
    }

    @Override
    public void documentStart(ICjDocumentChunk document) {
        // Neo4j JSON Lines has no document wrapper
    }

    @Override
    public void graphStart(ICjGraphChunk graph) {
        graphCount++;
    }

    @Override
    public void graphEnd() {
        // Neo4j JSON Lines has no graph wrapper
    }

    @Override
    public void nodeStart(ICjNodeChunk node) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("type", "node");
        map.put("id", node.id()); // null: generated in documentEnd
        List<String> labels = labels(node.label(), "node");
        if (!labels.isEmpty()) map.put("labels", labels);
        Map<String, Object> properties = properties(node.data().jsonValue(), "node");
        if (!properties.isEmpty()) map.put("properties", properties);
        if (node.ports().findAny().isPresent()) note("node ports have no Neo4j equivalent and were dropped");
        nodes.add(map);
    }

    @Override
    public void nodeEnd() {
        // node buffered in nodeStart
    }

    @Override
    public void edgeStart(ICjEdgeChunk edge) {
        String what = "edge" + (edge.id() == null ? "" : " '" + edge.id() + "'");
        List<ICjEndpoint> sources = edge.sources();
        List<ICjEndpoint> targets = edge.targets();
        List<ICjEndpoint> undirected = edge.undirectedEndpoints();
        int endpointCount = sources.size() + targets.size() + undirected.size();

        String start;
        String end;
        if (!sources.isEmpty() && !targets.isEmpty()) {
            start = sources.getFirst().node();
            end = targets.getFirst().node();
        } else if (!sources.isEmpty() && !undirected.isEmpty()) {
            start = sources.getFirst().node();
            end = undirected.getFirst().node();
        } else if (!undirected.isEmpty() && !targets.isEmpty()) {
            start = undirected.getFirst().node();
            end = targets.getFirst().node();
        } else if (undirected.size() >= 2) {
            start = undirected.get(0).node();
            end = undirected.get(1).node();
            note("Neo4j relationships are directed: undirected edges were written from their first to their second endpoint");
        } else {
            error(what + " has " + endpointCount + " endpoint(s); a Neo4j relationship needs a start and an end node. Not written");
            return;
        }
        if (endpointCount > 2) {
            note("Neo4j relationships connect two nodes: hyperedges were written from their first source to their first target only");
        }
        if (edge.endpoints().anyMatch(ep -> ep.port() != null || ep.data().jsonValue() != null)) {
            note("endpoint ports and endpoint data have no Neo4j equivalent and were dropped");
        }

        Map<String, Object> rel = new LinkedHashMap<>();
        if (edge.id() != null) {
            edgeIds.add(edge.id());
            // undo the reader's disambiguation of relationship ids that collide with node ids
            rel.put("id", edge.id().startsWith(Neo4jReader.REL_ID_PREFIX) ? edge.id().substring(Neo4jReader.REL_ID_PREFIX.length()) : edge.id());
        }
        rel.put("type", "relationship");
        List<String> label = labels(edge.label(), "edge");
        String type;
        if (!label.isEmpty()) {
            type = label.getFirst();
            if (label.size() > 1) note("a Neo4j relationship has one type: edges with several labels were written with their first");
        } else if (edge.edgeType() != null && edge.type() != null) {
            type = edge.type();
        } else {
            type = DEFAULT_RELATIONSHIP_TYPE;
            note("edges without label or type were written with the relationship type " + DEFAULT_RELATIONSHIP_TYPE);
        }
        rel.put("label", type);
        Map<String, Object> properties = properties(edge.data().jsonValue(), what);
        if (!properties.isEmpty()) rel.put("properties", properties);
        edges.add(new PendingEdge(rel, start, end));
    }

    @Override
    public void edgeEnd() {
        // edge buffered in edgeStart
    }

    @Override
    public void documentEnd() {
        Map<String, List<String>> labelsById = assignNodeIds();
        try {
            for (Map<String, Object> node : nodes) writeLine(node);
            for (PendingEdge e : edges) {
                if (!labelsById.containsKey(e.start()) || !labelsById.containsKey(e.end())) {
                    String missing = labelsById.containsKey(e.start()) ? e.end() : e.start();
                    error("relationship " + e.rel().getOrDefault("id", "") + " references '" + missing + "', which is "
                            + (edgeIds.contains(missing) ? "an edge (Neo4j has no relationships on relationships)" : "not a node")
                            + ". Not written");
                    continue;
                }
                Map<String, Object> rel = e.rel();
                rel.put("start", ref(e.start(), labelsById));
                rel.put("end", ref(e.end(), labelsById));
                writeLine(rel);
            }
        } catch (IOException ex) {
            throw new RuntimeException("Failed to write Neo4j JSON", ex);
        }
        if (nodes.isEmpty() && edges.isEmpty()) {
            warn("No nodes or edges to export to Neo4j; the document has no graph content");
        } else if (graphCount > 1) {
            warn("Neo4j JSON is a flat set of nodes and relationships: " + graphCount
                    + " CJ graphs (nested, compound or sibling) were flattened into one; their graph ids,"
                    + " labels and graph-level data are not represented in the output");
        }
        warnings.forEach((message, count) -> warn(message + (count > 1 ? " (" + count + "x)" : "")));
    }

    /** Gives id-less nodes an id that collides with no other node or edge id; returns the labels per node id. */
    private Map<String, List<String>> assignNodeIds() {
        Set<String> used = new HashSet<>(edgeIds);
        for (Map<String, Object> node : nodes) if (node.get("id") != null) used.add((String) node.get("id"));
        int counter = 0;
        int generated = 0;
        Map<String, List<String>> labelsById = new HashMap<>();
        for (Map<String, Object> node : nodes) {
            if (node.get("id") == null) {
                String id;
                do {
                    id = "node_" + counter++;
                } while (used.contains(id));
                used.add(id);
                node.put("id", id);
                generated++;
            }
            @SuppressWarnings("unchecked") List<String> labels = (List<String>) node.getOrDefault("labels", List.of());
            labelsById.putIfAbsent((String) node.get("id"), labels);
        }
        if (generated > 0) warn(generated + " node(s) without id got a generated id (node_0, node_1, ...)");
        return labelsById;
    }

    private static Map<String, Object> ref(String id, Map<String, List<String>> labelsById) {
        Map<String, Object> ref = new LinkedHashMap<>();
        ref.put("id", id);
        ref.put("labels", labelsById.get(id));
        return ref;
    }

    /** Label values without language; if there are none, all values (a CJ label may be multilingual). */
    private List<String> labels(@Nullable ICjLabel label, String what) {
        if (label == null) return List.of();
        List<ICjLabelEntry> entries = label.entries().toList();
        Set<String> plain = new LinkedHashSet<>();
        Set<String> all = new LinkedHashSet<>();
        for (ICjLabelEntry entry : entries) {
            if (entry.value() == null || entry.value().isEmpty()) continue;
            all.add(entry.value());
            if (entry.language() == null) plain.add(entry.value());
        }
        if (!plain.isEmpty() && plain.size() < all.size()) {
            note(what + " label entries with a language were dropped (Neo4j labels have no language)");
            return new ArrayList<>(plain);
        }
        if (plain.isEmpty() && all.size() > 1) {
            note(what + " labels given only in several languages were written as several Neo4j labels");
        }
        return new ArrayList<>(all);
    }

    /** CJ data object -> Neo4j properties: scalars, homogeneous lists of scalars, and points stay; the rest becomes a JSON string. */
    private Map<String, Object> properties(@Nullable IJsonValue data, String what) {
        Map<String, Object> properties = new LinkedHashMap<>();
        if (data == null) return properties;
        if (!data.isObject()) {
            note("data that is not a JSON object has no place in Neo4j properties and was dropped");
            return properties;
        }
        IJsonObject object = data.asObject();
        for (String key : object.keys()) {
            IJsonValue value = object.get(key);
            if (value == null || value.isNull()) continue; // Neo4j does not store null properties
            properties.put(key, property(value));
        }
        return properties;
    }

    private Object property(IJsonValue value) {
        if (value.isString()) return value.asString();
        if (value.isNumber()) return value.asNumber();
        if (value.isBoolean()) return value.asBoolean();
        if (value.isArray()) {
            List<Object> list = new ArrayList<>();
            boolean scalars = true;
            for (int i = 0; i < value.asArray().size(); i++) {
                IJsonValue element = value.asArray().get(i);
                if (element.isString()) list.add(element.asString());
                else if (element.isNumber()) list.add(element.asNumber());
                else if (element.isBoolean()) list.add(element.asBoolean());
                else scalars = false;
            }
            if (scalars) return list;
            note("Neo4j lists hold scalars only: lists with nested values or nulls were written as JSON strings");
            return value.toJsonString();
        }
        if (value.isObject() && value.asObject().get("crs") != null) {
            return objectMapper.readValue(value.toJsonString(), Object.class); // a point, as APOC writes it
        }
        note("Neo4j properties cannot be maps: nested objects were written as JSON strings");
        return value.toJsonString();
    }

    private void writeLine(Map<String, Object> object) throws IOException {
        outputSink.write(objectMapper.writeValueAsString(object) + "\n");
    }

    private void note(String message) {
        warnings.merge(message, 1, Integer::sum);
    }

    private void warn(String message) {
        Consumer<ContentError> handler = contentErrorHandler();
        if (handler != null) handler.accept(ContentError.warn(message));
    }

    private void error(String message) {
        Consumer<ContentError> handler = contentErrorHandler();
        if (handler != null) handler.accept(ContentError.error(message));
    }

    @Override
    public IJsonFactory jsonFactory() {
        return IJsonFactory.INSTANCE;
    }
}
