package com.graphinout.reader.plantuml;

import com.graphinout.base.cj.document.CjDirection;
import com.graphinout.base.cj.document.ICjDocument;
import com.graphinout.base.cj.document.ICjEdge;
import com.graphinout.base.cj.document.ICjEndpoint;
import com.graphinout.base.cj.document.ICjGraph;
import com.graphinout.base.cj.document.ICjLabelEntry;
import com.graphinout.base.cj.document.ICjNode;
import com.graphinout.base.cj.stream.CjStream2CjWriter;
import com.graphinout.base.cj.stream.ICjStream;
import com.graphinout.base.cj.writer.CjWriter2CjDocumentWriter;
import com.graphinout.base.gio.GioFileFormat;
import com.graphinout.base.gio.GioWriter;
import com.graphinout.base.output.OutputSink;
import com.graphinout.foundation.pure.json.document.IJsonValue;
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Writes an {@link ICjDocument} as a PlantUML class diagram (the inverse of {@link PlantUmlReader}). Each node becomes a
 * type declaration (kind from {@code uml:kind}), each edge a relationship whose arrow is derived from {@code uml:rel}.
 *
 * <p>Each top-level CJ graph is written as its own {@code @startuml .. @enduml} block, so a document with several
 * top-level graphs survives the round-trip as several diagram blocks. Sub-graphs become {@code package id { .. }}
 * blocks. Generic (non-UML) node data is written as {@code key = value} member lines and arbitrary edge types as a
 * {@code <<type>>} stereotype on the relationship, both of which the reader parses back into the CJ model.
 */
public class PlantUmlWriter implements GioWriter {

    /** uml:kind (LeafType name) -> PlantUML keyword. */
    private static final Map<String, String> KIND_KEYWORD = Map.of(
            "CLASS", "class", "INTERFACE", "interface", "ABSTRACT_CLASS", "abstract class",
            "ENUM", "enum", "ANNOTATION", "annotation", "PROTOCOL", "protocol",
            "STRUCT", "struct", "EXCEPTION", "exception", "ENTITY", "entity");

    /** uml:rel -> PlantUML arrow (source &lt;arrow&gt; target). */
    private static final Map<String, String> REL_ARROW = Map.of(
            "extension", "--|>", "realization", "..|>", "composition", "*--", "aggregation", "o--",
            "association-directed", "-->", "dependency", "..>", "association", "--", "link-dashed", "..");

    /** Data keys that the writer/reader handle specially and must not be re-emitted as generic members. */
    private static final java.util.Set<String> RESERVED_KEYS = java.util.Set.of("uml:kind", "uml:members");

    @Override
    public ICjStream createCjStream(OutputSink outputSink) {
        CjWriter2CjDocumentWriter cjWriter2CjDocumentWriter = new CjWriter2CjDocumentWriter(cjDoc -> {
            try {
                outputSink.write(toPlantUml(cjDoc));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        return new CjStream2CjWriter(cjWriter2CjDocumentWriter, true);
    }

    @Override
    public GioFileFormat fileFormat() {
        return PlantUmlReader.FORMAT;
    }

    public static String toPlantUml(ICjDocument cjDoc) {
        // Render each top-level graph as its own @startuml..@enduml block, so multiple graphs per document survive.
        StringBuilder sb = new StringBuilder();
        List<ICjGraph> graphs = cjDoc.graphs().toList();
        if (graphs.isEmpty()) {
            return "@startuml\n@enduml\n";
        }
        for (ICjGraph graph : graphs) {
            sb.append(renderTopGraph(graph));
        }
        return sb.toString();
    }

    /** Render one top-level CJ graph as a single {@code @startuml .. @enduml} block. */
    private static String renderTopGraph(ICjGraph graph) {
        // Collect and sort so output is deterministic (PlantUML's parse order is not stable across regenerations).
        List<String> nodeLines = new ArrayList<>(renderGraph(graph));
        nodeLines.sort(String::compareTo);

        List<String> edgeLines = new ArrayList<>();
        edgesRecursive(graph).forEach(e -> {
            String line = renderEdge(e);
            if (line != null) {
                edgeLines.add(line);
            }
        });
        edgeLines.sort(String::compareTo);

        StringBuilder sb = new StringBuilder();
        sb.append("@startuml\n");
        nodeLines.forEach(l -> sb.append(l).append('\n'));
        edgeLines.forEach(l -> sb.append(l).append('\n'));
        sb.append("@enduml\n");
        return sb.toString();
    }

    /** All edges of a graph including those inside its sub-graphs (edges reference node ids globally). */
    private static java.util.stream.Stream<ICjEdge> edgesRecursive(ICjGraph graph) {
        return java.util.stream.Stream.concat(graph.edges(),
                graph.graphs().flatMap(PlantUmlWriter::edgesRecursive));
    }

    private static @Nullable String renderEdge(ICjEdge e) {
        List<ICjEndpoint> endpoints = e.endpoints().toList();
        String[] st = sourceTarget(endpoints);
        if (st == null) {
            return null;
        }
        String type = e.edgeType() != null ? e.edgeType().type() : null;
        boolean directed = endpoints.stream().anyMatch(ICjEndpoint::isDirected);

        String arrow;
        String stereotype = null;
        if (type != null && REL_ARROW.containsKey(type)) {
            arrow = REL_ARROW.get(type);
        } else {
            // No known UML relationship: pick a plain arrow honoring directionality, carry the type as a stereotype.
            arrow = directed ? "-->" : "--";
            if (type != null && !type.isBlank()) {
                stereotype = type;
            }
        }

        String label = firstLabel(e.labelEntries());
        StringBuilder line = new StringBuilder(st[0]).append(' ').append(arrow).append(' ').append(st[1]);
        // PlantUML keeps only the stereotype when both a plain label and a <<stereotype>> are present, so a stereotype
        // (arbitrary edge type) takes precedence over a textual label here.
        if (stereotype != null) {
            line.append(" : <<").append(stereotype).append(">>");
        } else if (label != null) {
            line.append(" : ").append(label);
        }
        return line.toString();
    }

    /** Render a graph's direct nodes as declarations and its sub-graphs as `package <id> { ... }` blocks (sorted). */
    private static List<String> renderGraph(ICjGraph graph) {
        List<String> lines = new ArrayList<>();
        graph.nodes().forEach(n -> lines.add(nodeDecl(n)));
        graph.graphs().forEach(sub -> {
            StringBuilder pkg = new StringBuilder("package ").append(sub.id()).append(" {\n");
            for (String line : renderGraph(sub)) {
                for (String inner : line.split("\n")) {
                    pkg.append("  ").append(inner).append('\n');
                }
            }
            pkg.append('}');
            lines.add(pkg.toString());
        });
        lines.sort(String::compareTo);
        return lines;
    }

    private static String nodeDecl(ICjNode n) {
        String kind = stringProperty(n.jsonValue(), "uml:kind");
        String keyword = kind != null ? KIND_KEYWORD.getOrDefault(kind, "class") : "class";
        String id = n.id();
        String label = firstLabel(n.labelEntries());
        String head = (label != null && !label.equals(id))
                ? keyword + " \"" + label + "\" as " + id   // display name / alias
                : keyword + " " + id;

        List<String> bodyLines = new ArrayList<>();
        // Generic (non-UML) node data becomes `key = value` member lines (parsed back into data on read).
        IJsonValue json = n.jsonValue();
        if (json != null && json.isObject()) {
            json.asObject().forEach((key, value) -> {
                if (RESERVED_KEYS.contains(key)) {
                    return;
                }
                bodyLines.add(key + " = " + scalarString(value));
            });
        }
        // Verbatim UML members captured on read.
        String members = stringProperty(json, "uml:members");
        if (members != null && !members.isBlank()) {
            for (String member : members.split("\n")) {
                bodyLines.add(member);
            }
        }
        if (bodyLines.isEmpty()) {
            return head;
        }
        StringBuilder block = new StringBuilder(head).append(" {\n");
        for (String line : bodyLines) {
            block.append(line).append('\n');
        }
        block.append('}');
        return block.toString();
    }

    private static String scalarString(@Nullable IJsonValue value) {
        if (value == null) {
            return "";
        }
        if (value.isString()) {
            return value.asString();
        }
        return value.toJsonString();
    }

    private static @Nullable String stringProperty(@Nullable IJsonValue json, String key) {
        if (json == null || !json.isObject()) {
            return null;
        }
        IJsonValue v = json.resolve(key);
        return (v != null && v.isString()) ? v.asString() : null;
    }

    private static @Nullable String firstLabel(List<ICjLabelEntry> labels) {
        if (labels.isEmpty()) {
            return null;
        }
        String value = labels.getFirst().value();
        return (value == null || value.isEmpty()) ? null : value;
    }

    private static String @Nullable [] sourceTarget(List<ICjEndpoint> endpoints) {
        ICjEndpoint in = null, out = null;
        for (ICjEndpoint ep : endpoints) {
            if (ep.direction() == CjDirection.IN && in == null) in = ep;
            else if (ep.direction() == CjDirection.OUT && out == null) out = ep;
        }
        if (in != null && out != null) {
            return new String[]{in.node(), out.node()};
        }
        if (endpoints.size() == 2) {
            return new String[]{endpoints.get(0).node(), endpoints.get(1).node()};
        }
        return null;
    }

}
