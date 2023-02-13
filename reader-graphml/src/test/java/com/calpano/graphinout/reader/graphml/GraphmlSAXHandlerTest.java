package com.calpano.graphinout.reader.graphml;

import com.calpano.graphinout.base.gio.GioDocument;
import com.calpano.graphinout.base.gio.GioElementWithDescription;
import com.calpano.graphinout.base.gio.GioKey;
import com.calpano.graphinout.base.gio.GioWriter;
import com.calpano.graphinout.base.reader.GioReader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.*;
import org.mockito.Mock;
import org.mockito.Mockito;
import static org.mockito.Mockito.*;
import org.mockito.MockitoAnnotations;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

import javax.annotation.meta.When;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class GraphmlSAXHandlerTest {

    @DisplayName("Load file with GioStandardGMLService and test field by field successfully pass.")
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
        saxHandler=  new GraphmlSAXHandler(gioWriter, GioReader.ContentError.builder().build());
    }

    @Test
    void startGraphmlElement() throws SAXException, IOException {
        String uri="uri",localName="localName",  qName=GraphmlConstant.GRAPHML_ELEMENT_NAME;

        assertNull(saxHandler.getCurrentEntity());

        saxHandler.startElement(uri,localName,qName,attributes);
        assertNotNull(saxHandler.getCurrentEntity());
        assertTrue(saxHandler.getCurrentEntity().getEntity() instanceof GioDocument);
        assertNull(((GioDocument)saxHandler.getCurrentEntity().getEntity()).getKeys());
        assertNull(((GioDocument)saxHandler.getCurrentEntity().getEntity()).getDataList());
        assertNull(((GioDocument)saxHandler.getCurrentEntity().getEntity()).getDescription());
        verify(gioWriter,times(0)).startDocument(any());
        qName=GraphmlConstant.DESC_ELEMENT_NAME;

        saxHandler.startElement(uri,localName,qName,attributes);
        assertTrue(saxHandler.getCurrentEntity().getEntity() instanceof GioElementWithDescription);
        verify(gioWriter,times(0)).startDocument(any());

        saxHandler.characters("this is desc".toCharArray(),0,12);
        assertTrue(saxHandler.getCurrentEntity().getEntity() instanceof GioElementWithDescription);
        assertEquals("this is desc",((GioElementWithDescription)saxHandler.getCurrentEntity().getEntity()).description().get());
        verify(gioWriter,times(0)).startDocument(any());

        saxHandler.endElement(uri,localName,qName);
        verify(gioWriter,times(0)).startDocument(any());
        assertTrue(saxHandler.getCurrentEntity().getEntity() instanceof GioDocument);
        assertNull(((GioDocument)saxHandler.getCurrentEntity().getEntity()).getKeys());
        assertNull(((GioDocument)saxHandler.getCurrentEntity().getEntity()).getDataList());
        assertNotNull(((GioDocument)saxHandler.getCurrentEntity().getEntity()).getDescription());
        reset(attributes);
        qName=GraphmlConstant.KEY_ELEMENT_NAME;
        when(attributes.getLength()).thenReturn(2);
        when(attributes.getType(0)).thenReturn("id");
        when(attributes.getType(1)).thenReturn("for");
        when(attributes.getValue(0)).thenReturn("id");
        when(attributes.getValue(1)).thenReturn("All");

        saxHandler.startElement(uri,localName,qName,attributes);
        assertTrue(saxHandler.getCurrentEntity().getEntity() instanceof GioKey);
        verify(gioWriter,times(0)).startDocument(any());
        verify(attributes,times(2)).getType(anyInt());
        verify(attributes,times(1)).getLength();
        reset(attributes);
        qName=GraphmlConstant.DESC_ELEMENT_NAME;

        saxHandler.startElement(uri,localName,qName,attributes);
        assertTrue(saxHandler.getCurrentEntity().getEntity() instanceof GioElementWithDescription);
        verify(gioWriter,times(0)).startDocument(any());

        saxHandler.characters("this is desc".toCharArray(),0,12);
        assertTrue(saxHandler.getCurrentEntity().getEntity() instanceof GioElementWithDescription);
        assertEquals("this is desc",((GioElementWithDescription)saxHandler.getCurrentEntity().getEntity()).description().get());
        verify(gioWriter,times(0)).startDocument(any());

        saxHandler.endElement(uri,localName,qName);
        verify(gioWriter,times(0)).startDocument(any());
        assertTrue(saxHandler.getCurrentEntity().getEntity() instanceof GioKey);
        assertEquals("this is desc",((GioElementWithDescription)saxHandler.getCurrentEntity().getEntity()).description().get());

        qName=GraphmlConstant.KEY_ELEMENT_NAME;
        saxHandler.endElement(uri,localName,qName);
        verify(gioWriter,times(0)).startDocument(any());
        assertTrue(saxHandler.getCurrentEntity().getEntity() instanceof GioDocument);
        assertNotNull(((GioDocument)saxHandler.getCurrentEntity().getEntity()).getKeys());
        assertNull(((GioDocument)saxHandler.getCurrentEntity().getEntity()).getDataList());
        assertNotNull(((GioDocument)saxHandler.getCurrentEntity().getEntity()).getDescription());






    }

    @Test
    void endElement() {
    }

    @Test
    void characters() {
    }
    }
}