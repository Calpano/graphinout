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
import net.sourceforge.plantuml.text.StringLocated;
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
        // If the source carries no `@start…` header at all, wrap it as a class diagram. PlantUML auto-creates the
        // classes referenced in relationships (no explicit `class` declarations needed), so a bare set of
        // relationship lines is read as a class diagram.
        if (!content.stripLeading().startsWith("@")) {
            content = "@startuml\n" + content + "\n@enduml\n";
        }

        SourceStringReader reader = new SourceStringReader(content);
        List<ClassDiagram> diagrams = new ArrayList<>();
        for (BlockUml block : reader.getBlocks()) {
            Diagram diagram = block.getDiagram();
            if (diagram instanceof ClassDiagram c) {
                diagrams.add(c);
                continue;
            }
            // PlantUML auto-detects the diagram type, and a bare relationship like `A -> B : knows` defaults to a
            // sequence diagram. Since this reader only models class diagrams, retry the block with a single
            // `class <first-entity>` declaration injected: that one decl forces class-diagram mode, and PlantUML
            // then auto-creates the remaining entities from the relationships. Real class diagrams already parse as
            // ClassDiagram above, so they are never rewritten (their package nesting stays intact).
            ClassDiagram coerced = coerceToClassDiagram(block);
            if (coerced != null) {
                diagrams.add(coerced);
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

    /** A relationship line: two operands joined by a connector token that contains at least one {@code -} or
     * {@code .} (covers {@code ->}, {@code -->}, {@code ..>}, {@code --|>}, {@code *--}, {@code o--}, …). */
    private static final Pattern RELATION_LINE = Pattern.compile(
            "^\\s*(\"[^\"]+\"|[\\w.$]+)\\s*"             // 1: left operand
                    + "(?:\"[^\"]+\"\\s*)?"               // optional left cardinality
                    + "[-.<>|*o()#x{}+]*[-.]+[-.<>|*o()#x{}+]*" // arrow connector (must contain - or .)
                    + "\\s*(?:\"[^\"]+\"\\s*)?"           // optional right cardinality
                    + "(?:\"[^\"]+\"|[\\w.$]+)"           // right operand
                    + "\\s*(?::.*)?$");                   // optional `: label`

    /**
     * Retry a block that PlantUML did not detect as a class diagram (e.g. a bare {@code A -> B : knows} defaults to a
     * sequence diagram) by injecting a single {@code class <first-entity>} declaration. That one declaration forces
     * class-diagram mode, after which PlantUML auto-creates the remaining entities from the relationships.
     *
     * @return the coerced {@link ClassDiagram}, or {@code null} if the block has no relationship line to anchor on or
     * still does not parse as a class diagram.
     */
    private @Nullable ClassDiagram coerceToClassDiagram(BlockUml block) {
        List<String> lines = new ArrayList<>();
        for (StringLocated sl : block.getData()) {
            lines.add(sl.getString());
        }
        String firstEntity = null;
        for (String line : lines) {
            Matcher m = RELATION_LINE.matcher(line);
            if (m.matches()) {
                firstEntity = m.group(1);
                break;
            }
        }
        if (firstEntity == null) {
            return null; // nothing relationship-shaped to anchor a class declaration on
        }
        StringBuilder src = new StringBuilder();
        boolean injected = false;
        for (String line : lines) {
            src.append(line).append('\n');
            if (!injected && line.stripLeading().startsWith("@start")) {
                src.append("class ").append(firstEntity).append('\n');
                injected = true;
            }
        }
        if (!injected) {
            src.insert(0, "class " + firstEntity + "\n");
        }
        for (BlockUml b : new SourceStringReader(src.toString()).getBlocks()) {
            if (b.getDiagram() instanceof ClassDiagram c) {
                return c;
            }
        }
        return null;
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
            // A plain `class Foo` carries no information worth recording: dropping the default CLASS kind keeps a bare
            // node bare (no mechanical `uml:kind = CLASS` triple). Non-default kinds (INTERFACE, ENUM, …) are kept.
            LeafType kind = entity.getLeafType();
            if (kind != LeafType.CLASS) {
                node.addProperty("uml:kind", kind.name());
            }
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
        final String predicate = e[2];   // CJ edge type (the UML relationship, or a custom relation name)
        final String label = e[3];
        final boolean undirected = "1".equals(e[4]);
        final String line = e[5];        // UML kind kept as `line` when a label/stereotype takes the predicate slot
        final String nav = e[6];         // navigability/direction marker: undirected|both (plain) or to|from|both (decorated)
        final String style = e[7];       // presentation styling: dotted|bold (UML has no such relation)
        var edge = cjStream.createEdgeChunk();
        if (undirected) {
            edge.addEndpoint(ep -> ep.node(src).direction(CjDirection.UNDIR));
            edge.addEndpoint(ep -> ep.node(tgt).direction(CjDirection.UNDIR));
        } else {
            edge.addEndpoint(ep -> ep.node(src).direction(CjDirection.IN));
            edge.addEndpoint(ep -> ep.node(tgt).direction(CjDirection.OUT));
        }
        if (predicate != null && !predicate.isEmpty()) {
            edge.edgeType(ICjElementType.of(predicate));
        }
        if (line != null) {
            edge.addProperty("line", line);
        }
        if (nav != null) {
            edge.addProperty("nav", nav);
        }
        if (style != null) {
            edge.addProperty("style", style);
        }
        if (label != null) {
            edge.addLabelWithoutLanguage(label);
        }
        cjStream.edgeStart(edge);
        cjStream.edgeEnd();
    }

    /**
     * Classify a relationship into {@code [source, target, predicate, label, undirected, line, nav, style]} using
     * <b>standard UML relationship names</b> as the type.
     *
     * <ul>
     *   <li><b>solid line</b> ⇒ {@code association}; <b>dashed line</b> ⇒ {@code dependency} (direction from the arrow
     *       heads, {@code A <-- B} normalised to {@code B --> A}; undirected/bidirectional carried as the {@code nav}
     *       marker {@code undirected}|{@code both}).</li>
     *   <li><b>hollow triangle</b> ⇒ {@code generalization} (solid) / {@code realization} (dashed);
     *       <b>filled diamond</b> ⇒ {@code composition}; <b>hollow diamond</b> ⇒ {@code aggregation};
     *       plus/crowfoot/x ⇒ {@code nested}/{@code crowfoot}/{@code not-navigable}. Orientation is normalised per UML
     *       convention (triangle/crowfoot/plus at the target; the composition/aggregation diamond at the source/whole);
     *       a coexisting open arrow rides as {@code nav} ({@code to}|{@code from}|{@code both}).</li>
     *   <li><b>dotted</b>/<b>bold</b> are pure presentation (no UML meaning) ⇒ carried as the {@code style} marker;
     *       {@code solid}/{@code dashed} carry no marker (they are part of the relationship name).</li>
     * </ul>
     *
     * <p>A relationship <em>name</em> (a «stereotype» or textual label) becomes the predicate and the UML kind rides as
     * the {@code line} property — dropped when it is the default {@code association}; otherwise the kind is the predicate.
     *
     * @return the tuple, or {@code null} if the link has no endpoints.
     */
    private String @Nullable [] classify(Link link) {
        Entity e1 = link.getEntity1();
        Entity e2 = link.getEntity2();
        if (e1 == null || e2 == null) {
            return null;
        }
        LinkType lt = link.getType();
        // PlantUML numbers the decorations opposite to the entities: decor1 sits at the entity2 end, decor2 at entity1.
        LinkDecor atE2 = lt.getDecor1();
        LinkDecor atE1 = lt.getDecor2();
        String styleName = lt.getStyle().toString(); // "NORMAL(...)" | "DASHED(...)" | "DOTTED(...)" | "BOLD(...)"
        boolean dashed = styleName.startsWith("DASHED");
        // dotted/bold are presentation only (treated as a solid line for the UML relationship name)
        String style = styleName.startsWith("DOTTED") ? "dotted" : styleName.startsWith("BOLD") ? "bold" : null;
        String label = displayString(link.getLabel());

        // An arbitrary CJ edge type carried as a «type» stereotype label.
        String stereotypeType = null;
        if (label != null) {
            Matcher m = STEREOTYPE.matcher(label);
            if (m.matches()) {
                stereotypeType = m.group(1).trim();
                label = null; // the label channel was used for the type, not for a real label
            }
        }
        String name = (stereotypeType != null && !stereotypeType.isEmpty()) ? stereotypeType
                : (label != null && !label.isBlank()) ? label.trim() : null;

        String type;
        String src;
        String tgt;
        boolean undirected;
        String nav = null;

        String shapeE1 = decorationShape(atE1);
        String shapeE2 = decorationShape(atE2);
        if (shapeE1 != null || shapeE2 != null) {
            // Decorated relation: orient by the decorated end and the per-shape UML convention.
            boolean atEnd2 = shapeE2 != null; // structural decoration sits at the entity2 end
            String shape = atEnd2 ? shapeE2 : shapeE1;
            Entity decorEnd = atEnd2 ? e2 : e1;
            Entity otherEnd = atEnd2 ? e1 : e2;
            type = umlTypeForShape(shape, dashed);
            boolean decorAtTarget = !("diamond-filled".equals(shape) || "diamond-hollow".equals(shape));
            Entity source = decorAtTarget ? otherEnd : decorEnd;
            Entity target = decorAtTarget ? decorEnd : otherEnd;
            src = source.getName();
            tgt = target.getName();
            undirected = false;
            // Navigability open arrow(s) coexisting with the structural decoration.
            boolean arrowAtTarget = (target == e2 ? atE2 : atE1) == LinkDecor.ARROW;
            boolean arrowAtSource = (source == e2 ? atE2 : atE1) == LinkDecor.ARROW;
            nav = (arrowAtTarget && arrowAtSource) ? "both" : arrowAtTarget ? "to" : arrowAtSource ? "from" : null;
        } else {
            // Plain link family: solid ⇒ association, dashed ⇒ dependency; direction from the open arrow side.
            src = e1.getName();
            tgt = e2.getName();
            type = dashed ? "dependency" : "association";
            boolean a2End = (atE2 == LinkDecor.ARROW); // arrow at entity2 end ⇒ e1 -> e2
            boolean a1End = (atE1 == LinkDecor.ARROW); // arrow at entity1 end ⇒ e2 -> e1
            if (a1End && a2End) {
                undirected = true;
                nav = "both"; // `A <--> B`
            } else if (a2End) {
                undirected = false; // `A --> B`
            } else if (a1End) {
                undirected = false;
                String t = src; src = tgt; tgt = t; // `A <-- B` ⇒ `B --> A`
            } else {
                undirected = true;
                nav = "undirected"; // `A -- B`
            }
        }

        String predicate;
        String line;
        if (name != null) {
            predicate = name;
            line = "association".equals(type) ? null : type; // association is the default kind, ridden implicitly
        } else {
            predicate = type;
            line = null;
        }
        return new String[]{src, tgt, predicate, null, undirected ? "1" : "0", line, nav, style};
    }

    /** The structural decoration shape at an end, or {@code null} for NONE/ARROW/exotic decors (the plain-link family). */
    private static @Nullable String decorationShape(@Nullable LinkDecor d) {
        if (d == null) {
            return null;
        }
        return switch (d) {
            case EXTENDS -> "triangle";
            case COMPOSITION -> "diamond-filled";
            case AGGREGATION -> "diamond-hollow";
            case CROWFOOT, CIRCLE_CROWFOOT, LINE_CROWFOOT -> "crowfoot";
            case PLUS -> "plus";
            case NOT_NAVIGABLE -> "notnav";
            default -> null; // NONE, ARROW, or an exotic decoration ⇒ handled by the plain-link family
        };
    }

    /** The standard UML relationship name for a decoration shape + line style. */
    private static String umlTypeForShape(String shape, boolean dashed) {
        return switch (shape) {
            case "triangle" -> dashed ? "realization" : "generalization";
            case "diamond-filled" -> "composition";
            case "diamond-hollow" -> "aggregation";
            case "crowfoot" -> "crowfoot";
            case "plus" -> "nested";
            case "notnav" -> "not-navigable";
            default -> "association";
        };
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
