package com.graphinout.reader.ocif.document.extension.canvas;

import com.graphinout.foundation.pure.json.document.IJsonArray;
import com.graphinout.foundation.pure.json.document.IJsonArrayMutable;
import com.graphinout.foundation.pure.json.document.IJsonObject;
import com.graphinout.foundation.pure.json.document.IJsonObjectMutable;
import com.graphinout.foundation.pure.json.document.IJsonValue;
import com.graphinout.reader.ocif.document.extension.OcifExtension;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.graphinout.reader.ocif.Ocifs.factory;

/**
 * An OCIF canvas extension that preserves the Connected JSON (CJ) <em>graph tree</em> so that a
 * {@code CJ -> OCIF -> CJ} round-trip retains:
 * <ul>
 *     <li><b>multiple-graphs-per-document</b> — OCIF has a single flat canvas (nodes + relations); the original CJ
 *     forest of top-level graphs is recorded here and rebuilt on read.</li>
 *     <li><b>nested-graphs-in-nodes</b> — graphs nested inside a CJ node, which OCIF cannot natively nest, are recorded
 *     per node id ({@link #nodeGraphs}).</li>
 *     <li><b>attributes-on-graphs</b> — graph-level {@code data}, which OCIF has no slot for, is recorded per graph.</li>
 * </ul>
 *
 * <p>The flat OCIF {@code nodes}/{@code relations} arrays remain authoritative for the node/edge payloads; this
 * extension only records the <em>skeleton</em> (graph ids, graph data, and which node/edge ids live in which graph,
 * plus nesting) needed to reassemble the tree.
 */
public class CjGraphStructureCanvasExtension extends OcifExtension implements IOcifCanvasExtension {

    public static final String TYPE_NAME = "@connected-json/structure";
    public static final String TYPE_URI = "https://j-s-o-n.org/ocif-structure/schema.json";

    private static final String GRAPHS = "graphs";
    private static final String NODE_GRAPHS = "nodeGraphs";
    private static final String ID = "id";
    private static final String DATA = "data";
    private static final String NODE_IDS = "nodeIds";
    private static final String EDGE_IDS = "edgeIds";

    /** A recorded CJ graph (skeleton only). */
    public static final class GraphInfo {
        public @Nullable String id;
        public @Nullable IJsonValue data;
        public final List<String> nodeIds = new ArrayList<>();
        public final List<String> edgeIds = new ArrayList<>();
        public final List<GraphInfo> graphs = new ArrayList<>();
    }

    /** Top-level CJ graph forest. */
    private final List<GraphInfo> graphs = new ArrayList<>();

    /** node-id -> graph forest nested in that node. */
    private final Map<String, List<GraphInfo>> nodeGraphs = new LinkedHashMap<>();

    public CjGraphStructureCanvasExtension() {
        super(TYPE_URI, TYPE_NAME);
    }

    public static @NonNull CjGraphStructureCanvasExtension of(@NonNull IJsonObject obj) {
        CjGraphStructureCanvasExtension ext = new CjGraphStructureCanvasExtension();
        IJsonValue g = obj.get(GRAPHS);
        if (g != null && g.isArray()) {
            ext.graphs.addAll(parseGraphArray(g.asArray()));
        }
        IJsonValue ng = obj.get(NODE_GRAPHS);
        if (ng != null && ng.isObject()) {
            ng.asObject().forEach((nodeId, v) -> {
                if (v.isArray()) {
                    ext.nodeGraphs.put(nodeId, parseGraphArray(v.asArray()));
                }
            });
        }
        return ext;
    }

    private static List<GraphInfo> parseGraphArray(IJsonArray arr) {
        List<GraphInfo> out = new ArrayList<>();
        for (int i = 0; i < arr.size(); i++) {
            IJsonValue v = arr.get_(i);
            if (v.isObject()) {
                out.add(parseGraph(v.asObject()));
            }
        }
        return out;
    }

    private static GraphInfo parseGraph(IJsonObject o) {
        GraphInfo gi = new GraphInfo();
        IJsonValue id = o.get(ID);
        if (id != null && id.isString()) gi.id = id.asString();
        IJsonValue data = o.get(DATA);
        if (data != null) gi.data = data;
        addStrings(o.get(NODE_IDS), gi.nodeIds);
        addStrings(o.get(EDGE_IDS), gi.edgeIds);
        IJsonValue sub = o.get(GRAPHS);
        if (sub != null && sub.isArray()) gi.graphs.addAll(parseGraphArray(sub.asArray()));
        return gi;
    }

    private static void addStrings(@Nullable IJsonValue v, List<String> out) {
        if (v != null && v.isArray()) {
            IJsonArray a = v.asArray();
            for (int i = 0; i < a.size(); i++) {
                IJsonValue iv = a.get_(i);
                if (iv.isString()) out.add(iv.asString());
            }
        }
    }

    public List<GraphInfo> graphs() {
        return graphs;
    }

    public Map<String, List<GraphInfo>> nodeGraphs() {
        return nodeGraphs;
    }

    public boolean isEmpty() {
        return graphs.isEmpty() && nodeGraphs.isEmpty();
    }

    @Override
    public Set<String> definedKeys() {
        return Set.of(GRAPHS, NODE_GRAPHS);
    }

    @Override
    public @NonNull IJsonObject toJson() {
        IJsonObjectMutable o = factory().createObjectMutable();
        o.setString(TYPE, TYPE_NAME);
        o.setArray(GRAPHS, graphArrayToJson(graphs));
        if (!nodeGraphs.isEmpty()) {
            IJsonObjectMutable ng = factory().createObjectMutable();
            nodeGraphs.forEach((nodeId, list) -> ng.setArray(nodeId, graphArrayToJson(list)));
            o.setObject(NODE_GRAPHS, ng);
        }
        return o;
    }

    private static IJsonArrayMutable graphArrayToJson(List<GraphInfo> list) {
        IJsonArrayMutable arr = factory().createArrayMutable();
        for (GraphInfo gi : list) {
            arr.add(graphToJson(gi));
        }
        return arr;
    }

    private static IJsonObjectMutable graphToJson(GraphInfo gi) {
        IJsonObjectMutable o = factory().createObjectMutable();
        if (gi.id != null) o.setString(ID, gi.id);
        if (gi.data != null) o.setProperty(DATA, gi.data);
        if (!gi.nodeIds.isEmpty()) {
            IJsonArrayMutable a = factory().createArrayMutable();
            gi.nodeIds.forEach(a::add);
            o.setArray(NODE_IDS, a);
        }
        if (!gi.edgeIds.isEmpty()) {
            IJsonArrayMutable a = factory().createArrayMutable();
            gi.edgeIds.forEach(a::add);
            o.setArray(EDGE_IDS, a);
        }
        if (!gi.graphs.isEmpty()) {
            o.setArray(GRAPHS, graphArrayToJson(gi.graphs));
        }
        return o;
    }
}
