package com.graphinout.reader.gxl;

import com.graphinout.base.cj.document.CjDirection;
import com.graphinout.base.cj.document.ICjDocument;
import com.graphinout.base.cj.document.ICjEdgeMutable;
import com.graphinout.base.cj.document.ICjElementType;
import com.graphinout.base.cj.document.ICjEndpointMutable;
import com.graphinout.base.cj.document.ICjGraphMutable;
import com.graphinout.base.cj.document.ICjHasDataMutable;
import com.graphinout.base.cj.document.ICjHasLabelMutable;
import com.graphinout.base.cj.document.ICjNodeMutable;
import com.graphinout.base.cj.document.impl.CjDocumentElement;
import com.graphinout.foundation.pure.input.ContentError;
import com.graphinout.foundation.pure.input.Location;
import com.graphinout.foundation.pure.json.document.IJsonArrayMutable;
import com.graphinout.foundation.pure.json.document.IJsonFactory;
import com.graphinout.foundation.pure.json.document.IJsonObjectMutable;
import com.graphinout.foundation.pure.json.document.IJsonValue;
import com.graphinout.reader.gxl.GxlXml.Element;
import org.jspecify.annotations.Nullable;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

/**
 * Maps a GXL element tree to a CJ document.
 *
 * <ul>
 * <li>{@code <graph>} becomes a CJ graph (top-level, or nested in a node/edge). Its {@code <type>}, {@code role} and an
 * explicit {@code edgemode} are kept in data ({@link GXL#KEY_TYPE}, {@link GXL#KEY_ROLE}, {@link GXL#KEY_EDGEMODE}).</li>
 * <li>{@code <node>} becomes a CJ node; {@code <type xlink:href>} becomes a node type.</li>
 * <li>{@code <edge from to>} becomes a binary CJ edge: IN(from) / OUT(to) if directed, both UNDIR otherwise.
 * Directedness is {@code isdirected} if given, else the graph's {@code edgemode}. {@code <type>} becomes the edge type.</li>
 * <li>{@code <rel>} becomes an n-ary CJ edge, one endpoint per {@code <relend>}: direction {@code out} (the relation
 * leaves this end) maps to CJ IN (source), {@code in} to OUT (target), {@code none} or missing to UNDIR. The relend
 * {@code role} becomes the endpoint type.</li>
 * <li>{@code <attr>} becomes a data property: bool/int/float/string map to JSON boolean/integer/number/string,
 * seq/set/bag/tup to a JSON array, enum to a string, locator to its href string. Kinds JSON cannot tell apart are kept in
 * {@link GXL#KEY_KINDS}. An attr named {@value GXL#LABEL_ATTR} with a string value on a node or edge becomes its CJ
 * label.</li>
 * </ul>
 */
final class Gxl2Cj {

    private static final IJsonFactory F = IJsonFactory.INSTANCE;

    private final Consumer<ContentError> errorHandler;
    /** non-standard attributes already reported, so each is warned about once per document */
    private final Set<String> reportedAliases = new HashSet<>();

    Gxl2Cj(Consumer<ContentError> errorHandler) {
        this.errorHandler = errorHandler;
    }

    /** Collects data properties and value kinds of one element, then applies them in one go. */
    private static final class DataBuilder {
        final Map<String, IJsonValue> values = new LinkedHashMap<>();
        final Map<String, String> kinds = new LinkedHashMap<>();

        void put(String key, IJsonValue value) {
            values.put(key, value);
        }

        void applyTo(ICjHasDataMutable target) {
            if (!kinds.isEmpty()) {
                IJsonObjectMutable k = F.createObjectMutable();
                kinds.forEach(k::setString);
                values.put(GXL.KEY_KINDS, k);
            }
            if (!values.isEmpty()) {
                IJsonObjectMutable o = F.createObjectMutable();
                values.forEach(o::setProperty);
                target.dataMutable(d -> d.setJsonValue(o));
            }
        }
    }

    ICjDocument toCjDocument(Element root) {
        CjDocumentElement doc = new CjDocumentElement();
        if (!root.name.equals(GXL.GXL)) {
            error("Root element must be <gxl> but is <" + root.name + ">", root.location);
            return doc;
        }
        for (Element child : root.children) {
            if (child.name.equals(GXL.GRAPH)) {
                doc.addGraph(g -> graph(child, g));
            } else {
                warn("Ignoring <" + child.name + "> in <gxl>", child.location);
            }
        }
        return doc;
    }

    private void graph(Element e, ICjGraphMutable g) {
        String id = e.attr(GXL.ID);
        if (id != null) {
            g.id(id);
        } else {
            warn("GXL <graph> without id", e.location);
        }
        DataBuilder data = new DataBuilder();
        String mode = e.attr(GXL.EDGEMODE);
        boolean directedByDefault = directedByDefault(mode, e.location);
        if (mode != null) data.put(GXL.KEY_EDGEMODE, F.createString(mode));
        String role = e.attr(GXL.ROLE);
        if (role != null) data.put(GXL.KEY_ROLE, F.createString(role));

        for (Element child : e.children) {
            switch (child.name) {
                case GXL.TYPE -> {
                    String href = child.href();
                    if (href != null) data.put(GXL.KEY_TYPE, F.createString(href));
                }
                case GXL.ATTR -> attr(child, data, null);
                case GXL.NODE -> {
                    if (child.attr(GXL.ID) == null) {
                        error("GXL <node> without id; skipped", child.location);
                    } else {
                        g.addNode(n -> node(child, n));
                    }
                }
                case GXL.EDGE -> edge(child, g, directedByDefault);
                case GXL.REL -> rel(child, g, directedByDefault);
                default -> warn("Ignoring <" + child.name + "> in <graph>", child.location);
            }
        }
        data.applyTo(g);
    }

    private void node(Element e, ICjNodeMutable n) {
        n.id(e.attr(GXL.ID));
        DataBuilder data = new DataBuilder();
        for (Element child : e.children) {
            switch (child.name) {
                case GXL.TYPE -> {
                    String href = child.href();
                    if (href != null) n.addType(ICjElementType.of(href));
                }
                case GXL.ATTR -> attr(child, data, n);
                case GXL.GRAPH -> n.addGraph(sub -> graph(child, sub));
                default -> warn("Ignoring <" + child.name + "> in <node>", child.location);
            }
        }
        data.applyTo(n);
    }

    private void edge(Element e, ICjGraphMutable g, boolean directedByDefault) {
        String from = attrOrAlias(e, GXL.FROM, "source");
        String to = attrOrAlias(e, GXL.TO, "target");
        if (from == null || to == null) {
            error("GXL <edge> needs 'from' and 'to' (from=" + from + ", to=" + to + "); skipped", e.location);
            return;
        }
        boolean directed = isDirected(e, directedByDefault);
        g.addEdge(edge -> {
            String id = e.attr(GXL.ID);
            if (id != null) edge.id(id);
            String fromOrder = e.attr(GXL.FROMORDER);
            String toOrder = e.attr(GXL.TOORDER);
            edge.addEndpoint(ep -> endpoint(ep, from, directed ? CjDirection.IN : CjDirection.UNDIR, fromOrder));
            edge.addEndpoint(ep -> endpoint(ep, to, directed ? CjDirection.OUT : CjDirection.UNDIR, toOrder));
            edgeBody(e, edge, new DataBuilder());
        });
    }

    private static void endpoint(ICjEndpointMutable ep, String node, CjDirection direction, @Nullable String order) {
        ep.node(node).direction(direction);
        if (order != null) ep.dataMutable(d -> d.add(GXL.KEY_ORDER, order));
    }

    private void rel(Element e, ICjGraphMutable g, boolean directedByDefault) {
        boolean directed = isDirected(e, directedByDefault);
        g.addEdge(edge -> {
            String id = e.attr(GXL.ID);
            if (id != null) edge.id(id);
            for (Element relend : e.children(GXL.RELEND)) {
                String target = relend.attr(GXL.TARGET);
                if (target == null) {
                    error("GXL <relend> without 'target'; skipped", relend.location);
                    continue;
                }
                CjDirection direction = directed ? relendDirection(relend) : CjDirection.UNDIR;
                edge.addEndpoint(ep -> {
                    ep.node(target).direction(direction);
                    String role = relend.attr(GXL.ROLE);
                    if (role != null) ep.type(role);
                    DataBuilder epData = new DataBuilder();
                    String startOrder = relend.attr(GXL.STARTORDER);
                    if (startOrder != null) epData.put(GXL.KEY_STARTORDER, F.createString(startOrder));
                    String endOrder = relend.attr(GXL.ENDORDER);
                    if (endOrder != null) epData.put(GXL.KEY_ENDORDER, F.createString(endOrder));
                    for (Element a : relend.children) {
                        if (a.name.equals(GXL.ATTR)) attr(a, epData, null);
                        else warn("Ignoring <" + a.name + "> in <relend>", a.location);
                    }
                    epData.applyTo(ep);
                });
            }
            DataBuilder data = new DataBuilder();
            data.put(GXL.KEY_REL, F.createBoolean(true));
            edgeBody(e, edge, data);
        });
    }

    /**
     * Some GXL files in the wild use {@code source}/{@code target} instead of {@code from}/{@code to}, or a {@code type}
     * attribute instead of a {@code <type>} element. They are accepted, with one warning per document.
     */
    private @Nullable String attrOrAlias(Element e, String name, String alias) {
        String v = e.attr(name);
        if (v != null) return v;
        v = e.attr(alias);
        if (v != null) reportAliasOnce(e, alias, "'" + name + "'");
        return v;
    }

    private void reportAliasOnce(Element e, String alias, String standard) {
        if (reportedAliases.add(e.name + "@" + alias)) {
            warn("Non-standard attribute '" + alias + "' on <" + e.name + "> read as " + standard
                    + " (reported once per document)", e.location);
        }
    }

    /** type, attrs and nested graphs, shared by edge and rel */
    private void edgeBody(Element e, ICjEdgeMutable edge, DataBuilder data) {
        String typeAttr = e.attr(GXL.TYPE);
        if (typeAttr != null && e.firstChild(GXL.TYPE) == null) {
            reportAliasOnce(e, GXL.TYPE, "<type xlink:href>");
            edge.edgeType(typeAttr);
        }
        for (Element child : e.children) {
            switch (child.name) {
                case GXL.TYPE -> {
                    String href = child.href();
                    if (href != null) edge.edgeType(href);
                }
                case GXL.ATTR -> attr(child, data, edge);
                case GXL.GRAPH -> edge.addGraph(sub -> graph(child, sub));
                case GXL.RELEND -> { /* handled by rel() */ }
                default -> warn("Ignoring <" + child.name + "> in <" + e.name + ">", child.location);
            }
        }
        data.applyTo(edge);
    }

    private static CjDirection relendDirection(Element relend) {
        String dir = relend.attr(GXL.DIRECTION);
        if (dir == null) return CjDirection.UNDIR;
        return switch (dir) {
            case GXL.DIR_OUT -> CjDirection.IN;
            case GXL.DIR_IN -> CjDirection.OUT;
            default -> CjDirection.UNDIR;
        };
    }

    private boolean isDirected(Element e, boolean directedByDefault) {
        String v = e.attr(GXL.ISDIRECTED);
        if (v == null) return directedByDefault;
        return switch (v.trim()) {
            case "true" -> true;
            case "false" -> false;
            default -> {
                warn("Invalid isdirected='" + v + "'; using graph default", e.location);
                yield directedByDefault;
            }
        };
    }

    private boolean directedByDefault(@Nullable String mode, Location location) {
        if (mode == null) return true; // DTD default: directed
        return switch (mode) {
            case GXL.DIRECTED, GXL.DEFAULTDIRECTED -> true;
            case GXL.UNDIRECTED, GXL.DEFAULTUNDIRECTED -> false;
            default -> {
                warn("Unknown edgemode='" + mode + "'; treating edges as directed", location);
                yield true;
            }
        };
    }

    /** @param labelTarget if not null, an attr named "label" with a string value becomes its label */
    private void attr(Element a, DataBuilder data, @Nullable ICjHasLabelMutable labelTarget) {
        String name = a.attr(GXL.NAME);
        if (name == null) {
            warn("GXL <attr> without 'name'; ignored", a.location);
            return;
        }
        Element valueElement = null;
        for (Element c : a.children) {
            if (c.name.equals(GXL.ATTR)) {
                warn("Attributes of attribute '" + name + "' are not supported; ignored", c.location);
            } else if (valueElement == null) {
                valueElement = c;
            }
        }
        if (valueElement == null) {
            warn("GXL <attr name='" + name + "'> without a value; ignored", a.location);
            return;
        }
        if (labelTarget != null && name.equals(GXL.LABEL_ATTR) && valueElement.name.equals(GXL.STRING)) {
            labelTarget.addLabelWithoutLanguage(valueElement.text());
            return;
        }
        IJsonValue value = value(valueElement);
        if (value == null) return;
        data.put(name, value);
        switch (valueElement.name) {
            case GXL.ENUM, GXL.LOCATOR, GXL.SET, GXL.BAG, GXL.TUP -> data.kinds.put(name, valueElement.name);
            default -> { /* distinct JSON type, no kind needed */ }
        }
    }

    private @Nullable IJsonValue value(Element v) {
        String text = v.text().trim();
        return switch (v.name) {
            case GXL.BOOL -> switch (text) {
                case "true" -> F.createBoolean(true);
                case "false" -> F.createBoolean(false);
                default -> {
                    warn("Invalid bool '" + text + "'; kept as string", v.location);
                    yield F.createString(text);
                }
            };
            case GXL.INT -> {
                try {
                    yield F.createLong(Long.parseLong(text));
                } catch (NumberFormatException ex) {
                    warn("Invalid int '" + text + "'; kept as string", v.location);
                    yield F.createString(text);
                }
            }
            case GXL.FLOAT -> {
                try {
                    yield F.createDouble(Double.parseDouble(text));
                } catch (NumberFormatException ex) {
                    warn("Invalid float '" + text + "'; kept as string", v.location);
                    yield F.createString(text);
                }
            }
            case GXL.STRING -> F.createString(v.text());
            case GXL.ENUM -> F.createString(text);
            case GXL.LOCATOR -> {
                String href = v.href();
                if (href == null) {
                    warn("GXL <locator> without xlink:href; ignored", v.location);
                    yield null;
                }
                yield F.createString(href);
            }
            case GXL.SEQ, GXL.SET, GXL.BAG, GXL.TUP -> {
                IJsonArrayMutable array = F.createArrayMutable();
                for (Element item : v.children) {
                    IJsonValue itemValue = value(item);
                    if (itemValue != null) array.add(itemValue);
                }
                yield array;
            }
            default -> {
                warn("Unknown GXL value <" + v.name + ">; ignored", v.location);
                yield null;
            }
        };
    }

    private void warn(String message, Location location) {
        errorHandler.accept(ContentError.of(ContentError.ErrorLevel.Warn, message, location));
    }

    private void error(String message, Location location) {
        errorHandler.accept(ContentError.of(ContentError.ErrorLevel.Error, message, location));
    }

}
