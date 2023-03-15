package com.calpano.graphinout.base.gio;

import com.calpano.graphinout.base.graphml.GraphmlWriterImpl;
import com.calpano.graphinout.base.xml.XmlWriter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GioWriterTest {

    static class XmlWriterSpy implements XmlWriter {

        private final StringBuilder outPut = new StringBuilder();

        @Override
        public void characterData(String characterData) throws IOException {
            outPut.append("::characterData->").append(characterData);
        }

        @Override
        public void endDocument() throws IOException {
            outPut.append("::endDocument");
        }

        @Override
        public void endElement(String name) throws IOException {
            outPut.append("::endElement->").append(name);
        }

        public StringBuilder getOutPut() {
            return outPut;
        }

        @Override
        public void lineBreak() throws IOException {
            // TODO should we test it too or ignore it?
        }

        @Override
        public void startDocument() throws IOException {
            outPut.append("::startDocument->");
        }

        @Override
        public void startElement(String name, Map<String, String> attributes) throws IOException {
            outPut.append("::startElement->").append(name).append("->").append(attributes);
        }
    }

    // TODO why do we have two XmlWriterSpy implementations? Should re-use the other XmlWriterSpy
    @Spy
    XmlWriterSpy xmlWriterSpy;
    private GioWriter gioWriter;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        xmlWriterSpy.getOutPut().delete(0, xmlWriterSpy.getOutPut().length());
        gioWriter = new GioWriterImpl(new GraphmlWriterImpl(xmlWriterSpy));
    }

    @Test
    void data() throws IOException {
        gioWriter.key(GioKey.builder().id("test").attributeName("attrName").attributeType(GioDataType.typeInt).forType(GioKeyForType.All).build());
        assertEquals("::startElement->key->{id=test, attr.name=attrName, attr.type=int, for=Graphml}::endElement->key", xmlWriterSpy.getOutPut().toString());

    }

    @Test
    void endDocument() throws IOException {
        gioWriter.endDocument();
        assertEquals("::endElement->graphml::endDocument", xmlWriterSpy.getOutPut().toString());

    }

    //TODO The implementation logic will should check.
    //that the Edge can be closed stateless or it should stateful.
    @Disabled
    @Test
    void endEdge() throws IOException {
        gioWriter.endEdge();
        assertEquals("::endElement->edge", xmlWriterSpy.getOutPut().toString());
    }

    @Test
    void endGraph() throws IOException {
        gioWriter.endGraph(new URL("http:\\127.0.0.1"));
        assertEquals("::startElement->locator->{xlink:href=http:\\127.0.0.1, xlink:type=simple}::endElement->locator::endElement->graph", xmlWriterSpy.getOutPut().toString());

    }

    @Test
    void endNode() throws IOException {
        gioWriter.endNode(new URL("http:\\127.0.0.1"));
        assertEquals("::startElement->locator->{xlink:href=http:\\127.0.0.1, xlink:type=simple}::endElement->locator::endElement->node", xmlWriterSpy.getOutPut().toString());

    }

    @Test
    void startDocument() throws IOException {
        gioWriter.startDocument(GioDocument.builder().build());
        assertEquals("::startDocument->::startElement->graphml->{xmlns=http://graphml.graphdrawing.org/xmlns, xmlns:xsi=http://www.w3.org/2001/XMLSchema-instance, xsi:schemaLocation=http://graphml.graphdrawing.org/xmlns http://graphml.graphdrawing.org/xmlns/1.0/graphml.xsd}", xmlWriterSpy.getOutPut().toString());

    }

    @Test
    void startEdge() throws IOException {
        List<GioEndpoint> gioEndpoints = new ArrayList<>();
        gioEndpoints.add(GioEndpoint.builder().id("GioEndpoint1").node("node1").type(GioEndpointDirection.In).port("port1").build());
        gioEndpoints.add(GioEndpoint.builder().id("GioEndpoint2").node("node2").type(GioEndpointDirection.Out).port("port2").build());
        GioEdge edge = GioEdge.builder().id("edge1").endpoints(gioEndpoints).build();
        edge.customAttribute("foo", "bar");
        gioWriter.startEdge(edge);
        assertEquals("::startElement->edge->{id=edge1, source=node1, target=node1, directed=true, sourceport=port1, targetport=port1}", xmlWriterSpy.getOutPut().toString());
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
        assertEquals("::startElement->hyperedge->{id=edge1, foo=bar}::startElement->endpoint->{id=GioEndpoint1, node=node1, port=port1, type=In}::endElement->endpoint::startElement->endpoint->{id=GioEndpoint2, node=node2, port=port2, type=Out}::endElement->endpoint::startElement->endpoint->{id=GioEndpoint3, node=node3, port=port3, type=Out}::endElement->endpoint", xmlWriterSpy.getOutPut().toString());
    }
    @Test
    void startGraph() throws IOException {
        gioWriter.startGraph(GioGraph.builder().id("graph").edgedefaultDirected(true).build());
        assertEquals("::startElement->graph->{id=graph, edgedefault=directed}", xmlWriterSpy.getOutPut().toString());
    }

    @Test
    void startNode() throws IOException {
        gioWriter.startNode(GioNode.builder().description("GraphmlDescription").id("node ").build());
        gioWriter.startPort(GioPort.builder().name("port").build());
        gioWriter.endPort();
        gioWriter.data(GioData.builder().key("data").id("id").value("value").build());
        assertEquals("::startElement->node->{id=node }::startElement->desc->{}::characterData->GraphmlDescription::endElement->desc::startElement->port->{name=port}::endElement->port::startElement->data->{id=id, key=data}::characterData->value::endElement->data", xmlWriterSpy.getOutPut().toString());
    }
}