package com.graphinout.reader.grale;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.graphinout.base.cj.document.ICjData;
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
import com.graphinout.foundation.pure.json.document.IJsonFactory;
import com.graphinout.foundation.pure.json.document.IJsonValue;
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.util.List;

/**
 * Writes a CJ stream as a single {@link GraleReader grale} JSON document. grale is one JSON object,
 * so the stream is buffered into a Jackson tree and serialised in {@link #documentEnd()}.
 */
public class GraleWriter extends BaseCjOutput implements ICjStream {

    private final OutputSink outputSink;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private final ArrayNode nodes = objectMapper.createArrayNode();
    private final ArrayNode edges = objectMapper.createArrayNode();

    // envelope-level fields recovered from the CJ graph data (see Grale.WRAPPER_KEY)
    private @Nullable JsonNode options;
    private @Nullable JsonNode graphValue;
    private @Nullable JsonNode hyperedges;
    private @Nullable JsonNode diagnostics;
    private @Nullable JsonNode debug;

    public GraleWriter(OutputSink outputSink) {
        this.outputSink = outputSink;
    }

    @Override
    public IJsonFactory jsonFactory() {
        return IJsonFactory.INSTANCE;
    }

    @Override
    public void documentStart(ICjDocumentChunk document) {
        // grale has no document wrapper; envelope fields come from the graph chunk
    }

    @Override
    public void graphStart(ICjGraphChunk graph) {
        JsonNode data = toJsonNode(graph.data());
        if (data != null && data.has(Grale.WRAPPER_KEY)) {
            JsonNode grale = data.get(Grale.WRAPPER_KEY);
            options = grale.get("options");
            graphValue = grale.get("value");
            hyperedges = grale.get("hyperedges");
            diagnostics = grale.get("diagnostics");
            debug = grale.get("debug");
        }
    }

    @Override
    public void nodeStart(ICjNodeChunk node) {
        if (node.id() == null) {
            error("grale node has no id; skipped");
            return;
        }
        ObjectNode nodeObj = objectMapper.createObjectNode();
        nodeObj.put("v", node.id());

        ObjectNode value = toObjectNode(node.data());
        if (value != null && value.has(Grale.PARENT_KEY)) {
            nodeObj.set("parent", value.remove(Grale.PARENT_KEY));
        }
        value = withLabel(value, labelText(node.label()));
        value = withEstimatedSize(value);
        if (value != null && !value.isEmpty()) {
            nodeObj.set("value", value);
        }
        nodes.add(nodeObj);
    }

    @Override
    public void edgeStart(ICjEdgeChunk edge) {
        ObjectNode edgeObj = objectMapper.createObjectNode();

        String[] vw = endpointsToVw(edge);
        if (vw == null) {
            error("grale edge needs two endpoints; skipped");
            return;
        }
        edgeObj.put("v", vw[0]);
        edgeObj.put("w", vw[1]);

        ObjectNode value = toObjectNode(edge.data());
        if (value != null && value.has(Grale.NAME_KEY)) {
            edgeObj.set("name", value.remove(Grale.NAME_KEY));
        }
        if (edge.id() != null) {
            edgeObj.put("id", edge.id());
        }
        value = withLabel(value, labelText(edge.label()));
        if (value != null && !value.isEmpty()) {
            edgeObj.set("value", value);
        }
        edges.add(edgeObj);
    }

    @Override
    public void documentEnd() {
        // assemble the envelope in canonical order: options, value, nodes, edges, then extras
        ObjectNode root = objectMapper.createObjectNode();
        root.set("options", options != null ? options : defaultOptions());
        if (graphValue != null) root.set("value", graphValue);
        root.set("nodes", nodes);
        root.set("edges", edges);
        if (hyperedges != null) root.set("hyperedges", hyperedges);
        if (diagnostics != null) root.set("diagnostics", diagnostics);
        if (debug != null) root.set("debug", debug);

        try {
            outputSink.write(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(root));
        } catch (IOException e) {
            throw new RuntimeException("Failed to write grale output", e);
        }
    }

    @Override
    public void graphEnd() {
        // no-op
    }

    @Override
    public void nodeEnd() {
        // node already emitted in nodeStart
    }

    @Override
    public void edgeEnd() {
        // edge already emitted in edgeStart
    }

    // ----------------------------------------------------------------- helpers

    private ObjectNode defaultOptions() {
        ObjectNode o = objectMapper.createObjectNode();
        o.put("directed", true);
        o.put("multigraph", false);
        o.put("compound", false);
        return o;
    }

    /** Recover {@code v}/{@code w}: source(in)/target(out) when directed, else the first two endpoints. */
    private @Nullable String[] endpointsToVw(ICjEdgeChunk edge) {
        List<ICjEndpoint> sources = edge.sources();
        List<ICjEndpoint> targets = edge.targets();
        if (sources.size() == 1 && targets.size() == 1) {
            return new String[]{sources.get(0).node(), targets.get(0).node()};
        }
        List<ICjEndpoint> all = edge.endpoints().toList();
        if (all.size() >= 2) {
            return new String[]{all.get(0).node(), all.get(1).node()};
        }
        return null;
    }

    /** Ensure {@code value.meta.label = label} when a CJ label is present and none is set yet. */
    private @Nullable ObjectNode withLabel(@Nullable ObjectNode value, @Nullable String label) {
        if (label == null) return value;
        ObjectNode v = value != null ? value : objectMapper.createObjectNode();
        JsonNode metaNode = v.get("meta");
        ObjectNode meta = metaNode != null && metaNode.isObject()
                ? (ObjectNode) metaNode : v.putObject("meta");
        if (!meta.has("label")) {
            meta.put("label", label);
        }
        return v;
    }

    /**
     * Fill in dagre {@code width}/{@code height} estimated from the node's label text when the source
     * carried none. Author-provided sizes are never overwritten; a node without a label is left as-is.
     */
    private @Nullable ObjectNode withEstimatedSize(@Nullable ObjectNode value) {
        if (value == null || value.has("width") && value.has("height")) {
            return value; // nothing to do, or sizes already present
        }
        JsonNode meta = value.get("meta");
        JsonNode labelNode = meta != null ? meta.get("label") : null;
        if (labelNode == null || !labelNode.isTextual()) {
            return value; // no label to size from
        }
        RobotoLabelMetrics.Box box = RobotoLabelMetrics.estimate(labelNode.asText());
        if (!value.has("width")) value.put("width", box.width());
        if (!value.has("height")) value.put("height", box.height());
        return value;
    }

    private static @Nullable String labelText(@Nullable ICjLabel label) {
        if (label == null) return null;
        return label.entries().map(ICjLabelEntry::value).filter(s -> s != null && !s.isEmpty())
                .findFirst().orElse(null);
    }

    private @Nullable ObjectNode toObjectNode(ICjData data) {
        JsonNode n = toJsonNode(data);
        return n != null && n.isObject() ? (ObjectNode) n : null;
    }

    private @Nullable JsonNode toJsonNode(ICjData data) {
        if (data == null || data.isEmpty()) return null;
        IJsonValue jsonValue = data.jsonValue();
        if (jsonValue == null || jsonValue.isNull()) return null;
        try {
            return objectMapper.readTree(jsonValue.toJsonString());
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to read CJ data as JSON", e);
        }
    }

    private void error(String message) {
        if (contentErrorHandler() != null) {
            contentErrorHandler().accept(com.graphinout.foundation.pure.input.ContentError.error(message));
        }
    }
}
