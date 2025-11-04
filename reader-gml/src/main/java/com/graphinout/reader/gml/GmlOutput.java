package com.graphinout.reader.gml;

import com.graphinout.base.cj.document.CjDirection;
import com.graphinout.base.cj.document.ICjDocument;
import com.graphinout.base.cj.document.ICjEdge;
import com.graphinout.base.cj.document.ICjEndpoint;
import com.graphinout.base.cj.document.ICjGraph;
import com.graphinout.base.cj.document.ICjNode;
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
        // Document-level attributes
        cjDoc.data(data -> {
            IJsonValue json = data.jsonValue();
            IJsonObject obj = json == null ? null : json.asObjectOrNull();
            if (obj != null) {
                obj.keys().stream().sorted().forEach(key -> {
                    String val = obj.get_(key).asPrimitive().toJavaString();
                    b.append(key).append(" ").append(formatValue(val)).append("\n");
                });
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

        // Emit additional edge data properties
        cjEdge.data(data -> {
            IJsonValue json = data.jsonValue();
            IJsonObject obj = json == null ? null : json.asObjectOrNull();
            if (obj != null) {
                obj.keys().stream().sorted().forEach(key -> {
                    if (!key.equals("source") && !key.equals("target") && !key.equals("label")) {
                        String val = obj.get_(key).asPrimitive().toJavaString();
                        attributes.put(key, formatValue(val));
                    }
                });
            }
        });

        b.append("  edge [\n");
        attributes.forEach((key, value) -> b.append("    ").append(key).append(" ").append(value).append("\n"));
        b.append("  ]\n");
    }

    private static String formatValue(String v) {
        if (v == null) return "\"\"";
        // If numeric (int or decimal), print without quotes
        if (v.matches("-?\\d+(\\.\\d+)?")) {
            return v;
        }
        // Otherwise, quote and escape embedded quotes by backslash removal (input already unescaped)
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
                obj.keys().stream().sorted().forEach(key -> {
                    // don't duplicate 'name'
                    if (!key.equals("name")) {
                        String val = obj.get_(key).asPrimitive().toJavaString();
                        b.append("  ").append(key).append(" ").append(formatValue(val)).append("\n");
                    }
                });
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

        Map<String, String> gmlAttributes = new TreeMap<>();
        gmlAttributes.put("id", id);
        cjNode.labelEntries().stream().findFirst().ifPresent(label -> //
                gmlAttributes.put("label", formatValue(label.value())));

        // Emit additional data properties stored on the node
        cjNode.data(data -> {
            IJsonValue json = data.jsonValue();
            IJsonObject obj = json == null ? null : json.asObjectOrNull();
            if (obj != null) {
                obj.keys().stream().sorted().forEach(key -> {
                    if (!key.equals("id") && !key.equals("label")) {
                        String val = obj.get_(key).asPrimitive().toJavaString();
                        gmlAttributes.put(key, formatValue(val));
                    }
                });
            }
        });

        b.append("  node [\n");
        gmlAttributes.forEach((key, value) -> b.append("    ").append(key).append(" ").append(value).append("\n"));
        b.append("  ]\n");
    }

    public String toGml() {
        StringBuilder b = new StringBuilder();

        documentToGml(cjDoc, b);

        return b.toString();
    }

}
