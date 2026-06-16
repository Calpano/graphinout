package com.graphinout.reader.plantuml;

import com.graphinout.base.cj.document.CjDirection;
import com.graphinout.base.cj.document.ICjElementType;
import com.graphinout.base.cj.stream.ICjStream;
import com.graphinout.base.gio.GioFileFormat;
import com.graphinout.base.gio.GioReader;
import com.graphinout.base.input.InputSource;
import com.graphinout.base.input.SingleInputSource;
import com.graphinout.foundation.pure.input.ContentError;
import net.sourceforge.plantuml.BlockUml;
import net.sourceforge.plantuml.SourceStringReader;
import net.sourceforge.plantuml.abel.Entity;
import net.sourceforge.plantuml.abel.LeafType;
import net.sourceforge.plantuml.abel.Link;
import net.sourceforge.plantuml.classdiagram.ClassDiagram;
import net.sourceforge.plantuml.core.Diagram;
import net.sourceforge.plantuml.decoration.LinkDecor;
import net.sourceforge.plantuml.decoration.LinkType;
import net.sourceforge.plantuml.klimt.creole.Display;
import org.apache.commons.io.IOUtils;
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Reads PlantUML <b>class diagrams</b> into the Connected JSON model. Classes/interfaces/enums become nodes (with a
 * {@code uml:kind} property and an optional display label); relationships become directed, typed edges (with a
 * {@code uml:rel} property). Uses the PlantUML library only to <em>parse</em> (no rendering, so no Graphviz needed).
 *
 * <p>Relationship classification and the source-&gt;target orientation are chosen so that {@link PlantUmlWriter}
 * reproduces an arrow that re-parses to the same model (stable round-trip). Each {@code @startuml .. @enduml} block
 * becomes a separate top-level CJ graph, so a multi-block document keeps its multiple graphs.
 */
public class PlantUmlReader implements GioReader {

    public static final String FORMAT_ID = "plantuml";
    public static final GioFileFormat FORMAT = new GioFileFormat(FORMAT_ID, "PlantUML Class Diagram", //
            ".puml", ".plantuml", ".pu", ".iuml");

    /** Leaf types we treat as class-diagram nodes. */
    private static final Set<LeafType> CLASS_KINDS = EnumSet.of(LeafType.CLASS, LeafType.INTERFACE,
            LeafType.ABSTRACT_CLASS, LeafType.ENUM, LeafType.ANNOTATION, LeafType.PROTOCOL, LeafType.STRUCT,
            LeafType.EXCEPTION, LeafType.ENTITY);

    /** A generic node-data member line: {@code key = value} (the encoding {@link PlantUmlWriter} uses for CJ data). */
    private static final Pattern DATA_MEMBER = Pattern.compile("^([A-Za-z_][\\w:.-]*)\\s*=\\s*(.*)$");

    /** A relationship stereotype label {@code «type»} carrying an arbitrary CJ edge type. */
    private static final Pattern STEREOTYPE = Pattern.compile("^[«<]{1,2}(.*?)[»>]{1,2}$");

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
    public void read(InputSource inputSource, ICjStream cjStream) throws IOException {
        if (inputSource.isMulti()) {
            throw new IllegalArgumentException("Cannot handle multi-sources");
        }
        SingleInputSource sis = (SingleInputSource) inputSource;
        String content = IOUtils.toString(sis.inputStream(), StandardCharsets.UTF_8);
        if (content.isBlank()) {
            return;
        }
        if (!content.contains("@startuml")) {
            content = "@startuml\n" + content + "\n@enduml\n";
        }

        SourceStringReader reader = new SourceStringReader(content);
        List<ClassDiagram> diagrams = new ArrayList<>();
        for (BlockUml block : reader.getBlocks()) {
            Diagram diagram = block.getDiagram();
            if (diagram instanceof ClassDiagram c) {
                diagrams.add(c);
            } else {
                reportWarn("Skipping non-class diagram: " + diagram.getClass().getSimpleName());
            }
        }
        if (diagrams.isEmpty()) {
            return;
        }

        cjStream.documentStart(cjStream.createDocumentChunk());
        for (ClassDiagram cd : diagrams) {
            emitDiagram(cjStream, cd);
        }
        cjStream.documentEnd();
    }

    /** Emit one PlantUML class diagram (one {@code @startuml} block) as a single top-level CJ graph. */
    private void emitDiagram(ICjStream cjStream, ClassDiagram cd) {
        // relationships (all emitted at the top graph; CJ edges reference node ids globally, across packages)
        List<String[]> edges = new ArrayList<>();
        for (Link link : cd.getLinks()) {
            if (link.getType().getStyle().isInvisible()) {
                continue; // layout-only link
            }
            String[] e = classify(link);
            if (e != null) {
                edges.add(e);
            }
        }

        // group class leafs by their containing package (getParentContainer); null/root => top level
        Entity root = cd.getRootGroup();
        Map<Entity, List<Entity>> leafsByGroup = new LinkedHashMap<>();
        for (Entity leaf : cd.leafs()) {
            if (!CLASS_KINDS.contains(leaf.getLeafType())) {
                continue;
            }
            Entity container = leaf.getParentContainer();
            leafsByGroup.computeIfAbsent(container == null ? root : container, k -> new ArrayList<>()).add(leaf);
        }

        // emit (CJ order within a graph: nodes, then edges, then sub-graphs)
        cjStream.graphStart(cjStream.createGraphChunk());
        emitLeafNodes(cjStream, leafsByGroup.get(root));
        for (String[] e : edges) {
            emitEdge(cjStream, e);
        }
        for (Entity pkg : cd.getChildrenGroups(root)) {
            emitPackage(cjStream, cd, pkg, leafsByGroup);
        }
        cjStream.graphEnd();
    }

    /** Emit a PlantUML package/namespace as a nested CJ sub-graph (id = package name), recursing into sub-packages. */
    private void emitPackage(ICjStream cjStream, ClassDiagram cd, Entity group, Map<Entity, List<Entity>> leafsByGroup) {
        var graph = cjStream.createGraphChunk();
        graph.id(group.getName());
        cjStream.graphStart(graph);
        emitLeafNodes(cjStream, leafsByGroup.get(group));
        for (Entity child : cd.getChildrenGroups(group)) {
            emitPackage(cjStream, cd, child, leafsByGroup);
        }
        cjStream.graphEnd();
    }

    private void emitLeafNodes(ICjStream cjStream, @Nullable List<Entity> leafs) {
        if (leafs == null) {
            return;
        }
        for (Entity entity : leafs) {
            String id = entity.getName();
            var node = cjStream.createNodeChunk();
            node.id(id);
            node.addProperty("uml:kind", entity.getLeafType().name());
            String display = displayString(entity.getDisplay());
            if (display != null && !display.equals(id)) {
                node.addLabelWithoutLanguage(display);  // an `as` alias / quoted display name
            }
            List<CharSequence> body = entity.getBodier().getRawBody();
            if (body != null && !body.isEmpty()) {
                List<String> umlMembers = new ArrayList<>();
                for (CharSequence raw : body) {
                    String line = raw.toString();
                    Matcher m = DATA_MEMBER.matcher(line.trim());
                    if (m.matches()) {
                        // a generic `key = value` CJ-data attribute
                        node.addProperty(m.group(1), m.group(2));
                    } else {
                        umlMembers.add(line);
                    }
                }
                if (!umlMembers.isEmpty()) {
                    node.addProperty("uml:members", String.join("\n", umlMembers));
                }
            }
            cjStream.nodeStart(node);
            cjStream.nodeEnd();
        }
    }

    private void emitEdge(ICjStream cjStream, String[] e) {
        final String src = e[0];
        final String tgt = e[1];
        final String rel = e[2];
        final String label = e[3];
        final boolean undirected = "1".equals(e[4]);
        var edge = cjStream.createEdgeChunk();
        if (undirected) {
            edge.addEndpoint(ep -> ep.node(src).direction(CjDirection.UNDIR));
            edge.addEndpoint(ep -> ep.node(tgt).direction(CjDirection.UNDIR));
        } else {
            edge.addEndpoint(ep -> ep.node(src).direction(CjDirection.IN));
            edge.addEndpoint(ep -> ep.node(tgt).direction(CjDirection.OUT));
        }
        edge.edgeType(ICjElementType.of(rel));
        if (label != null) {
            edge.addLabelWithoutLanguage(label);
        }
        cjStream.edgeStart(edge);
        cjStream.edgeEnd();
    }

    /**
     * @return [source, target, rel, label, undirected] with source-&gt;target oriented for a stable round-trip, or
     * null. {@code undirected} is "1" for a plain (decoration-less) association, "0" otherwise.
     */
    private String @Nullable [] classify(Link link) {
        Entity e1 = link.getEntity1();
        Entity e2 = link.getEntity2();
        if (e1 == null || e2 == null) {
            return null;
        }
        // Preserve PlantUML's entity1 -> entity2 order (PlantUML keeps the textual left/right order); the writer emits
        // a canonical arrow for the relationship kind, so re-parsing reproduces the same order and decors.
        String src = e1.getName();
        String tgt = e2.getName();
        LinkType lt = link.getType();
        LinkDecor d1 = lt.getDecor1();
        LinkDecor d2 = lt.getDecor2();
        boolean solid = lt.getStyle().isNormal();
        boolean noDecor = (d1 == null || d1 == LinkDecor.NONE) && (d2 == null || d2 == LinkDecor.NONE);
        String label = displayString(link.getLabel());

        // An arbitrary CJ edge type was carried as a «type» stereotype label.
        String stereotypeType = null;
        if (label != null) {
            Matcher m = STEREOTYPE.matcher(label);
            if (m.matches()) {
                stereotypeType = m.group(1).trim();
                label = null; // the label channel was used for the type, not for a real label
            }
        }

        String rel;
        boolean undirected = false;
        if (d1 == LinkDecor.EXTENDS || d2 == LinkDecor.EXTENDS) {
            rel = solid ? "extension" : "realization";
        } else if (d1 == LinkDecor.COMPOSITION || d2 == LinkDecor.COMPOSITION) {
            rel = "composition";
        } else if (d1 == LinkDecor.AGGREGATION || d2 == LinkDecor.AGGREGATION) {
            rel = "aggregation";
        } else if (d1 == LinkDecor.ARROW || d2 == LinkDecor.ARROW) {
            rel = solid ? "association-directed" : "dependency";
        } else if (noDecor && solid) {
            // plain `--` : an undirected association
            rel = "association";
            undirected = true;
        } else {
            rel = solid ? "association" : "link-dashed";
        }
        if (stereotypeType != null && !stereotypeType.isEmpty()) {
            rel = stereotypeType; // arbitrary CJ edge type wins over the arrow-derived UML relationship
        }
        return new String[]{src, tgt, rel, label, undirected ? "1" : "0"};
    }

    private static @Nullable String displayString(@Nullable Display display) {
        if (display == null || Display.isNull(display) || display.isWhite()) {
            return null;
        }
        String s = display.toString().trim();
        // Display.toString() wraps lines like "[likes]"; strip the surrounding brackets for a single line.
        if (s.startsWith("[") && s.endsWith("]")) {
            s = s.substring(1, s.length() - 1).trim();
        }
        return s.isEmpty() ? null : s;
    }

    private void reportWarn(String message) {
        if (errorHandler != null) {
            errorHandler.accept(ContentError.of(ContentError.ErrorLevel.Warn, message));
        }
    }

}
