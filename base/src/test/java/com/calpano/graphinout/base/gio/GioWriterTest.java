package com.calpano.graphinout.base.gio;

import com.calpano.graphinout.base.graphml.Graphml2XmlWriter;
import com.calpano.graphinout.base.graphml.gio.Gio2GraphmlWriter;
import com.calpano.graphinout.foundation.xml.XmlFragmentString;
import com.calpano.graphinout.foundation.xml.XmlWriterSpy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Disabled
@Deprecated
class GioWriterTest {

    @Spy XmlWriterSpy xmlWriterSpy;
    private GioWriter gioWriter;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        xmlWriterSpy.getOut().delete(0, xmlWriterSpy.getOut().length());
        gioWriter = new Gio2GraphmlWriter(new Graphml2XmlWriter(xmlWriterSpy));
    }

    @Test
    void data() throws IOException {
        gioWriter.key(GioKey.builder().id("test").attributeName("attrName").attributeType(GioDataType.typeInt).forType(GioKeyForType.All).build());
        assertEquals("::startElement->key->{id=test, attr.name=attrName, attr.type=int, for=all}::endElement->key", xmlWriterSpy.getOut().toString());
    }

    @Test
    void endDocument() throws IOException {
        gioWriter.endDocument();
        assertEquals("::endElement->graphml::endDocument", xmlWriterSpy.getOut().toString());
    }

    @Test
    void endEdge() throws IOException {
        gioWriter.startEdge(GioEdge.builder()//
                .endpoint(GioEndpoint.builder().node("node1").type(GioEndpointDirection.Undirected).build()) //
                .endpoint(GioEndpoint.builder().node("node2").type(GioEndpointDirection.Undirected).build()) //
                .build());
        gioWriter.endEdge();
        assertEquals("::startElement->edge->{source=node1, target=node2, directed=false}::endElement->edge", xmlWriterSpy.getOut().toString());
    }

    @Test
    void endGraph() throws IOException {
        gioWriter.endGraph(new URL("http:\\127.0.0.1"));
        assertEquals("::startElement->locator->{xlink:href=http:\\127.0.0.1}::endElement->locator::endElement->graph", xmlWriterSpy.getOut().toString());

    }

    @Test
    void endNode() throws IOException {
        gioWriter.endNode(new URL("http:\\127.0.0.1"));
        assertEquals("::startElement->locator->{xlink:href=http:\\127.0.0.1}::endElement->locator::endElement->node", xmlWriterSpy.getOut().toString());

    }

    @Test
    void startDocument() throws IOException {
        gioWriter.startDocument(GioDocument.builder().build());
        assertEquals("::startDocument->::startElement->graphml->{xmlns=http://graphml.graphdrawing.org/xmlns, xmlns:xsi=http://www.w3.org/2001/XMLSchema-instance, xsi:schemaLocation=http://graphml.graphdrawing.org/xmlns http://graphml.graphdrawing.org/xmlns/1.0/graphml.xsd}", xmlWriterSpy.getOut().toString());

    }

    @Test
    void startEdge() throws IOException {
        List<GioEndpoint> gioEndpoints = new ArrayList<>();
        gioEndpoints.add(GioEndpoint.builder().id("GioEndpoint1").node("node1").type(GioEndpointDirection.In).port("port1").build());
        gioEndpoints.add(GioEndpoint.builder().id("GioEndpoint2").node("node2").type(GioEndpointDirection.Out).port("port2").build());
        GioEdge edge = GioEdge.builder().id("edge1").endpoints(gioEndpoints).build();
        edge.customAttribute("foo", "bar");
        gioWriter.startEdge(edge);
        assertEquals("::startElement->edge->{id=edge1, source=node1, target=node2, directed=true, sourceport=port1, targetport=port2}", xmlWriterSpy.getOut().toString());
    }

    @Test
    void startEdge_With_3_Endpoints() throws IOException {
        List<GioEndpoint> gioEndpoints = new ArrayList<>();
        gioEndpoints.add(GioEndpoint.builder().id("GioEndpoint1").node("node1").type(GioEndpointDirection.In).port("port1").build());
        gioEndpoints.add(GioEndpoint.builder().id("GioEndpoint2").node("node2").type(GioEndpointDirection.Out).port("port2").build());
        gioEndpoints.add(GioEndpoint.builder().id("GioEndpoint3").node("node3").type(GioEndpointDirection.Out).port("port3").build());

        GioEdge edge = GioEdge.builder().id("edge1").endpoints(gioEndpoints).build();
        edge.customAttribute("foo", "bar");
        gioWriter.startEdge(edge);
        assertEquals("::startElement->hyperedge->{id=edge1, foo=bar}::startElement" + "->endpoint->{id=GioEndpoint1, node=node1, port=port1, type=in}::endElement" + "->endpoint::startElement->endpoint->{id=GioEndpoint2, node=node2, port=port2, type=out}::endElement" + "->endpoint::startElement->endpoint->{id=GioEndpoint3, node=node3, port=port3, type=out}::endElement->endpoint", xmlWriterSpy.getOut().toString());
    }

    @Test
    void startGraph() throws IOException {
        gioWriter.startGraph(GioGraph.builder().id("graph").edgedefaultDirected(true).build());
        assertEquals("::startElement->graph->{id=graph, edgedefault=directed}", xmlWriterSpy.getOut().toString());
    }

    @Test
    void startNode() throws IOException {
        gioWriter.startNode(GioNode.builder().description(XmlFragmentString.ofPlainText("GraphmlDescription")).id("node ").build());
        gioWriter.startPort(GioPort.builder().name("port").build());
        gioWriter.endPort();
        gioWriter.data(GioData.builder().key("data").id("id").xmlValue(XmlFragmentString.ofPlainText("value")).build());
        assertEquals("::startElement->node->{id=node }::startElement->desc->{}::characterData->GraphmlDescription::endElement->desc::startElement->port->{name=port}::endElement->port::startElement->data->{id=id, key=data}::characterData->value::endElement->data", xmlWriterSpy.getOut().toString());
    }

}
