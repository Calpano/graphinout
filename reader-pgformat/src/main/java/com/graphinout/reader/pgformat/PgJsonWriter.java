package com.graphinout.reader.pgformat;

import com.graphinout.base.cj.document.ICjDocumentChunk;
import com.graphinout.base.cj.document.ICjEdgeChunk;
import com.graphinout.base.cj.document.ICjEndpoint;
import com.graphinout.base.cj.document.ICjGraphChunk;
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
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

/**
 * Writes CJ documents to PG-JSON (Property Graph JSON) format.
 * Specification: <a href="https://pg-format.github.io/specification/">https://pg-format.github.io/specification/</a>
 * <p>
 * PG-JSON is one flat property graph, so nested and multiple CJ graphs are merged, a CJ edge maps to one
 * {@code from -> to} pair, and only object-valued CJ data becomes properties. Everything that cannot be represented is
 * reported to the content error handler.
 */
public class PgJsonWriter extends BaseCjOutput implements ICjStream {

    private final OutputSink outputSink;
    private final @Nullable Consumer<ContentError> errorHandler;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final List<Map<String, Object>> nodes = new ArrayList<>();
    private final List<Map<String, Object>> edges = new ArrayList<>();
    private int graphDepth = 0;
    private int graphCount = 0;
    private boolean warnedGraphsMerged = false;

    public PgJsonWriter(OutputSink outputSink) {
        this(outputSink, null);
    }

    public PgJsonWriter(OutputSink outputSink, @Nullable Consumer<ContentError> errorHandler) {
        this.outputSink = outputSink;
        this.errorHandler = errorHandler;
    }

    @Override
    public void documentStart(ICjDocumentChunk document) {
        // nodes and edges are collected and written at documentEnd
    }

    @Override
    public void documentEnd() {
        assignMissingNodeIds();
        try {
            Map<String, Object> pgJson = new LinkedHashMap<>();
            pgJson.put("nodes", nodes);
            pgJson.put("edges", edges);
            outputSink.write(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(pgJson));
        } catch (IOException e) {
            throw new RuntimeException("Failed to write PG-JSON", e);
        }
    }

    /** PG-JSON node ids are mandatory: give id-less nodes a generated id that collides with no existing id. */
    private void assignMissingNodeIds() {
        Set<Object> used = new HashSet<>();
        nodes.forEach(n -> {
            if (n.get("id") != null) used.add(n.get("id"));
        });
        int counter = 0;
        for (Map<String, Object> node : nodes) {
            if (node.get("id") != null) continue;
            String id;
            do {
                id = "node_" + counter++;
            } while (used.contains(id));
            used.add(id);
            node.put("id", id);
        }
    }

    @Override
    public void graphStart(ICjGraphChunk graph) {
        graphDepth++;
        graphCount++;
        if ((graphDepth > 1 || graphCount > 1) && !warnedGraphsMerged) {
            warnedGraphsMerged = true;
            warn("PG-JSON holds one flat graph: nested and multiple graphs are merged into it");
        }
    }

    @Override
    public void graphEnd() {
        graphDepth--;
    }

    @Override
    public void nodeStart(ICjNodeChunk node) {
        Map<String, Object> nodeMap = new LinkedHashMap<>();
        nodeMap.put("id", node.id()); // null is replaced by a generated id at documentEnd
        nodeMap.put("labels", labels(node.label() == null ? null : node.label().entries().toList()));
        nodeMap.put("properties", properties(node.data().jsonValue(), "node " + node.id()));
        nodes.add(nodeMap);
    }

    @Override
    public void nodeEnd() {
        // node already added in nodeStart
    }

    @Override
    public void edgeStart(ICjEdgeChunk edge) {
        String what = "edge" + (edge.id() == null ? "" : " '" + edge.id() + "'");
        List<ICjEndpoint> sources = edge.sources();
        List<ICjEndpoint> targets = edge.targets();
        List<ICjEndpoint> undirected = edge.undirectedEndpoints();
        int endpointCount = sources.size() + targets.size() + undirected.size();

        String from;
        String to;
        boolean isUndirected = false;
        if (!sources.isEmpty() && !targets.isEmpty()) {
            from = sources.getFirst().node();
            to = targets.getFirst().node();
        } else if (sources.isEmpty() && targets.isEmpty() && undirected.size() >= 2) {
            isUndirected = true;
            from = undirected.get(0).node();
            to = undirected.get(1).node();
        } else if (!sources.isEmpty() && !undirected.isEmpty()) {
            from = sources.getFirst().node();
            to = undirected.getFirst().node();
        } else if (!targets.isEmpty() && !undirected.isEmpty()) {
            from = undirected.getFirst().node();
            to = targets.getFirst().node();
        } else {
            error(what + " has " + endpointCount + " endpoint(s); PG-JSON edges need 'from' and 'to'. Edge not written");
            return;
        }
        if (endpointCount > 2) {
            warn(what + " has " + endpointCount + " endpoints; PG-JSON edges have two. Wrote " + from + " -> " + to + " only");
        }

        Map<String, Object> edgeMap = new LinkedHashMap<>();
        if (edge.id() != null) edgeMap.put("id", edge.id());
        edgeMap.put("from", from);
        edgeMap.put("to", to);
        if (isUndirected) edgeMap.put("undirected", true);
        edgeMap.put("labels", labels(edge.label() == null ? null : edge.label().entries().toList()));
        edgeMap.put("properties", properties(edge.data().jsonValue(), what));
        edges.add(edgeMap);
    }

    @Override
    public void edgeEnd() {
        // edge already added in edgeStart
    }

    /** All label values; may be empty (PG-JSON requires the array, not a label). */
    private static List<String> labels(@Nullable List<ICjLabelEntry> entries) {
        List<String> labels = new ArrayList<>();
        if (entries != null) {
            for (ICjLabelEntry entry : entries) {
                if (entry.value() != null && !entry.value().isEmpty() && !labels.contains(entry.value())) {
                    labels.add(entry.value());
                }
            }
        }
        return labels;
    }

    /** CJ data object -> PG properties: every key maps to an array of scalars. */
    private Map<String, List<Object>> properties(@Nullable IJsonValue data, String what) {
        Map<String, List<Object>> properties = new LinkedHashMap<>();
        if (data == null) return properties;
        if (!data.isObject()) {
            warn(what + ": data is not a JSON object (" + data + "); PG-JSON properties are key/value. Not written");
            return properties;
        }
        IJsonObject object = data.asObject();
        for (String key : object.keys()) {
            IJsonValue value = object.get(key);
            if (value == null || value.isNull()) continue;
            List<Object> values = new ArrayList<>();
            if (value.isArray()) {
                value.asArray().forEach(v -> addScalar(v, values, what, key));
            } else {
                addScalar(value, values, what, key);
            }
            if (!values.isEmpty()) properties.put(key, values);
        }
        return properties;
    }

    private void addScalar(IJsonValue value, List<Object> values, String what, String key) {
        if (value.isString()) values.add(value.asString());
        else if (value.isNumber()) values.add(value.asNumber());
        else if (value.isBoolean()) values.add(value.asBoolean());
        else if (value.isNull()) { /* PG values are non-null scalars */ }
        else {
            // nested object/array: PG-JSON values are scalars, keep the content as a JSON string
            warn(what + ": property '" + key + "' holds nested JSON; written as a JSON string");
            values.add(value.toString());
        }
    }

    private void warn(String message) {
        if (errorHandler != null) errorHandler.accept(ContentError.warn(message));
    }

    private void error(String message) {
        if (errorHandler != null) errorHandler.accept(ContentError.error(message));
    }

    @Override
    public IJsonFactory jsonFactory() {
        return IJsonFactory.INSTANCE;
    }
}
