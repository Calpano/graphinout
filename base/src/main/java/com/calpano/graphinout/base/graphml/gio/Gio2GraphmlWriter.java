package com.calpano.graphinout.base.graphml.gio;

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
import com.calpano.graphinout.base.graphml.GraphmlDirection;
import com.calpano.graphinout.base.graphml.GraphmlKeyForType;
import com.calpano.graphinout.base.graphml.GraphmlWriter;
import com.calpano.graphinout.base.graphml.IGraphmlData;
import com.calpano.graphinout.base.graphml.IGraphmlDefault;
import com.calpano.graphinout.base.graphml.IGraphmlDescription;
import com.calpano.graphinout.base.graphml.IGraphmlDocument;
import com.calpano.graphinout.base.graphml.IGraphmlEdge;
import com.calpano.graphinout.base.graphml.IGraphmlEndpoint;
import com.calpano.graphinout.base.graphml.IGraphmlGraph;
import com.calpano.graphinout.base.graphml.IGraphmlHyperEdge;
import com.calpano.graphinout.base.graphml.IGraphmlKey;
import com.calpano.graphinout.base.graphml.IGraphmlLocator;
import com.calpano.graphinout.base.graphml.IGraphmlNode;
import com.calpano.graphinout.base.graphml.builder.GraphmlDataBuilder;
import com.calpano.graphinout.base.graphml.builder.GraphmlDocumentBuilder;
import com.calpano.graphinout.base.graphml.builder.GraphmlElementBuilder;
import com.calpano.graphinout.base.graphml.builder.GraphmlElementWithDescBuilder;
import com.calpano.graphinout.base.graphml.builder.GraphmlEndpointBuilder;
import com.calpano.graphinout.base.graphml.builder.GraphmlGraphBuilder;
import com.calpano.graphinout.base.graphml.builder.GraphmlHyperEdgeBuilder;
import com.calpano.graphinout.base.graphml.builder.GraphmlKeyBuilder;
import com.calpano.graphinout.base.graphml.builder.GraphmlNodeBuilder;
import com.calpano.graphinout.base.graphml.impl.GraphmlEdge;
import com.calpano.graphinout.base.graphml.impl.GraphmlElement;
import com.calpano.graphinout.base.graphml.impl.GraphmlHyperEdge;
import com.calpano.graphinout.foundation.json.impl.BufferingJsonWriter;

import javax.annotation.Nullable;
import java.io.IOException;
import java.net.URL;
import java.util.Optional;

public class Gio2GraphmlWriter extends BufferingJsonWriter implements GioWriter {

    final GraphmlWriter graphmlWriter;
    private @Nullable GraphmlElement openEdge;
    private String baseuri;

    public Gio2GraphmlWriter(GraphmlWriter graphmlWriter) {
        this.graphmlWriter = graphmlWriter;
    }

    @Override
    public void baseUri(String baseUri) throws IOException {
        this.baseuri = baseUri;
    }

    @Override
    public void data(GioData gioData) throws IOException {
        GraphmlDataBuilder builder = IGraphmlData.builder() //
                .attributes(gioData.getCustomAttributes()) //
                .key(gioData.getKey()).value(gioData.getValue());
        gioData.id().ifPresent(builder::id);
        IGraphmlData graphmlData = builder.build();
        graphmlWriter.data(graphmlData);
    }

    @Override
    public void endDocument() throws IOException {
        graphmlWriter.documentEnd();
    }

    @Override
    public void endEdge() throws IOException {
        assert this.openEdge != null : "openEdge may not be null when ending an edge";
        assert this.openEdge instanceof GraphmlHyperEdge | this.openEdge instanceof GraphmlEdge;
        if (openEdge instanceof GraphmlHyperEdge) {
            graphmlWriter.hyperEdgeEnd();
        } else {
            graphmlWriter.edgeEnd();
        }
        this.openEdge = null;
    }

    @Override
    public void endGraph(@Nullable URL url) throws IOException {
        // FIXME remove URL param and move to startGraph using locator(url)
        graphmlWriter.graphEnd();
    }

    @Override
    public void endNode(@Nullable URL url) throws IOException {
        // FIXME remove URL param and move to startNode using locator(url)
        graphmlWriter.nodeEnd();
    }

    @Override
    public void endPort() throws IOException {
        graphmlWriter.portEnd();
    }

    @Override
    public void key(GioKey gioKey) throws IOException {
        GraphmlKeyBuilder builder = IGraphmlKey.builder()//
                .id(gioKey.getId())//
                .forType(GraphmlKeyForType.keyForType(gioKey.getForType().value));
        gioKey.defaultValue().ifPresent(defaultValue -> builder.defaultValue(IGraphmlDefault.builder().value(defaultValue).build()));
        gioKey.attributeName().ifPresent(builder::attrName);
        gioKey.attributeType().ifPresent(attType -> builder.attrType(attType.graphmlName));
        customAttributes(gioKey, builder);
        desc(gioKey, builder);
        IGraphmlKey graphmlKey = builder.build();
        graphmlWriter.key(graphmlKey);
    }

    @Override
    public void startDocument(GioDocument document) throws IOException {
        GraphmlDocumentBuilder builder = IGraphmlDocument.builder();
        desc(document, builder);
        customAttributes(document, builder);
        // TODO: Implement baseuri handling
        graphmlWriter.documentStart(builder.build());
    }

    @Override
    public void startEdge(GioEdge gioEdge) throws IOException {
        assert gioEdge.isValid();
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
                GraphmlEdge edge = IGraphmlEdge.builder().id(gioEdge.getId()).directed(directed) //
                        .sourceId(s.getNode())//
                        .sourcePortId(s.getPort()).targetId(t.getNode()) //
                        .targetPortId(t.getPort()).build();
                graphmlWriter.edgeStart(edge);
                this.openEdge = edge;
                return;
            }
        }
        assert gioEdge.getEndpoints() != null;
        assert gioEdge.getEndpoints().size() > 2;
        // default case: hyperedge
        GraphmlHyperEdgeBuilder builder = IGraphmlHyperEdge.builder().id(gioEdge.getId());
        gioEdge.getEndpoints().stream().map(this::graphmlEndpoint).forEach(builder::addEndpoint);
        customAttributes(gioEdge, builder);
        GraphmlHyperEdge graphmlHyperEdge = builder.build();
        graphmlWriter.hyperEdgeStart(graphmlHyperEdge);
        this.openEdge = graphmlHyperEdge;
    }

    @Override
    public void startGraph(GioGraph gioGraph) throws IOException {
        GraphmlGraphBuilder builder = IGraphmlGraph.builder().id(gioGraph.getId()).edgeDefault(gioGraph.isEdgedefaultDirected() ? IGraphmlGraph.EdgeDefault.directed : IGraphmlGraph.EdgeDefault.undirected);
        desc(gioGraph, builder);
        graphmlWriter.graphStart(builder.build());
    }

    @Override
    public void startNode(GioNode gioNode) throws IOException {
        GraphmlNodeBuilder builder = IGraphmlNode.builder().id(gioNode.getId());
        desc(gioNode, builder);
        graphmlWriter.nodeStart(builder.build());
    }

    @Override
    public void startPort(GioPort port) throws IOException {
        graphmlWriter.portStart(port.toGraphml());
    }

    private void customAttributes(GioElement gioElement, GraphmlElementBuilder<?> graphmlElement) {
        if (gioElement.getCustomAttributes() != null) {
            graphmlElement.attributes(gioElement.getCustomAttributes());
        }
    }

    private void desc(GioElementWithDescription elementWithDescription, GraphmlElementWithDescBuilder<?> builder) {
        elementWithDescription.description().ifPresent(desc -> builder.desc(IGraphmlDescription.builder().value(desc).build()));
    }

    private IGraphmlEndpoint graphmlEndpoint(GioEndpoint endpoint) {
        GraphmlEndpointBuilder builder = IGraphmlEndpoint.builder().id(endpoint.getId()).node(endpoint.getNode());
        Optional.ofNullable(endpoint.getPort()).ifPresent(builder::port);
        Direction dir = switch (endpoint.getType()) {
            case In -> Direction.In;
            case Out -> Direction.Out;
            case Undirected -> Direction.Undirected;
        };
        builder.type(GraphmlDirection.of(dir));
        return builder.build();
    }

    private IGraphmlLocator locator(@Nullable URL url) {
        return url == null ? null : IGraphmlLocator.builder().xLinkHref(url).build();
    }

}
