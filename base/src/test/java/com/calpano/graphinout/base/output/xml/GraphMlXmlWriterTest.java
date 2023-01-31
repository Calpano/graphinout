package com.calpano.graphinout.base.output.xml;

import com.calpano.graphinout.base.Direction;
import com.calpano.graphinout.base.gio.*;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.calpano.graphinout.base.gio.GioNode.*;
import static  org.junit.jupiter.api.Assertions.*;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

class GraphMlXmlWriterTest {

    @Spy
    static XmlWriterSpy xmlWriterSpy;
    private static GraphMlXmlWriter xmlWriter;


    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        xmlWriter = new GraphMlXmlWriter(xmlWriterSpy);
        xmlWriterSpy.getOutPut().delete(0,xmlWriterSpy.getOutPut().length());
    }

    @Test
    void startGraphMl() throws IOException {
        xmlWriter.startGraphMl(GioDocument.builder().build());
        assertEquals("::startDocument->::startElement->graphml->{}",xmlWriterSpy.getOutPut().toString());
    }

    @Test
    void startKey() throws IOException {
        xmlWriter.startKey(GioKey.builder().id("test").attrName("attrName").attrType("attrType").forType(GioKeyForType.All).build());
        assertEquals("::startElement->key->{id=test, attr.name=attrName, attr.type=attrType, for=all}",xmlWriterSpy.getOutPut().toString());

    }

    @Test
    void end() throws IOException {
        xmlWriter.endGraphMl(GioDocument.builder().build());
        assertEquals("::endElement->graphml::endDocument",xmlWriterSpy.getOutPut().toString());

    }

    @Test
    void startGraph() throws IOException {
        xmlWriter.startGraph(GioGraph.builder().id("graph").edgedefault(true).build());
        assertEquals("::startElement->graph->{id=graph, edgedefault=true}",xmlWriterSpy.getOutPut().toString());

    }

    @Test
    void startNode() throws IOException {
        List<GioPort> gioPortList  =  new ArrayList<>();
        gioPortList.add(GioPort.builder().name("port").build());
        xmlWriter.startNode(builder().id("node ").ports(gioPortList).build());
        assertEquals("::startElement->node->{id=node }::startElement->port->{name=port}::endElement->port",xmlWriterSpy.getOutPut().toString());

    }

    @Test
    void endNode() throws IOException {
        List<GioPort> gioPortList  =  new ArrayList<>();
        gioPortList.add(GioPort.builder().name("port").build());
        xmlWriter.endNode(builder().id("node ").ports(gioPortList).build());
        assertEquals("::endElement->node",xmlWriterSpy.getOutPut().toString());

    }

    @Test
    void startEdge() throws IOException {
        List<GioEndpoint> gioEndpoints = new ArrayList<>();
        gioEndpoints.add(GioEndpoint.builder().id("GioEndpoint1").node("node1").type(Direction.In).port("port1").build());
        gioEndpoints.add(GioEndpoint.builder().id("GioEndpoint2").node("node2").type(Direction.Out).port("port2").build());
        xmlWriter.startEdge(GioEdge.builder().id("edge1").extraAttrib("extraAttrib").endpoints(gioEndpoints).build());
        assertEquals("::startElement->hyperedge->{id=edge1, hyperEdge.extra.attrib=extraAttrib}::startElement->endpoint->{id=GioEndpoint1, port=port1, node=node1, type=in}::endElement->endpoint::startElement->endpoint->{id=GioEndpoint2, port=port2, node=node2, type=out}::endElement->endpoint",xmlWriterSpy.getOutPut().toString());

    }

    @Test
    void endEdge() throws IOException {
        List<GioEndpoint> gioEndpoints = new ArrayList<>();
        gioEndpoints.add(GioEndpoint.builder().id("GioEndpoint1").node("node1").type(Direction.In).port("port1").build());
        gioEndpoints.add(GioEndpoint.builder().id("GioEndpoint2").node("node2").type(Direction.Out).port("port2").build());
        xmlWriter.endEdge(GioEdge.builder().id("edge1").extraAttrib("extraAttrib").endpoints(gioEndpoints).build());
        assertEquals("::endElement->hyperedge",xmlWriterSpy.getOutPut().toString());

    }

    @Test
    void endGraph() throws IOException {
        xmlWriter.endGraph(GioGraph.builder().id("graph").edgedefault(true).build());
        assertEquals("::endElement->graph",xmlWriterSpy.getOutPut().toString());

    }

    @Test
    void endGraphMl() throws IOException {
        xmlWriter.endGraphMl(GioDocument.builder().build());
        assertEquals("::endElement->graphml::endDocument",xmlWriterSpy.getOutPut().toString());

    }

    class XmlWriterSpy implements XmlWriter {

        private StringBuilder outPut = new StringBuilder();

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

        @Override
        public void startDocument() throws IOException {
            outPut.append("::startDocument->");
        }

        @Override
        public void startElement(String name, Map<String, String> attributes) throws IOException {
            outPut.append("::startElement->").append(name).append("->").append(attributes);
        }

        public StringBuilder getOutPut() {
            return outPut;
        }
    }
}