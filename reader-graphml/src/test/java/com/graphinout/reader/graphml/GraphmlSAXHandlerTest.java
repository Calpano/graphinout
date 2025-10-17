package com.graphinout.reader.graphml;

import com.graphinout.base.gio.GioData;
import com.graphinout.base.gio.GioDocument;
import com.graphinout.base.gio.GioEdge;
import com.graphinout.base.gio.GioElementWithDescription;
import com.graphinout.base.gio.GioGraph;
import com.graphinout.base.gio.GioKey;
import com.graphinout.base.gio.GioNode;
import com.graphinout.base.gio.GioWriter;
import com.graphinout.base.graphml.GraphmlElements;
import com.graphinout.base.reader.ContentError;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

class GraphmlSAXHandlerTest {

    @DisplayName("The GraphmlSAXHandler  test field by field successfully pass.")
    @Nested
    class successfulTest {

        GraphmlSAXHandler saxHandler;
        @Mock
        GioWriter gioWriter;
        @Mock
        Attributes attributes;
        @Mock
        private Consumer<ContentError> mockErrorConsumer;

        private InOrder inOrder;

        @DisplayName("Only GioDocument End Element successfully pass.")
        @Test
        void endGioDocument() throws SAXException {
            String uri = "uri", localName = "", qName = GraphmlElements.GRAPHML;

            assertNull(saxHandler.getCurrentEntity());

            saxHandler.startElement(uri, localName, qName, attributes);
            assertAll("", () -> assertInstanceOf(GioDocument.class, saxHandler.getCurrentEntity().getEntity()),
                    () -> assertNull(((GioDocument) saxHandler.getCurrentEntity().getEntity()).getDescription()), () -> verifyNoMoreInteractions(gioWriter));

            reset(attributes);
            reset(gioWriter);
            saxHandler.endElement(uri, localName, qName);
            assertAll("", () -> assertNull(saxHandler.getCurrentEntity()), () -> inOrder.verify(gioWriter, times(1)).startDocument(any()), () -> inOrder.verify(gioWriter, times(1)).endDocument(), () -> verifyNoMoreInteractions(gioWriter));
        }

        @Test
        void endGraph_With_locator() throws SAXException {
            final String uri = "uri", localName = "";
            assertNull(saxHandler.getCurrentEntity());
            // <graphml ...>
            saxHandler.startElement(uri, localName, GraphmlElements.GRAPHML, attributes);
            reset(attributes);
            reset(gioWriter);
            when(attributes.getLength()).thenReturn(2);
            when(attributes.getQName(0)).thenReturn("id");
            when(attributes.getValue(0)).thenReturn("id");
            when(attributes.getQName(1)).thenReturn("edgedefault");
            when(attributes.getValue(1)).thenReturn("true");
            // <graph ...>
            saxHandler.startElement(uri, localName, GraphmlElements.GRAPH, attributes);
            // <locator ...>
            reset(attributes);
            reset(gioWriter);
            when(attributes.getLength()).thenReturn(1);
            when(attributes.getQName(0)).thenReturn("xlink:href");
            when(attributes.getValue(0)).thenReturn("http://example.com");
            saxHandler.startElement(uri, localName, GraphmlElements.LOCATOR, attributes);
            assertAll("<locator>",
                    () -> assertInstanceOf(URL.class, saxHandler.getCurrentEntity().getEntity()),
                    () -> assertEquals("http://example.com", saxHandler.getCurrentEntity().getEntity().toString()),
                    () -> verifyNoMoreInteractions(gioWriter));
            reset(attributes);
            reset(gioWriter);
            // </locator>
            saxHandler.endElement(uri, localName, GraphmlElements.LOCATOR);
            assertAll("</locator>",
                    () -> assertInstanceOf(URL.class, saxHandler.getCurrentEntity().getEntity()),
                    () -> assertEquals("http://example.com", saxHandler.getCurrentEntity().getEntity().toString()),
                    () -> verifyNoMoreInteractions(gioWriter));
            // </graph>
            saxHandler.endElement(uri, localName, GraphmlElements.GRAPH);
            assertAll("</graph>",
                    () -> assertInstanceOf(GioDocumentEntity.class, saxHandler.getCurrentEntity()),
                    () -> verify(gioWriter).endGraph(new URL("http://example.com"))
            );
        }

        @Test
        void endNode_Wit_locator() throws SAXException {
            String uri = "uri", localName = "", qName = GraphmlElements.GRAPHML;

            assertNull(saxHandler.getCurrentEntity());
            saxHandler.startElement(uri, localName, qName, attributes);
            qName = GraphmlElements.GRAPH;
            localName = "";
            reset(attributes);
            reset(gioWriter);
            when(attributes.getLength()).thenReturn(2);
            when(attributes.getQName(0)).thenReturn("id");
            when(attributes.getQName(1)).thenReturn("edgedefault");
            when(attributes.getValue(0)).thenReturn("id");
            when(attributes.getValue(1)).thenReturn("true");
            saxHandler.startElement(uri, localName, qName, attributes);
            reset(attributes);
            reset(gioWriter);
            localName = GraphmlElements.NODE;
            qName = GraphmlElements.NODE;
            when(attributes.getLength()).thenReturn(2);
            when(attributes.getQName(0)).thenReturn("id");
            when(attributes.getQName(1)).thenReturn("edgedefault");
            when(attributes.getValue(0)).thenReturn("id");
            when(attributes.getValue(1)).thenReturn("true");
            saxHandler.startElement(uri, localName, qName, attributes);
            qName = GraphmlElements.LOCATOR;
            reset(attributes);
            reset(gioWriter);
            when(attributes.getLength()).thenReturn(1);
            when(attributes.getQName(0)).thenReturn("xlink:href");
            when(attributes.getValue(0)).thenReturn("http://example.com");
            saxHandler.startElement(uri, localName, qName, attributes);
            assertAll("", () -> assertInstanceOf(URL.class, saxHandler.getCurrentEntity().getEntity()), () -> assertEquals("http://example.com", saxHandler.getCurrentEntity().getEntity().toString()), () -> verifyNoMoreInteractions(gioWriter));
            reset(attributes);
            reset(gioWriter);
            saxHandler.endElement(uri, localName, qName);
            assertAll("", () -> assertInstanceOf(URL.class, saxHandler.getCurrentEntity().getEntity()), () -> assertEquals("http://example.com", saxHandler.getCurrentEntity().getEntity().toString()), () -> verifyNoMoreInteractions(gioWriter));
            qName = GraphmlElements.NODE;
            saxHandler.endElement(uri, localName, qName);
            assertAll("",
                    () -> assertInstanceOf(GioGraph.class, saxHandler.getCurrentEntity().getEntity()),
                    () -> verify(gioWriter, times(1)).endNode(new URL("http://example.com")));
        }

        @Test
        void parse_Non_Graphml_XML_data() throws SAXException {
            String uri = "uri", localName = "", qName = GraphmlElements.GRAPHML;

            assertNull(saxHandler.getCurrentEntity());
            saxHandler.startElement(uri, localName, qName, attributes);
            qName = GraphmlElements.GRAPH;
            localName = "";
            reset(attributes);
            reset(gioWriter);
            when(attributes.getLength()).thenReturn(2);
            when(attributes.getQName(0)).thenReturn("id");
            when(attributes.getQName(1)).thenReturn("edgedefault");
            when(attributes.getValue(0)).thenReturn("id");
            when(attributes.getValue(1)).thenReturn("true");
            saxHandler.startElement(uri, localName, qName, attributes);
            reset(attributes);
            reset(gioWriter);
            qName = GraphmlElements.NODE;
            localName = GraphmlElements.NODE;
            when(attributes.getLength()).thenReturn(2);
            when(attributes.getQName(0)).thenReturn("id");
            when(attributes.getQName(1)).thenReturn("edgedefault");
            when(attributes.getValue(0)).thenReturn("id");
            when(attributes.getValue(1)).thenReturn("true");
            saxHandler.startElement(uri, localName, qName, attributes);
            qName = GraphmlElements.DATA;
            saxHandler.startElement(uri, localName, qName, attributes);
            reset(attributes);
            reset(gioWriter);
            qName = "y:SVGNode";
            localName = "y:SVGNode";
            saxHandler.startElement(uri, localName, qName, attributes);
            assertAll("", () -> assertInstanceOf(GioData.class, saxHandler.getCurrentEntity().getEntity()), () -> assertEquals("<y:SVGNode>", ((GioData) saxHandler.getCurrentEntity().getEntity()).getValue().rawXml()));
            saxHandler.endElement(uri, localName, qName);
            assertAll("", () -> assertInstanceOf(GioData.class, saxHandler.getCurrentEntity().getEntity()), () -> assertEquals("<y:SVGNode></y:SVGNode>", ((GioData) saxHandler.getCurrentEntity().getEntity()).getValue().rawXml()));
            qName = GraphmlElements.DATA;
            saxHandler.endElement(uri, localName, qName);
            qName = GraphmlElements.NODE;
            saxHandler.endElement(uri, localName, qName);


        }

        @BeforeEach
        void setUp() {
            MockitoAnnotations.openMocks(this);
            saxHandler = new GraphmlSAXHandler(gioWriter, mockErrorConsumer);
            saxHandler.setStructuralAssertionsEnabled(true);
            inOrder = Mockito.inOrder(gioWriter);
        }

        @DisplayName("All possible field in GioEdge field by field successfully pass.")
        @Test
        void startEdge_All_Possible_Field_Test() throws SAXException {
            String uri = "uri", localName = "", qName = GraphmlElements.GRAPHML;

            assertNull(saxHandler.getCurrentEntity());
            saxHandler.startElement(uri, localName, qName, attributes);
            qName = GraphmlElements.GRAPH;
            localName = "";

            when(attributes.getLength()).thenReturn(2);
            when(attributes.getQName(0)).thenReturn("id");
            when(attributes.getQName(1)).thenReturn("edgedefault");
            when(attributes.getValue(0)).thenReturn("id");
            when(attributes.getValue(1)).thenReturn("true");
            saxHandler.startElement(uri, localName, qName, attributes);

            qName = GraphmlElements.EDGE;
            reset(attributes);
            when(attributes.getLength()).thenReturn(1);
            when(attributes.getQName(0)).thenReturn("id");
            when(attributes.getValue(0)).thenReturn("id");
            saxHandler.startElement(uri, localName, qName, attributes);
            assertAll("", () -> assertInstanceOf(GioEdgeEntity.class, saxHandler.getCurrentEntity()), () -> assertInstanceOf(GioEdge.class, ((GioEdgeEntity) saxHandler.getCurrentEntity()).buildEdge()), () -> assertEquals("id", ((GioEdgeEntity) saxHandler.getCurrentEntity()).buildEdge().getId()), () -> verify(gioWriter, times(1)).startDocument(any()), () -> verify(gioWriter, times(1)).startGraph(any()), () -> verifyNoMoreInteractions(gioWriter));
            reset(attributes);
            reset(gioWriter);

            qName = GraphmlElements.DESC;
            saxHandler.startElement(uri, localName, qName, attributes);
            assertAll("", () -> assertTrue(saxHandler.getCurrentEntity().getEntity() instanceof GioElementWithDescription), () -> verifyNoMoreInteractions(gioWriter));
            String descEdge = "this is desc for Edge";
            saxHandler.characters(descEdge.toCharArray(), 0, descEdge.length());
            assertAll("", () -> assertTrue(saxHandler.getCurrentEntity().getEntity() instanceof GioElementWithDescription), () -> assertEquals(descEdge, ((GioElementWithDescription) saxHandler.getCurrentEntity().getEntity()).description().get().rawXml()), () -> verifyNoMoreInteractions(gioWriter));
            saxHandler.endElement(uri, localName, qName);
            assertAll("", () -> assertInstanceOf(GioEdgeEntity.class, saxHandler.getCurrentEntity()), () -> assertInstanceOf(GioEdge.class, ((GioEdgeEntity) saxHandler.getCurrentEntity()).buildEdge()), () -> assertEquals("id", ((GioEdgeEntity) saxHandler.getCurrentEntity()).buildEdge().getId()), () -> assertEquals("this is desc for Edge", ((GioEdgeEntity) saxHandler.getCurrentEntity()).buildEdge().description().get().rawXml()), () -> verifyNoMoreInteractions(gioWriter));
            qName = GraphmlElements.EDGE;
            saxHandler.endElement(uri, localName, qName);
            assertAll("", () -> assertInstanceOf(GioGraphEntity.class, saxHandler.getCurrentEntity()), () -> verify(gioWriter, times(1)).startEdge(any()), () -> verify(gioWriter, times(1)).endEdge(), () -> verifyNoMoreInteractions(gioWriter));
        }

        @DisplayName("Only GioEdge Start and End successfully pass.")
        @Test
        void startEdge_Only_Start_And_End_Element() throws SAXException {

            String uri = "uri", localName = "", qName = GraphmlElements.GRAPHML;

            assertNull(saxHandler.getCurrentEntity());
            saxHandler.startElement(uri, localName, qName, attributes);
            qName = GraphmlElements.GRAPH;
            localName = "";
            reset(attributes);
            reset(gioWriter);
            when(attributes.getLength()).thenReturn(2);
            when(attributes.getQName(0)).thenReturn("id");
            when(attributes.getQName(1)).thenReturn("edgedefault");
            when(attributes.getValue(0)).thenReturn("id");
            when(attributes.getValue(1)).thenReturn("true");
            saxHandler.startElement(uri, localName, qName, attributes);

            qName = GraphmlElements.EDGE;
            reset(gioWriter);
            reset(attributes);
            when(attributes.getLength()).thenReturn(1);
            when(attributes.getQName(0)).thenReturn("id");
            when(attributes.getValue(0)).thenReturn("id");
            saxHandler.startElement(uri, localName, qName, attributes);
            assertAll("", () -> assertInstanceOf(GioEdgeEntity.class, saxHandler.getCurrentEntity()), () -> assertInstanceOf(GioEdge.class, ((GioEdgeEntity) saxHandler.getCurrentEntity()).buildEdge()), () -> assertEquals("id", ((GioEdgeEntity) saxHandler.getCurrentEntity()).buildEdge().getId()), () -> assertNull(((GioEdgeEntity) saxHandler.getCurrentEntity()).buildEdge().getDescription()), () -> inOrder.verify(gioWriter, times(1)).startGraph(any()), () -> verifyNoMoreInteractions(gioWriter));
            reset(attributes);
            reset(gioWriter);
            saxHandler.endElement(uri, localName, qName);
            assertAll("", () -> assertInstanceOf(GioGraphEntity.class, saxHandler.getCurrentEntity()), () -> inOrder.verify(gioWriter, times(1)).startEdge(any()), () -> inOrder.verify(gioWriter, times(1)).endEdge(), () -> verifyNoMoreInteractions(gioWriter));
        }

        @DisplayName("All possible field in GioDocument field by field successfully pass.")
        @Test
        void startGioDocument_All_Possible_Field() throws SAXException {
            final String uri = "uri", localName = "";

            // <graphml>
            saxHandler.startElement(uri, localName, GraphmlElements.GRAPHML, attributes);
            assertAll("", //
                    () -> assertInstanceOf(GioDocument.class, saxHandler.getCurrentEntity().getEntity()),//
                    () -> assertNull(((GioDocument) saxHandler.getCurrentEntity().getEntity()).getDescription()),//
                    () -> verifyNoMoreInteractions(gioWriter));
            reset(attributes);
            reset(gioWriter);

            // <graphml><desc>
            saxHandler.startElement(uri, localName, GraphmlElements.DESC, attributes);
            assertAll("", //
                    () -> assertTrue(saxHandler.getCurrentEntity().getEntity() instanceof GioElementWithDescription),//
                    () -> verifyNoMoreInteractions(gioWriter));
            String descGraphml = "this is desc for Graphml";
            reset(attributes);
            reset(gioWriter);
            saxHandler.characters(descGraphml.toCharArray(), 0, descGraphml.length());
            assertAll("", //
                    () -> assertTrue(saxHandler.getCurrentEntity().getEntity() instanceof GioElementWithDescription),//
                    () -> assertEquals(descGraphml, ((GioElementWithDescription) saxHandler.getCurrentEntity().getEntity()).description().get().rawXml()),//
                    () -> verifyNoMoreInteractions(gioWriter));
            reset(attributes);
            reset(gioWriter);
            // <graphml></desc>
            saxHandler.endElement(uri, localName, GraphmlElements.DESC);
            assertAll("",//
                    () -> verifyNoMoreInteractions(gioWriter), //
                    () -> assertInstanceOf(GioDocument.class, saxHandler.getCurrentEntity().getEntity()), //
                    () -> assertNotNull(((GioDocument) saxHandler.getCurrentEntity().getEntity()).getDescription()));
            reset(attributes);
            reset(gioWriter);

            // <graphml><key>
            when(attributes.getLength()).thenReturn(2);
            when(attributes.getQName(0)).thenReturn("id");
            when(attributes.getQName(1)).thenReturn("for");
            when(attributes.getValue(0)).thenReturn("id");
            when(attributes.getValue(1)).thenReturn("All");
            saxHandler.startElement(uri, localName, GraphmlElements.KEY, attributes);
            assertAll("", //
                    () -> assertInstanceOf(GioKey.class, saxHandler.getCurrentEntity().getEntity()), //
                    () -> verify(attributes, times(2)).getQName(anyInt()),//
                    () -> verify(attributes, times(1)).getLength(),
                    () -> inOrder.verify(gioWriter, times(1)).startDocument(any())
            );
            reset(attributes);
            reset(gioWriter);

            // <graphml><key><desc>
            saxHandler.startElement(uri, localName, GraphmlElements.DESC, attributes);
            assertAll("", () -> assertTrue(saxHandler.getCurrentEntity().getEntity() instanceof GioElementWithDescription), () -> verifyNoMoreInteractions(gioWriter));
            reset(attributes);
            reset(gioWriter);
            String descKey = "this is desc for key";
            saxHandler.characters(descKey.toCharArray(), 0, descKey.length());
            assertAll("", () -> assertTrue(saxHandler.getCurrentEntity().getEntity() instanceof GioElementWithDescription), () -> assertEquals(descKey, ((GioElementWithDescription) saxHandler.getCurrentEntity().getEntity()).description().get().rawXml()), () -> verifyNoMoreInteractions(gioWriter));
            reset(attributes);
            reset(gioWriter);

            // <graphml><key></desc>
            saxHandler.endElement(uri, localName, GraphmlElements.DESC);
            assertAll("", () -> verifyNoMoreInteractions(gioWriter), () -> assertInstanceOf(GioKey.class, saxHandler.getCurrentEntity().getEntity()), () -> assertEquals(descKey, ((GioElementWithDescription) saxHandler.getCurrentEntity().getEntity()).description().get().rawXml()));
            reset(attributes);
            reset(gioWriter);

            // <graphml></key>
            saxHandler.endElement(uri, localName, GraphmlElements.KEY);
            assertAll("", () -> assertInstanceOf(GioDocument.class, saxHandler.getCurrentEntity().getEntity()),
                    () -> assertEquals("this is desc for Graphml", ((GioDocument) saxHandler.getCurrentEntity().getEntity()).getDescription().rawXml()));
            reset(attributes);
            reset(gioWriter);

            // <graphml><key>
            when(attributes.getLength()).thenReturn(2);
            when(attributes.getQName(0)).thenReturn("id");
            when(attributes.getQName(1)).thenReturn("for");
            when(attributes.getValue(0)).thenReturn("id2");
            when(attributes.getValue(1)).thenReturn("All");
            saxHandler.startElement(uri, localName, GraphmlElements.KEY, attributes);
            assertAll("", () -> assertInstanceOf(GioKey.class, saxHandler.getCurrentEntity().getEntity()), () -> verify(attributes, times(2)).getQName(anyInt()), () -> verify(attributes, times(1)).getLength(), () -> verifyNoMoreInteractions(gioWriter));
            reset(attributes);
            reset(gioWriter);

            // <graphml><key><desc>
            saxHandler.startElement(uri, localName, GraphmlElements.DESC, attributes);
            assertAll("", () -> assertTrue(saxHandler.getCurrentEntity().getEntity() instanceof GioElementWithDescription), () -> verifyNoMoreInteractions(gioWriter));
            reset(attributes);
            reset(gioWriter);
            String descKey2 = "this is desc for key2";
            saxHandler.characters(descKey2.toCharArray(), 0, descKey2.length());
            assertAll("", () -> assertTrue(saxHandler.getCurrentEntity().getEntity() instanceof GioElementWithDescription), () -> assertEquals(descKey2, ((GioElementWithDescription) saxHandler.getCurrentEntity().getEntity()).description().get().rawXml()), () -> verifyNoMoreInteractions(gioWriter));
            reset(attributes);
            reset(gioWriter);

            // <graphml><key></desc>
            saxHandler.endElement(uri, localName, GraphmlElements.DESC);
            assertAll("", () -> assertInstanceOf(GioKey.class, saxHandler.getCurrentEntity().getEntity()), () -> assertEquals("this is desc for key2", ((GioElementWithDescription) saxHandler.getCurrentEntity().getEntity()).description().get().rawXml()), () -> verifyNoMoreInteractions(gioWriter));
            reset(attributes);
            reset(gioWriter);

            // <graphml></key>
            saxHandler.endElement(uri, localName, GraphmlElements.KEY);
            assertAll("", () -> assertInstanceOf(GioDocument.class, saxHandler.getCurrentEntity().getEntity()),
                    () -> assertEquals("this is desc for Graphml", ((GioDocument) saxHandler.getCurrentEntity().getEntity()).getDescription().rawXml()));
            reset(attributes);
            reset(gioWriter);

            // <graphml><data>
            when(attributes.getLength()).thenReturn(2);
            when(attributes.getQName(0)).thenReturn("id");
            when(attributes.getQName(1)).thenReturn("key");
            when(attributes.getValue(0)).thenReturn("id");
            when(attributes.getValue(1)).thenReturn("key");
            saxHandler.startElement(uri, localName, GraphmlElements.DATA, attributes);
            assertAll("", () -> assertInstanceOf(GioData.class, saxHandler.getCurrentEntity().getEntity()),
                    () -> verify(attributes, times(2)).getQName(anyInt()),
                    () -> verify(attributes, times(1)).getLength()
            );
            reset(attributes);
            reset(gioWriter);

            // <graphml></data>
            saxHandler.endElement(uri, localName, GraphmlElements.DATA);
            assertAll("", () -> assertInstanceOf(GioDocument.class, saxHandler.getCurrentEntity().getEntity()),
                    () -> assertEquals("this is desc for Graphml", ((GioDocument) saxHandler.getCurrentEntity().getEntity()).getDescription().rawXml()), () -> inOrder.verify(gioWriter, times(1)).data(any()), () -> verifyNoMoreInteractions(gioWriter));
        }

        @DisplayName("Start Document with only Graph.")
        @Test
        void startGioDocument_Only_Graph() throws SAXException {
            String uri = "uri", localName = "", qName = GraphmlElements.GRAPHML;

            assertNull(saxHandler.getCurrentEntity());

            saxHandler.startElement(uri, localName, qName, attributes);
            assertAll("", () -> assertInstanceOf(GioDocument.class, saxHandler.getCurrentEntity().getEntity()),
                    () -> assertNull(((GioDocument) saxHandler.getCurrentEntity().getEntity()).getDescription()), () -> verifyNoMoreInteractions(gioWriter));

            //ADD Graph
            reset(attributes);
            reset(gioWriter);
            qName = GraphmlElements.GRAPH;
            when(attributes.getLength()).thenReturn(2);
            when(attributes.getQName(0)).thenReturn("id");
            when(attributes.getQName(1)).thenReturn("edgedefault");
            when(attributes.getValue(0)).thenReturn("id");
            when(attributes.getValue(1)).thenReturn("false");

            saxHandler.startElement(uri, localName, qName, attributes);
            assertAll("", () -> assertInstanceOf(GioGraph.class, saxHandler.getCurrentEntity().getEntity()), () -> verify(attributes, times(2)).getQName(anyInt()), () -> verify(attributes, times(1)).getLength(), () -> inOrder.verify(gioWriter, times(1)).startDocument(any()), () -> verifyNoMoreInteractions(gioWriter));


        }

        @DisplayName("Only GioDocument  start and End Element  successfully pass.")
        @Test
        void startGioDocument_Only_Start_And_End_Element() throws SAXException {
            String uri = "uri", localName = GraphmlElements.GRAPHML, qName = GraphmlElements.GRAPHML;

            assertNull(saxHandler.getCurrentEntity());

            saxHandler.startElement(uri, localName, qName, attributes);
            assertAll("", () -> assertInstanceOf(GioDocument.class, saxHandler.getCurrentEntity().getEntity()),
                    () -> assertNull(((GioDocument) saxHandler.getCurrentEntity().getEntity()).getDescription()), () -> verifyNoMoreInteractions(gioWriter));

            qName = GraphmlElements.GRAPHML;

            saxHandler.endElement(uri, localName, qName);
            assertAll("", () -> assertNull(saxHandler.getCurrentEntity()), () -> inOrder.verify(gioWriter, times(1)).startDocument(any()), () -> inOrder.verify(gioWriter, times(1)).endDocument(), () -> verifyNoMoreInteractions(gioWriter));

        }

        @DisplayName("All possible field in GioGraph field by field successfully pass.")
        @Test
        void startGraph() throws SAXException {
            String uri = "uri", localName = "", qName = GraphmlElements.GRAPHML;
            saxHandler.startElement(uri, localName, qName, attributes);
            reset(attributes);
            reset(gioWriter);
            qName = GraphmlElements.GRAPH;
            when(attributes.getLength()).thenReturn(2);
            when(attributes.getQName(0)).thenReturn("id");
            when(attributes.getQName(1)).thenReturn("edgedefault");
            when(attributes.getValue(0)).thenReturn("id");
            when(attributes.getValue(1)).thenReturn("true");
            saxHandler.startElement(uri, localName, qName, attributes);
            assertAll("", //
                    () -> assertInstanceOf(GioGraph.class, saxHandler.getCurrentEntity().getEntity()),//
                    () -> assertEquals("id", ((GioGraph) saxHandler.getCurrentEntity().getEntity()).getId()), //
                    () -> assertTrue(((GioGraph) saxHandler.getCurrentEntity().getEntity()).isEdgedefaultDirected()),//
                    () -> assertNull(((GioGraph) saxHandler.getCurrentEntity().getEntity()).getDescription()), //
                    () -> inOrder.verify(gioWriter, times(1)).startDocument(any()), //
                    () -> verifyNoMoreInteractions(gioWriter));
            reset(attributes);
            reset(gioWriter);
            //ADD Desc
            qName = GraphmlElements.DESC;
            saxHandler.startElement(uri, localName, qName, attributes);
            assertAll("", //
                    () -> assertTrue(saxHandler.getCurrentEntity().getEntity() instanceof GioElementWithDescription), //
                    () -> verifyNoMoreInteractions(gioWriter));
            String descGraph = "this is desc for Graph";
            saxHandler.characters(descGraph.toCharArray(), 0, descGraph.length());
            assertAll("", //
                    () -> assertTrue(saxHandler.getCurrentEntity().getEntity() instanceof GioElementWithDescription), //
                    () -> assertEquals(descGraph, ((GioElementWithDescription) saxHandler.getCurrentEntity().getEntity()).description().get().rawXml()),//
                    () -> verifyNoMoreInteractions(gioWriter));
            saxHandler.endElement(uri, localName, qName);
            assertAll("", //
                    () -> assertInstanceOf(GioGraph.class, saxHandler.getCurrentEntity().getEntity()), //
                    () -> assertEquals("id", ((GioGraph) saxHandler.getCurrentEntity().getEntity()).getId()), //
                    () -> assertTrue(((GioGraph) saxHandler.getCurrentEntity().getEntity()).isEdgedefaultDirected()), //
                    () -> assertNotNull(((GioGraph) saxHandler.getCurrentEntity().getEntity()).getDescription()),//
                    () -> verifyNoMoreInteractions(gioWriter));

            //ADD data
            reset(attributes);
            reset(gioWriter);
            qName = GraphmlElements.DATA;
            when(attributes.getLength()).thenReturn(2);
            when(attributes.getQName(0)).thenReturn("id");
            when(attributes.getQName(1)).thenReturn("key");
            when(attributes.getValue(0)).thenReturn("id");
            when(attributes.getValue(1)).thenReturn("key");

            saxHandler.startElement(uri, localName, qName, attributes);
            assertAll("", //
                    () -> assertInstanceOf(GioData.class, saxHandler.getCurrentEntity().getEntity()), //
                    () -> verify(attributes, times(2)).getQName(anyInt()),//
                    () -> verify(attributes, times(1)).getLength(), //
                    () -> inOrder.verify(gioWriter, times(1)).startGraph(any()), //
                    () -> verifyNoMoreInteractions(gioWriter));
            reset(attributes);
            reset(gioWriter);
            qName = GraphmlElements.DATA;
            saxHandler.endElement(uri, localName, qName);
            assertAll("", //
                    () -> assertInstanceOf(GioGraph.class, saxHandler.getCurrentEntity().getEntity()), //
                    () -> assertEquals(descGraph, ((GioElementWithDescription) saxHandler.getCurrentEntity().getEntity()).description().get().rawXml()),//
                    () -> assertEquals("this is desc for Graph", ((GioElementWithDescription) saxHandler.getCurrentEntity().getEntity()).description().get().rawXml()),//
                    () -> inOrder.verify(gioWriter, times(1)).data(any()), () -> verifyNoMoreInteractions(gioWriter));

            //ADD data 2
            reset(attributes);
            reset(gioWriter);
            qName = GraphmlElements.DATA;
            when(attributes.getLength()).thenReturn(2);
            when(attributes.getQName(0)).thenReturn("id");
            when(attributes.getQName(1)).thenReturn("key");
            when(attributes.getValue(0)).thenReturn("id2");
            when(attributes.getValue(1)).thenReturn("key2");

            saxHandler.startElement(uri, localName, qName, attributes);
            assertAll("", //
                    () -> assertInstanceOf(GioData.class, saxHandler.getCurrentEntity().getEntity()), //
                    () -> verify(attributes, times(2)).getQName(anyInt()), //
                    () -> verify(attributes, times(1)).getLength(),//
                    () -> verifyNoMoreInteractions(gioWriter));
            reset(attributes);
            reset(gioWriter);
            qName = GraphmlElements.DATA;
            saxHandler.endElement(uri, localName, qName);
            assertAll("",//
                    () -> assertInstanceOf(GioGraph.class, saxHandler.getCurrentEntity().getEntity()), //
                    () -> assertEquals("this is desc for Graph", ((GioElementWithDescription) saxHandler.getCurrentEntity().getEntity()).description().get().rawXml()),//
                    () -> verify(gioWriter, times(1)).data(any()), () -> verifyNoMoreInteractions(gioWriter));
            //ADD  an element that sends a graph to GioWriter
            qName = GraphmlElements.NODE;
            reset(attributes);
            reset(gioWriter);
            when(attributes.getLength()).thenReturn(1);
            when(attributes.getQName(0)).thenReturn("id");
            when(attributes.getValue(0)).thenReturn("id");
            saxHandler.startElement(uri, localName, qName, attributes);

            assertAll("When the current element is a graph and a node is added to it, " //
                            + "the graph must be sent to the stream ", //
                    () -> assertInstanceOf(GioNode.class, saxHandler.getCurrentEntity().getEntity()),//
                    () -> assertNull(((GioNode) saxHandler.getCurrentEntity().getEntity()).getDescription()), //
                    () -> verifyNoMoreInteractions(gioWriter));


        }

        @DisplayName("Only GioGraph Start and End successfully pass..")
        @Test
        void startGraph_Only_Start_And_End() throws SAXException {

            String uri = "uri", localName = "", qName = GraphmlElements.GRAPHML;

            assertNull(saxHandler.getCurrentEntity());
            saxHandler.startElement(uri, localName, qName, attributes);
            qName = GraphmlElements.GRAPH;

            reset(attributes);
            reset(gioWriter);
            when(attributes.getLength()).thenReturn(2);
            when(attributes.getQName(0)).thenReturn("id");
            when(attributes.getQName(1)).thenReturn("edgedefault");
            when(attributes.getValue(0)).thenReturn("id");
            when(attributes.getValue(1)).thenReturn("true");
            saxHandler.startElement(uri, localName, qName, attributes);
            assertAll("", //
                    () -> assertInstanceOf(GioGraph.class, saxHandler.getCurrentEntity().getEntity()),//
                    () -> assertEquals("id", ((GioGraph) saxHandler.getCurrentEntity().getEntity()).getId()), //
                    () -> assertTrue(((GioGraph) saxHandler.getCurrentEntity().getEntity()).isEdgedefaultDirected()),//
                    () -> assertNull(((GioGraph) saxHandler.getCurrentEntity().getEntity()).getDescription()),//
                    () -> inOrder.verify(gioWriter, times(1)).startDocument(any()), //
                    () -> verifyNoMoreInteractions(gioWriter));
            reset(attributes);
            reset(gioWriter);
            qName = GraphmlElements.GRAPH;
            saxHandler.endElement(uri, localName, qName);
            assertAll("",//
                    () -> assertInstanceOf(GioDocumentEntity.class, saxHandler.getCurrentEntity()), //
                    () -> inOrder.verify(gioWriter, times(1)).startGraph(any()),//
                    () -> inOrder.verify(gioWriter, times(1)).endGraph(null), //
                    () -> verifyNoMoreInteractions(gioWriter));
        }

        @Test
        void startLocator_Only_Start_And_End_Element() throws SAXException {
            saxHandler.setStructuralAssertionsEnabled(false);
            String uri = "uri", qName = GraphmlElements.LOCATOR, localName = "";


            assertNull(saxHandler.getCurrentEntity());
            reset(attributes);
            reset(gioWriter);
            when(attributes.getLength()).thenReturn(1);
            when(attributes.getQName(0)).thenReturn("xlink:href");
            when(attributes.getValue(0)).thenReturn("http://example.com");
            saxHandler.startElement(uri, localName, qName, attributes);
            assertAll("", //
                    () -> assertInstanceOf(URL.class, saxHandler.getCurrentEntity().getEntity()), //
                    () -> assertEquals("http://example.com", saxHandler.getCurrentEntity().getEntity().toString()),//
                    () -> verifyNoMoreInteractions(gioWriter));
            reset(attributes);
            reset(gioWriter);
            saxHandler.endElement(uri, localName, qName);
            assertAll("", () -> assertInstanceOf(URL.class, saxHandler.getCurrentEntity().getEntity()), () -> assertEquals("http://example.com", saxHandler.getCurrentEntity().getEntity().toString()), () -> verifyNoMoreInteractions(gioWriter));
        }

        @DisplayName("All possible field in GioNode field by field successfully pass.")
        @Test
        void startNode_All_Possible_Field_Test() throws SAXException {
            String uri = "uri", localName = "";
            saxHandler.startElement(uri, localName, GraphmlElements.GRAPHML, attributes);
            when(attributes.getLength()).thenReturn(2);
            when(attributes.getQName(0)).thenReturn("id");
            when(attributes.getQName(1)).thenReturn("edgedefault");
            when(attributes.getValue(0)).thenReturn("id");
            when(attributes.getValue(1)).thenReturn("true");
            saxHandler.startElement(uri, localName, GraphmlElements.GRAPH, attributes);
            reset(attributes);
            when(attributes.getLength()).thenReturn(1);
            when(attributes.getQName(0)).thenReturn("id");
            when(attributes.getValue(0)).thenReturn("id1");
            saxHandler.startElement(uri, localName, GraphmlElements.NODE, attributes);
            assertAll("",//
                    () -> assertInstanceOf(GioNode.class, saxHandler.getCurrentEntity().getEntity()), //
                    () -> assertEquals("id1", ((GioNode) saxHandler.getCurrentEntity().getEntity()).getId()),//
                    () -> assertNull(((GioNode) saxHandler.getCurrentEntity().getEntity()).getDescription()),//
                    () -> inOrder.verify(gioWriter, times(1)).startDocument(any()), //
                    () -> inOrder.verify(gioWriter, times(1)).startGraph(any()), //
                    () -> verifyNoMoreInteractions(gioWriter));
            reset(attributes);
            reset(gioWriter);
            saxHandler.startElement(uri, localName, GraphmlElements.DESC, attributes);
            assertAll("",//
                    () -> assertTrue(saxHandler.getCurrentEntity().getEntity() instanceof GioElementWithDescription), //
                    () -> verify(gioWriter, times(0)).startNode(any()));
            String descNode = "this is desc for Node";
            saxHandler.characters(descNode.toCharArray(), 0, descNode.length());
            assertAll("",//
                    () -> assertTrue(saxHandler.getCurrentEntity().getEntity() instanceof GioElementWithDescription), //
                    () -> assertEquals(descNode, ((GioElementWithDescription) saxHandler.getCurrentEntity().getEntity()).description().get().rawXml()),//
                    () -> verifyNoMoreInteractions(gioWriter));
            reset(gioWriter);
            saxHandler.endElement(uri, localName, GraphmlElements.DESC);
            assertAll("", //
                    () -> assertInstanceOf(GioNode.class, saxHandler.getCurrentEntity().getEntity()), //
                    () -> assertEquals("id1", ((GioNode) saxHandler.getCurrentEntity().getEntity()).getId()), //
                    () -> assertEquals("this is desc for Node", ((GioElementWithDescription) saxHandler.getCurrentEntity().getEntity()).description().get().rawXml()), //
                    () -> verifyNoMoreInteractions(gioWriter));

            //ADD data
            reset(attributes);
            reset(gioWriter);
            when(attributes.getLength()).thenReturn(2);
            when(attributes.getQName(0)).thenReturn("id");
            when(attributes.getQName(1)).thenReturn("key");
            when(attributes.getValue(0)).thenReturn("id");
            when(attributes.getValue(1)).thenReturn("key");

            saxHandler.startElement(uri, localName, GraphmlElements.DATA, attributes);
            assertAll("", //
                    () -> assertInstanceOf(GioData.class, saxHandler.getCurrentEntity().getEntity()), //
                    () -> verify(attributes, times(2)).getQName(anyInt()),//
                    () -> verify(attributes, times(1)).getLength(), //
                    () -> inOrder.verify(gioWriter, times(1)).startNode(any()), //
                    () -> verifyNoMoreInteractions(gioWriter));
            reset(attributes);
            reset(gioWriter);
            saxHandler.endElement(uri, localName, GraphmlElements.DATA);
            assertAll("",//
                    () -> assertInstanceOf(GioNode.class, saxHandler.getCurrentEntity().getEntity()), //
                    () -> assertEquals("this is desc for Node", ((GioElementWithDescription) saxHandler.getCurrentEntity().getEntity()).description().get().rawXml()), //
                    () -> inOrder.verify(gioWriter, times(1)).data(any()),//
                    () -> verifyNoMoreInteractions(gioWriter));
            //ADD data 2
            reset(attributes);
            reset(gioWriter);
            when(attributes.getLength()).thenReturn(2);
            when(attributes.getQName(0)).thenReturn("id");
            when(attributes.getQName(1)).thenReturn("key");
            when(attributes.getValue(0)).thenReturn("id2");
            when(attributes.getValue(1)).thenReturn("key2");

            saxHandler.startElement(uri, localName, GraphmlElements.DATA, attributes);
            assertAll("", //
                    () -> assertInstanceOf(GioData.class, saxHandler.getCurrentEntity().getEntity()),//
                    () -> verify(attributes, times(2)).getQName(anyInt()),//
                    () -> verify(attributes, times(1)).getLength(), //
                    () -> verifyNoMoreInteractions(gioWriter));
            reset(attributes);
            reset(gioWriter);
            saxHandler.endElement(uri, localName, GraphmlElements.DATA);
            assertAll("", //
                    () -> assertInstanceOf(GioNode.class, saxHandler.getCurrentEntity().getEntity()), //
                    () -> assertEquals("this is desc for Node", ((GioElementWithDescription) saxHandler.getCurrentEntity().getEntity()).description().get().rawXml()), //
                    () -> verify(gioWriter, times(1)).data(any()), //
                    () -> verifyNoMoreInteractions(gioWriter));

            reset(gioWriter);
            saxHandler.endElement(uri, localName, GraphmlElements.NODE);
            assertAll("", //
                    () -> assertInstanceOf(GioGraph.class, saxHandler.getCurrentEntity().getEntity()), //
                    () -> verify(gioWriter, times(1)).endNode(null), //
                    () -> verifyNoMoreInteractions(gioWriter));
        }

        @DisplayName("Only GioNode Start and End successfully pass.")
        @Test
        void startNode_Only_Start_And_End_Element() throws SAXException {
            String uri = "uri", localName = "", qName = GraphmlElements.GRAPHML;

            assertNull(saxHandler.getCurrentEntity());
            saxHandler.startElement(uri, localName, qName, attributes);
            qName = GraphmlElements.GRAPH;
            reset(attributes);
            reset(gioWriter);
            when(attributes.getLength()).thenReturn(2);
            when(attributes.getQName(0)).thenReturn("id");
            when(attributes.getQName(1)).thenReturn("edgedefault");
            when(attributes.getValue(0)).thenReturn("id");
            when(attributes.getValue(1)).thenReturn("true");
            saxHandler.startElement(uri, localName, qName, attributes);

            qName = GraphmlElements.NODE;
            reset(attributes);
            reset(gioWriter);
            when(attributes.getLength()).thenReturn(2);
            when(attributes.getQName(0)).thenReturn("id");
            when(attributes.getQName(1)).thenReturn("edgedefault");
            when(attributes.getValue(0)).thenReturn("id");
            when(attributes.getValue(1)).thenReturn("true");
            saxHandler.startElement(uri, localName, qName, attributes);
            assertAll("",//
                    () -> assertInstanceOf(GioNode.class, saxHandler.getCurrentEntity().getEntity()), //
                    () -> assertEquals("id", ((GioNode) saxHandler.getCurrentEntity().getEntity()).getId()), //
                    () -> assertNull(((GioNode) saxHandler.getCurrentEntity().getEntity()).getDescription()), //
                    () -> inOrder.verify(gioWriter, times(1)).startGraph(any()), //
                    () -> verifyNoMoreInteractions(gioWriter));

            reset(attributes);
            reset(gioWriter);
            saxHandler.endElement(uri, localName, qName);
            assertAll("",//
                    () -> assertInstanceOf(GioGraphEntity.class, saxHandler.getCurrentEntity()), //
                    () -> inOrder.verify(gioWriter, times(1)).startNode(any()), //
                    () -> inOrder.verify(gioWriter, times(1)).endNode(null), //
                    () -> verifyNoMoreInteractions(gioWriter));
        }

    }

    @DisplayName("The GraphmlSAXHandler  test field by field not pass.")
    @Nested
    class errorTest {

        private final List<ContentError> storage = new ArrayList<>();
        GraphmlSAXHandler saxHandler;
        @Mock
        GioWriter gioWriter;
        @Mock
        Attributes attributes;

        @DisplayName("Add characters to invalid tag must  Exception Throw.")
        @Test
        void addCharactersToInvalidTag() throws SAXException {
            String uri = "uri", localName = "", qName = GraphmlElements.GRAPHML;

            assertNull(saxHandler.getCurrentEntity());
            saxHandler.startElement(uri, localName, qName, attributes);
            assertAll("", //
                    () -> assertInstanceOf(GioDocument.class, saxHandler.getCurrentEntity().getEntity()), //
                    () -> assertNull(((GioDocument) saxHandler.getCurrentEntity().getEntity()).getDescription()), //
                    () -> verifyNoMoreInteractions(gioWriter));
            reset(attributes);
            //ADD invalid_tag
            localName = "invalid_tag";
            qName = "invalid_tag";
            saxHandler.startElement(uri, localName, qName, attributes);
            assertAll("",//
                    () -> assertTrue(saxHandler.getCurrentEntity().getEntity() instanceof GioElementWithDescription), //
                    () -> verifyNoMoreInteractions(gioWriter), () -> assertEquals(1, storage.size()), //
                    () -> assertEquals("The Element <invalid_tag> not acceptable tag for Graphml.", storage.get(0).getMessage()), //
                    () -> assertEquals(ContentError.ErrorLevel.Warn, storage.get(0).getLevel()));//

            reset(attributes);
            reset(gioWriter);
            String descGraphml = "this is desc for Graphml";

            saxHandler.characters(descGraphml.toCharArray(), 0, descGraphml.length());

            assertAll("", //
                    () -> assertTrue(saxHandler.getCurrentEntity().getEntity() instanceof GioElementWithDescription), //
                    () -> verifyNoMoreInteractions(gioWriter),//
                    () -> assertEquals("Unexpected characters 'this is desc for Graphml' [Element 'graphml' does not allow characters.]", storage.get(1).getMessage()), //
                    () -> assertEquals(ContentError.ErrorLevel.Warn, storage.get(1).getLevel()));//
        }

        @DisplayName("End invalid tag must log ContentError.")
        @Test
        void endInvalidTag() throws SAXException {
            String uri = "uri", localName = "", qName = GraphmlElements.GRAPHML;

            assertNull(saxHandler.getCurrentEntity());

            saxHandler.startElement(uri, localName, qName, attributes);
            reset(attributes);

            localName = "invalid_tag";
            qName = "invalid_tag";

            saxHandler.startElement(uri, localName, qName, attributes);
            assertAll("", //
                    () -> assertEquals(saxHandler.getCurrentEntity().getEntity().getClass(), GioDocument.class), //
                    () -> assertEquals(1, storage.size()),//
                    () -> assertEquals("The Element <invalid_tag> not acceptable tag for Graphml.", storage.get(0).getMessage()),//
                    () -> assertEquals(ContentError.ErrorLevel.Warn, storage.get(0).getLevel()));
        }

        @BeforeEach
        void setUp() {
            Consumer<ContentError> consumer = new Consumer<ContentError>() {
                @Override
                public void accept(ContentError contentError) {
                    storage.add(contentError);
                }
            };
            MockitoAnnotations.openMocks(this);
            saxHandler = new GraphmlSAXHandler(gioWriter, consumer);

        }

        @DisplayName("Only GioDocument  start and End Element  must Exception throw.")
        @Test
        void startGioDocument_Only_Start_And_End_Element() throws SAXException {
            String uri = "uri", localName = "invalid_tag", qName = "invalid_tag";
            assertNull(saxHandler.getCurrentEntity());
            saxHandler.startElement(uri, localName, qName, attributes);
            assertAll("", //
                    () -> assertNull(saxHandler.getCurrentEntity()), //
                    () -> verifyNoMoreInteractions(gioWriter), //
                    () -> assertEquals(1, storage.size()), //
                    () -> assertEquals("The Element <invalid_tag> not acceptable tag for Graphml.", storage.get(0).getMessage()), //
                    () -> assertEquals(ContentError.ErrorLevel.Warn, storage.get(0).getLevel()));
        }


    }


}

