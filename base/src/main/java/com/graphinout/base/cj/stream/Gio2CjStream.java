package com.graphinout.base.cj.stream;

import com.graphinout.base.cj.element.ICjDocumentChunkMutable;
import com.graphinout.base.cj.element.ICjEdgeChunkMutable;
import com.graphinout.base.cj.element.ICjGraphChunkMutable;
import com.graphinout.base.cj.element.ICjHasDataMutable;
import com.graphinout.base.cj.element.ICjHasLabelMutable;
import com.graphinout.base.cj.element.ICjNodeChunkMutable;
import com.graphinout.base.cj.element.ICjPortMutable;
import com.graphinout.base.cj.stream.api.ICjStream;
import com.graphinout.base.gio.GioData;
import com.graphinout.base.gio.GioDocument;
import com.graphinout.base.gio.GioEdge;
import com.graphinout.base.gio.GioElementWithDescription;
import com.graphinout.base.gio.GioGraph;
import com.graphinout.base.gio.GioKey;
import com.graphinout.base.gio.GioNode;
import com.graphinout.base.gio.GioPort;
import com.graphinout.base.gio.GioWriter;
import com.graphinout.base.graphml.CjGraphmlMapping;
import com.graphinout.foundation.input.SingleInputSourceOfString;
import com.graphinout.foundation.json.JsonException;
import com.graphinout.foundation.json.path.IJsonContainerNavigationStep;
import com.graphinout.foundation.json.stream.impl.Json2JavaJsonWriter;
import com.graphinout.foundation.json.stream.impl.JsonReaderImpl;
import com.graphinout.foundation.json.value.IJsonFactory;
import com.graphinout.foundation.json.value.IJsonValue;
import com.graphinout.foundation.xml.XmlFragmentString;

import javax.annotation.Nullable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import static com.graphinout.base.graphml.CjGraphmlMapping.CjDataProperty.CustomXmlAttributes;
import static com.graphinout.base.graphml.CjGraphmlMapping.CjDataProperty.Description;
import static com.graphinout.foundation.json.path.IJsonContainerNavigationStep.pathOf;
import static com.graphinout.foundation.util.Nullables.ifPresentAccept;
import static java.util.Optional.ofNullable;

public class Gio2CjStream implements GioWriter {

    // Track most recently started element type to route incoming <data>/<label> correctly in nested contexts (e.g., graph inside node)
    private enum OpenCtx {None, Document, Graph, Node, Edge, Port}

    private static class GraphCtx {

        ICjGraphChunkMutable chunk;
        java.util.ArrayList<ICjNodeChunkMutable> bufferedNodes = new java.util.ArrayList<>();
        java.util.ArrayList<ICjEdgeChunkMutable> bufferedEdges = new java.util.ArrayList<>();
        java.util.ArrayList<GraphCtx> bufferedSubgraphs = new java.util.ArrayList<>();

        GraphCtx(ICjGraphChunkMutable c) {this.chunk = c;}
    }

    private final Map<String, GioKey> keysById = new HashMap<>();
    private final ICjStream cjStream;
    private final java.util.ArrayDeque<GraphCtx> graphStack = new java.util.ArrayDeque<>();
    // Pending chunks to accumulate data before sending to stream
    private ICjDocumentChunkMutable pendingDocument;
    private ICjNodeChunkMutable pendingNode;
    private ICjEdgeChunkMutable pendingEdge;
    private final java.util.ArrayDeque<ICjPortMutable> portStack = new java.util.ArrayDeque<>();
    private OpenCtx lastStarted = OpenCtx.None;

    public Gio2CjStream(ICjStream cjStream) {this.cjStream = cjStream;}

    private static void copyDescAndAttributes(GioElementWithDescription gio, ICjHasDataMutable cj) {
        // description
        ifPresentAccept(gio.getDescription(), desc -> cj.dataMutable(dm -> dm.add(pathOf(Description.cjPropertyKey), desc)));
        // custom attributes
        ifPresentAccept(gio.getCustomAttributes(), attrs -> cj.dataMutable(dm -> attrs.forEach((k, v) -> dm.add(pathOf(CustomXmlAttributes.cjPropertyKey, k), v))));
    }

    private static IJsonValue parseJsonFromXml(@Nullable XmlFragmentString xml) {
        if (xml == null) return null;
        try {
            String raw = xml.rawXml();
            JsonReaderImpl reader = new JsonReaderImpl();
            Json2JavaJsonWriter writer = new Json2JavaJsonWriter();
            reader.read(SingleInputSourceOfString.of("graphml-data", raw), writer);
            return writer.jsonValue();
        } catch (Exception e) {
            return null;
        }
    }

    private static com.graphinout.foundation.json.value.IJsonValue toJsonAttributesObject(IJsonFactory f, Map<String, String> attrs) {
        com.graphinout.foundation.json.value.java.JavaJsonObject o = new com.graphinout.foundation.json.value.java.JavaJsonObject();
        attrs.forEach((k, v) -> o.addProperty(k, f.createString(v)));
        return o;
    }

    private static com.graphinout.foundation.json.value.IJsonValue toJsonObject(IJsonFactory f, Map<String, Object> map) {
        com.graphinout.foundation.json.value.java.JavaJsonObject o = new com.graphinout.foundation.json.value.java.JavaJsonObject();
        map.forEach((k, v) -> {
            switch (v) {
                case null -> {
                }
                case String s -> o.addProperty(k, f.createString(s));
                case IJsonValue jv -> o.addProperty(k, jv);
                case Boolean b -> o.addProperty(k, f.createBoolean(b));
                case Number n -> o.addProperty(k, f.createNumberFromString(n.toString()));
                default -> o.addProperty(k, f.createString(v.toString()));
            }
        });
        return o;
    }

    // JsonValueWriter passthroughs are unused here
    @Override
    public void arrayEnd() throws JsonException {}

    @Override
    public void arrayStart() throws JsonException {}

    @Override
    public void baseUri(String baseUri) {
        ensurePendingDocument();
        pendingDocument.baseUri(baseUri);
    }

    @Override
    public void data(GioData data) {
        // Special-case GraphML-driven CJ mappings first
        String keyId = data.getKey();
        if (keyId != null && !keyId.isBlank()) {
            // Base URI -> document.baseUri
            if (keyId.equals(com.graphinout.base.graphml.CjGraphmlMapping.GraphmlDataElement.BaseUri.attrName)) {
                ensurePendingDocument();
                pendingDocument.baseUri(data.getValue().rawXml());
                return;
            }
            // EdgeType -> ICjEdgeType on current edge
            if (keyId.equals(com.graphinout.base.graphml.CjGraphmlMapping.GraphmlDataElement.EdgeType.attrName)) {
                if (pendingEdge != null && data.getValue() != null) {
                    pendingEdge.edgeType(com.graphinout.base.cj.ICjEdgeType.fromJsonString(data.getValue().rawXml()));
                }
                return;
            }
            // Label -> ICjHasLabelMutable
            if (keyId.equals(com.graphinout.base.graphml.CjGraphmlMapping.GraphmlDataElement.Label.attrName)) {
                ICjHasLabelMutable labelTarget = currentHasLabelTarget();
                if (labelTarget != null) {
                    com.graphinout.foundation.json.value.IJsonValue json = parseJsonFromXml(data.getValue());
                    if (json != null) {
                        if (json.isArray()) {
                            labelTarget.setLabel(l -> json.asArray().forEach(v -> {
                                if (v.isObject()) {
                                    IJsonValue valV = v.asObject().get("value");
                                    String value = valV != null ? valV.asString() : null;
                                    IJsonValue langV = v.asObject().get("language");
                                    String lang = langV != null ? langV.asString() : null;
                                    l.addEntry(e -> {
                                        e.value(value);
                                        if (lang != null) e.language(lang);
                                    });
                                } else if (v.isPrimitive()) {
                                    String value = v.asString();
                                    l.addEntry(e -> e.value(value));
                                }
                            }));
                        } else if (json.isPrimitive()) {
                            String value = json.asString();
                            labelTarget.setLabel(l -> l.addEntry(e -> e.value(value)));
                        } else if (json.isObject()) {
                            IJsonValue valV = json.asObject().get("value");
                            String value = valV != null ? valV.asString() : null;
                            IJsonValue langV = json.asObject().get("language");
                            String lang = langV != null ? langV.asString() : null;
                            labelTarget.setLabel(l -> l.addEntry(e -> {
                                e.value(value);
                                if (lang != null) e.language(lang);
                            }));
                        }
                    } else if (data.getValue() != null) {
                        // Fallback: treat raw XML/text content as a single plain label value
                        String value = data.getValue().rawXml();
                        labelTarget.setLabel(l -> l.addEntry(e -> e.value(value)));
                    }
                }
                return;
            }
            // Arbitrary JSON data blob -> set entire CJ data jsonValue
            if (keyId.equals(com.graphinout.base.graphml.CjGraphmlMapping.GraphmlDataElement.CjJsonData.attrName)) {
                ICjHasDataMutable target = currentHasDataTarget();
                if (target != null) {
                    com.graphinout.foundation.json.value.IJsonValue json = parseJsonFromXml(data.getValue());
                    if (json != null) target.dataMutable(dm -> dm.setJsonValue(json));
                }
                return;
            }
        }

        // Default behavior: Attach GraphML <data> to the current pending element (edge > node > graph > document)
        ICjHasDataMutable target = currentHasDataTarget();
        if (target == null) return;
        target.dataMutable(dm -> {
            // prefer mapping into individual properties via the GraphML key id
            if (keyId != null && !keyId.isBlank()) {
                GioKey key = keysById.get(keyId);
                com.graphinout.base.graphml.GraphmlDataType gType = key != null && key.getAttributeType() != null ? com.graphinout.base.graphml.GraphmlDataType.fromGraphmlName(key.getAttributeType().graphmlName()) : com.graphinout.base.graphml.GraphmlDataType.typeString;
                String propertyKey = key != null && key.getAttributeName() != null ? key.getAttributeName() : keyId;
                dm.add(IJsonContainerNavigationStep.pathOf(propertyKey), CjGraphmlMapping.toJsonPrimitive(dm.factory(), gType, data.getValue()));
            } else if (data.getValue() != null) {
                // generic JSON data blob under cj_jsonData key (mirrors CjDocument2Graphml behavior)
                dm.add(IJsonContainerNavigationStep.pathOf(CjGraphmlMapping.GraphmlDataElement.CjJsonData.attrName), dm.factory().createXmlString(data.getValue().rawXml(), data.getValue().xmlSpace()));
            }
        });
    }

    @Override
    public void endDocument() {
        // Flush document if not yet sent
        if (pendingDocument != null) {
            cjStream.documentStart(pendingDocument);
            pendingDocument = null;
        }
        cjStream.documentEnd();
    }

    @Override
    public void endEdge() {
        // Buffer node if one is pending; nodes must precede edges
        if (pendingNode != null) {
            if (!graphStack.isEmpty()) {
                graphStack.peek().bufferedNodes.add(pendingNode);
            } else {
                ensureDocumentStartedIfNeeded();
                // Fallback: emit a minimal node outside of any graph (should not happen in GraphML)
                cjStream.nodeStart(pendingNode);
                cjStream.nodeEnd();
            }
            pendingNode = null;
        }
        if (pendingEdge != null) {
            if (!graphStack.isEmpty()) {
                graphStack.peek().bufferedEdges.add(pendingEdge);
            } else {
                // No graph context? start document and emit directly as fallback
                ensureDocumentStartedIfNeeded();
                cjStream.edgeStart(pendingEdge);
                cjStream.edgeEnd();
            }
            pendingEdge = null;
        }
        // Edges are emitted when closing the graph (or recursively when emitting buffered graphs)
    }

    @Override
    public void endGraph(@Nullable URL locator) {
        if (!graphStack.isEmpty()) {
            GraphCtx ctx = graphStack.pop();
            if (!graphStack.isEmpty()) {
                // buffer this finished subgraph under its parent
                graphStack.peek().bufferedSubgraphs.add(ctx);
            } else {
                // top-level graph: emit now in canonical order
                ensureDocumentStartedIfNeeded();
                emitGraphRecursively(ctx);
            }
        }
    }

    @Override
    public void endNode(@Nullable URL locator) {
        if (pendingNode != null) {
            if (!graphStack.isEmpty()) {
                graphStack.peek().bufferedNodes.add(pendingNode);
            } else {
                ensureDocumentStartedIfNeeded();
                // Fallback: emit a minimal node outside of any graph (should not happen in GraphML)
                cjStream.nodeStart(pendingNode);
                cjStream.nodeEnd();
            }
            pendingNode = null;
        }
    }

    @Override
    public void endPort() {
        // pop current port after we've consumed data/label
        if (!portStack.isEmpty()) portStack.pop();
    }

    @Override
    public void key(GioKey gioKey) {
        // remember key for value type lookup only; do NOT persist into CJ data
        keysById.put(gioKey.getId(), gioKey);
    }

    @Override
    public void objectEnd() throws JsonException {}

    @Override
    public void objectStart() throws JsonException {}

    @Override
    public void onBigDecimal(BigDecimal bigDecimal) throws JsonException {}

    @Override
    public void onBigInteger(BigInteger bigInteger) throws JsonException {}

    @Override
    public void onBoolean(boolean b) throws JsonException {}

    @Override
    public void onDouble(double d) throws JsonException {}

    @Override
    public void onFloat(float f) throws JsonException {}

    @Override
    public void onInteger(int i) throws JsonException {}

    @Override
    public void onKey(String key) throws JsonException {}

    @Override
    public void onLong(long l) throws JsonException {}

    @Override
    public void onNull() throws JsonException {}

    @Override
    public void onString(String s) throws JsonException {}

    @Override
    public void startDocument(GioDocument document) {
        ensurePendingDocument();
        // copy description and custom attributes
        copyDescAndAttributes(document, pendingDocument);
        lastStarted = OpenCtx.Document;
    }

    @Override
    public void startEdge(GioEdge edge) {
        pendingEdge = cjStream.createEdgeChunk();
        ofNullable(edge.getId()).ifPresent(pendingEdge::id);
        copyDescAndAttributes(edge, pendingEdge);
        lastStarted = OpenCtx.Edge;
        // endpoints
        edge.getEndpoints().forEach(gioEndpoint -> pendingEdge.addEndpoint(cjEp -> {
            // do not persist endpoint id into data; only map known fields
            ofNullable(gioEndpoint.getPort()).ifPresent(cjEp::port);
            cjEp.node(gioEndpoint.getNode());
            cjEp.direction(gioEndpoint.getType().toCjDirection());
        }));
    }

    @Override
    public void startGraph(GioGraph gioGraph) {
        // If a subgraph starts under a not-yet-started node/edge, do NOT auto-close them here; just prepare subgraph
        ensureDocumentStartedIfNeeded();
        ICjGraphChunkMutable graph = cjStream.createGraphChunk();
        ofNullable(gioGraph.getId()).ifPresent(graph::id);
        copyDescAndAttributes(gioGraph, graph);
        GraphCtx ctx = new GraphCtx(graph);
        graphStack.push(ctx);
        lastStarted = OpenCtx.Graph;
    }

    @Override
    public void startNode(GioNode node) {
        pendingNode = cjStream.createNodeChunk();
        ofNullable(node.getId()).ifPresent(pendingNode::id);
        copyDescAndAttributes(node, pendingNode);
        lastStarted = OpenCtx.Node;
    }

    @Override
    public void startPort(GioPort port) {
        ICjPortMutable parentPort = currentPort();
        if (parentPort != null) {
            parentPort.addPort(cjPort -> {
                ofNullable(port.getName()).ifPresent(cjPort::id);
                copyDescAndAttributes(port, cjPort);
                portStack.push(cjPort);
            });
            lastStarted = OpenCtx.Port;
        } else if (pendingNode != null) {
            pendingNode.addPort(cjPort -> {
                ofNullable(port.getName()).ifPresent(cjPort::id);
                copyDescAndAttributes(port, cjPort);
                portStack.push(cjPort);
            });
            lastStarted = OpenCtx.Port;
        }
    }

    private ICjHasDataMutable currentHasDataTarget() {
        // Prefer the most recently started element context
        switch (lastStarted) {
            case Port -> {
                ICjPortMutable cp = currentPort();
                if (cp != null) return cp;
            }
            case Edge -> {
                if (pendingEdge != null) return pendingEdge;
            }
            case Node -> {
                if (pendingNode != null) return pendingNode;
            }
            case Graph -> {
                if (!graphStack.isEmpty()) return graphStack.peek().chunk;
            }
            case Document -> {
                if (pendingDocument != null) return pendingDocument;
            }
            default -> {
            }
        }
        // Fallback order (edge > node > port > graph > document)
        if (pendingEdge != null) return pendingEdge;
        if (pendingNode != null) return pendingNode;
        ICjPortMutable cp = currentPort();
        if (cp != null) return cp;
        if (!graphStack.isEmpty()) return graphStack.peek().chunk;
        if (pendingDocument != null) return pendingDocument;
        return null;
    }

    private @Nullable ICjHasLabelMutable currentHasLabelTarget() {
        // Prefer the most recently started element
        switch (lastStarted) {
            case Port -> {
                ICjPortMutable cp = currentPort();
                if (cp instanceof ICjHasLabelMutable hl) return hl;
            }
            case Edge -> {
                if (pendingEdge != null) return pendingEdge;
            }
            case Node -> {
                if (pendingNode != null) return pendingNode;
            }
            case Graph -> {
                if (!graphStack.isEmpty()) return (ICjHasLabelMutable) graphStack.peek().chunk;
            }
            case Document, None -> {
            }
        }
        // Fallback legacy order: edge > port > node > graph
        if (pendingEdge != null) return pendingEdge;
        ICjPortMutable cp = currentPort();
        if (cp instanceof ICjHasLabelMutable) return (ICjHasLabelMutable) cp;
        if (pendingNode != null) return pendingNode;
        if (!graphStack.isEmpty()) return (ICjHasLabelMutable) graphStack.peek().chunk;
        return null;
    }

    private @Nullable ICjPortMutable currentPort() {
        return portStack.peek();
    }

    private void emitGraphRecursively(GraphCtx ctx) {
        cjStream.graphStart(ctx.chunk);
        // nodes
        for (ICjNodeChunkMutable n : ctx.bufferedNodes) {
            cjStream.nodeStart(n);
            cjStream.nodeEnd();
        }
        // edges
        for (ICjEdgeChunkMutable e : ctx.bufferedEdges) {
            cjStream.edgeStart(e);
            cjStream.edgeEnd();
        }
        // subgraphs
        for (GraphCtx g : ctx.bufferedSubgraphs) {
            emitGraphRecursively(g);
        }
        cjStream.graphEnd();
    }

    private void ensureDocumentStartedIfNeeded() {
        if (pendingDocument != null) {
            cjStream.documentStart(pendingDocument);
            pendingDocument = null;
        }
    }

    // Helpers
    private void ensurePendingDocument() {
        if (pendingDocument == null) pendingDocument = cjStream.createDocumentChunk();
    }

}
