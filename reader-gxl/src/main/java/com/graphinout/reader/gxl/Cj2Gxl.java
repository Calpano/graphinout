package com.graphinout.reader.gxl;

import com.graphinout.base.cj.document.CjDirection;
import com.graphinout.base.cj.document.ICjData;
import com.graphinout.base.cj.document.ICjDocument;
import com.graphinout.base.cj.document.ICjEdge;
import com.graphinout.base.cj.document.ICjElementType;
import com.graphinout.base.cj.document.ICjEndpoint;
import com.graphinout.base.cj.document.ICjGraph;
import com.graphinout.base.cj.document.ICjLabelEntry;
import com.graphinout.base.cj.document.ICjNode;
import com.graphinout.foundation.pure.input.ContentError;
import com.graphinout.foundation.pure.json.document.IJsonObject;
import com.graphinout.foundation.pure.json.document.IJsonValue;
import org.jspecify.annotations.Nullable;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;
import java.util.function.Consumer;

/**
 * Writes a CJ document as GXL 1.0, the inverse of {@link Gxl2Cj}.
 *
 * <p>A CJ edge becomes an {@code <edge>} if it has exactly two endpoints with directions IN/OUT (directed) or
 * UNDIR/UNDIR (undirected) and no endpoint roles; otherwise, or if it came from a {@code <rel>}, it becomes a
 * {@code <rel>} with one {@code <relend>} per endpoint. The graph's {@code edgemode} is the one read from GXL if still
 * consistent with the edges, else {@code directed}, {@code undirected} or, for mixed graphs, {@code defaultdirected}
 * with {@code isdirected="false"} on the undirected edges.
 *
 * <p>GXL cannot hold everything CJ can. These are reported as content warnings: ports, more than one node type, more
 * than one label entry, document-level data, JSON objects as values (written as their JSON string), and graphs nested
 * directly in a graph (GXL nests graphs only in nodes, edges and rels; such a graph is wrapped in a new node).
 */
final class Cj2Gxl {

    private final Consumer<ContentError> errorHandler;
    private final StringBuilder sb = new StringBuilder();
    private int generatedIds = 0;

    Cj2Gxl(Consumer<ContentError> errorHandler) {
        this.errorHandler = errorHandler;
    }

    /** How a CJ edge is written. */
    private record EdgeShape(boolean binary, boolean directed, @Nullable ICjEndpoint from, @Nullable ICjEndpoint to) {}

    String toGxl(ICjDocument doc) {
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        sb.append("<!DOCTYPE gxl SYSTEM \"http://www.gupro.de/GXL/gxl-1.0.dtd\">\n");
        if (!doc.data().isEmpty()) {
            warn("Document-level data is not written: GXL has no document attributes");
        }
        sb.append("<gxl xmlns:xlink=\"").append(GXL.XLINK_NS).append("\">\n");
        doc.graphs().forEach(g -> graph(g, "  "));
        sb.append("</gxl>\n");
        return sb.toString();
    }

    private void graph(ICjGraph g, String ind) {
        List<ICjEdge> edges = g.edges().toList();
        List<EdgeShape> shapes = edges.stream().map(Cj2Gxl::shape).toList();
        long directed = shapes.stream().filter(EdgeShape::directed).count();
        long undirected = shapes.size() - directed;
        boolean anyRel = shapes.stream().anyMatch(s -> !s.binary());
        boolean anyEdgeId = edges.stream().anyMatch(e -> e.id() != null);

        String storedMode = string(g.data(), GXL.KEY_EDGEMODE);
        String mode = edgeMode(storedMode, directed, undirected);
        boolean directedByDefault = mode.equals(GXL.DIRECTED) || mode.equals(GXL.DEFAULTDIRECTED);

        sb.append(ind).append("<graph id=\"").append(esc(idOrGenerate(g.id(), "graph"))).append('"');
        String role = string(g.data(), GXL.KEY_ROLE);
        if (role != null) sb.append(" role=\"").append(esc(role)).append('"');
        // DTD default is "directed": only write it if it was given or differs
        if (storedMode != null || !mode.equals(GXL.DIRECTED)) sb.append(" edgemode=\"").append(mode).append('"');
        if (anyEdgeId) sb.append(" edgeids=\"true\"");
        if (anyRel) sb.append(" hypergraph=\"true\"");
        sb.append(">\n");

        String in = ind + "  ";
        String type = string(g.data(), GXL.KEY_TYPE);
        if (type != null) typeElement(type, in);
        attrs(g.data(), in, false);
        g.nodes().forEach(n -> node(n, in));
        g.graphs().forEach(sub -> {
            // GXL has no graph-in-graph: wrap in a node so the hierarchy survives
            String nodeId = generateId("graphNode");
            warn("Graph '" + sub.id() + "' nested directly in a graph is written inside a new node '" + nodeId + "'");
            sb.append(in).append("<node id=\"").append(esc(nodeId)).append("\">\n");
            graph(sub, in + "  ");
            sb.append(in).append("</node>\n");
        });
        for (int i = 0; i < edges.size(); i++) {
            ICjEdge e = edges.get(i);
            EdgeShape s = shapes.get(i);
            if (s.binary()) edge(e, s, directedByDefault, in);
            else rel(e, s.directed(), directedByDefault, in);
        }
        sb.append(ind).append("</graph>\n");
    }

    private static String edgeMode(@Nullable String stored, long directed, long undirected) {
        if (stored != null) {
            boolean consistent = switch (stored) {
                case GXL.DIRECTED -> undirected == 0;
                case GXL.UNDIRECTED -> directed == 0;
                case GXL.DEFAULTDIRECTED, GXL.DEFAULTUNDIRECTED -> true;
                default -> false;
            };
            if (consistent) return stored;
        }
        if (undirected == 0) return GXL.DIRECTED;
        if (directed == 0) return GXL.UNDIRECTED;
        return GXL.DEFAULTDIRECTED;
    }

    private static EdgeShape shape(ICjEdge e) {
        List<ICjEndpoint> eps = e.endpoints().toList();
        boolean directed = eps.stream().anyMatch(ICjEndpoint::isDirected);
        boolean fromRel = Boolean.TRUE.equals(bool(e.data(), GXL.KEY_REL));
        if (fromRel || eps.size() != 2 || eps.stream().anyMatch(ep -> ep.type() != null || !onlyKey(ep.data(), GXL.KEY_ORDER))) {
            return new EdgeShape(false, directed, null, null);
        }
        ICjEndpoint a = eps.get(0), b = eps.get(1);
        if (a.direction() == CjDirection.IN && b.direction() == CjDirection.OUT) return new EdgeShape(true, true, a, b);
        if (a.direction() == CjDirection.OUT && b.direction() == CjDirection.IN) return new EdgeShape(true, true, b, a);
        if (a.isUndirected() && b.isUndirected()) return new EdgeShape(true, false, a, b);
        return new EdgeShape(false, directed, null, null);
    }

    private void node(ICjNode n, String ind) {
        String id = n.id();
        if (id == null) {
            id = generateId("node");
            warn("Node without id written as '" + id + "'");
        }
        sb.append(ind).append("<node id=\"").append(esc(id)).append("\">\n");
        String in = ind + "  ";
        List<ICjElementType> types = n.types().toList();
        if (!types.isEmpty()) {
            typeElement(types.getFirst().type(), in);
            if (types.size() > 1) warn("Node '" + id + "' has " + types.size() + " types; GXL allows one, only the first is written");
        }
        label(n.labelEntries(), "node '" + id + "'", in);
        attrs(n.data(), in, !n.labelEntries().isEmpty());
        n.graphs().forEach(sub -> graph(sub, in));
        sb.append(ind).append("</node>\n");
    }

    private void edge(ICjEdge e, EdgeShape s, boolean directedByDefault, String ind) {
        assert s.from() != null && s.to() != null;
        sb.append(ind).append("<edge");
        if (e.id() != null) sb.append(" id=\"").append(esc(e.id())).append('"');
        sb.append(" from=\"").append(esc(s.from().node())).append('"');
        sb.append(" to=\"").append(esc(s.to().node())).append('"');
        String fromOrder = string(s.from().data(), GXL.KEY_ORDER);
        if (fromOrder != null) sb.append(" fromorder=\"").append(esc(fromOrder)).append('"');
        String toOrder = string(s.to().data(), GXL.KEY_ORDER);
        if (toOrder != null) sb.append(" toorder=\"").append(esc(toOrder)).append('"');
        if (s.directed() != directedByDefault) sb.append(" isdirected=\"").append(s.directed()).append('"');
        warnPorts(e);
        edgeBody(e, ind);
    }

    private void rel(ICjEdge e, boolean directed, boolean directedByDefault, String ind) {
        sb.append(ind).append("<rel");
        if (e.id() != null) sb.append(" id=\"").append(esc(e.id())).append('"');
        if (directed != directedByDefault) sb.append(" isdirected=\"").append(directed).append('"');
        warnPorts(e);
        String in = ind + "  ";
        sb.append(">\n");
        edgeContent(e, in);
        for (ICjEndpoint ep : e.endpoints().toList()) {
            sb.append(in).append("<relend target=\"").append(esc(ep.node())).append('"');
            if (ep.type() != null) sb.append(" role=\"").append(esc(ep.type())).append('"');
            if (directed) {
                String dir = switch (ep.direction()) {
                    case IN -> GXL.DIR_OUT;
                    case OUT -> GXL.DIR_IN;
                    default -> GXL.DIR_NONE;
                };
                sb.append(" direction=\"").append(dir).append('"');
            }
            String startOrder = string(ep.data(), GXL.KEY_STARTORDER);
            if (startOrder != null) sb.append(" startorder=\"").append(esc(startOrder)).append('"');
            String endOrder = string(ep.data(), GXL.KEY_ENDORDER);
            if (endOrder != null) sb.append(" endorder=\"").append(esc(endOrder)).append('"');
            int before = sb.length();
            sb.append(">\n");
            int afterOpen = sb.length();
            attrs(ep.data(), in + "  ", false);
            if (sb.length() == afterOpen) {
                sb.setLength(before);
                sb.append("/>\n");
            } else {
                sb.append(in).append("</relend>\n");
            }
        }
        sb.append(ind).append("</rel>\n");
    }

    /** {@code >} + content + closing tag, or {@code />} if empty */
    private void edgeBody(ICjEdge e, String ind) {
        int before = sb.length();
        sb.append(">\n");
        int afterOpen = sb.length();
        edgeContent(e, ind + "  ");
        if (sb.length() == afterOpen) {
            sb.setLength(before);
            sb.append("/>\n");
        } else {
            sb.append(ind).append("</edge>\n");
        }
    }

    /** type, label, attrs and nested graphs of an edge or rel */
    private void edgeContent(ICjEdge e, String in) {
        String type = e.type();
        if (type != null) typeElement(type, in);
        String what = "edge" + (e.id() == null ? "" : " '" + e.id() + "'");
        label(e.labelEntries(), what, in);
        attrs(e.data(), in, !e.labelEntries().isEmpty());
        e.graphs().forEach(sub -> graph(sub, in));
    }

    private void warnPorts(ICjEdge e) {
        e.endpoints().filter(ep -> ep.port() != null).forEach(ep ->
                warn("Port '" + ep.port() + "' on node '" + ep.node() + "' is not written: GXL has no ports"));
    }

    private void typeElement(String href, String ind) {
        sb.append(ind).append("<type xlink:href=\"").append(esc(href)).append("\"/>\n");
    }

    private void label(List<ICjLabelEntry> labels, String what, String ind) {
        if (labels.isEmpty()) return;
        String value = labels.getFirst().value();
        if (labels.size() > 1) warn("Label of " + what + " has " + labels.size() + " entries; only the first is written");
        sb.append(ind).append("<attr name=\"").append(GXL.LABEL_ATTR).append("\"><string>")
          .append(escText(value == null ? "" : value)).append("</string></attr>\n");
    }

    /** One {@code <attr>} per data property, skipping {@code gxl.*} keys. */
    private void attrs(ICjData data, String ind, boolean hasLabel) {
        IJsonValue json = data.jsonValue();
        if (json == null) return;
        if (!json.isObject()) {
            attr("data", json, null, ind);
            return;
        }
        IJsonObject obj = json.asObject();
        IJsonValue kindsValue = obj.get(GXL.KEY_KINDS);
        IJsonObject kinds = kindsValue != null && kindsValue.isObject() ? kindsValue.asObject() : null;
        obj.forEach((key, value) -> {
            if (key.startsWith(GXL.KEY_PREFIX)) return;
            if (hasLabel && key.equals(GXL.LABEL_ATTR)) {
                warn("Data property 'label' is not written: the element's label is written as attr 'label'");
                return;
            }
            String kind = kinds == null ? null : kinds.getString(key);
            attr(key, value, kind, ind);
        });
    }

    private void attr(String name, @Nullable IJsonValue value, @Nullable String kind, String ind) {
        if (value == null || value.isNull()) return;
        sb.append(ind).append("<attr name=\"").append(esc(name)).append("\">");
        value(name, value, kind);
        sb.append("</attr>\n");
    }

    private void value(String name, IJsonValue v, @Nullable String kind) {
        if (v.isBoolean()) {
            sb.append("<bool>").append(v.asBoolean()).append("</bool>");
        } else if (v.isNumber()) {
            Number n = v.asNumber();
            if (isIntegral(n)) sb.append("<int>").append(n).append("</int>");
            else sb.append("<float>").append(n.doubleValue()).append("</float>");
        } else if (v.isString()) {
            String s = v.asString();
            if (GXL.ENUM.equals(kind)) sb.append("<enum>").append(escText(s)).append("</enum>");
            else if (GXL.LOCATOR.equals(kind)) sb.append("<locator xlink:href=\"").append(esc(s)).append("\"/>");
            else sb.append("<string>").append(escText(s)).append("</string>");
        } else if (v.isArray()) {
            String tag = GXL.SET.equals(kind) || GXL.BAG.equals(kind) || GXL.TUP.equals(kind) ? kind : GXL.SEQ;
            sb.append('<').append(tag).append('>');
            v.asArray().forEach(item -> {
                if (item != null && !item.isNull()) value(name, item, null);
            });
            sb.append("</").append(tag).append('>');
        } else {
            warn("Value of attr '" + name + "' is a JSON object, which GXL cannot represent; written as a JSON string");
            sb.append("<string>").append(escText(v.toJsonString())).append("</string>");
        }
    }

    private static boolean isIntegral(Number n) {
        if (n instanceof Integer || n instanceof Long || n instanceof Short || n instanceof Byte || n instanceof BigInteger) {
            return true;
        }
        return n instanceof BigDecimal bd && bd.scale() <= 0;
    }

    private String idOrGenerate(@Nullable String id, String prefix) {
        return id != null ? id : generateId(prefix);
    }

    private String generateId(String prefix) {
        return "_" + prefix + (++generatedIds);
    }

    private static @Nullable String string(ICjData data, String key) {
        IJsonValue v = data.getProperty(key);
        return v != null && v.isString() ? v.asString() : null;
    }

    private static @Nullable Boolean bool(ICjData data, String key) {
        IJsonValue v = data.getProperty(key);
        return v != null && v.isBoolean() ? v.asBoolean() : null;
    }

    /** true if data is empty or holds only {@code key} */
    private static boolean onlyKey(ICjData data, String key) {
        IJsonValue json = data.jsonValue();
        if (json == null) return true;
        if (!json.isObject()) return false;
        IJsonObject o = json.asObject();
        for (String k : o.keys()) {
            if (!k.equals(key)) return false;
        }
        return true;
    }

    private void warn(String message) {
        errorHandler.accept(ContentError.of(ContentError.ErrorLevel.Warn, message));
    }

    private static String escText(String s) {
        StringBuilder b = new StringBuilder(s.length());
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '&' -> b.append("&amp;");
                case '<' -> b.append("&lt;");
                case '>' -> b.append("&gt;");
                default -> b.append(c);
            }
        }
        return b.toString();
    }

    private static String esc(String s) {
        return escText(s).replace("\"", "&quot;");
    }

}
