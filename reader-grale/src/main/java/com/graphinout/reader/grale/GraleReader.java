package com.graphinout.reader.grale;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ObjectNode;
import com.graphinout.base.cj.document.ICjEdgeChunkMutable;
import com.graphinout.base.cj.document.ICjGraphChunkMutable;
import com.graphinout.base.cj.document.ICjNodeChunkMutable;
import com.graphinout.base.cj.stream.ICjStream;
import com.graphinout.base.gio.GioFileFormat;
import com.graphinout.base.gio.GioReader;
import com.graphinout.base.gio.GioWriter;
import com.graphinout.base.input.InputSource;
import com.graphinout.base.output.OutputSink;
import com.graphinout.foundation.pure.collections.jajson.JsonParser;
import com.graphinout.foundation.pure.input.ContentError;
import com.graphinout.foundation.pure.json.document.IJsonValue;
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.util.function.Consumer;

/**
 * Reads and writes the <b>grale</b> graph-layout-engine JSON format.
 *
 * <p>grale is a strict superset of the dagre / graphlib {@code json.write} envelope:
 * <pre>
 *   { "options": { "directed", "multigraph", "compound" },
 *     "value": &lt;graph label&gt;,
 *     "nodes": [ { "v", "value"?, "parent"? } ],
 *     "edges": [ { "v", "w", "name"?, "id"?, "value"? } ],
 *     "hyperedges"?: [...], "diagnostics"?: {...}, "debug"?: [...] }
 * </pre>
 *
 * <h2>Mapping to Connected JSON</h2>
 * <ul>
 *   <li>each grale node becomes a CJ node: {@code v} → node id and {@code value.meta.label} (when a
 *       string) → the node's native label. The rest of {@code value} becomes the node {@code data},
 *       with the dagre {@code width}/{@code height} folded into a {@code size} sub-object and the
 *       label removed (it now lives in the CJ label);</li>
 *   <li>each grale edge becomes a directed CJ edge: {@code v} → source endpoint, {@code w} → target
 *       endpoint, {@code id} → edge id, {@code value.meta.label} → the edge's native label, and the
 *       rest of the edge {@code value} object → edge {@code data} (label removed);</li>
 *   <li>the envelope-level fields ({@code options}, graph {@code value}, {@code hyperedges},
 *       {@code diagnostics}, {@code debug}) are preserved verbatim on the CJ graph's {@code data}
 *       under the {@code "grale"} key, so a grale → CJ → grale round-trip is faithful.</li>
 * </ul>
 *
 * <p>The rarely-used envelope fields that have no native CJ slot — a node's compound {@code parent}
 * and a multigraph edge's {@code name} — are carried on the element {@code data} under the reserved
 * keys {@link Grale#PARENT_KEY} / {@link Grale#NAME_KEY} and stripped back out on write.
 *
 * <p><b>Node sizing on write:</b> grale needs a node {@code width}/{@code height}. The writer takes
 * them from the CJ node's {@code data.size}; when that is absent but the node has a label (e.g. it
 * came from a format that stores only a label), it estimates the box from the label text via
 * {@link RobotoLabelMetrics} — Roboto at 16&nbsp;px, wrapped at 50 characters, honouring {@code <br>}
 * line breaks. Author-supplied sizes are never overwritten.
 */
public class GraleReader implements GioReader, GioWriter {

    public static final String FORMAT_ID = "grale";
    public static final GioFileFormat FORMAT = new GioFileFormat(FORMAT_ID,
            "grale Graph Layout JSON", ".grale.json", ".grale");

    private final ObjectMapper objectMapper = new ObjectMapper();
    private @Nullable Consumer<ContentError> errorHandler;

    @Override
    public GioFileFormat fileFormat() {
        return FORMAT;
    }

    @Override
    public void setContentErrorHandler(Consumer<ContentError> errorHandler) {
        this.errorHandler = errorHandler;
    }

    @Override
    public ICjStream createCjStream(OutputSink outputSink) {
        return new GraleWriter(outputSink);
    }

    @Override
    public void read(InputSource inputSource, ICjStream cjStream) throws IOException {
        JsonNode root;
        try (InputStream is = inputSource.asSingle().inputStream()) {
            root = objectMapper.readTree(is);
        }
        if (root == null || !root.isObject()) {
            reportError("grale input is not a JSON object");
            return;
        }
        if (!root.has("nodes") && !root.has("edges") && !root.has("options")) {
            reportError("grale input has none of 'options', 'nodes', 'edges'");
            return;
        }

        boolean directed = root.path("options").path("directed").asBoolean(true);

        cjStream.documentStart(cjStream.createDocumentChunk());

        ICjGraphChunkMutable graph = cjStream.createGraphChunk();
        graph.dataJsonValue(toCjJson(buildGraphLevelData(root)));
        cjStream.graphStart(graph);

        JsonNode nodes = root.get("nodes");
        if (nodes != null && nodes.isArray()) {
            for (JsonNode node : nodes) {
                readNode(node, cjStream);
            }
        }

        JsonNode edges = root.get("edges");
        if (edges != null && edges.isArray()) {
            for (JsonNode edge : edges) {
                readEdge(edge, directed, cjStream);
            }
        }

        cjStream.graphEnd();
        cjStream.documentEnd();
    }

    /** Collect the envelope-level fields that are not nodes/edges into a {@code {"grale": {...}}} object. */
    private JsonNode buildGraphLevelData(JsonNode root) {
        ObjectNode grale = objectMapper.createObjectNode();
        for (String key : Grale.GRAPH_LEVEL_KEYS) {
            JsonNode v = root.get(key);
            if (v != null && !v.isNull()) {
                grale.set(key, v);
            }
        }
        ObjectNode data = objectMapper.createObjectNode();
        data.set(Grale.WRAPPER_KEY, grale);
        return data;
    }

    private void readNode(JsonNode node, ICjStream cjStream) {
        if (!node.isObject() || !node.has("v")) {
            reportError("grale node without 'v': " + node);
            return;
        }
        cjStream.node((ICjNodeChunkMutable n) -> {
            n.id(node.get("v").asString());

            ObjectNode value = toNodeData(node.get("value"));

            // carry the compound 'parent' (no native CJ slot) on data, stripped back out on write
            if (node.has("parent") && !node.get("parent").isNull()) {
                value.set(Grale.PARENT_KEY, node.get("parent"));
            }
            if (!value.isEmpty()) {
                n.dataJsonValue(toCjJson(value));
            }

            String label = textLabel(node.get("value"));
            if (label != null) {
                n.addLabelWithoutLanguage(label);
            }
        });
    }

    private void readEdge(JsonNode edge, boolean directed, ICjStream cjStream) {
        if (!edge.isObject() || !edge.has("v") || !edge.has("w")) {
            reportError("grale edge without 'v'/'w': " + edge);
            return;
        }
        cjStream.edge((ICjEdgeChunkMutable e) -> {
            if (edge.has("id") && !edge.get("id").isNull()) {
                e.id(edge.get("id").asString());
            }

            String v = edge.get("v").asString();
            String w = edge.get("w").asString();
            if (directed) {
                e.addEndpointIncoming(v); // source (direction=in)
                e.addEndpointOutgoing(w); // target (direction=out)
            } else {
                e.addEndpointUndirected(v);
                e.addEndpointUndirected(w);
            }

            ObjectNode value = edge.has("value") && edge.get("value").isObject()
                    ? (ObjectNode) edge.get("value").deepCopy()
                    : objectMapper.createObjectNode();
            // the label lives in the CJ edge label, not in data (kept: the rest of meta)
            stripMetaLabel(value);

            // carry the multigraph 'name' (no native CJ slot) on data, stripped back out on write
            if (edge.has("name") && !edge.get("name").isNull()) {
                value.set(Grale.NAME_KEY, edge.get("name"));
            }
            if (!value.isEmpty()) {
                e.dataJsonValue(toCjJson(value));
            }

            String label = textLabel(edge.get("value"));
            if (label != null) {
                e.setLabel(lbl -> lbl.addEntry(entry -> entry.value(label)));
            }
        });
    }

    /**
     * Turn a grale node {@code value} into CJ node {@code data}, moving each field to its canonical CJ
     * home: the dagre {@code width}/{@code height} become a {@code size} sub-object, and
     * {@code meta.label} is dropped because the label is carried by the CJ node's native label (see
     * {@link #readNode}). All other fields (layout output like {@code x}/{@code y}, styling in
     * {@code meta}, …) are kept verbatim so the round-trip stays faithful.
     */
    private ObjectNode toNodeData(@Nullable JsonNode graleValue) {
        ObjectNode value = graleValue != null && graleValue.isObject()
                ? (ObjectNode) graleValue.deepCopy()
                : objectMapper.createObjectNode();

        // width/height -> size {width, height}
        ObjectNode size = objectMapper.createObjectNode();
        if (value.has("width")) size.set("width", value.remove("width"));
        if (value.has("height")) size.set("height", value.remove("height"));
        if (!size.isEmpty()) value.set("size", size);

        // the label lives in the CJ node label, not in data; keep the rest of meta (fill, color, …)
        stripMetaLabel(value);
        return value;
    }

    /** Remove {@code meta.label} (carried by the CJ element label) while keeping the rest of
     * {@code meta}; drop {@code meta} entirely once it is empty. */
    private static void stripMetaLabel(ObjectNode value) {
        if (value.get("meta") instanceof ObjectNode meta) {
            meta.remove("label");
            if (meta.isEmpty()) value.remove("meta");
        }
    }

    /** Extract {@code value.meta.label} when it is a plain string, else {@code null}. */
    private static @Nullable String textLabel(@Nullable JsonNode value) {
        if (value == null || !value.isObject()) return null;
        JsonNode meta = value.get("meta");
        if (meta == null || !meta.isObject()) return null;
        JsonNode label = meta.get("label");
        return label != null && label.isString() ? label.asString() : null;
    }

    /**
     * Convert a Jackson tree into a foundation-native {@link IJsonValue} (via JSON text) so it fires
     * cleanly through the CJ writer machinery.
     */
    private static IJsonValue toCjJson(JsonNode node) {
        return JsonParser.parse(node.toString());
    }

    private void reportError(String message) {
        if (errorHandler != null) {
            errorHandler.accept(ContentError.error(message));
        }
    }
}
