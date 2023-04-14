package com.calpano.graphinout.base.graphml;

import com.calpano.graphinout.base.Direction;
import com.calpano.graphinout.base.xml.XmlWriter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.calpano.graphinout.base.graphml.GraphmlDocument.builder;
import static org.junit.jupiter.api.Assertions.assertEquals;

class GraphmlWriterTest {


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

        public StringBuilder getOutPut() {
            return outPut;
        }

        @Override
        public void lineBreak() throws IOException {
            // ignored in tests
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

    @Spy
    static XmlWriterSpy xmlWriterSpy;
    private static GraphmlWriter graphmlWriter;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        graphmlWriter = new GraphmlWriterImpl(xmlWriterSpy);
        xmlWriterSpy.getOutPut().delete(0, xmlWriterSpy.getOutPut().length());
    }

    @Test
    void data() throws IOException {
        graphmlWriter.key(GraphmlKey.builder().id("test").attrName("attrName").attrType("attrType").forType(GraphmlKeyForType.All).build());
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
        GraphmlLocator locator = GraphmlLocator.builder()
                .xLinkHref(new URL("http:\\127.0.0.1"))
                .build();
        graphmlWriter.endGraph(Optional.of(locator));
        assertEquals("::startElement->locator->{xlink:href=http:\\127.0.0.1}::endElement->locator::endElement->graph", xmlWriterSpy.getOutPut().toString());
    }

    @Test
    void endNode() throws IOException {
        GraphmlLocator locator = GraphmlLocator.builder()
                .xLinkHref(new URL("http:\\127.0.0.1"))
                .build();
        locator.getAttributes().put("locator.extra.attrib", "local");
        graphmlWriter.endNode(Optional.of(locator));
        assertEquals("::startElement->locator->{xlink:href=http:\\127.0.0.1}::endElement->locator::endElement->node", xmlWriterSpy.getOutPut().toString());
    }

    @Test
    void startDocument() throws IOException {
        graphmlWriter.startDocument(builder().build());
        assertEquals("::startDocument->::startElement->graphml->{xmlns=http://graphml.graphdrawing.org/xmlns, xmlns:xsi=http://www.w3.org/2001/XMLSchema-instance, xsi:schemaLocation=http://graphml.graphdrawing.org/xmlns http://graphml.graphdrawing.org/xmlns/1.0/graphml.xsd}", xmlWriterSpy.getOutPut().toString());

    }

    @Test
    void startEdge() throws IOException {
        graphmlWriter.startHyperEdge(GraphmlHyperEdge.builder("edge1")
                .addEndpoint(GraphmlEndpoint.builder().id("GioEndpoint1").node("node1").type(Direction.In).port("port1").build())
                .addEndpoint(GraphmlEndpoint.builder().id("GioEndpoint2").node("node2").type(Direction.Out).port("port2").build())
                .build());
        assertEquals("::startElement->hyperedge->{id=edge1}::startElement->endpoint->{id=GioEndpoint1, node=node1, port=port1, type=in}::endElement->endpoint::startElement->endpoint->{id=GioEndpoint2, node=node2, port=port2, type=out}::endElement->endpoint", xmlWriterSpy.getOutPut().toString());

    }

    @Test
    void startGraph() throws IOException {
        graphmlWriter.startGraph(GraphmlGraph.builder().id("graph").edgedefault(GraphmlGraph.EdgeDefault.directed).build());
        assertEquals("::startElement->graph->{id=graph, edgedefault=directed}", xmlWriterSpy.getOutPut().toString());

    }

    @Test
    void startNode() throws IOException {
        List<GraphmlPort> gioPortList = new ArrayList<>();
        gioPortList.add(GraphmlPort.builder().name("port").build());
        List<GraphmlData> graphmlDataList = new ArrayList<>();
        graphmlDataList.add(GraphmlData.builder().key("data").id("id").value("value").build());

        graphmlWriter.startNode(GraphmlNode.builder().desc(GraphmlDescription.builder().value("GraphmlDescription").build()).id("node ").dataList(graphmlDataList).build());
        assertEquals("::startElement->node->{id=node }::startElement->desc->{}::characterData->GraphmlDescription::endElement->desc::startElement->data->{id=id, key=data}::characterData->value::endElement->data", xmlWriterSpy.getOutPut().toString());
    }
}
