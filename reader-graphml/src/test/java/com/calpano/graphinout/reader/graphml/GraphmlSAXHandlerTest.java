package com.calpano.graphinout.reader.graphml;

import com.calpano.graphinout.base.gio.*;
import com.calpano.graphinout.base.reader.ContentErrors;
import com.calpano.graphinout.base.reader.GioReader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

class GraphmlSAXHandlerTest {

    @DisplayName("The GraphmlSAXHandler  test field by field successfully pass.")
    @Nested
    class successfulTest {

        GraphmlSAXHandler saxHandler;

        @Mock
        GioWriter gioWriter;
        @Mock
        Attributes attributes;

        @BeforeEach
        void setUp() {
            MockitoAnnotations.openMocks(this);
            saxHandler = new GraphmlSAXHandler(gioWriter, ContentErrors.defaultErrorHandler());
        }

        @DisplayName("Only GioDocument  start and End Element  successfully pass.")
        @Test
        void startGioDocument_Only_Start_And_End_Element() throws SAXException, IOException {
            String uri = "uri", localName = GraphmlConstant.GRAPHML_ELEMENT_NAME, qName = "";

            assertNull(saxHandler.getCurrentEntity());

            saxHandler.startElement(uri, localName, qName, attributes);
            assertAll("",
                    () -> assertInstanceOf(GioDocument.class, saxHandler.getCurrentEntity().getEntity()),
                    () -> assertNull(((GioDocument) saxHandler.getCurrentEntity().getEntity()).getKeys()),
                    () -> assertNull(((GioDocument) saxHandler.getCurrentEntity().getEntity()).getDataList()),
                    () -> assertNull(((GioDocument) saxHandler.getCurrentEntity().getEntity()).getDescription()),
                    () -> verify(gioWriter, times(0)).startDocument(any()),
                    () -> verify(gioWriter, times(0)).startGraph(any()),
                    () -> verify(gioWriter, times(0)).startNode(any()),
                    () -> verify(gioWriter, times(0)).startEdge(any()),
                    () -> verify(gioWriter, times(0)).endDocument(),
                    () -> verify(gioWriter, times(0)).endGraph(any()),
                    () -> verify(gioWriter, times(0)).endNode(any()),
                    () -> verify(gioWriter, times(0)).endEdge(any())
            );

            localName = GraphmlConstant.GRAPHML_ELEMENT_NAME;

            saxHandler.endElement(uri, localName, qName);
            assertAll("",
                    () -> assertNull(saxHandler.getCurrentEntity()),
                    () -> verify(gioWriter, times(1)).startDocument(any()),
                    () -> verify(gioWriter, times(1)).endDocument(),
                    () -> verify(gioWriter, times(0)).startGraph(any()),
                    () -> verify(gioWriter, times(0)).startNode(any()),
                    () -> verify(gioWriter, times(0)).startEdge(any()),
                    () -> verify(gioWriter, times(0)).endGraph(any()),
                    () -> verify(gioWriter, times(0)).endNode(any()),
                    () -> verify(gioWriter, times(0)).endEdge(any()));

        }

        @DisplayName("All possible field in GioDocument field by field successfully pass.")
        @Test
        void startGioDocument_All_Possible_Field() throws SAXException, IOException {
            String uri = "uri", localName = GraphmlConstant.GRAPHML_ELEMENT_NAME, qName = "";

            assertNull(saxHandler.getCurrentEntity());

            saxHandler.startElement(uri, localName, qName, attributes);
            assertAll("",
                    () -> assertInstanceOf(GioDocument.class, saxHandler.getCurrentEntity().getEntity()),
                    () -> assertNull(((GioDocument) saxHandler.getCurrentEntity().getEntity()).getKeys()),
                    () -> assertNull(((GioDocument) saxHandler.getCurrentEntity().getEntity()).getDataList()),
                    () -> assertNull(((GioDocument) saxHandler.getCurrentEntity().getEntity()).getDescription()),
                    () -> verify(gioWriter, times(0)).startDocument(any()),
                    () -> verify(gioWriter, times(0)).startGraph(any()),
                    () -> verify(gioWriter, times(0)).startNode(any()),
                    () -> verify(gioWriter, times(0)).startEdge(any()),
                    () -> verify(gioWriter, times(0)).endDocument(),
                    () -> verify(gioWriter, times(0)).endGraph(any()),
                    () -> verify(gioWriter, times(0)).endNode(any()),
                    () -> verify(gioWriter, times(0)).endEdge(any()));

            //ADD Desc
            localName = GraphmlConstant.DESC_ELEMENT_NAME;
            saxHandler.startElement(uri, localName, qName, attributes);
            assertAll("",
                    () -> assertTrue(saxHandler.getCurrentEntity().getEntity() instanceof GioElementWithDescription),
                    () -> verify(gioWriter, times(0)).startDocument(any()),
                    () -> verify(gioWriter, times(0)).startGraph(any()),
                    () -> verify(gioWriter, times(0)).startNode(any()),
                    () -> verify(gioWriter, times(0)).startEdge(any()),
                    () -> verify(gioWriter, times(0)).endDocument(),
                    () -> verify(gioWriter, times(0)).endGraph(any()),
                    () -> verify(gioWriter, times(0)).endNode(any()),
                    () -> verify(gioWriter, times(0)).endEdge(any()));

            saxHandler.characters("this is desc for Graphml".toCharArray(), 0, 24);
            assertAll("",
                    () -> assertTrue(saxHandler.getCurrentEntity().getEntity() instanceof GioElementWithDescription),
                    () -> assertEquals("this is desc for Graphml", ((GioElementWithDescription) saxHandler.getCurrentEntity().getEntity()).description().get()),
                    () -> verify(gioWriter, times(0)).startDocument(any()),
                    () -> verify(gioWriter, times(0)).startGraph(any()),
                    () -> verify(gioWriter, times(0)).startNode(any()),
                    () -> verify(gioWriter, times(0)).startEdge(any()),
                    () -> verify(gioWriter, times(0)).endDocument(),
                    () -> verify(gioWriter, times(0)).endGraph(any()),
                    () -> verify(gioWriter, times(0)).endNode(any()),
                    () -> verify(gioWriter, times(0)).endEdge(any()));

            saxHandler.endElement(uri, localName, qName);
            assertAll("",
                    () -> verify(gioWriter, times(0)).startDocument(any()),
                    () -> verify(gioWriter, times(0)).startGraph(any()),
                    () -> verify(gioWriter, times(0)).startNode(any()),
                    () -> verify(gioWriter, times(0)).startEdge(any()),
                    () -> verify(gioWriter, times(0)).endDocument(),
                    () -> verify(gioWriter, times(0)).endGraph(any()),
                    () -> verify(gioWriter, times(0)).endNode(any()),
                    () -> verify(gioWriter, times(0)).endEdge(any()),
                    () -> assertInstanceOf(GioDocument.class, saxHandler.getCurrentEntity().getEntity()),
                    () -> assertNull(((GioDocument) saxHandler.getCurrentEntity().getEntity()).getKeys()),
                    () -> assertNull(((GioDocument) saxHandler.getCurrentEntity().getEntity()).getDataList()),
                    () -> assertNotNull(((GioDocument) saxHandler.getCurrentEntity().getEntity()).getDescription()));

            //ADD Key
            reset(attributes);
            localName = GraphmlConstant.KEY_ELEMENT_NAME;
            when(attributes.getLength()).thenReturn(2);
            when(attributes.getType(0)).thenReturn("id");
            when(attributes.getType(1)).thenReturn("for");
            when(attributes.getValue(0)).thenReturn("id");
            when(attributes.getValue(1)).thenReturn("All");

            saxHandler.startElement(uri, localName, qName, attributes);
            assertAll("",
                    () -> assertInstanceOf(GioKey.class, saxHandler.getCurrentEntity().getEntity()),
                    () -> verify(gioWriter, times(0)).startDocument(any()),
                    () -> verify(gioWriter, times(0)).startGraph(any()),
                    () -> verify(gioWriter, times(0)).startNode(any()),
                    () -> verify(gioWriter, times(0)).startEdge(any()),
                    () -> verify(gioWriter, times(0)).endDocument(),
                    () -> verify(gioWriter, times(0)).endGraph(any()),
                    () -> verify(gioWriter, times(0)).endNode(any()),
                    () -> verify(gioWriter, times(0)).endEdge(any()),
                    () -> verify(attributes, times(2)).getType(anyInt()),
                    () -> verify(attributes, times(1)).getLength());
            reset(attributes);
            localName = GraphmlConstant.DESC_ELEMENT_NAME;

            saxHandler.startElement(uri, localName, qName, attributes);
            assertAll("",
                    () -> assertTrue(saxHandler.getCurrentEntity().getEntity() instanceof GioElementWithDescription),
                    () -> verify(gioWriter, times(0)).startDocument(any()),
                    () -> verify(gioWriter, times(0)).startGraph(any()),
                    () -> verify(gioWriter, times(0)).startNode(any()),
                    () -> verify(gioWriter, times(0)).startEdge(any()),
                    () -> verify(gioWriter, times(0)).endDocument(),
                    () -> verify(gioWriter, times(0)).endGraph(any()),
                    () -> verify(gioWriter, times(0)).endNode(any()),
                    () -> verify(gioWriter, times(0)).endEdge(any()));

            saxHandler.characters("this is desc for key".toCharArray(), 0, 19);
            assertAll("",
                    () -> assertTrue(saxHandler.getCurrentEntity().getEntity() instanceof GioElementWithDescription),
                    () -> assertEquals("this is desc for key", ((GioElementWithDescription) saxHandler.getCurrentEntity().getEntity()).description().get()),
                    () -> verify(gioWriter, times(0)).startDocument(any()),
                    () -> verify(gioWriter, times(0)).startGraph(any()),
                    () -> verify(gioWriter, times(0)).startNode(any()),
                    () -> verify(gioWriter, times(0)).startEdge(any()),
                    () -> verify(gioWriter, times(0)).endDocument(),
                    () -> verify(gioWriter, times(0)).endGraph(any()),
                    () -> verify(gioWriter, times(0)).endNode(any()),
                    () -> verify(gioWriter, times(0)).endEdge(any()));

            saxHandler.endElement(uri, localName, qName);
            assertAll("",
                    () -> verify(gioWriter, times(0)).startDocument(any()),
                    () -> verify(gioWriter, times(0)).startGraph(any()),
                    () -> verify(gioWriter, times(0)).startNode(any()),
                    () -> verify(gioWriter, times(0)).startEdge(any()),
                    () -> verify(gioWriter, times(0)).endDocument(),
                    () -> verify(gioWriter, times(0)).endGraph(any()),
                    () -> verify(gioWriter, times(0)).endNode(any()),
                    () -> verify(gioWriter, times(0)).endEdge(any()),
                    () -> assertInstanceOf(GioKey.class, saxHandler.getCurrentEntity().getEntity()),
                    () -> assertEquals("this is desc for key", ((GioElementWithDescription) saxHandler.getCurrentEntity().getEntity()).description().get()));

            localName = GraphmlConstant.KEY_ELEMENT_NAME;
            saxHandler.endElement(uri, localName, qName);
            assertAll("",
                    () -> assertInstanceOf(GioDocument.class, saxHandler.getCurrentEntity().getEntity()),
                    () -> assertEquals(1, ((GioDocument) saxHandler.getCurrentEntity().getEntity()).getKeys().size()),
                    () -> assertNull(((GioDocument) saxHandler.getCurrentEntity().getEntity()).getDataList()),
                    () -> assertEquals("this is desc for Graphml", ((GioDocument) saxHandler.getCurrentEntity().getEntity()).getDescription()),
                    () -> verify(gioWriter, times(0)).startDocument(any()),
                    () -> verify(gioWriter, times(0)).startGraph(any()),
                    () -> verify(gioWriter, times(0)).startNode(any()),
                    () -> verify(gioWriter, times(0)).startEdge(any()),
                    () -> verify(gioWriter, times(0)).endDocument(),
                    () -> verify(gioWriter, times(0)).endGraph(any()),
                    () -> verify(gioWriter, times(0)).endNode(any()),
                    () -> verify(gioWriter, times(0)).endEdge(any()));


            //ADD Key 2
            reset(attributes);
            localName = GraphmlConstant.KEY_ELEMENT_NAME;
            when(attributes.getLength()).thenReturn(2);
            when(attributes.getType(0)).thenReturn("id");
            when(attributes.getType(1)).thenReturn("for");
            when(attributes.getValue(0)).thenReturn("id2");
            when(attributes.getValue(1)).thenReturn("All");

            saxHandler.startElement(uri, localName, qName, attributes);
            assertAll("",
                    () -> assertInstanceOf(GioKey.class, saxHandler.getCurrentEntity().getEntity()),
                    () -> verify(attributes, times(2)).getType(anyInt()),
                    () -> verify(attributes, times(1)).getLength(),
                    () -> verify(gioWriter, times(0)).startDocument(any()),
                    () -> verify(gioWriter, times(0)).startGraph(any()),
                    () -> verify(gioWriter, times(0)).startNode(any()),
                    () -> verify(gioWriter, times(0)).startEdge(any()),
                    () -> verify(gioWriter, times(0)).endDocument(),
                    () -> verify(gioWriter, times(0)).endGraph(any()),
                    () -> verify(gioWriter, times(0)).endNode(any()),
                    () -> verify(gioWriter, times(0)).endEdge(any()));
            reset(attributes);
            localName = GraphmlConstant.DESC_ELEMENT_NAME;

            saxHandler.startElement(uri, localName, qName, attributes);
            assertAll("",
                    () -> assertTrue(saxHandler.getCurrentEntity().getEntity() instanceof GioElementWithDescription),
                    () -> verify(gioWriter, times(0)).startDocument(any()),
                    () -> verify(gioWriter, times(0)).startGraph(any()),
                    () -> verify(gioWriter, times(0)).startNode(any()),
                    () -> verify(gioWriter, times(0)).startEdge(any()),
                    () -> verify(gioWriter, times(0)).endDocument(),
                    () -> verify(gioWriter, times(0)).endGraph(any()),
                    () -> verify(gioWriter, times(0)).endNode(any()),
                    () -> verify(gioWriter, times(0)).endEdge(any()));

            saxHandler.characters("this is desc for key2".toCharArray(), 0, 19);
            assertAll("",
                    () -> assertTrue(saxHandler.getCurrentEntity().getEntity() instanceof GioElementWithDescription),
                    () -> assertEquals("this is desc for key2", ((GioElementWithDescription) saxHandler.getCurrentEntity().getEntity()).description().get()),
                    () -> verify(gioWriter, times(0)).startDocument(any()),
                    () -> verify(gioWriter, times(0)).startGraph(any()),
                    () -> verify(gioWriter, times(0)).startNode(any()),
                    () -> verify(gioWriter, times(0)).startEdge(any()),
                    () -> verify(gioWriter, times(0)).endDocument(),
                    () -> verify(gioWriter, times(0)).endGraph(any()),
                    () -> verify(gioWriter, times(0)).endNode(any()),
                    () -> verify(gioWriter, times(0)).endEdge(any()));

            saxHandler.endElement(uri, localName, qName);
            assertAll("",
                    () -> assertInstanceOf(GioKey.class, saxHandler.getCurrentEntity().getEntity()),
                    () -> assertEquals("this is desc for key2", ((GioElementWithDescription) saxHandler.getCurrentEntity().getEntity()).description().get()),
                    () -> verify(gioWriter, times(0)).startDocument(any()),
                    () -> verify(gioWriter, times(0)).startGraph(any()),
                    () -> verify(gioWriter, times(0)).startNode(any()),
                    () -> verify(gioWriter, times(0)).startEdge(any()),
                    () -> verify(gioWriter, times(0)).endDocument(),
                    () -> verify(gioWriter, times(0)).endGraph(any()),
                    () -> verify(gioWriter, times(0)).endNode(any()),
                    () -> verify(gioWriter, times(0)).endEdge(any()));

            localName = GraphmlConstant.KEY_ELEMENT_NAME;
            saxHandler.endElement(uri, localName, qName);
            assertAll("",
                    () -> assertInstanceOf(GioDocument.class, saxHandler.getCurrentEntity().getEntity()),
                    () -> assertEquals(2, ((GioDocument) saxHandler.getCurrentEntity().getEntity()).getKeys().size()),
                    () -> assertNull(((GioDocument) saxHandler.getCurrentEntity().getEntity()).getDataList()),
                    () -> assertEquals("this is desc for Graphml", ((GioDocument) saxHandler.getCurrentEntity().getEntity()).getDescription()),
                    () -> verify(gioWriter, times(0)).startDocument(any()),
                    () -> verify(gioWriter, times(0)).startGraph(any()),
                    () -> verify(gioWriter, times(0)).startNode(any()),
                    () -> verify(gioWriter, times(0)).startEdge(any()),
                    () -> verify(gioWriter, times(0)).endDocument(),
                    () -> verify(gioWriter, times(0)).endGraph(any()),
                    () -> verify(gioWriter, times(0)).endNode(any()),
                    () -> verify(gioWriter, times(0)).endEdge(any()));

            //ADD data
            reset(attributes);
            localName = GraphmlConstant.NODE_DATA_ELEMENT_NAME;
            when(attributes.getLength()).thenReturn(2);
            when(attributes.getType(0)).thenReturn("id");
            when(attributes.getType(1)).thenReturn("key");
            when(attributes.getValue(0)).thenReturn("id");
            when(attributes.getValue(1)).thenReturn("key");

            saxHandler.startElement(uri, localName, qName, attributes);
            assertAll("",
                    () -> assertInstanceOf(GioData.class, saxHandler.getCurrentEntity().getEntity()),
                    () -> verify(attributes, times(2)).getType(anyInt()),
                    () -> verify(attributes, times(1)).getLength(),
                    () -> verify(gioWriter, times(0)).startDocument(any()),
                    () -> verify(gioWriter, times(0)).startGraph(any()),
                    () -> verify(gioWriter, times(0)).startNode(any()),
                    () -> verify(gioWriter, times(0)).startEdge(any()),
                    () -> verify(gioWriter, times(0)).endDocument(),
                    () -> verify(gioWriter, times(0)).endGraph(any()),
                    () -> verify(gioWriter, times(0)).endNode(any()),
                    () -> verify(gioWriter, times(0)).endEdge(any()));
            reset(attributes);

            localName = GraphmlConstant.NODE_DATA_ELEMENT_NAME;
            saxHandler.endElement(uri, localName, qName);
            assertAll("",
                    () -> assertInstanceOf(GioDocument.class, saxHandler.getCurrentEntity().getEntity()),
                    () -> assertEquals(2, ((GioDocument) saxHandler.getCurrentEntity().getEntity()).getKeys().size()),
                    () -> assertEquals(1, ((GioDocument) saxHandler.getCurrentEntity().getEntity()).getDataList().size()),
                    () -> assertEquals("this is desc for Graphml", ((GioDocument) saxHandler.getCurrentEntity().getEntity()).getDescription()),
                    () -> verify(gioWriter, times(0)).startDocument(any()),
                    () -> verify(gioWriter, times(0)).startGraph(any()),
                    () -> verify(gioWriter, times(0)).startNode(any()),
                    () -> verify(gioWriter, times(0)).startEdge(any()),
                    () -> verify(gioWriter, times(0)).endDocument(),
                    () -> verify(gioWriter, times(0)).endGraph(any()),
                    () -> verify(gioWriter, times(0)).endNode(any()),
                    () -> verify(gioWriter, times(0)).endEdge(any()));


            //ADD data 2
            reset(attributes);
            localName = GraphmlConstant.NODE_DATA_ELEMENT_NAME;
            when(attributes.getLength()).thenReturn(2);
            when(attributes.getType(0)).thenReturn("id");
            when(attributes.getType(1)).thenReturn("key");
            when(attributes.getValue(0)).thenReturn("id2");
            when(attributes.getValue(1)).thenReturn("key2");

            saxHandler.startElement(uri, localName, qName, attributes);
            assertAll("",
                    () -> assertInstanceOf(GioData.class, saxHandler.getCurrentEntity().getEntity()),
                    () -> verify(attributes, times(2)).getType(anyInt()),
                    () -> verify(attributes, times(1)).getLength(),
                    () -> verify(gioWriter, times(0)).startDocument(any()),
                    () -> verify(gioWriter, times(0)).startGraph(any()),
                    () -> verify(gioWriter, times(0)).startNode(any()),
                    () -> verify(gioWriter, times(0)).startEdge(any()),
                    () -> verify(gioWriter, times(0)).endDocument(),
                    () -> verify(gioWriter, times(0)).endGraph(any()),
                    () -> verify(gioWriter, times(0)).endNode(any()),
                    () -> verify(gioWriter, times(0)).endEdge(any()));
            reset(attributes);

            localName = GraphmlConstant.NODE_DATA_ELEMENT_NAME;
            saxHandler.endElement(uri, localName, qName);
            assertAll("",
                    () -> assertInstanceOf(GioDocument.class, saxHandler.getCurrentEntity().getEntity()),
                    () -> assertEquals(2, ((GioDocument) saxHandler.getCurrentEntity().getEntity()).getKeys().size()),
                    () -> assertEquals(2, ((GioDocument) saxHandler.getCurrentEntity().getEntity()).getDataList().size()),
                    () -> assertEquals("this is desc for Graphml", ((GioDocument) saxHandler.getCurrentEntity().getEntity()).getDescription()),
                    () -> verify(gioWriter, times(0)).startDocument(any()),
                    () -> verify(gioWriter, times(0)).startGraph(any()),
                    () -> verify(gioWriter, times(0)).startNode(any()),
                    () -> verify(gioWriter, times(0)).startEdge(any()),
                    () -> verify(gioWriter, times(0)).endDocument(),
                    () -> verify(gioWriter, times(0)).endGraph(any()),
                    () -> verify(gioWriter, times(0)).endNode(any()),
                    () -> verify(gioWriter, times(0)).endEdge(any()));


            //ADD Graph 2
            reset(attributes);
            localName = GraphmlConstant.GRAPH_ELEMENT_NAME;
            when(attributes.getLength()).thenReturn(2);
            when(attributes.getType(0)).thenReturn("id");
            when(attributes.getType(1)).thenReturn("edgedefault");
            when(attributes.getValue(0)).thenReturn("id");
            when(attributes.getValue(1)).thenReturn("false");

            saxHandler.startElement(uri, localName, qName, attributes);
            assertAll("",
                    () -> assertInstanceOf(GioGraph.class, saxHandler.getCurrentEntity().getEntity()),
                    () -> verify(attributes, times(2)).getType(anyInt()),
                    () -> verify(attributes, times(1)).getLength(),
                    () -> verify(gioWriter, times(1)).startDocument(any()),
                    () -> verify(gioWriter, times(0)).startGraph(any()),
                    () -> verify(gioWriter, times(0)).startNode(any()),
                    () -> verify(gioWriter, times(0)).startEdge(any()),
                    () -> verify(gioWriter, times(0)).endDocument(),
                    () -> verify(gioWriter, times(0)).endGraph(any()),
                    () -> verify(gioWriter, times(0)).endNode(any()),
                    () -> verify(gioWriter, times(0)).endEdge(any()));
            reset(attributes);

        }

        @DisplayName("Only GioDocument  End Element successfully pass.")
        @Test
        void endGioDocument() throws SAXException, IOException {
            String uri = "uri", localName = GraphmlConstant.GRAPHML_ELEMENT_NAME, qName = "";
            assertNull(saxHandler.getCurrentEntity());
            saxHandler.endElement(uri, localName, qName);
            assertAll("",
                    () -> assertNull(saxHandler.getCurrentEntity()),
                    () -> verify(gioWriter, times(0)).startDocument(any()),
                    () -> verify(gioWriter, times(0)).startGraph(any()),
                    () -> verify(gioWriter, times(0)).startNode(any()),
                    () -> verify(gioWriter, times(0)).startEdge(any()),
                    () -> verify(gioWriter, times(1)).endDocument(),
                    () -> verify(gioWriter, times(0)).endGraph(any()),
                    () -> verify(gioWriter, times(0)).endNode(any()),
                    () -> verify(gioWriter, times(0)).endEdge(any()));
        }

        @DisplayName("Only GioGraph Start and End successfully pass..")
        @Test
        void startGraph_Only_Sart_And_End() throws SAXException, IOException {
            String uri = "uri", localName = GraphmlConstant.GRAPH_ELEMENT_NAME, qName = "";

            assertNull(saxHandler.getCurrentEntity());
            reset(attributes);

            when(attributes.getLength()).thenReturn(2);
            when(attributes.getType(0)).thenReturn("id");
            when(attributes.getType(1)).thenReturn("edgedefault");
            when(attributes.getValue(0)).thenReturn("id");
            when(attributes.getValue(1)).thenReturn("true");
            saxHandler.startElement(uri, localName, qName, attributes);
            assertAll("",
                    () -> assertInstanceOf(GioGraph.class, saxHandler.getCurrentEntity().getEntity()),
                    () -> assertEquals("id", ((GioGraph) saxHandler.getCurrentEntity().getEntity()).getId()),
                    () -> assertTrue(((GioGraph) saxHandler.getCurrentEntity().getEntity()).isEdgedefault()),
                    () -> assertNull(((GioGraph) saxHandler.getCurrentEntity().getEntity()).getDescription()),
                    () -> assertNull(((GioGraph) saxHandler.getCurrentEntity().getEntity()).getDataList())
                    ,
                    () -> verify(gioWriter, times(0)).startDocument(any()),
                    () -> verify(gioWriter, times(0)).startGraph(any()),
                    () -> verify(gioWriter, times(0)).startNode(any()),
                    () -> verify(gioWriter, times(0)).startEdge(any()),
                    () -> verify(gioWriter, times(0)).endDocument(),
                    () -> verify(gioWriter, times(0)).endGraph(any()),
                    () -> verify(gioWriter, times(0)).endNode(any()),
                    () -> verify(gioWriter, times(0)).endEdge(any()));
            reset(attributes);
            localName = GraphmlConstant.GRAPH_ELEMENT_NAME;
            saxHandler.endElement(uri, localName, qName);
            assertAll("",
                    () -> assertNull(saxHandler.getCurrentEntity()),
                    () -> verify(gioWriter, times(0)).startDocument(any()),
                    () -> verify(gioWriter, times(0)).startNode(any()),
                    () -> verify(gioWriter, times(0)).startEdge(any()),
                    () -> verify(gioWriter, times(0)).endDocument(),
                    () -> verify(gioWriter, times(0)).endNode(any()),
                    () -> verify(gioWriter, times(0)).endEdge(any()),
                    () -> verify(gioWriter, times(1)).startGraph(any()),
                    () -> verify(gioWriter, times(1)).endGraph(null));


        }

        @DisplayName("All possible field in GioGraph field by field successfully pass.")
        @Test
        void startGraph() throws SAXException, IOException {
            String uri = "uri", localName = GraphmlConstant.GRAPH_ELEMENT_NAME, qName = "";

            assertNull(saxHandler.getCurrentEntity());
            reset(attributes);

            when(attributes.getLength()).thenReturn(2);
            when(attributes.getType(0)).thenReturn("id");
            when(attributes.getType(1)).thenReturn("edgedefault");
            when(attributes.getValue(0)).thenReturn("id");
            when(attributes.getValue(1)).thenReturn("true");
            saxHandler.startElement(uri, localName, qName, attributes);
            assertAll("",
                    () -> assertInstanceOf(GioGraph.class, saxHandler.getCurrentEntity().getEntity()),
                    () -> assertEquals("id", ((GioGraph) saxHandler.getCurrentEntity().getEntity()).getId()),
                    () -> assertTrue(((GioGraph) saxHandler.getCurrentEntity().getEntity()).isEdgedefault()),
                    () -> assertNull(((GioGraph) saxHandler.getCurrentEntity().getEntity()).getDescription()),
                    () -> assertNull(((GioGraph) saxHandler.getCurrentEntity().getEntity()).getDataList()),
                    () -> verify(gioWriter, times(0)).startDocument(any()),
                    () -> verify(gioWriter, times(0)).startGraph(any()),
                    () -> verify(gioWriter, times(0)).startNode(any()),
                    () -> verify(gioWriter, times(0)).startEdge(any()),
                    () -> verify(gioWriter, times(0)).endDocument(),
                    () -> verify(gioWriter, times(0)).endGraph(any()),
                    () -> verify(gioWriter, times(0)).endNode(any()),
                    () -> verify(gioWriter, times(0)).endEdge(any()));
            reset(attributes);

            //ADD Desc
            localName = GraphmlConstant.DESC_ELEMENT_NAME;
            saxHandler.startElement(uri, localName, qName, attributes);
            assertAll("",
                    () -> assertTrue(saxHandler.getCurrentEntity().getEntity() instanceof GioElementWithDescription),
                    () -> verify(gioWriter, times(0)).startDocument(any()),
                    () -> verify(gioWriter, times(0)).startGraph(any()),
                    () -> verify(gioWriter, times(0)).startNode(any()),
                    () -> verify(gioWriter, times(0)).startEdge(any()),
                    () -> verify(gioWriter, times(0)).endDocument(),
                    () -> verify(gioWriter, times(0)).endGraph(any()),
                    () -> verify(gioWriter, times(0)).endNode(any()),
                    () -> verify(gioWriter, times(0)).endEdge(any()));

            saxHandler.characters("this is desc for Graph".toCharArray(), 0, 24);
            assertAll("",
                    () -> assertTrue(saxHandler.getCurrentEntity().getEntity() instanceof GioElementWithDescription),
                    () -> assertEquals("this is desc for Graph", ((GioElementWithDescription) saxHandler.getCurrentEntity().getEntity()).description().get()),
                    () -> verify(gioWriter, times(0)).startDocument(any()));

            saxHandler.endElement(uri, localName, qName);
            assertAll("",
                    () -> assertInstanceOf(GioGraph.class, saxHandler.getCurrentEntity().getEntity()),
                    () -> assertEquals("id", ((GioGraph) saxHandler.getCurrentEntity().getEntity()).getId()),
                    () -> assertTrue(((GioGraph) saxHandler.getCurrentEntity().getEntity()).isEdgedefault()),
                    () -> assertNotNull(((GioGraph) saxHandler.getCurrentEntity().getEntity()).getDescription()),
                    () -> assertNull(((GioGraph) saxHandler.getCurrentEntity().getEntity()).getDataList()),
                    () -> verify(gioWriter, times(0)).startDocument(any()),
                    () -> verify(gioWriter, times(0)).startGraph(any()),
                    () -> verify(gioWriter, times(0)).startNode(any()),
                    () -> verify(gioWriter, times(0)).startEdge(any()),
                    () -> verify(gioWriter, times(0)).endDocument(),
                    () -> verify(gioWriter, times(0)).endGraph(any()),
                    () -> verify(gioWriter, times(0)).endNode(any()),
                    () -> verify(gioWriter, times(0)).endEdge(any()));


            //ADD data
            reset(attributes);
            localName = GraphmlConstant.NODE_DATA_ELEMENT_NAME;
            when(attributes.getLength()).thenReturn(2);
            when(attributes.getType(0)).thenReturn("id");
            when(attributes.getType(1)).thenReturn("key");
            when(attributes.getValue(0)).thenReturn("id");
            when(attributes.getValue(1)).thenReturn("key");

            saxHandler.startElement(uri, localName, qName, attributes);
            assertAll("",
                    () -> assertInstanceOf(GioData.class, saxHandler.getCurrentEntity().getEntity()),
                    () -> verify(attributes, times(2)).getType(anyInt()),
                    () -> verify(attributes, times(1)).getLength(),
                    () -> verify(gioWriter, times(0)).startDocument(any()),
                    () -> verify(gioWriter, times(0)).startGraph(any()),
                    () -> verify(gioWriter, times(0)).startNode(any()),
                    () -> verify(gioWriter, times(0)).startEdge(any()),
                    () -> verify(gioWriter, times(0)).endDocument(),
                    () -> verify(gioWriter, times(0)).endGraph(any()),
                    () -> verify(gioWriter, times(0)).endNode(any()),
                    () -> verify(gioWriter, times(0)).endEdge(any()));

            reset(attributes);

            localName = GraphmlConstant.NODE_DATA_ELEMENT_NAME;
            saxHandler.endElement(uri, localName, qName);
            assertAll("",
                    () -> assertInstanceOf(GioGraph.class, saxHandler.getCurrentEntity().getEntity()),
                    () -> assertEquals(1, ((GioGraph) saxHandler.getCurrentEntity().getEntity()).getDataList().size()),
                    () -> assertEquals("this is desc for Graph", ((GioElementWithDescription) saxHandler.getCurrentEntity().getEntity()).description().get()),
                    () -> verify(gioWriter, times(0)).startDocument(any()),
                    () -> verify(gioWriter, times(0)).startGraph(any()),
                    () -> verify(gioWriter, times(0)).startNode(any()),
                    () -> verify(gioWriter, times(0)).startEdge(any()),
                    () -> verify(gioWriter, times(0)).endDocument(),
                    () -> verify(gioWriter, times(0)).endGraph(any()),
                    () -> verify(gioWriter, times(0)).endNode(any()),
                    () -> verify(gioWriter, times(0)).endEdge(any()));


            //ADD data 2
            reset(attributes);
            localName = GraphmlConstant.NODE_DATA_ELEMENT_NAME;
            when(attributes.getLength()).thenReturn(2);
            when(attributes.getType(0)).thenReturn("id");
            when(attributes.getType(1)).thenReturn("key");
            when(attributes.getValue(0)).thenReturn("id2");
            when(attributes.getValue(1)).thenReturn("key2");

            saxHandler.startElement(uri, localName, qName, attributes);
            assertAll("",
                    () -> assertInstanceOf(GioData.class, saxHandler.getCurrentEntity().getEntity()),
                    () -> verify(attributes, times(2)).getType(anyInt()),
                    () -> verify(attributes, times(1)).getLength(),
                    () -> verify(gioWriter, times(0)).startDocument(any()),
                    () -> verify(gioWriter, times(0)).startGraph(any()),
                    () -> verify(gioWriter, times(0)).startNode(any()),
                    () -> verify(gioWriter, times(0)).startEdge(any()),
                    () -> verify(gioWriter, times(0)).endDocument(),
                    () -> verify(gioWriter, times(0)).endGraph(any()),
                    () -> verify(gioWriter, times(0)).endNode(any()),
                    () -> verify(gioWriter, times(0)).endEdge(any()));
            reset(attributes);

            localName = GraphmlConstant.NODE_DATA_ELEMENT_NAME;
            saxHandler.endElement(uri, localName, qName);
            assertAll("",

                    () -> assertInstanceOf(GioGraph.class, saxHandler.getCurrentEntity().getEntity()),
                    () -> assertEquals(2, ((GioGraph) saxHandler.getCurrentEntity().getEntity()).getDataList().size()),
                    () -> assertEquals("this is desc for Graph", ((GioElementWithDescription) saxHandler.getCurrentEntity().getEntity()).description().get()),
                    () -> verify(gioWriter, times(0)).startDocument(any()),
                    () -> verify(gioWriter, times(0)).startGraph(any()),
                    () -> verify(gioWriter, times(0)).startNode(any()),
                    () -> verify(gioWriter, times(0)).startEdge(any()),
                    () -> verify(gioWriter, times(0)).endDocument(),
                    () -> verify(gioWriter, times(0)).endGraph(any()),
                    () -> verify(gioWriter, times(0)).endNode(any()),
                    () -> verify(gioWriter, times(0)).endEdge(any()));

            //ADD  an element that sends a graph to GioWriter
            localName = GraphmlConstant.NODE_ELEMENT_NAME;
            when(attributes.getLength()).thenReturn(1);
            when(attributes.getType(0)).thenReturn("id");
            when(attributes.getValue(0)).thenReturn("id");
            saxHandler.startElement(uri, localName, qName, attributes);

            assertAll("When the current element is a graph and a node is added to it, " +
                            "the graph must be sent to the stream ",
                    () -> assertInstanceOf(GioNode.class, saxHandler.getCurrentEntity().getEntity()),
                    () -> assertNull(((GioNode) saxHandler.getCurrentEntity().getEntity()).getDataList()),
                    () -> assertNull(((GioNode) saxHandler.getCurrentEntity().getEntity()).getDescription()),
                    () -> assertTrue(((GioNode) saxHandler.getCurrentEntity().getEntity()).getPorts().isEmpty()),
                    () -> verify(gioWriter, times(0)).startDocument(any()),
                    () -> verify(gioWriter, times(1)).startGraph(any()),
                    () -> verify(gioWriter, times(0)).startNode(any()),
                    () -> verify(gioWriter, times(0)).startEdge(any()),
                    () -> verify(gioWriter, times(0)).endDocument(),
                    () -> verify(gioWriter, times(0)).endGraph(any()),
                    () -> verify(gioWriter, times(0)).endNode(any()),
                    () -> verify(gioWriter, times(0)).endEdge(any())
            );

        }

        @DisplayName("Only GioNode Start and End successfully pass.")
        @Test
        void startNode_Only_Start_And_End_Element() throws SAXException {
            String uri = "uri", localName = GraphmlConstant.NODE_ELEMENT_NAME, qName = "";

            assertNull(saxHandler.getCurrentEntity());
            reset(attributes);

            when(attributes.getLength()).thenReturn(2);
            when(attributes.getType(0)).thenReturn("id");
            when(attributes.getType(1)).thenReturn("edgedefault");
            when(attributes.getValue(0)).thenReturn("id");
            when(attributes.getValue(1)).thenReturn("true");
            saxHandler.startElement(uri, localName, qName, attributes);
            assertAll("",
                    () -> assertInstanceOf(GioNode.class, saxHandler.getCurrentEntity().getEntity()),
                    () -> assertEquals("id", ((GioNode) saxHandler.getCurrentEntity().getEntity()).getId()),
                    () -> assertNull(((GioNode) saxHandler.getCurrentEntity().getEntity()).getDescription()),
                    () -> assertNull(((GioNode) saxHandler.getCurrentEntity().getEntity()).getDataList()),
                    () -> assertTrue(((GioNode) saxHandler.getCurrentEntity().getEntity()).getPorts().isEmpty()),
                    () -> verify(gioWriter, times(0)).startDocument(any()),
                    () -> verify(gioWriter, times(0)).startGraph(any()),
                    () -> verify(gioWriter, times(0)).startNode(any()),
                    () -> verify(gioWriter, times(0)).startEdge(any()),
                    () -> verify(gioWriter, times(0)).endDocument(),
                    () -> verify(gioWriter, times(0)).endGraph(any()),
                    () -> verify(gioWriter, times(0)).endNode(any()),
                    () -> verify(gioWriter, times(0)).endEdge(any()));
            reset(attributes);

            saxHandler.endElement(uri, localName, qName);
            assertAll("",
                    () -> assertNull(saxHandler.getCurrentEntity()),
                    () -> verify(gioWriter, times(0)).startDocument(any()),
                    () -> verify(gioWriter, times(0)).startGraph(any()),
                    () -> verify(gioWriter, times(0)).startEdge(any()),
                    () -> verify(gioWriter, times(0)).endDocument(),
                    () -> verify(gioWriter, times(0)).endGraph(any()),
                    () -> verify(gioWriter, times(0)).endEdge(any()),
                    () -> verify(gioWriter, times(1)).startNode(any()),
                    () -> verify(gioWriter, times(1)).endNode(null));
        }

        @DisplayName("All possible field in GioNode field by field successfully pass.")
        @Test
        void startNode_All_Possible_Field_Test() throws SAXException {
            String uri = "uri", localName = GraphmlConstant.NODE_ELEMENT_NAME, qName = "";

            assertNull(saxHandler.getCurrentEntity());
            reset(attributes);

            when(attributes.getLength()).thenReturn(2);
            when(attributes.getType(0)).thenReturn("id");
            when(attributes.getType(1)).thenReturn("edgedefault");
            when(attributes.getValue(0)).thenReturn("id");
            when(attributes.getValue(1)).thenReturn("true");
            saxHandler.startElement(uri, localName, qName, attributes);
            assertAll("",
                    () -> assertInstanceOf(GioNode.class, saxHandler.getCurrentEntity().getEntity()),
                    () -> assertEquals("id", ((GioNode) saxHandler.getCurrentEntity().getEntity()).getId()),
                    () -> assertNull(((GioNode) saxHandler.getCurrentEntity().getEntity()).getDescription()),
                    () -> assertNull(((GioNode) saxHandler.getCurrentEntity().getEntity()).getDataList()),
                    () -> assertTrue(((GioNode) saxHandler.getCurrentEntity().getEntity()).getPorts().isEmpty()),
                    () -> verify(gioWriter, times(0)).startDocument(any()),
                    () -> verify(gioWriter, times(0)).startGraph(any()),
                    () -> verify(gioWriter, times(0)).startNode(any()),
                    () -> verify(gioWriter, times(0)).startEdge(any()),
                    () -> verify(gioWriter, times(0)).endDocument(),
                    () -> verify(gioWriter, times(0)).endGraph(any()),
                    () -> verify(gioWriter, times(0)).endNode(any()),
                    () -> verify(gioWriter, times(0)).endEdge(any()));
            reset(attributes);

            localName = GraphmlConstant.DESC_ELEMENT_NAME;
            saxHandler.startElement(uri, localName, qName, attributes);
            assertAll("",
                    () -> assertTrue(saxHandler.getCurrentEntity().getEntity() instanceof GioElementWithDescription),
                    () -> verify(gioWriter, times(0)).startNode(any()));

            saxHandler.characters("this is desc for Node".toCharArray(), 0, 24);
            assertAll("",
                    () -> assertTrue(saxHandler.getCurrentEntity().getEntity() instanceof GioElementWithDescription),
                    () -> assertEquals("this is desc for Node", ((GioElementWithDescription) saxHandler.getCurrentEntity().getEntity()).description().get()),
                    () -> verify(gioWriter, times(0)).startDocument(any()),
                    () -> verify(gioWriter, times(0)).startGraph(any()),
                    () -> verify(gioWriter, times(0)).startNode(any()),
                    () -> verify(gioWriter, times(0)).startEdge(any()),
                    () -> verify(gioWriter, times(0)).endDocument(),
                    () -> verify(gioWriter, times(0)).endGraph(any()),
                    () -> verify(gioWriter, times(0)).endNode(any()),
                    () -> verify(gioWriter, times(0)).endEdge(any()));

            saxHandler.endElement(uri, localName, qName);
            assertAll("",
                    () -> assertInstanceOf(GioNode.class, saxHandler.getCurrentEntity().getEntity()),
                    () -> assertEquals("id", ((GioNode) saxHandler.getCurrentEntity().getEntity()).getId()),
                    () -> assertEquals("this is desc for Node", ((GioElementWithDescription) saxHandler.getCurrentEntity().getEntity()).description().get()),
                    () -> assertNull(((GioNode) saxHandler.getCurrentEntity().getEntity()).getDataList()),
                    () -> assertTrue(((GioNode) saxHandler.getCurrentEntity().getEntity()).getPorts().isEmpty()),
                    () -> verify(gioWriter, times(0)).startDocument(any()),
                    () -> verify(gioWriter, times(0)).startGraph(any()),
                    () -> verify(gioWriter, times(0)).startNode(any()),
                    () -> verify(gioWriter, times(0)).startEdge(any()),
                    () -> verify(gioWriter, times(0)).endDocument(),
                    () -> verify(gioWriter, times(0)).endGraph(any()),
                    () -> verify(gioWriter, times(0)).endNode(any()),
                    () -> verify(gioWriter, times(0)).endEdge(any()));

            localName = GraphmlConstant.NODE_ELEMENT_NAME;
            saxHandler.endElement(uri, localName, qName);
            assertAll("",
                    () -> assertNull(saxHandler.getCurrentEntity()),
                    () -> verify(gioWriter, times(0)).startDocument(any()),
                    () -> verify(gioWriter, times(0)).startGraph(any()),
                    () -> verify(gioWriter, times(0)).startEdge(any()),
                    () -> verify(gioWriter, times(0)).endDocument(),
                    () -> verify(gioWriter, times(0)).endGraph(any()),
                    () -> verify(gioWriter, times(0)).endEdge(any()),
                    () -> verify(gioWriter, times(1)).startNode(any()),
                    () -> verify(gioWriter, times(1)).endNode(null));
        }


        @DisplayName("Only GioEdge Start and End successfully pass.")
        @Test
        void startEdge_Only_Start_And_End_Element() throws SAXException {
            String uri = "uri", localName = GraphmlConstant.EDGE_ELEMENT_NAME, qName = "";

            assertNull(saxHandler.getCurrentEntity());
            reset(attributes);

            when(attributes.getLength()).thenReturn(1);
            when(attributes.getType(0)).thenReturn("id");
            when(attributes.getValue(0)).thenReturn("id");
            saxHandler.startElement(uri, localName, qName, attributes);
            assertAll("",
                    () -> assertInstanceOf(GioEdge.class, saxHandler.getCurrentEntity().getEntity()),
                    () -> assertEquals("id", ((GioEdge) saxHandler.getCurrentEntity().getEntity()).getId()),
                    () -> assertNull(((GioEdge) saxHandler.getCurrentEntity().getEntity()).getDescription()),
                    () -> assertNull(((GioEdge) saxHandler.getCurrentEntity().getEntity()).getDataList()),
                    () -> verify(gioWriter, times(0)).startDocument(any()),
                    () -> verify(gioWriter, times(0)).startGraph(any()),
                    () -> verify(gioWriter, times(0)).startNode(any()),
                    () -> verify(gioWriter, times(0)).startEdge(any()),
                    () -> verify(gioWriter, times(0)).endDocument(),
                    () -> verify(gioWriter, times(0)).endGraph(any()),
                    () -> verify(gioWriter, times(0)).endNode(any()),
                    () -> verify(gioWriter, times(0)).endEdge(any()));
            reset(attributes);

            saxHandler.endElement(uri, localName, qName);
            assertAll("",
                    () -> assertNull(saxHandler.getCurrentEntity()),
                    () -> verify(gioWriter, times(0)).startDocument(any()),
                    () -> verify(gioWriter, times(0)).startGraph(any()),
                    () -> verify(gioWriter, times(0)).startNode(any()),
                    () -> verify(gioWriter, times(0)).endDocument(),
                    () -> verify(gioWriter, times(0)).endGraph(any()),
                    () -> verify(gioWriter, times(0)).endNode(any()),
                    () -> verify(gioWriter, times(1)).startEdge(any()),
                    () -> verify(gioWriter, times(1)).endEdge(null));
        }

        @DisplayName("All possible field in GioEdge field by field successfully pass.")
        @Test
        void startEdge_All_Possible_Field_Test() throws SAXException {
            String uri = "uri", localName = GraphmlConstant.EDGE_ELEMENT_NAME, qName = "";


            assertNull(saxHandler.getCurrentEntity());
            reset(attributes);

            when(attributes.getLength()).thenReturn(1);
            when(attributes.getType(0)).thenReturn("id");
            when(attributes.getValue(0)).thenReturn("id");
            saxHandler.startElement(uri, localName, qName, attributes);
            assertAll("",
                    () -> assertInstanceOf(GioEdge.class, saxHandler.getCurrentEntity().getEntity()),
                    () -> assertEquals("id", ((GioEdge) saxHandler.getCurrentEntity().getEntity()).getId()),
                    () -> assertNull(((GioEdge) saxHandler.getCurrentEntity().getEntity()).getDescription()),
                    () -> assertNull(((GioEdge) saxHandler.getCurrentEntity().getEntity()).getDataList()),
                    () -> verify(gioWriter, times(0)).startDocument(any()),
                    () -> verify(gioWriter, times(0)).startGraph(any()),
                    () -> verify(gioWriter, times(0)).startNode(any()),
                    () -> verify(gioWriter, times(0)).startEdge(any()),
                    () -> verify(gioWriter, times(0)).endDocument(),
                    () -> verify(gioWriter, times(0)).endGraph(any()),
                    () -> verify(gioWriter, times(0)).endNode(any()),
                    () -> verify(gioWriter, times(0)).endEdge(any()));
            reset(attributes);


            localName = GraphmlConstant.DESC_ELEMENT_NAME;
            saxHandler.startElement(uri, localName, qName, attributes);
            assertAll("",
                    () -> assertTrue(saxHandler.getCurrentEntity().getEntity() instanceof GioElementWithDescription),
                    () -> verify(gioWriter, times(0)).startDocument(any()),
                    () -> verify(gioWriter, times(0)).startGraph(any()),
                    () -> verify(gioWriter, times(0)).startNode(any()),
                    () -> verify(gioWriter, times(0)).startEdge(any()),
                    () -> verify(gioWriter, times(0)).endDocument(),
                    () -> verify(gioWriter, times(0)).endGraph(any()),
                    () -> verify(gioWriter, times(0)).endNode(any()),
                    () -> verify(gioWriter, times(0)).endEdge(any()));

            saxHandler.characters("this is desc for Edge".toCharArray(), 0, 24);
            assertAll("",
                    () -> assertTrue(saxHandler.getCurrentEntity().getEntity() instanceof GioElementWithDescription),
                    () -> assertEquals("this is desc for Edge", ((GioElementWithDescription) saxHandler.getCurrentEntity().getEntity()).description().get()),
                    () -> verify(gioWriter, times(0)).startDocument(any()),
                    () -> verify(gioWriter, times(0)).startGraph(any()),
                    () -> verify(gioWriter, times(0)).startNode(any()),
                    () -> verify(gioWriter, times(0)).startEdge(any()),
                    () -> verify(gioWriter, times(0)).endDocument(),
                    () -> verify(gioWriter, times(0)).endGraph(any()),
                    () -> verify(gioWriter, times(0)).endNode(any()),
                    () -> verify(gioWriter, times(0)).endEdge(any()));

            saxHandler.endElement(uri, localName, qName);
            assertAll("",
                    () -> assertInstanceOf(GioEdge.class, saxHandler.getCurrentEntity().getEntity()),
                    () -> assertEquals("id", ((GioEdge) saxHandler.getCurrentEntity().getEntity()).getId()),
                    () -> assertEquals("this is desc for Edge", ((GioElementWithDescription) saxHandler.getCurrentEntity().getEntity()).description().get()),
                    () -> assertNull(((GioEdge) saxHandler.getCurrentEntity().getEntity()).getDataList()),
                    () -> verify(gioWriter, times(0)).startDocument(any()),
                    () -> verify(gioWriter, times(0)).startGraph(any()),
                    () -> verify(gioWriter, times(0)).startNode(any()),
                    () -> verify(gioWriter, times(0)).startEdge(any()),
                    () -> verify(gioWriter, times(0)).endDocument(),
                    () -> verify(gioWriter, times(0)).endGraph(any()),
                    () -> verify(gioWriter, times(0)).endNode(any()),
                    () -> verify(gioWriter, times(0)).endEdge(any()));

            localName = GraphmlConstant.EDGE_ELEMENT_NAME;
            saxHandler.endElement(uri, localName, qName);
            assertAll("",
                    () -> assertNull(saxHandler.getCurrentEntity()),
                    () -> verify(gioWriter, times(0)).startDocument(any()),
                    () -> verify(gioWriter, times(0)).startGraph(any()),
                    () -> verify(gioWriter, times(0)).startNode(any()),
                    () -> verify(gioWriter, times(0)).endDocument(),
                    () -> verify(gioWriter, times(0)).endGraph(any()),
                    () -> verify(gioWriter, times(0)).endNode(any()),
                    () -> verify(gioWriter, times(1)).startEdge(any()),
                    () -> verify(gioWriter, times(1)).endEdge(null));
        }

        @Test
        void characters() {
        }
    }
}