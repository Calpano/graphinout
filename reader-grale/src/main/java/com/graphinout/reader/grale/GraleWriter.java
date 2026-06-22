package com.graphinout.reader.grale;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.core.util.Separators;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Writes a CJ stream as a single {@link GraleReader grale} JSON document. grale is one JSON object,
 * so the stream is buffered into a Jackson tree and serialised in {@link #documentEnd()}.
 */
public class GraleWriter extends BaseCjOutput implements ICjStream {

    private final OutputSink outputSink;
    private final ObjectMapper objectMapper = new ObjectMapper();
    /** Renders one node/edge object compactly on a single line: {@code { "v": "2", "value": { … } }}. */
    private final ObjectWriter oneLineWriter = objectMapper.writer(oneLinePrinter());

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
        String label = labelText(node.label());
        value = withLabel(value, label);
        value = withSize(value, label);
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
        // Envelope fields in canonical order (only the present ones); arrays render one element per line,
        // scalar/object fields render compactly on a single line.
        LinkedHashMap<String, JsonNode> fields = new LinkedHashMap<>();
        fields.put("options", options != null ? options : defaultOptions());
        if (graphValue != null) fields.put("value", graphValue);
        fields.put("nodes", nodes);
        fields.put("edges", edges);
        if (hyperedges != null) fields.put("hyperedges", hyperedges);
        if (diagnostics != null) fields.put("diagnostics", diagnostics);
        if (debug != null) fields.put("debug", debug);

        try {
            outputSink.write(render(fields));
        } catch (IOException e) {
            throw new RuntimeException("Failed to write grale output", e);
        }
    }

    /**
     * Pretty-print the grale envelope: one top-level field per line, but each element of an array
     * ({@code nodes}, {@code edges}, …) on its own single line (e.g.
     * {@code { "v": "2", "value": { "width": 25, "height": 27 } }}). Object/scalar fields are rendered
     * compactly inline too.
     */
    private String render(LinkedHashMap<String, JsonNode> fields) throws JsonProcessingException {
        StringBuilder sb = new StringBuilder("{\n");
        int i = 0;
        int n = fields.size();
        for (Map.Entry<String, JsonNode> e : fields.entrySet()) {
            boolean last = (++i == n);
            sb.append("  \"").append(e.getKey()).append("\": ");
            JsonNode v = e.getValue();
            if (v.isArray()) {
                ArrayNode arr = (ArrayNode) v;
                if (arr.isEmpty()) {
                    sb.append("[]");
                } else {
                    sb.append("[\n");
                    for (int j = 0; j < arr.size(); j++) {
                        sb.append("    ").append(oneLineWriter.writeValueAsString(arr.get(j)));
                        sb.append(j < arr.size() - 1 ? ",\n" : "\n");
                    }
                    sb.append("  ]");
                }
            } else {
                sb.append(oneLineWriter.writeValueAsString(v));
            }
            sb.append(last ? "\n" : ",\n");
        }
        return sb.append("}\n").toString();
    }

    /** A pretty-printer that keeps a whole value on one line: {@code { "k": v, "k2": [ … ] }}. */
    private static DefaultPrettyPrinter oneLinePrinter() {
        Separators separators = Separators.createDefaultInstance()
                .withObjectFieldValueSpacing(Separators.Spacing.AFTER);   // "key": value
        DefaultPrettyPrinter pp = new DefaultPrettyPrinter(separators);
        pp.indentObjectsWith(DefaultPrettyPrinter.FixedSpaceIndenter.instance);  // "{ … }" inline
        pp.indentArraysWith(DefaultPrettyPrinter.FixedSpaceIndenter.instance);   // "[ … ]" inline
        return pp;
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
     * Supply dagre {@code width}/{@code height} at the grale {@code value} top level. They come from the
     * CJ node's {@code data.size} (the canonical home, written as {@code {width, height}}); when that is
     * absent they are estimated from {@code label} via {@link RobotoLabelMetrics}. Author-provided sizes
     * are never overwritten; a node with neither a size nor a label is left as-is.
     */
    private @Nullable ObjectNode withSize(@Nullable ObjectNode value, @Nullable String label) {
        // unfold the canonical CJ data.size {width, height} onto the dagre top-level width/height
        if (value != null && value.get("size") instanceof ObjectNode size) {
            if (!value.has("width") && size.has("width")) value.set("width", size.get("width"));
            if (!value.has("height") && size.has("height")) value.set("height", size.get("height"));
            value.remove("size");
        }
        if (value != null && value.has("width") && value.has("height")) {
            return value; // sizes already present (author-provided)
        }
        if (label == null) {
            return value; // nothing to estimate from
        }
        RobotoLabelMetrics.Box box = RobotoLabelMetrics.estimate(label);
        ObjectNode v = value != null ? value : objectMapper.createObjectNode();
        if (!v.has("width")) v.put("width", box.width());
        if (!v.has("height")) v.put("height", box.height());
        return v;
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
