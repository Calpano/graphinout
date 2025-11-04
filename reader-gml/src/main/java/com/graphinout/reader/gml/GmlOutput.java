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

import static org.slf4j.LoggerFactory.getLogger;

public record GmlOutput(ICjDocument cjDoc) {

    private static final Logger log = getLogger(GmlOutput.class);

    private static void documentToGml(ICjDocument cjDoc, StringBuilder b) {
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

    private static void edgeToGml(ICjEdge cjEdge, StringBuilder b) {
        Map<String, String> attributes = new TreeMap<>();
        ICjEndpoint inEp = cjEdge.endpoints().filter(ep -> ep.direction() == CjDirection.IN).findFirst().orElse(null);
        ICjEndpoint outEp = cjEdge.endpoints().filter(ep -> ep.direction() == CjDirection.OUT).findFirst().orElse(null);

        if (outEp != null && inEp != null) {
            attributes.put("source", outEp.node());
            attributes.put("target", inEp.node());
        } else {
            List<ICjEndpoint> eps = cjEdge.endpoints().toList();
            if (eps.size() == 2) {
                attributes.put("source", eps.get(0).node());
                attributes.put("target", eps.get(1).node());
            } else {
                log.warn("Cannot represent hyper-edge in GML");
                return;
            }
        }

        cjEdge.labelEntries().stream().findFirst().ifPresent(label -> attributes.put("label", formatValue(label.value())));

        // Collect additional edge data properties (generic)
        b.append("  edge [\n");
        // flat mandatory first
        attributes.forEach((key, value) -> b.append("    ").append(key).append(" ").append(value).append("\n"));

        cjEdge.data(data -> {
            IJsonValue json = data.jsonValue();
            IJsonObject obj = json == null ? null : json.asObjectOrNull();
            if (obj != null) {
                // skip keys already emitted as mandatory
                java.util.Set<String> skip = java.util.Set.of("source", "target", "label");
                emitJsonObjectProperties(obj, b, 2, skip);
            }
        });

        b.append("  ]\n");
    }

    private static String formatValue(String v) {
        if (v == null) return "\"\"";
        // If numeric (int or decimal), print without quotes
        if (v.matches("-?\\d+(\\.\\d+)?")) {
            return v;
        }
        // Otherwise, quote
        return "\"" + v + "\"";
    }

    private static void graphToGml(ICjGraph cjGraph, StringBuilder b) {
        b.append("graph\n");
        b.append("[\n");
        // Graph-level attributes from labels and data
        cjGraph.labelEntries().stream().findFirst().ifPresent(label -> //
                b.append("  name ").append(formatValue(label.value())).append("\n"));
        cjGraph.data(data -> {
            IJsonValue json = data.jsonValue();
            IJsonObject obj = json == null ? null : json.asObjectOrNull();
            if (obj != null) {
                java.util.Set<String> skip = new java.util.HashSet<>();
                // flat known graph attributes
                if (obj.hasProperty("name") && obj.get_("name").isPrimitive()) {
                    String s = obj.get_("name").asPrimitive().toJavaString();
                    b.append("  name ").append(formatValue(s)).append("\n");
                    skip.add("name");
                }
                if (obj.hasProperty("directed") && obj.get_("directed").isPrimitive()) {
                    String s = obj.get_("directed").asPrimitive().toJavaString();
                    b.append("  directed ").append(formatValue(s)).append("\n");
                    skip.add("directed");
                }
                if (obj.hasProperty("hierarchic") && obj.get_("hierarchic").isPrimitive()) {
                    String s = obj.get_("hierarchic").asPrimitive().toJavaString();
                    b.append("  hierarchic  ").append(formatValue(s)).append("\n");
                    skip.add("hierarchic");
                }
                emitJsonObjectProperties(obj, b, 1, skip);
            }
        });

        // Nodes section
        cjGraph.nodes().sorted(Comparator.comparing(ICjNode::id)).forEach(cjNode -> nodeToGml(cjNode, b));

        // Edges section
        cjGraph.edges().forEach(cjEdge -> edgeToGml(cjEdge, b));
        b.append("]\n");
    }

    private static void nodeToGml(ICjNode cjNode, StringBuilder b) {
        String id = cjNode.id();
        if (id == null) {
            log.warn("Skip node without id");
            return;
        }

        b.append("  node [\n");
        // mandatory flat attrs
        b.append("    id ").append(id).append("\n");
        cjNode.labelEntries().stream().findFirst().ifPresent(label -> //
                b.append("    label ").append(formatValue(label.value())).append("\n"));

        // Generic JSON
        cjNode.data(data -> {
            IJsonValue json = data.jsonValue();
            IJsonObject obj = json == null ? null : json.asObjectOrNull();
            if (obj != null) {
                java.util.Set<String> skip = java.util.Set.of("id", "label");
                emitJsonObjectProperties(obj, b, 2, skip);
            }
        });
        b.append("  ]\n");
    }

    private static void emitJsonObjectProperties(IJsonObject obj, StringBuilder b, int indentLevel, java.util.Set<String> skipKeys) {
        List<String> keys = obj.keys().stream().filter(k -> !skipKeys.contains(k)).sorted().toList();
        for (String key : keys) {
            IJsonValue val = obj.get_(key);
            emitJsonEntry(key, val, b, indentLevel);
        }
    }

    private static void emitJsonEntry(String key, IJsonValue val, StringBuilder b, int indentLevel) {
        String indent = "  ".repeat(indentLevel);
        switch (val.jsonType().valueType()) {
            case Primitive -> {
                String s = val.asPrimitive().toJavaString();
                b.append(indent).append(key).append(" ").append(formatValue(s)).append("\n");
            }
            case Object -> {
                b.append(indent).append(key).append("\n");
                b.append(indent).append("[\n");
                emitJsonObjectProperties(val.asObject(), b, indentLevel + 1, java.util.Set.of());
                b.append(indent).append("]\n");
            }
            case Array -> {
                IJsonArray arr = val.asArray();
                for (int i = 0; i < arr.size(); i++) {
                    IJsonValue e = arr.get(i);
                    if (e.isPrimitive()) {
                        String s = e.asPrimitive().toJavaString();
                        b.append(indent).append(key).append(" ").append(formatValue(s)).append("\n");
                    } else if (e.isObject()) {
                        b.append(indent).append(key).append("\n");
                        b.append(indent).append("[\n");
                        emitJsonObjectProperties(e.asObject(), b, indentLevel + 1, java.util.Set.of());
                        b.append(indent).append("]\n");
                    } else if (e.isArray()) {
                        // Nested arrays: represent as repeated key blocks recursively
                        emitJsonEntry(key, e, b, indentLevel);
                    }
                }
            }
        }
    }

    public String toGml() {
        StringBuilder b = new StringBuilder();
        documentToGml(cjDoc, b);
        return b.toString();
    }

}
