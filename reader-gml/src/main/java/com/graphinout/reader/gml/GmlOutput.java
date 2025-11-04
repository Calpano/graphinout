package com.graphinout.reader.gml;

import com.graphinout.base.cj.document.CjDirection;
import com.graphinout.base.cj.document.ICjDocument;
import com.graphinout.base.cj.document.ICjEdge;
import com.graphinout.base.cj.document.ICjEndpoint;
import com.graphinout.base.cj.document.ICjGraph;
import com.graphinout.base.cj.document.ICjNode;
import com.graphinout.foundation.json.value.IJsonArray;
import com.graphinout.foundation.json.value.IJsonObject;
import com.graphinout.foundation.json.value.IJsonValue;
import org.slf4j.Logger;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Consumer;

import static org.slf4j.LoggerFactory.getLogger;

public record GmlOutput(ICjDocument cjDoc) {

    public static final String EDGE = "edge";
    public static final String GRAPH = "graph";
    public static final String NAME = "name";
    public static final String DIRECTED = "directed";
    public static final String HIERARCHIC = "hierarchic";
    public static final String NODE = "node";
    private static final Logger log = getLogger(GmlOutput.class);

    private static void documentToGml(ICjDocument cjDoc, IGmlHandler b) {
        // Document-level attributes (generic JSON emission)
        cjDoc.data(data -> {
            IJsonValue json = data.jsonValue();
            IJsonObject obj = json == null ? null : json.asObjectOrNull();
            if (obj != null) {
                emitJsonObjectProperties(obj, b, 0, java.util.Set.of());
            }
        });

        // Graph(s)
        List<ICjGraph> graphs = cjDoc.graphs().toList();
        for (ICjGraph g : graphs) {
            graphToGml(g, b);
        }
    }

    private static void edgeToGml(ICjEdge cjEdge, IGmlHandler b) {
        Map<String, String> attributes = new TreeMap<>();
        ICjEndpoint inEp = cjEdge.endpoints().filter(ep -> ep.direction() == CjDirection.IN).findFirst().orElse(null);
        ICjEndpoint outEp = cjEdge.endpoints().filter(ep -> ep.direction() == CjDirection.OUT).findFirst().orElse(null);

        if (outEp != null && inEp != null) {
            attributes.put("source", formatValue(outEp.node()));
            attributes.put("target", formatValue(inEp.node()));
        } else {
            List<ICjEndpoint> eps = cjEdge.endpoints().toList();
            if (eps.size() == 2) {
                attributes.put("source", formatValue(eps.get(0).node()));
                attributes.put("target", formatValue(eps.get(1).node()));
            } else {
                log.warn("Cannot represent hyper-edge in GML");
                return;
            }
        }

        cjEdge.labelEntries().stream().findFirst().ifPresent(label -> attributes.put("label", formatValue(label.value())));

        // Collect additional edge data properties (generic)
        b.key(EDGE);
        b.open();
        // flat mandatory first
        attributes.forEach((key, value) -> {
            b.key(key);
            b.value(value);
        });

        cjEdge.data(data -> {
            IJsonValue json = data.jsonValue();
            IJsonObject obj = json == null ? null : json.asObjectOrNull();
            if (obj != null) {
                // skip keys already emitted as mandatory
                java.util.Set<String> skip = java.util.Set.of("source", "target", "label");
                emitJsonObjectProperties(obj, b, 2, skip);
            }
        });

        b.close();
    }

    private static void emitJsonEntry(String key, IJsonValue val, IGmlHandler b, int indentLevel) {
        String indent = "  ".repeat(indentLevel);
        switch (val.jsonType().valueType()) {
            case Primitive -> {
                String s = val.asPrimitive().toJavaString();
                b.key(key);
                b.value(formatValue(s));
            }
            case Object -> {
                b.key(key);
                b.open();
                emitJsonObjectProperties(val.asObject(), b, indentLevel + 1, java.util.Set.of());
                b.close();
            }
            case Array -> {
                IJsonArray arr = val.asArray();
                for (int i = 0; i < arr.size(); i++) {
                    IJsonValue e = arr.get(i);
                    if (e.isPrimitive()) {
                        String s = e.asPrimitive().toJavaString();
                        b.key(key);
                        b.value(formatValue(s));
                    } else if (e.isObject()) {
                        b.key(key);
                        b.open();
                        emitJsonObjectProperties(e.asObject(), b, indentLevel + 1, java.util.Set.of());
                        b.close();
                    } else if (e.isArray()) {
                        // Nested arrays: represent as repeated key blocks recursively
                        emitJsonEntry(key, e, b, indentLevel);
                    }
                }
            }
        }
    }

    private static void emitJsonObjectProperties(IJsonObject obj, IGmlHandler b, int indentLevel, java.util.Set<String> skipKeys) {
        List<String> keys = obj.keys().stream().filter(k -> !skipKeys.contains(k)).sorted().toList();
        for (String key : keys) {
            IJsonValue val = obj.get_(key);
            emitJsonEntry(key, val, b, indentLevel);
        }
    }

    private static String formatValue(String v) {
        if (v == null) return "\"\"";

        try {
            double d = Double.parseDouble(v);
            if (d == (long) d) { // Check if it's an integer value (e.g., 1.0, 2.0)
                return String.valueOf((long) d);
            } else {
                return String.valueOf(d);
            }
        } catch (NumberFormatException e) {
            // Not a number, so quote it
            return "\"" + v + "\"";
        }
    }

    private static void graphToGml(ICjGraph cjGraph, IGmlHandler b) {
        b.key(GRAPH);
        b.open();
        // Graph-level attributes from labels and data
        cjGraph.labelEntries().stream().findFirst().ifPresent(label -> //
        {
            b.key(NAME);
            b.value(formatValue(label.value()));
        });
        cjGraph.data(data -> {
            IJsonValue json = data.jsonValue();
            IJsonObject obj = json == null ? null : json.asObjectOrNull();
            if (obj != null) {
                java.util.Set<String> skip = new java.util.HashSet<>();
                // flat known graph attributes
                Consumer<String> onProp = prop -> {
                    if (obj.hasProperty(prop) && obj.get_(prop).isPrimitive()) {
                        String s = obj.get_(prop).asPrimitive().toJavaString();
                        b.key(prop);
                        b.value(formatValue(s));
                        skip.add(prop);
                    }
                };
                onProp.accept(NAME);
                onProp.accept(DIRECTED);
                onProp.accept(HIERARCHIC);
                emitJsonObjectProperties(obj, b, 1, skip);
            }
        });

        // Nodes section
        cjGraph.nodes().sorted(Comparator.comparing(ICjNode::id)).forEach(cjNode -> nodeToGml(cjNode, b));

        // Edges section
        cjGraph.edges().sorted(Comparator.comparing(ICjEdge::id)).forEach(cjEdge -> edgeToGml(cjEdge, b));
        b.close();
    }

    private static void nodeToGml(ICjNode cjNode, IGmlHandler b) {
        String id = cjNode.id();
        if (id == null) {
            log.warn("Skip node without id");
            return;
        }

        b.key(NODE);
        b.open();
        // mandatory flat attrs
        b.key("id");
        b.value(formatValue(id));
        cjNode.labelEntries().stream().findFirst().ifPresent(label -> //
        {
            b.key("label");
            b.value(formatValue(label.value()));
        });

        // Generic JSON
        cjNode.data(data -> {
            IJsonValue json = data.jsonValue();
            IJsonObject obj = json == null ? null : json.asObjectOrNull();
            if (obj != null) {
                java.util.Set<String> skip = java.util.Set.of("id", "label");
                emitJsonObjectProperties(obj, b, 2, skip);
            }
        });
        b.close();
    }

    public String toGml() {
        GmlStringHandler gmlStringHandler = new GmlStringHandler();
        documentToGml(cjDoc, gmlStringHandler);
        return gmlStringHandler.result();
    }

    public List<Object> toGmlList() {
        GmlListHandler gmlListHandler = new GmlListHandler();
        documentToGml(cjDoc, gmlListHandler);
        return gmlListHandler.list();
    }

}
