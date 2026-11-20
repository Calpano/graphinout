package com.graphinout.reader.pgformat;

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
import org.jspecify.annotations.Nullable;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonParser;
import tools.jackson.core.JsonToken;
import tools.jackson.core.ObjectReadContext;
import tools.jackson.core.TokenStreamLocation;
import tools.jackson.core.json.JsonFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

/**
 * Reads and writes PG-JSON (Property Graph JSON) format.
 * Specification: <a href="https://pg-format.github.io/specification/">https://pg-format.github.io/specification/</a>
 * <p>
 * Mapping to CJ:
 * <ul>
 *     <li>node/edge {@code labels} become CJ label entries (all of them, in order);</li>
 *     <li>a property with one value becomes a scalar CJ data property, a property with several values a JSON array;</li>
 *     <li>an edge {@code from -> to} becomes a CJ edge with endpoint {@code from} as source ({@code direction=in}) and
 *     {@code to} as target ({@code direction=out}); {@code "undirected": true} gives two undirected endpoints.</li>
 * </ul>
 * Input that violates the specification is read leniently where the intent is clear and reported as
 * {@link ContentError}: a scalar property value instead of an array, a numeric id, edges listed before nodes.
 */
public class PgJsonReader implements GioReader, GioWriter {

    public static final String FORMAT_ID = "pg-json";
    public static final GioFileFormat FORMAT = new GioFileFormat(FORMAT_ID, "PG-JSON (Property Graph Format)",
            ".pg.json", ".pgjson");

    /** A parsed PG edge, kept until it can be emitted (edges may come before nodes). */
    private record PgEdge(@Nullable String id, String from, String to, boolean undirected, List<String> labels,
                          Map<String, List<Object>> properties, Location location) {}

    private @Nullable Consumer<ContentError> errorHandler;
    private final JsonFactory jsonFactory = new JsonFactory();

    @Override
    public GioFileFormat fileFormat() {
        return FORMAT;
    }

    @Override
    public void read(InputSource inputSource, ICjStream cjStream) throws IOException {
        try (InputStream is = inputSource.asSingle().inputStream();
             JsonParser parser = jsonFactory.createParser(ObjectReadContext.empty(), is)) {
            if (parser.nextToken() != JsonToken.START_OBJECT) {
                throw new IOException("Expected JSON object at root");
            }
            cjStream.documentStart(cjStream.createDocumentChunk());
            cjStream.graphStart(cjStream.createGraphChunk());

            // node ids seen so far; edge endpoints are checked against them once the 'nodes' array was read
            Set<String> nodeIds = new HashSet<>();
            boolean nodesDone = false;
            List<PgEdge> pendingEdges = new ArrayList<>();
            while (parser.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = parser.currentName();
                parser.nextToken();
                if ("nodes".equals(fieldName)) {
                    if (expectArray(parser, "nodes")) {
                        while (parser.nextToken() != JsonToken.END_ARRAY) {
                            processNode(parser, cjStream, nodeIds);
                        }
                    }
                    nodesDone = true;
                    // edges that were listed before the nodes
                    for (PgEdge edge : pendingEdges) emitEdge(edge, cjStream, nodeIds);
                    pendingEdges.clear();
                } else if ("edges".equals(fieldName)) {
                    if (expectArray(parser, "edges")) {
                        while (parser.nextToken() != JsonToken.END_ARRAY) {
                            PgEdge edge = parseEdge(parser);
                            if (edge == null) continue;
                            if (nodesDone) emitEdge(edge, cjStream, nodeIds);
                            else pendingEdges.add(edge);
                        }
                    }
                } else {
                    warn("Unknown field in graph: " + fieldName, parser);
                    parser.skipChildren();
                }
            }
            if (!pendingEdges.isEmpty()) {
                // no 'nodes' array at all: emit the edges, without endpoint checks
                for (PgEdge edge : pendingEdges) emitEdge(edge, cjStream, null);
            }
            cjStream.graphEnd();
            cjStream.documentEnd();
        } catch (JacksonException e) {
            TokenStreamLocation loc = e.getLocation();
            throw new IOException("Invalid PG-JSON" + (loc == null ? "" : " at line " + loc.getLineNr() + ", column " + loc.getColumnNr())
                    + ": " + e.getOriginalMessage(), e);
        }
    }

    private boolean expectArray(JsonParser parser, String field) {
        if (parser.currentToken() == JsonToken.START_ARRAY) return true;
        error("Expected an array for '" + field + "', found " + parser.currentToken(), parser);
        parser.skipChildren();
        return false;
    }

    private void processNode(JsonParser parser, ICjStream cjStream, Set<String> nodeIds) {
        if (parser.currentToken() != JsonToken.START_OBJECT) {
            error("Expected a node object, found " + parser.currentToken(), parser);
            parser.skipChildren();
            return;
        }
        Location location = location(parser);
        String id = null;
        List<String> labels = List.of();
        Map<String, List<Object>> properties = Map.of();
        while (parser.nextToken() != JsonToken.END_OBJECT) {
            String fieldName = parser.currentName();
            parser.nextToken();
            switch (fieldName) {
                case "id" -> id = parseId(parser, "node");
                case "labels" -> labels = parseLabels(parser);
                case "properties" -> properties = parseProperties(parser);
                default -> {
                    warn("Unknown field in node: " + fieldName, parser);
                    parser.skipChildren();
                }
            }
        }
        if (id == null) {
            error("Node without 'id' (mandatory)", location);
        } else if (!nodeIds.add(id)) {
            warn("Duplicate node id '" + id + "'", location);
        }
        String nodeId = id;
        List<String> nodeLabels = labels;
        Map<String, List<Object>> nodeProperties = properties;
        cjStream.node(node -> {
            if (nodeId != null) node.id(nodeId);
            nodeLabels.forEach(node::addLabelWithoutLanguage);
            addProperties(node, nodeProperties);
        });
    }

    private @Nullable PgEdge parseEdge(JsonParser parser) {
        if (parser.currentToken() != JsonToken.START_OBJECT) {
            error("Expected an edge object, found " + parser.currentToken(), parser);
            parser.skipChildren();
            return null;
        }
        Location location = location(parser);
        String id = null;
        String from = null;
        String to = null;
        boolean undirected = false;
        List<String> labels = List.of();
        Map<String, List<Object>> properties = Map.of();
        while (parser.nextToken() != JsonToken.END_OBJECT) {
            String fieldName = parser.currentName();
            parser.nextToken();
            switch (fieldName) {
                case "id" -> id = parseId(parser, "edge");
                case "from" -> from = parseId(parser, "edge 'from'");
                case "to" -> to = parseId(parser, "edge 'to'");
                case "undirected" -> {
                    JsonToken t = parser.currentToken();
                    if (t == JsonToken.VALUE_TRUE || t == JsonToken.VALUE_FALSE) {
                        undirected = t == JsonToken.VALUE_TRUE;
                    } else if (t != JsonToken.VALUE_NULL) {
                        error("Edge 'undirected' must be a boolean, found " + t + "; reading the edge as directed", parser);
                        parser.skipChildren();
                    }
                }
                case "labels" -> labels = parseLabels(parser);
                case "properties" -> properties = parseProperties(parser);
                default -> {
                    warn("Unknown field in edge: " + fieldName, parser);
                    parser.skipChildren();
                }
            }
        }
        if (from == null || to == null) {
            error("Edge without " + (from == null ? "'from'" : "'to'") + " (mandatory); edge skipped", location);
            return null;
        }
        return new PgEdge(id, from, to, undirected, labels, properties, location);
    }

    /** @param nodeIds known node ids, or null if they cannot be checked */
    private void emitEdge(PgEdge e, ICjStream cjStream, @Nullable Set<String> nodeIds) {
        if (nodeIds != null) {
            for (String endpoint : List.of(e.from(), e.to())) {
                if (!nodeIds.contains(endpoint)) {
                    warn("Edge " + (e.id() == null ? "" : "'" + e.id() + "' ") + "references unknown node '" + endpoint + "'", e.location());
                }
            }
        }
        cjStream.edge(edge -> {
            if (e.id() != null) edge.id(e.id());
            e.labels().forEach(edge::addLabelWithoutLanguage);
            if (e.undirected()) {
                edge.addEndpointUndirected(e.from());
                edge.addEndpointUndirected(e.to());
            } else {
                // CJ direction is seen from the edge: the edge comes IN from its source and goes OUT to its target
                edge.addEndpointIncoming(e.from());
                edge.addEndpointOutgoing(e.to());
            }
            addProperties(edge, e.properties());
        });
    }

    /** A string id; numbers are accepted (with a warning) as their text. */
    private @Nullable String parseId(JsonParser parser, String context) {
        JsonToken t = parser.currentToken();
        if (t == JsonToken.VALUE_STRING) return parser.getString();
        if (t == JsonToken.VALUE_NUMBER_INT || t == JsonToken.VALUE_NUMBER_FLOAT) {
            warn(context + " id should be a string, found number " + parser.getString(), parser);
            return parser.getString();
        }
        if (t != JsonToken.VALUE_NULL) {
            error(context + " id must be a string, found " + t, parser);
            parser.skipChildren();
        }
        return null;
    }

    private List<String> parseLabels(JsonParser parser) {
        List<String> labels = new ArrayList<>();
        if (parser.currentToken() != JsonToken.START_ARRAY) {
            error("'labels' must be an array of strings, found " + parser.currentToken(), parser);
            parser.skipChildren();
            return labels;
        }
        while (parser.nextToken() != JsonToken.END_ARRAY) {
            if (parser.currentToken() == JsonToken.VALUE_STRING && !parser.getString().isEmpty()) {
                labels.add(parser.getString());
            } else {
                error("Label must be a non-empty string, found " + parser.currentToken(), parser);
                parser.skipChildren();
            }
        }
        return labels;
    }

    private Map<String, List<Object>> parseProperties(JsonParser parser) {
        Map<String, List<Object>> properties = new LinkedHashMap<>();
        if (parser.currentToken() != JsonToken.START_OBJECT) {
            error("'properties' must be an object, found " + parser.currentToken(), parser);
            parser.skipChildren();
            return properties;
        }
        while (parser.nextToken() != JsonToken.END_OBJECT) {
            String key = parser.currentName();
            parser.nextToken();
            List<Object> values = new ArrayList<>();
            if (parser.currentToken() == JsonToken.START_ARRAY) {
                while (parser.nextToken() != JsonToken.END_ARRAY) {
                    addScalar(parser, key, values);
                }
            } else {
                // spec: property values are arrays; a single scalar is read as a one-element array
                warn("Property '" + key + "' should be an array of values", parser);
                addScalar(parser, key, values);
            }
            if (!values.isEmpty()) properties.put(key, values);
        }
        return properties;
    }

    /** Adds the scalar at the current token to values; skips (and reports) null, objects and arrays. */
    private void addScalar(JsonParser parser, String key, List<Object> values) {
        switch (parser.currentToken()) {
            case VALUE_STRING -> values.add(parser.getString());
            case VALUE_NUMBER_INT, VALUE_NUMBER_FLOAT -> values.add(parser.getNumberValue());
            case VALUE_TRUE -> values.add(Boolean.TRUE);
            case VALUE_FALSE -> values.add(Boolean.FALSE);
            case VALUE_NULL -> warn("Property '" + key + "' contains null; value skipped", parser);
            default -> {
                error("Property '" + key + "' values must be scalars, found " + parser.currentToken() + "; value skipped", parser);
                parser.skipChildren();
            }
        }
    }

    private void addProperties(ICjHasDataMutable element, Map<String, List<Object>> properties) {
        if (properties.isEmpty()) return;
        element.dataMutable(data -> properties.forEach((key, values) -> {
            if (values.size() == 1) {
                Object value = values.getFirst();
                switch (value) {
                    case String s -> data.add(key, s);
                    case Number n -> data.add(key, n);
                    case Boolean b -> data.add(key, b.booleanValue());
                    default -> throw new IllegalStateException("unexpected value " + value);
                }
            } else {
                IJsonArrayMutable array = IJsonFactory.INSTANCE.createArrayMutable();
                for (Object value : values) {
                    switch (value) {
                        case String s -> array.add(s);
                        case Number n -> array.add(n);
                        case Boolean b -> array.add(b.booleanValue());
                        default -> throw new IllegalStateException("unexpected value " + value);
                    }
                }
                data.add(key, array);
            }
        }));
    }

    @Override
    public void setContentErrorHandler(Consumer<ContentError> errorHandler) {
        this.errorHandler = errorHandler;
    }

    private static Location location(JsonParser parser) {
        TokenStreamLocation loc = parser.currentTokenLocation();
        return new Location(loc.getLineNr(), loc.getColumnNr());
    }

    private void warn(String message, JsonParser parser) {
        warn(message, location(parser));
    }

    private void warn(String message, Location location) {
        if (errorHandler != null) errorHandler.accept(ContentError.of(ContentError.ErrorLevel.Warn, message, location));
    }

    private void error(String message, JsonParser parser) {
        error(message, location(parser));
    }

    private void error(String message, Location location) {
        if (errorHandler != null) errorHandler.accept(ContentError.of(ContentError.ErrorLevel.Error, message, location));
    }

    @Override
    public ICjStream createCjStream(OutputSink outputSink) {
        return new PgJsonWriter(outputSink, errorHandler);
    }
}
