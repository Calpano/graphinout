package com.calpano.graphinout.base.graphml;

import com.calpano.graphinout.base.gio.Direction;
import com.calpano.graphinout.base.gio.GioData;
import com.calpano.graphinout.base.gio.GioDocument;
import com.calpano.graphinout.base.gio.GioEdge;
import com.calpano.graphinout.base.gio.GioElement;
import com.calpano.graphinout.base.gio.GioElementWithDescription;
import com.calpano.graphinout.base.gio.GioEndpoint;
import com.calpano.graphinout.base.gio.GioGraph;
import com.calpano.graphinout.base.gio.GioKey;
import com.calpano.graphinout.base.gio.GioNode;
import com.calpano.graphinout.base.gio.GioPort;
import com.calpano.graphinout.base.gio.GioWriter;

import javax.annotation.Nullable;
import java.io.IOException;
import java.net.URL;
import java.util.Optional;
import java.util.TreeMap;

public class Gio2GraphmlWriter extends BufferingJsonWriter implements GioWriter {

    final GraphmlWriter graphmlWriter;
    private @Nullable GraphmlElement openEdge;
    private String baseuri;

    public Gio2GraphmlWriter(GraphmlWriter graphmlWriter) {
        this.graphmlWriter = graphmlWriter;
    }

    @Override
    public void data(GioData gioData) throws IOException {
        GraphmlData graphmlData = GraphmlData.builder().key(gioData.getKey()).value(gioData.getValue()).build();
        gioData.id().ifPresent(graphmlData::setId);
        graphmlWriter.data(graphmlData);
    }

    @Override
    public void endDocument() throws IOException {
        graphmlWriter.endDocument();
    }

    @Override
    public void endEdge() throws IOException {
        assert this.openEdge != null : "openEdge may not be null when ending an edge";
        assert this.openEdge instanceof GraphmlHyperEdge | this.openEdge instanceof GraphmlEdge;
        if (openEdge instanceof GraphmlHyperEdge) {
            graphmlWriter.endHyperEdge();
        } else {
            graphmlWriter.endEdge();
        }
        this.openEdge = null;
    }

    @Override
    public void endGraph(@Nullable URL url) throws IOException {
        graphmlWriter.endGraph(locator(url));
    }

    @Override
    public void endNode(@Nullable URL url) throws IOException {
        graphmlWriter.endNode(locator(url));

    }

    @Override
    public void endPort() throws IOException {
        graphmlWriter.endPort();
    }

    @Override
    public void key(GioKey gioKey) throws IOException {
        GraphmlKey graphmlKey = GraphmlKey.builder()//
                .id(gioKey.getId())//
                .forType(GraphmlKeyForType.keyForType(gioKey.getForType().value))//
                .build();
        gioKey.defaultValue().ifPresent(defaultValue -> graphmlKey.setDefaultValue(GraphmlDefault.builder().value(defaultValue).build()));
        gioKey.attributeName().ifPresent(graphmlKey::setAttrName);
        gioKey.attributeType().ifPresent(attType -> graphmlKey.setAttrType(attType.graphmlName));
        customAttributes(gioKey, graphmlKey);
        desc(gioKey, graphmlKey);
        graphmlWriter.key(graphmlKey);
    }

    @Override
    public void startDocument(GioDocument document) throws IOException {
        GraphmlDocument graphmlDocument = new GraphmlDocument();
        desc(document, graphmlDocument);
        if (document.getCustomAttributes() != null) {
            if (graphmlDocument.getExtraAttrib() == null) graphmlDocument.setExtraAttrib(new TreeMap<>());
            document.getCustomAttributes().forEach((k, v) -> graphmlDocument.getExtraAttrib().put(k, v));
        }
        // TODO: Implement baseuri handling
        graphmlWriter.startDocument(graphmlDocument);
    }

    @Override
    public void startEdge(GioEdge gioEdge) throws IOException {
        if (gioEdge.getEndpoints().size() == 2) {
            GioEndpoint s = gioEdge.getEndpoints().get(0);
            GioEndpoint t = gioEdge.getEndpoints().get(1);
            Boolean directed;
            if (s.getType().isDirected() && t.getType().isDirected()) {
                directed = true;
            } else if (s.getType().isUndirected() && t.getType().isUndirected()) {
                directed = false;
            } else {
                // edge can only be represented as hyper-edge
                directed = null;
            }
            if (directed != null) {
                GraphmlEdge edge = new GraphmlEdge();
                if (gioEdge.getId() != null) edge.setId(gioEdge.getId());
                edge.setDirected(directed);
                edge.setSourceId(s.getNode());
                edge.setTargetId(t.getNode());
                if (s.getPort() != null) {
                    edge.setSourcePortId(s.getPort());
                }
                if (t.getPort() != null) {
                    edge.setTargetPortId(t.getPort());
                }
                assert gioEdge.isValid();
                graphmlWriter.startEdge(edge);
                this.openEdge = edge;
                return;
            }
        }
        assert gioEdge.getEndpoints() != null;
        assert gioEdge.getEndpoints().size() > 2;
        // default case: hyperedge
        GraphmlHyperEdge.GraphmlHyperEdgeBuilder builder = GraphmlHyperEdge.builder(gioEdge.getId());
        gioEdge.getEndpoints().stream().map(this::graphmlEndpoint).forEach(builder::addEndpoint);
        GraphmlHyperEdge graphmlHyperEdge = builder.build();
        customAttributes(gioEdge, graphmlHyperEdge);
        graphmlWriter.startHyperEdge(graphmlHyperEdge);
        this.openEdge = graphmlHyperEdge;
    }

    @Override
    public void startGraph(GioGraph gioGraph) throws IOException {
        GraphmlGraph graphmlGraph = GraphmlGraph.builder().id(gioGraph.getId()).edgedefault(gioGraph.isEdgedefaultDirected() ? GraphmlGraph.EdgeDefault.directed : GraphmlGraph.EdgeDefault.undirected).build();
        desc(gioGraph, graphmlGraph);

        graphmlWriter.startGraph(graphmlGraph);
    }

    @Override
    public void startNode(GioNode gioNode) throws IOException {
        GraphmlNode graphmlNode = GraphmlNode.builder().id(gioNode.getId()).build();
        desc(gioNode, graphmlNode);
        graphmlWriter.startNode(graphmlNode);
    }

    @Override
    public void startPort(GioPort port) throws IOException {
        graphmlWriter.startPort(new GraphmlPort(port.getName()));
    }

    private void customAttributes(GioElement gioElement, GraphmlElement graphmlElement) {
        if (gioElement.getCustomAttributes() != null) {
            if (graphmlElement.getExtraAttrib() == null) graphmlElement.setExtraAttrib(new TreeMap<>());
            graphmlElement.getExtraAttrib().putAll(gioElement.getCustomAttributes());
        }
    }

    private void desc(GioElementWithDescription elementWithDescription, GraphmlGraphCommonElement graphmlGraphCommonElement) {
        elementWithDescription.description().ifPresent(desc -> graphmlGraphCommonElement.setDesc(GraphmlDescription.builder().value(desc).build()));
    }

    private GraphmlEndpoint graphmlEndpoint(GioEndpoint endpoint) {
        GraphmlEndpoint graphmlEndpoint = GraphmlEndpoint.builder().id(endpoint.getId()).node(endpoint.getNode()).build();
        Optional.ofNullable(endpoint.getPort()).ifPresent(graphmlEndpoint::setPort);
        Direction dir = switch (endpoint.getType()) {
            case In -> Direction.In;
            case Out -> Direction.Out;
            case Undirected -> Direction.Undirected;
        };
        graphmlEndpoint.setType(GraphmlDirection.of(dir));
        return graphmlEndpoint;
    }

    private GraphmlLocator locator(@Nullable URL url) {
        return url == null ? null : GraphmlLocator.builder().xLinkHref(url).build();
    }

    @Override
    public void baseUri(String baseUri) throws IOException {
        this.baseuri = baseUri;
    }

}
