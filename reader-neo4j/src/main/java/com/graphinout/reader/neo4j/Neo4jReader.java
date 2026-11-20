package com.graphinout.reader.neo4j;

import com.graphinout.base.cj.document.ICjHasDataMutable;
import com.graphinout.base.cj.stream.ICjStream;
import com.graphinout.base.gio.GioFileFormat;
import com.graphinout.base.gio.GioReader;
import com.graphinout.base.gio.GioWriter;
import com.graphinout.base.input.InputSource;
import com.graphinout.base.output.OutputSink;
import com.graphinout.foundation.pure.input.ContentError;
import com.graphinout.foundation.pure.input.Location;
import com.graphinout.foundation.pure.json.document.IJsonArrayMutable;
import com.graphinout.foundation.pure.json.document.IJsonFactory;
import com.graphinout.foundation.pure.json.document.IJsonObjectMutable;
import com.graphinout.foundation.pure.json.document.IJsonValue;
import org.jspecify.annotations.Nullable;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.MappingIterator;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;

/**
 * Reads and writes the Neo4j APOC JSON export format ({@code apoc.export.json.*}).
 * <p>
 * Accepted input shapes (all of them may be mixed):
 * <ul>
 *     <li>JSON Lines — one {@code {"type":"node",...}} or {@code {"type":"relationship",...}} object per line
 *     ({@code jsonFormat: JSON_LINES}, the default);</li>
 *     <li>a JSON array of such objects ({@code ARRAY_JSON});</li>
 *     <li>{@code {"nodes":[...],"rels":[...]}} ({@code JSON}) and {@code {"nodes":{"0":{...}},"rels":{...}}}
 *     ({@code JSON_ID_AS_KEYS});</li>
 *     <li>query and path exports — rows like {@code {"u":{node},"rel":{rel},"p":{"length":1,"rels":[..],"nodes":[..]}}}
 *     — and Memgraph's variant with scalar {@code "start": 1, "end": 2}. Node and relationship objects are found
 *     anywhere in a row; nodes repeated across rows are read once.</li>
 * </ul>
 * Mapping to CJ: a node's {@code labels} become CJ label entries, its {@code properties} CJ data (lists, maps and
 * points keep their JSON structure, numbers their full precision). A relationship becomes an edge from {@code start}
 * (CJ source, endpoint direction {@code in}) to {@code end} (target, {@code out}); its {@code label} (the relationship
 * type) becomes the edge label. APOC numbers nodes and relationships separately, but CJ ids are one space: a
 * relationship id that equals a node id is prefixed with {@value #REL_ID_PREFIX}. A relationship endpoint that no
 * node object declares becomes a node built from the {@code start}/{@code end} reference.
 * <p>
 * Malformed lines and spec violations are reported as {@link ContentError}s with their line; reading continues.
 */
public class Neo4jReader implements GioReader, GioWriter {

    public static final String FORMAT_ID = "neo4j-json";
    public static final GioFileFormat FORMAT = new GioFileFormat(FORMAT_ID, "Neo4j APOC JSON Export Format",
            ".neo4j.json", ".neo4j-export.json");
    /** Prefix for relationship ids that collide with a node id (APOC numbers both from 0). */
    public static final String REL_ID_PREFIX = "rel:";

    private static final ObjectMapper MAPPER = JsonMapper.builder()
            // keep decimals exact (and never produce Infinity for huge values)
            .enable(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS)
            .build();

    private @Nullable Consumer<ContentError> errorHandler;

    @Override
    public GioFileFormat fileFormat() {
        return FORMAT;
    }

    /** A relationship, kept until all nodes are known (CJ needs nodes before edges; ids may collide). */
    private record PendingRel(@Nullable String id, @Nullable String label, String start, String end,
                              @Nullable JsonNode properties, Location location) {}

    /** Per-read state: a new instance for every {@link #read} call. */
    private final class ReadState {
        final ICjStream cjStream;
        final Set<String> nodeIds = new HashSet<>();
        /** hash of labels + properties per node id, to tell harmless repetitions from conflicting duplicates */
        final Map<String, Integer> nodeContentHash = new HashMap<>();
        /** endpoint references as given (with labels/properties, if any), for implicit nodes */
        final Map<String, JsonNode> endpointRefs = new LinkedHashMap<>();
        final Map<String, PendingRel> rels = new LinkedHashMap<>();
        final List<PendingRel> relsWithoutId = new ArrayList<>();

        ReadState(ICjStream cjStream) {
            this.cjStream = cjStream;
        }
    }

    @Override
    public void read(InputSource inputSource, ICjStream cjStream) throws IOException {
        ReadState state = new ReadState(cjStream);
        cjStream.documentStart(cjStream.createDocumentChunk());
        cjStream.graphStart(cjStream.createGraphChunk());
        try (BufferedReader in = new BufferedReader(new InputStreamReader(inputSource.asSingle().inputStream(), StandardCharsets.UTF_8))) {
            readValues(in, state);
        }
        emitImplicitNodes(state);
        emitRelationships(state);
        cjStream.graphEnd();
        cjStream.documentEnd();
    }

    // ---- splitting the input into top-level JSON values -------------------------------------------------------------

    /**
     * Splits the input into top-level JSON values without parsing it twice: a value ends where the bracket depth
     * (outside strings) returns to zero, so a pretty-printed value may span lines while each JSON Lines line is its own
     * value. A malformed value is reported and skipped; a line starting with '{' or '[' while a value is still open
     * means the open value was truncated.
     */
    private void readValues(BufferedReader in, ReadState state) throws IOException {
        StringBuilder buffer = new StringBuilder();
        int bufferStartLine = 0;
        int depth = 0;
        boolean inString = false;
        boolean escape = false;
        String line;
        int lineNo = 0;
        while ((line = in.readLine()) != null) {
            lineNo++;
            if (lineNo == 1 && line.startsWith("﻿")) line = line.substring(1);
            if (depth > 0 && !inString && !line.isEmpty() && (line.charAt(0) == '{' || line.charAt(0) == '[')) {
                error("Incomplete JSON value (truncated?); skipped", new Location(bufferStartLine, 1));
                buffer.setLength(0);
                depth = 0;
            }
            if (buffer.isEmpty()) {
                if (line.isBlank()) continue;
                bufferStartLine = lineNo;
            }
            buffer.append(line).append('\n');
            for (int i = 0; i < line.length(); i++) {
                char c = line.charAt(i);
                if (inString) {
                    if (escape) escape = false;
                    else if (c == '\\') escape = true;
                    else if (c == '"') inString = false;
                } else if (c == '"') inString = true;
                else if (c == '{' || c == '[') depth++;
                else if (c == '}' || c == ']') depth--;
            }
            if (depth <= 0 && !inString) {
                parseAndHandle(buffer.toString(), bufferStartLine, state);
                buffer.setLength(0);
                depth = 0;
            }
        }
        if (!buffer.toString().isBlank()) {
            error("Incomplete JSON value at end of input (truncated?); skipped", new Location(bufferStartLine, 1));
        }
    }

    private void parseAndHandle(String json, int line, ReadState state) {
        List<JsonNode> values = new ArrayList<>();
        try (MappingIterator<JsonNode> it = MAPPER.readerFor(JsonNode.class).readValues(json)) {
            while (it.hasNext()) values.add(it.next());
        } catch (JacksonException e) {
            error("Malformed JSON: " + e.getOriginalMessage() + "; skipped", new Location(line, 1));
            return;
        }
        for (JsonNode value : values) {
            Location location = new Location(line, 1);
            if (!value.isContainer()) {
                warn("Unexpected top-level value " + value + "; skipped", location);
            } else if (!handle(value, state, location)) {
                warn("No Neo4j node or relationship found in " + abbreviate(value) + "; skipped", location);
            }
        }
    }

    // ---- finding nodes and relationships ------------------------------------------------------------------------------

    /** @return true if the value is, or contains, a node or relationship */
    private boolean handle(JsonNode value, ReadState state, Location location) {
        if (value.isArray()) {
            boolean found = false;
            for (JsonNode element : value) found |= handle(element, state, location);
            return found;
        }
        if (!value.isObject()) return false;
        JsonNode typeNode = value.get("type");
        if (typeNode != null && typeNode.isString()) {
            String type = typeNode.asString();
            String normalized = type.toLowerCase(Locale.ROOT);
            if (normalized.equals("node") || normalized.equals("relationship")) {
                if (!type.equals(normalized)) warn("Type '" + type + "' should be lowercase '" + normalized + "'", location);
                if (normalized.equals("node")) readNode(value, state, location);
                else readRelationship(value, state, location);
                return true;
            }
        }
        // a container: JSON / JSON_ID_AS_KEYS wrapper, query row, path {length, nodes, rels}, map or list column
        boolean found = false;
        for (Map.Entry<String, JsonNode> field : value.properties()) {
            if (field.getValue().isContainer()) found |= handle(field.getValue(), state, location);
        }
        return found;
    }

    private void readNode(JsonNode node, ReadState state, Location location) {
        String id = readId(node.get("id"), "Node", location);
        List<String> labels = readLabels(node.get("labels"), location);
        JsonNode properties = readProperties(node.get("properties"), "Node " + (id == null ? "" : "'" + id + "'"), location);
        if (id == null) {
            error("Node without 'id' (mandatory)", location);
        } else {
            int hash = Objects.hash(labels, properties);
            Integer previous = state.nodeContentHash.putIfAbsent(id, hash);
            if (previous != null) {
                // query and path exports repeat a node in every row it appears in: read it once
                if (previous != hash) warn("Node '" + id + "' appears again with different labels or properties; kept the first", location);
                return;
            }
            state.nodeIds.add(id);
        }
        emitNode(state.cjStream, id, labels, properties);
    }

    private void readRelationship(JsonNode rel, ReadState state, Location location) {
        String id = readId(rel.get("id"), "Relationship", location);
        String label = readRelType(rel.get("label"), location);
        String what = "Relationship" + (id == null ? "" : " '" + id + "'");
        String start = readEndpoint(rel.get("start"), state);
        String end = readEndpoint(rel.get("end"), state);
        if (start == null || end == null) {
            error(what + " without " + (start == null ? "'start'" : "'end'") + " node (mandatory); skipped", location);
            return;
        }
        if (label == null) warn(what + " without 'label' (its relationship type)", location);
        JsonNode properties = readProperties(rel.get("properties"), what, location);
        PendingRel pending = new PendingRel(id, label, start, end, properties, location);
        if (id == null) {
            state.relsWithoutId.add(pending);
            return;
        }
        PendingRel previous = state.rels.putIfAbsent(id, pending);
        if (previous != null && !(Objects.equals(previous.label(), label) && previous.start().equals(start)
                && previous.end().equals(end) && Objects.equals(previous.properties(), properties))) {
            warn("Relationship '" + id + "' appears again with different content; kept the first", location);
        }
    }

    /** {@code {"id":..., "labels":..., "properties":...}} or, as Memgraph writes it, the bare id. */
    private @Nullable String readEndpoint(@Nullable JsonNode ref, ReadState state) {
        if (ref == null || ref.isNull()) return null;
        if (ref.isString() || ref.isNumber()) return ref.asString();
        if (ref.isObject()) {
            JsonNode id = ref.get("id");
            if (id == null || !(id.isString() || id.isNumber())) return null;
            state.endpointRefs.putIfAbsent(id.asString(), ref);
            return id.asString();
        }
        return null;
    }

    private @Nullable String readId(@Nullable JsonNode id, String what, Location location) {
        if (id == null || id.isNull()) return null;
        if (id.isString() || id.isNumber()) return id.asString(); // APOC writes strings, Memgraph numbers
        error(what + " 'id' must be a string or number, found " + id.getNodeType(), location);
        return null;
    }

    private @Nullable String readRelType(@Nullable JsonNode label, Location location) {
        if (label == null || label.isNull()) return null;
        if (label.isString()) return label.asString();
        if (label.isNumber() || label.isBoolean()) return label.asString();
        if (label.isArray() && !label.isEmpty() && label.get(0).isString()) {
            warn("Relationship 'label' should be a single string, found an array; used '" + label.get(0).asString() + "'", location);
            return label.get(0).asString();
        }
        error("Relationship 'label' must be a string, found " + label.getNodeType(), location);
        return null;
    }

    private List<String> readLabels(@Nullable JsonNode labels, Location location) {
        List<String> result = new ArrayList<>();
        if (labels == null || labels.isNull()) return result;
        if (labels.isString()) {
            warn("Node 'labels' should be an array, found the string \"" + labels.asString() + "\"; read as one label", location);
            result.add(labels.asString());
            return result;
        }
        if (!labels.isArray()) {
            error("Node 'labels' must be an array of strings, found " + labels.getNodeType(), location);
            return result;
        }
        for (JsonNode label : labels) {
            if (label.isString() && !label.asString().isEmpty()) {
                if (!result.contains(label.asString())) result.add(label.asString());
            } else if (!label.isNull()) {
                warn("Node label must be a non-empty string, found " + label + "; skipped", location);
            }
        }
        return result;
    }

    private @Nullable JsonNode readProperties(@Nullable JsonNode properties, String what, Location location) {
        if (properties == null || properties.isNull()) return null;
        if (!properties.isObject()) {
            error(what + " 'properties' must be an object, found " + properties.getNodeType() + "; skipped", location);
            return null;
        }
        return properties;
    }

    // ---- emitting CJ --------------------------------------------------------------------------------------------------

    private void emitNode(ICjStream cjStream, @Nullable String id, List<String> labels, @Nullable JsonNode properties) {
        cjStream.node(node -> {
            if (id != null) node.id(id);
            labels.forEach(node::addLabelWithoutLanguage);
            addProperties(node, properties);
        });
    }

    /** Endpoints no node object declared: build the node from the start/end reference (APOC includes labels there). */
    private void emitImplicitNodes(ReadState state) {
        List<String> missing = new ArrayList<>();
        for (PendingRel rel : allRels(state)) {
            for (String endpoint : List.of(rel.start(), rel.end())) {
                if (state.nodeIds.add(endpoint)) {
                    missing.add(endpoint);
                    JsonNode ref = state.endpointRefs.get(endpoint);
                    List<String> labels = ref == null ? List.of() : readLabels(ref.get("labels"), rel.location());
                    JsonNode properties = ref == null ? null : readProperties(ref.get("properties"), "Node '" + endpoint + "'", rel.location());
                    emitNode(state.cjStream, endpoint, labels, properties);
                }
            }
        }
        if (!missing.isEmpty()) {
            warn(missing.size() + " relationship endpoint(s) not declared as node objects; created them from the relationship's start/end: "
                    + abbreviate(missing), null);
        }
    }

    private void emitRelationships(ReadState state) {
        int renamed = 0;
        for (PendingRel rel : allRels(state)) {
            String id = rel.id();
            if (id != null && state.nodeIds.contains(id)) {
                id = REL_ID_PREFIX + id;
                renamed++;
            }
            String edgeId = id;
            state.cjStream.edge(edge -> {
                if (edgeId != null) edge.id(edgeId);
                if (rel.label() != null) edge.addLabelWithoutLanguage(rel.label());
                // CJ direction is seen from the edge: it comes IN from its source (start), goes OUT to its target (end)
                edge.addEndpointIncoming(rel.start());
                edge.addEndpointOutgoing(rel.end());
                addProperties(edge, rel.properties());
            });
        }
        if (renamed > 0) {
            warn(renamed + " relationship id(s) equal a node id (APOC numbers nodes and relationships separately);"
                    + " prefixed them with '" + REL_ID_PREFIX + "' to keep CJ ids unique", null);
        }
    }

    private static List<PendingRel> allRels(ReadState state) {
        List<PendingRel> all = new ArrayList<>(state.rels.values());
        all.addAll(state.relsWithoutId);
        return all;
    }

    private static void addProperties(ICjHasDataMutable element, @Nullable JsonNode properties) {
        if (properties == null || properties.isEmpty()) return;
        element.dataMutable(data -> {
            for (Map.Entry<String, JsonNode> p : properties.properties()) {
                // Neo4j does not store null properties; a null here carries no information
                if (!p.getValue().isNull()) data.add(p.getKey(), toJson(p.getValue()));
            }
        });
    }

    /** Jackson tree -> CJ JSON value, keeping lists, maps (e.g. points) and exact numbers. */
    static IJsonValue toJson(JsonNode n) {
        IJsonFactory f = IJsonFactory.INSTANCE;
        if (n.isString()) return f.createString(n.asString());
        if (n.isNumber()) return f.createNumber(n.numberValue());
        if (n.isBoolean()) return f.createBoolean(n.booleanValue());
        if (n.isArray()) {
            IJsonArrayMutable array = f.createArrayMutable();
            for (JsonNode element : n) array.add(toJson(element));
            return array;
        }
        if (n.isObject()) {
            IJsonObjectMutable object = f.createObjectMutable();
            for (Map.Entry<String, JsonNode> e : n.properties()) object.addProperty(e.getKey(), toJson(e.getValue()));
            return object;
        }
        return f.createNull();
    }

    private static String abbreviate(Object o) {
        String s = String.valueOf(o);
        return s.length() <= 120 ? s : s.substring(0, 117) + "...";
    }

    // ---- content errors -----------------------------------------------------------------------------------------------

    @Override
    public void setContentErrorHandler(Consumer<ContentError> errorHandler) {
        this.errorHandler = errorHandler;
    }

    private void warn(String message, @Nullable Location location) {
        if (errorHandler != null) errorHandler.accept(ContentError.of(ContentError.ErrorLevel.Warn, message, location == null ? Location.UNAVAILABLE : location));
    }

    private void error(String message, @Nullable Location location) {
        if (errorHandler != null) errorHandler.accept(ContentError.of(ContentError.ErrorLevel.Error, message, location == null ? Location.UNAVAILABLE : location));
    }

    @Override
    public ICjStream createCjStream(OutputSink outputSink) {
        Neo4jWriter writer = new Neo4jWriter(outputSink);
        if (errorHandler != null) {
            writer.setContentErrorHandler(errorHandler);
        }
        return writer;
    }
}
