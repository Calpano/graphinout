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
import java.util.Set;
import java.util.function.Consumer;

import static com.graphinout.reader.gml.Gml.DIRECTED;
import static com.graphinout.reader.gml.Gml.EDGE;
import static com.graphinout.reader.gml.Gml.GRAPH;
import static com.graphinout.reader.gml.Gml.HIERARCHIC;
import static com.graphinout.reader.gml.Gml.LABEL;
import static com.graphinout.reader.gml.Gml.NODE;
import static org.slf4j.LoggerFactory.getLogger;

public record GmlOutput(ICjDocument cjDoc) {

    private static final Logger log = getLogger(GmlOutput.class);

    private static boolean allElementsSimpleObjects(IJsonArray arr) {
        for (int i = 0; i < arr.size(); i++) {
            IJsonValue e = arr.get_(i);
            if (!e.isObject()) return false;
            IJsonObject o = e.asObject();
            for (String k : o.keys()) {
                IJsonValue v = o.get_(k);
                if (!v.isPrimitive()) return false;
            }
        }
        return !arr.isEmpty();
    }

    private static void documentToGml(ICjDocument cjDoc, IGmlHandler b) {
        // Document-level attributes (generic JSON emission)
        cjDoc.data(data -> {
            IJsonValue json = data.jsonValue();
            IJsonObject obj = json == null ? null : json.asObjectOrNull();
            if (obj != null) {
                emitJsonObjectProperties(obj, b, java.util.Set.of());
            }
        });

        // Graph(s)
        List<ICjGraph> graphs = cjDoc.graphs().toList();
        for (ICjGraph g : graphs) {
            graphToGml(g, b);
        }
    }

    private static void edgeToGml(ICjEdge cjEdge, IGmlHandler b) {
        // Preserve order of mandatory attributes
        Map<String, String> attributes = new java.util.LinkedHashMap<>();
        ICjEndpoint inEp = cjEdge.endpoints().filter(ep -> ep.direction() == CjDirection.IN).findFirst().orElse(null);
        ICjEndpoint outEp = cjEdge.endpoints().filter(ep -> ep.direction() == CjDirection.OUT).findFirst().orElse(null);

        if (outEp != null && inEp != null) {
            attributes.put(Gml.SOURCE, formatValue(inEp.node()));
            attributes.put(Gml.TARGET, formatValue(outEp.node()));
        } else {
            List<ICjEndpoint> eps = cjEdge.endpoints().toList();
            if (eps.size() == 2) {
                attributes.put(Gml.SOURCE, formatValue(eps.get(0).node()));
                attributes.put(Gml.TARGET, formatValue(eps.get(1).node()));
            } else {
                log.warn("Cannot represent hyper-edge in GML");
                return;
            }
        }

        cjEdge.labelEntries().stream().findFirst().ifPresent(label -> attributes.put(Gml.LABEL, formatValue(label.value())));

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
                Set<String> skip = java.util.Set.of(Gml.SOURCE, Gml.TARGET, Gml.LABEL);
                emitJsonObjectPropertiesPreferred(obj, b, skip, java.util.List.of("graphics", "LabelGraphics"));
            }
        });

        b.close();
    }

    private static void emitJsonEntry(String key, IJsonValue val, IGmlHandler b) {
        switch (val.jsonType().valueType()) {
            case Primitive -> {
                String s = val.asPrimitive().toJavaString();
                b.key(key);
                b.value(formatValue(s));
            }
            case Object -> {
                b.key(key);
                b.open();
                emitJsonObjectProperties(val.asObject(), b, java.util.Set.of());
                b.close();
            }
            case Array -> {
                // repeat key for each value
                val.asArray().forEach(v -> emitJsonEntry(key, v, b));
            }
        }
    }

    /** Preserves original key order from the underlying JSON object; do not sort */
    private static void emitJsonObjectProperties(IJsonObject obj, IGmlHandler b, Set<String> skipKeys) {
        List<String> keys = obj.keys().stream().filter(k -> !skipKeys.contains(k)).toList();
        for (String key : keys) {
            IJsonValue val = obj.get_(key);
            emitJsonEntry(key, val, b);
        }
    }

    private static void emitJsonObjectPropertiesPreferred(IJsonObject obj, IGmlHandler b, Set<String> skipKeys, List<String> preferredFirst) {
        java.util.LinkedHashSet<String> emitted = new java.util.LinkedHashSet<>();
        for (String p : preferredFirst) {
            if (skipKeys.contains(p)) continue;
            if (obj.hasProperty(p)) {
                emitJsonEntry(p, obj.get_(p), b);
                emitted.add(p);
            }
        }
        // Emit remaining keys in original order
        List<String> keys = obj.keys().stream().filter(k -> !skipKeys.contains(k) && !emitted.contains(k)).toList();
        for (String key : keys) {
            emitJsonEntry(key, obj.get_(key), b);
        }
    }

    private static String formatValue(String v) {
        // Return raw token text for handler; quoting and numeric formatting are handler responsibilities
        return v == null ? "" : v;
    }

    private static void graphToGml(ICjGraph cjGraph, IGmlHandler b) {
        b.key(GRAPH);
        b.open();
        // Graph-level attributes from labels and data
        cjGraph.labelEntries().stream().findFirst().ifPresent(label -> //
        {
            b.key(LABEL);
            b.value(formatValue(label.value()));
        });
        cjGraph.data(data -> {
            IJsonValue json = data.jsonValue();
            IJsonObject obj = json == null ? null : json.asObjectOrNull();
            if (obj != null) {
                Set<String> skip = new java.util.HashSet<>();
                // flat known graph attributes
                Consumer<String> onProp = prop -> {
                    if (obj.hasProperty(prop) && obj.get_(prop).isPrimitive()) {
                        String s = obj.get_(prop).asPrimitive().toJavaString();
                        b.key(prop);
                        b.value(formatValue(s));
                        skip.add(prop);
                    }
                };
                onProp.accept(LABEL);
                // Emit known attributes in preferred order matching samples
                onProp.accept(HIERARCHIC);
                onProp.accept(DIRECTED);
                emitJsonObjectProperties(obj, b, skip);
            }
        });

        // Nodes section
        Comparator<String> numericStr = Comparator.nullsLast((s1, s2) -> {
            if (s1 == s2) return 0;
            if (s1 == null) return 1;
            if (s2 == null) return -1;
            try {
                double da = Double.parseDouble(s1);
                double db = Double.parseDouble(s2);
                return Double.compare(da, db);
            } catch (NumberFormatException e) {
                return s1.compareTo(s2);
            }
        });
        cjGraph.nodes().sorted(Comparator.comparing(ICjNode::id, numericStr)).forEach(cjNode -> nodeToGml(cjNode, b));

        // Edges section
        cjGraph.edges().sorted(Comparator.comparing(ICjEdge::id, numericStr)).forEach(cjEdge -> edgeToGml(cjEdge, b));
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
            b.key(Gml.LABEL);
            b.value(formatValue(label.value()));
        });

        // Generic JSON
        cjNode.data(data -> {
            IJsonValue json = data.jsonValue();
            IJsonObject obj = json == null ? null : json.asObjectOrNull();
            if (obj != null) {
                Set<String> skip = java.util.Set.of("id", Gml.LABEL);
                emitJsonObjectPropertiesPreferred(obj, b, skip, java.util.List.of("graphics", "LabelGraphics", "group", "fill", "border"));
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
