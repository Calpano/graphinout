package com.calpano.graphinout.base.graphml;

import com.calpano.graphinout.base.graphml.impl.Graphml2XmlWriter;
import com.calpano.graphinout.base.graphml.impl.GraphmlData;
import com.calpano.graphinout.base.graphml.impl.GraphmlDescription;
import com.calpano.graphinout.base.graphml.impl.GraphmlGraph;
import com.calpano.graphinout.base.graphml.impl.GraphmlNode;
import com.calpano.graphinout.foundation.xml.XmlWriterSpy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GraphmlWriterTest {

    @Spy
    static XmlWriterSpy xmlWriterSpy;
    private static GraphmlWriter graphmlWriter;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        graphmlWriter = new Graphml2XmlWriter(xmlWriterSpy);
        xmlWriterSpy.getOutPut().delete(0, xmlWriterSpy.getOutPut().length());
    }

    @Test
    void data() throws IOException {
        graphmlWriter.key(IGraphmlKey.builder().id("test").attrName("attrName").attrType("attrType").forType(GraphmlKeyForType.All).build());
        assertEquals("::startElement->key->{id=test, attr.name=attrName, attr.type=attrType, for=all}::endElement->key", xmlWriterSpy.getOutPut().toString());
    }

    @Test
    void endDocument() throws IOException {
        graphmlWriter.endDocument();
        assertEquals("::endElement->graphml::endDocument", xmlWriterSpy.getOutPut().toString());
    }

    @Test
    void endEdge() throws IOException {
        graphmlWriter.endHyperEdge();
        assertEquals("::endElement->hyperedge", xmlWriterSpy.getOutPut().toString());
    }

    @Test
    void endGraph() throws IOException {
        IGraphmlLocator locator = IGraphmlLocator.builder()
                .xLinkHref(new URL("http:\\127.0.0.1"))
                .build();
        graphmlWriter.endGraph(locator);
        assertEquals("::startElement->locator->{xlink:href=http:\\127.0.0.1}::endElement->locator::endElement->graph", xmlWriterSpy.getOutPut().toString());
    }

    @Test
    void endNode() throws IOException {
        IGraphmlLocator locator = IGraphmlLocator.builder()
                .xLinkHref(new URL("http:\\127.0.0.1"))
                .build();
        locator.attributes().put("locator.extra.attrib", "local");
        graphmlWriter.endNode(locator);
        assertEquals("::startElement->locator->{xlink:href=http:\\127.0.0.1}::endElement->locator::endElement->node", xmlWriterSpy.getOutPut().toString());
    }

    @Test
    void startDocument() throws IOException {
        graphmlWriter.startDocument(IGraphmlDocument.builder().build());
        assertEquals("::startDocument->::startElement->graphml->{xmlns=http://graphml.graphdrawing.org/xmlns, xmlns:xsi=http://www.w3.org/2001/XMLSchema-instance, xsi:schemaLocation=http://graphml.graphdrawing.org/xmlns http://graphml.graphdrawing.org/xmlns/1.0/graphml.xsd}", xmlWriterSpy.getOutPut().toString());

    }

    @Test
    void startEdge() throws IOException {
        graphmlWriter.startHyperEdge(IGraphmlHyperEdge.builder("edge1")
                .addEndpoint(IGraphmlEndpoint.builder().id("GioEndpoint1").node("node1").type(GraphmlDirection.In).port("port1").build())
                .addEndpoint(IGraphmlEndpoint.builder().id("GioEndpoint2").node("node2").type(GraphmlDirection.Out).port("port2").build())
                .build());
        assertEquals("::startElement->hyperedge->{id=edge1}::startElement->endpoint->{id=GioEndpoint1, node=node1, port=port1, type=in}::endElement->endpoint::startElement->endpoint->{id=GioEndpoint2, node=node2, port=port2, type=out}::endElement->endpoint", xmlWriterSpy.getOutPut().toString());

    }

    @Test
    void startGraph() throws IOException {
        graphmlWriter.startGraph(IGraphmlGraph.builder().id("graph").edgedefault(GraphmlGraph.EdgeDefault.directed).build());
        assertEquals("::startElement->graph->{id=graph, edgedefault=directed}", xmlWriterSpy.getOutPut().toString());

    }

    @Test
    void startNode() throws IOException {
        List<IGraphmlPort> gioPortList = new ArrayList<>();
        gioPortList.add(IGraphmlPort.builder().name("port").build());

        graphmlWriter.startNode(GraphmlNode.builder().desc(GraphmlDescription.builder().value("GraphmlDescription").build()).id("node ").build());
        graphmlWriter.data(GraphmlData.builder().key("data").id("id").value("value").build());
        assertEquals("::startElement->node->{id=node }::startElement->desc->{}::characterData->GraphmlDescription::endElement->desc::startElement->data->{id=id, key=data}::characterData->value::endElement->data", xmlWriterSpy.getOutPut().toString());
    }

}
