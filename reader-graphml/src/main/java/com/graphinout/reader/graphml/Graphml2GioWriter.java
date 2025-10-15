package com.graphinout.reader.graphml;

import com.graphinout.base.gio.GioData;
import com.graphinout.base.gio.GioDocument;
import com.graphinout.base.gio.GioEdge;
import com.graphinout.base.gio.GioEndpoint;
import com.graphinout.base.gio.GioEndpointDirection;
import com.graphinout.base.gio.GioGraph;
import com.graphinout.base.gio.GioKey;
import com.graphinout.base.gio.GioNode;
import com.graphinout.base.gio.GioPort;
import com.graphinout.base.gio.GioWriter;
import com.graphinout.base.graphml.GraphmlWriter;
import com.graphinout.base.graphml.IGraphmlData;
import com.graphinout.base.graphml.IGraphmlDocument;
import com.graphinout.base.graphml.IGraphmlEdge;
import com.graphinout.base.graphml.IGraphmlElement;
import com.graphinout.base.graphml.IGraphmlEndpoint;
import com.graphinout.base.graphml.IGraphmlGraph;
import com.graphinout.base.graphml.IGraphmlHyperEdge;
import com.graphinout.base.graphml.IGraphmlKey;
import com.graphinout.base.graphml.IGraphmlLocator;
import com.graphinout.base.graphml.IGraphmlNode;
import com.graphinout.base.graphml.IGraphmlPort;
import com.graphinout.foundation.util.Nullables;
import com.graphinout.foundation.util.PowerStackOnClasses;

import java.io.IOException;

import static com.graphinout.foundation.util.Nullables.ifPresentAccept;
import static com.graphinout.foundation.util.Nullables.mapOrNull;

public class Graphml2GioWriter implements GraphmlWriter {

    private final GioWriter gioWriter;
    /** Graphml elements on the stack for locators */
    private final PowerStackOnClasses<IGraphmlElement> stack = PowerStackOnClasses.create();

    public Graphml2GioWriter(GioWriter gioWriter) {this.gioWriter = gioWriter;}

    @Override
    public void data(IGraphmlData graphml) throws IOException {
        GioData.GioDataBuilder gioBuilder = GioData.builder();
        gioBuilder.key(graphml.key());
        gioBuilder.xmlValue(graphml.xmlValue());
        gioWriter.data(gioBuilder.build());
    }

    @Override
    public void documentEnd() throws IOException {
        stack.pop(IGraphmlDocument.class);
        gioWriter.endDocument();
    }

    @Override
    public void documentStart(IGraphmlDocument graphml) throws IOException {
        stack.push(graphml);
        GioDocument.GioDocumentBuilder gioBuilder = GioDocument.builder();
        gioBuilder.customAttributes(graphml.customXmlAttributes());
        ifPresentAccept(graphml.desc(), desc -> gioBuilder.description(desc.xmlValue()));
        gioWriter.startDocument(gioBuilder.build());
    }

    @Override
    public void edgeEnd() throws IOException {
        stack.pop(IGraphmlEdge.class);
        gioWriter.endEdge();
    }

    @Override
    public void edgeStart(IGraphmlEdge graphml) throws IOException {
        Boolean edgeDirected = graphml.directed();
        if (edgeDirected == null) {
            // take from surrounding graph
            IGraphmlGraph.EdgeDefault graphDirectedEdges = stack.peek(IGraphmlGraph.class).edgeDefault();
            edgeDirected = graphDirectedEdges != IGraphmlGraph.EdgeDefault.undirected;
        }

        stack.push(graphml);
        GioEdge.GioEdgeBuilder gioBuilder = GioEdge.builder();
        ifPresentAccept(graphml.id(), gioBuilder::id);
        gioBuilder.customAttributes(graphml.customXmlAttributes());
        ifPresentAccept(graphml.desc(), desc -> gioBuilder.description(desc.xmlValue()));

        GioEndpoint.GioEndpointBuilder sourceBuilder = GioEndpoint.builder();
        sourceBuilder.node(graphml.source());
        ifPresentAccept(graphml.sourcePort(), sourceBuilder::port);
        sourceBuilder.type(edgeDirected ? GioEndpointDirection.In : GioEndpointDirection.Undirected);
        gioBuilder.endpoint(sourceBuilder.build());

        GioEndpoint.GioEndpointBuilder targetBuilder = GioEndpoint.builder();
        targetBuilder.node(graphml.target());
        ifPresentAccept(graphml.targetPort(), targetBuilder::port);
        targetBuilder.type(edgeDirected ? GioEndpointDirection.Out : GioEndpointDirection.Undirected);
        gioBuilder.endpoint(targetBuilder.build());

        gioWriter.startEdge(gioBuilder.build());
    }

    @Override
    public void graphEnd() throws IOException {
        IGraphmlGraph graphml = stack.pop(IGraphmlGraph.class);
        gioWriter.endGraph(Nullables.mapOrNull(graphml.locator(), IGraphmlLocator::xlinkHref));
    }

    @Override
    public void graphStart(IGraphmlGraph graphml) throws IOException {
        stack.push(graphml);
        GioGraph.GioGraphBuilder gioBuilder = GioGraph.builder();
        ifPresentAccept(graphml.id(), gioBuilder::id);
        gioBuilder.customAttributes(graphml.customXmlAttributes());
        ifPresentAccept(graphml.desc(), desc -> gioBuilder.description(desc.xmlValue()));
        gioBuilder.edgedefaultDirected(graphml.edgeDefault() == IGraphmlGraph.EdgeDefault.directed);
        gioWriter.startGraph(gioBuilder.build());
    }

    @Override
    public void hyperEdgeEnd() throws IOException {
        stack.pop(IGraphmlHyperEdge.class);
        gioWriter.endEdge();
    }

    @Override
    public void hyperEdgeStart(IGraphmlHyperEdge graphml) throws IOException {
        stack.push(graphml);
        GioEdge.GioEdgeBuilder gioBuilder = GioEdge.builder();
        ifPresentAccept(graphml.id(), gioBuilder::id);
        gioBuilder.customAttributes(graphml.customXmlAttributes());
        ifPresentAccept(graphml.desc(), desc -> gioBuilder.description(desc.xmlValue()));

        for (IGraphmlEndpoint graphmlEp : graphml.endpoints()) {
            GioEndpoint.GioEndpointBuilder gioEpBuilder = GioEndpoint.builder();
            gioEpBuilder.node(graphmlEp.node());
            ifPresentAccept(graphmlEp.port(), gioEpBuilder::port);
            gioEpBuilder.type(graphmlEp.type().toGio());
            gioBuilder.endpoint(gioEpBuilder.build());
        }

        gioWriter.startEdge(gioBuilder.build());
    }

    @Override
    public void key(IGraphmlKey graphmlKey) throws IOException {
     //   stack.push(graphmlKey);
        GioKey.GioKeyBuilder builder = GioKey.builder();
        builder.customAttributes(graphmlKey.customXmlAttributes());
        ifPresentAccept(graphmlKey.desc(), desc -> builder.description(desc.xmlValue()));
        builder.id(graphmlKey.id());
        builder.forType(graphmlKey.forType().toGio());
        builder.attributeType(graphmlKey.toGioDataType());
        builder.attributeName(graphmlKey.attrName());
        ifPresentAccept(graphmlKey.defaultValue(), defaultValue -> builder.defaultValue(defaultValue.xmlValue()));
        gioWriter.key(builder.build());
    }

    @Override
    public void nodeEnd() throws IOException {
        IGraphmlNode graphml = stack.pop(IGraphmlNode.class);
        gioWriter.endNode(mapOrNull(graphml.locator(), IGraphmlLocator::xlinkHref));
    }

    @Override
    public void nodeStart(IGraphmlNode graphml) throws IOException {
        stack.push(graphml);
        GioNode.GioNodeBuilder gioBuilder = GioNode.builder();
        ifPresentAccept(graphml.id(), gioBuilder::id);
        gioBuilder.customAttributes(graphml.customXmlAttributes());
        ifPresentAccept(graphml.desc(), desc -> gioBuilder.description(desc.xmlValue()));
        gioWriter.startNode(gioBuilder.build());
    }

    @Override
    public void portEnd() throws IOException {
        stack.pop(IGraphmlPort.class);
        gioWriter.endPort();
    }

    @Override
    public void portStart(IGraphmlPort graphml) throws IOException {
        stack.push(graphml);
        GioPort.GioPortBuilder gioBuilder = GioPort.builder();
        ifPresentAccept(graphml.name(), gioBuilder::name);
        gioBuilder.customAttributes(graphml.customXmlAttributes());
        ifPresentAccept(graphml.desc(), desc -> gioBuilder.description(desc.xmlValue()));
        gioWriter.startPort(gioBuilder.build());
    }

}
