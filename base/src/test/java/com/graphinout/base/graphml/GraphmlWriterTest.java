package com.graphinout.base.graphml;

import com.graphinout.foundation.xml.XmlWriterSpy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import java.io.IOException;
import java.net.URI;

import static org.hamcrest.CoreMatchers.endsWith;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

class GraphmlWriterTest {

    @Spy static XmlWriterSpy xmlWriterSpy;
    private static GraphmlWriter graphmlWriter;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        graphmlWriter = new Graphml2XmlWriter(xmlWriterSpy);
        xmlWriterSpy.getOut().delete(0, xmlWriterSpy.getOut().length());
    }

    @Test
    void data() throws IOException {
        graphmlWriter.key(IGraphmlKey.builder().id("test").forType(GraphmlKeyForType.All) //
                .attrName("attrName").attrType(GraphmlDataType.typeDouble).build());
        String output = xmlWriterSpy.getOut().toString();
        assertThat(output, startsWith("<NEWLINE /><element key><atts {attr.name=attrName, attr.type=double, id=test}>"));
        assertThat(output, endsWith("</element key>"));
    }

    @Test
    void documentEnd() throws IOException {
        graphmlWriter.documentEnd();
        assertEquals("</element graphml></DOCUMENT>", xmlWriterSpy.getOut().toString());
    }

    @Test
    void documentStart() throws IOException {
        graphmlWriter.documentStart(IGraphmlDocument.builder().build());
        assertEquals("<DOCUMENT><element graphml><atts {xmlns=http://graphml.graphdrawing.org/xmlns, xmlns:xsi=http://www.w3.org/2001/XMLSchema-instance, xsi:schemaLocation=http://graphml.graphdrawing.org/xmlns http://graphml.graphdrawing.org/xmlns/1.1/graphml.xsd}><NEWLINE />", xmlWriterSpy.getOut().toString());
    }

    @Test
    void edgeEnd() throws IOException {
        graphmlWriter.hyperEdgeEnd();
        assertEquals("</element hyperedge>", xmlWriterSpy.getOut().toString());
    }

    @Test
    void edgeStart() throws IOException {
        graphmlWriter.hyperEdgeStart(IGraphmlHyperEdge.builder().id("edge1").addEndpoint(IGraphmlEndpoint.builder().id("GioEndpoint1").node("node1").type(GraphmlDirection.In).port("port1").build()).addEndpoint(IGraphmlEndpoint.builder().id("GioEndpoint2").node("node2").type(GraphmlDirection.Out).port("port2").build()).build());
        assertEquals("<element hyperedge><atts {id=edge1}><element endpoint><atts {id=GioEndpoint1, node=node1, port=port1, type=in}></element endpoint><element endpoint><atts {id=GioEndpoint2, node=node2, port=port2, type=out}></element endpoint>", xmlWriterSpy.getOut().toString());
    }

    @Test
    void graphEnd() throws IOException {
        // A graph must be started before it can be ended.
        graphmlWriter.graphStart(IGraphmlGraph.builder().id("g1").edgeDefault(IGraphmlGraph.EdgeDefault.directed).build());
        xmlWriterSpy.getOut().delete(0, xmlWriterSpy.getOut().length());

        graphmlWriter.graphEnd();
        assertEquals("</element graph>", xmlWriterSpy.getOut().toString());
    }

    @Test
    void graphStart() throws IOException {
        IGraphmlLocator locator = IGraphmlLocator.builder().xLinkHref(URI.create("http://127.0.0.1").toURL()).build();
        graphmlWriter.graphStart(IGraphmlGraph.builder() //
                .id("graph") //
                .edgeDefault(IGraphmlGraph.EdgeDefault.directed) //
                .locator(locator)//
                .build());
        assertEquals("<NEWLINE /><element graph><atts {edgedefault=directed, id=graph}><element locator><atts {xlink:href=http://127.0.0.1, xlink:type=simple, xmlns:xlink=http://www.w3.org/TR/2000/PR-xlink-20001220/}></element locator>", xmlWriterSpy.getOut().toString());
    }

    @Test
    void nodeEnd() throws IOException {
        graphmlWriter.nodeEnd();
        assertEquals("</element node>", xmlWriterSpy.getOut().toString());
    }

    @Test
    void nodeStart() throws IOException {
        IGraphmlLocator locator = IGraphmlLocator.builder().xLinkHref(URI.create("http://127.0.0.1").toURL()).attribute("locator.extra.attrib", "local").build();
        graphmlWriter.nodeStart(IGraphmlNode.builder() //
                .id("node ") //
                .desc(IGraphmlDescription.of("GraphmlDescription")) //
                .locator(locator) //
                .build());

        assertEquals("<NEWLINE /><element node><atts {id=node }>" + "<element desc><atts {}>" + "<raw>GraphmlDescription</raw></element desc>" + "<element locator><atts {locator.extra.attrib=local, xlink:href=http://127.0.0.1, xlink:type=simple, xmlns:xlink=http://www.w3.org/TR/2000/PR-xlink-20001220/}>" + "</element locator>", xmlWriterSpy.getOut().toString());
    }

}
