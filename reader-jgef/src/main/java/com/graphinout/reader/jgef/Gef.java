package com.graphinout.reader.jgef;

import com.graphinout.foundation.pure.collections.jajson.JaJson;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

/**
 * Normalizes <a href="https://j-s-o-n.org/connected-json/8.0.0/json-graph-entry-format/">JSON Graph Entry Format
 * (GEF)</a> into canonical Connected JSON, so the strict CJ reader can consume it. GEF is a lenient superset of CJ:
 * every CJ file is already valid GEF.
 *
 * <p>Handled here: <b>graph-at-root</b> (top-level {@code nodes}/{@code edges} are wrapped into {@code graphs[]}),
 * {@code source}/{@code target} (and {@code sources}/{@code targets}) shortcuts → {@code endpoints}, singular aliases
 * ({@code node}/{@code edge}/{@code graph}), string/array <b>label shorthand</b> → {@code {entries:[…]}}, numeric
 * ids/refs → strings, string ports → {@code {id}}, and unknown keys → {@code data}.
 *
 * <p>Not yet handled: per-edge {@code directed}/{@code edgeDefault} (a {@code source}/{@code target} pair is read as a
 * directed out→in edge).
 */
public final class Gef {

    private static final Set<String> DOC_KEYS = Set.of("$schema", "$id", "connectedJson", "@context");
    private static final Set<String> GRAPH_RESERVED = Set.of("id", "label", "data", "nodes", "edges", "graphs", "meta");
    private static final Set<String> NODE_RESERVED = Set.of("id", "label", "data", "ports", "graphs", "types", "node-types");
    private static final Set<String> EDGE_RESERVED = Set.of("id", "label", "data", "type", "endpoints", "graphs");
    private static final Set<String> ENDPOINT_RESERVED = Set.of("node", "port", "direction", "type", "data");

    private Gef() {}

    /** GEF JSON text → canonical Connected JSON text. */
    public static String toConnectedJson(String gefJson) {
        return JaJson.toJsonString(normalizeRoot(JaJson.parse(gefJson)));
    }

    @SuppressWarnings("unchecked")
    static Object normalizeRoot(Object root) {
        if (root instanceof List) { // a bare array of graphs
            forEachMap(root, Gef::normalizeGraph);
            Map<String, Object> doc = new LinkedHashMap<>();
            doc.put("graphs", root);
            return doc;
        }
        if (!(root instanceof Map)) {
            return root;
        }
        Map<String, Object> r = (Map<String, Object>) root;
        pluralize(r);
        if (r.containsKey("nodes") || r.containsKey("edges")) { // graph-at-root: the document IS a graph
            Map<String, Object> doc = new LinkedHashMap<>();
            Map<String, Object> graph = new LinkedHashMap<>();
            r.forEach((k, v) -> (DOC_KEYS.contains(k) ? doc : graph).put(k, v));
            normalizeGraph(graph);
            List<Object> graphs = new ArrayList<>();
            graphs.add(graph);
            doc.put("graphs", graphs);
            return doc;
        }
        forEachMap(r.get("graphs"), Gef::normalizeGraph); // already a CJ-shaped document
        return r;
    }

    private static void normalizeGraph(Map<String, Object> g) {
        pluralize(g);
        coerceId(g, "id");
        normalizeLabel(g);
        forEachMap(g.get("nodes"), Gef::normalizeNode);
        forEachMap(g.get("edges"), Gef::normalizeEdge);
        forEachMap(g.get("graphs"), Gef::normalizeGraph);
        moveUnknownToData(g, GRAPH_RESERVED);
    }

    private static void normalizeNode(Map<String, Object> n) {
        pluralize(n);
        coerceId(n, "id");
        normalizeLabel(n);
        normalizePorts(n.get("ports"));
        forEachMap(n.get("graphs"), Gef::normalizeGraph);
        moveUnknownToData(n, NODE_RESERVED);
    }

    private static void normalizeEdge(Map<String, Object> e) {
        pluralize(e);
        List<Object> endpoints = asMutableList(e.get("endpoints"));
        addEndpoints(endpoints, e.remove("source"), "out");
        addEndpoints(endpoints, e.remove("sources"), "out");
        addEndpoints(endpoints, e.remove("target"), "in");
        addEndpoints(endpoints, e.remove("targets"), "in");
        if (!endpoints.isEmpty()) {
            e.put("endpoints", endpoints);
        }
        forEachMap(e.get("endpoints"), Gef::normalizeEndpoint);
        coerceId(e, "id");
        normalizeLabel(e);
        forEachMap(e.get("graphs"), Gef::normalizeGraph);
        moveUnknownToData(e, EDGE_RESERVED);
    }

    private static void normalizeEndpoint(Map<String, Object> ep) {
        coerceId(ep, "node");
        coerceId(ep, "port");
        moveUnknownToData(ep, ENDPOINT_RESERVED);
    }

    // -- shortcuts ---------------------------------------------------------------------------------------------------

    private static void addEndpoints(List<Object> endpoints, Object refs, String direction) {
        if (refs == null) {
            return;
        }
        if (refs instanceof List) {
            for (Object ref : (List<?>) refs) {
                endpoints.add(endpoint(ref, direction));
            }
        } else {
            endpoints.add(endpoint(refs, direction));
        }
    }

    private static Map<String, Object> endpoint(Object nodeRef, String direction) {
        Map<String, Object> ep = new LinkedHashMap<>();
        ep.put("direction", direction);
        ep.put("node", str(nodeRef));
        return ep;
    }

    @SuppressWarnings("unchecked")
    private static void normalizePorts(Object ports) {
        if (!(ports instanceof List)) {
            return;
        }
        List<Object> list = (List<Object>) ports;
        for (int i = 0; i < list.size(); i++) {
            Object p = list.get(i);
            if (p instanceof String) {
                Map<String, Object> port = new LinkedHashMap<>();
                port.put("id", p);
                list.set(i, port);
            } else if (p instanceof Map) {
                Map<String, Object> port = (Map<String, Object>) p;
                coerceId(port, "id");
                normalizeLabel(port);
                normalizePorts(port.get("ports"));
            }
        }
    }

    @SuppressWarnings("unchecked")
    private static void normalizeLabel(Map<String, Object> owner) {
        Object label = owner.get("label");
        if (label instanceof String) {
            owner.put("label", entries(singletonList(valueEntry((String) label))));
        } else if (label instanceof List) {
            owner.put("label", entries((List<Object>) label));
        } else if (label instanceof Map) {
            Map<String, Object> m = (Map<String, Object>) label;
            if (!m.containsKey("entries") && m.containsKey("value")) {
                owner.put("label", entries(singletonList(label)));
            }
        }
    }

    private static Map<String, Object> entries(List<Object> entryList) {
        Map<String, Object> label = new LinkedHashMap<>();
        label.put("entries", entryList);
        return label;
    }

    private static Map<String, Object> valueEntry(String value) {
        Map<String, Object> entry = new LinkedHashMap<>();
        entry.put("value", value);
        return entry;
    }

    // -- aliases / coercion ------------------------------------------------------------------------------------------

    private static void pluralize(Map<String, Object> m) {
        rename(m, "node", "nodes");
        rename(m, "edge", "edges");
        rename(m, "graph", "graphs");
    }

    private static void rename(Map<String, Object> m, String from, String to) {
        if (m.containsKey(from) && !m.containsKey(to)) {
            Object v = m.remove(from);
            m.put(to, v instanceof List ? v : singletonList(v)); // "graph"/"node"/"edge" singular → array
        }
    }

    private static void coerceId(Map<String, Object> m, String key) {
        Object v = m.get(key);
        if (v instanceof Number) {
            m.put(key, v.toString());
        }
    }

    private static String str(Object o) {
        return o == null ? null : o.toString();
    }

    @SuppressWarnings("unchecked")
    private static void moveUnknownToData(Map<String, Object> m, Set<String> reserved) {
        List<String> unknown = new ArrayList<>();
        for (String k : m.keySet()) {
            if (!reserved.contains(k)) {
                unknown.add(k);
            }
        }
        if (unknown.isEmpty()) {
            return;
        }
        Object existing = m.get("data");
        Map<String, Object> data = existing instanceof Map ? (Map<String, Object>) existing : new LinkedHashMap<>();
        for (String k : unknown) {
            data.put(k, m.remove(k));
        }
        if (!data.isEmpty()) {
            m.put("data", data);
        }
    }

    // -- small helpers -----------------------------------------------------------------------------------------------

    @SuppressWarnings("unchecked")
    private static List<Object> asMutableList(Object o) {
        return o instanceof List ? (List<Object>) o : new ArrayList<>();
    }

    private static List<Object> singletonList(Object o) {
        List<Object> l = new ArrayList<>();
        l.add(o);
        return l;
    }

    @SuppressWarnings("unchecked")
    private static void forEachMap(Object listObj, Consumer<Map<String, Object>> fn) {
        if (listObj instanceof List) {
            for (Object item : (List<?>) listObj) {
                if (item instanceof Map) {
                    fn.accept((Map<String, Object>) item);
                }
            }
        }
    }
}
