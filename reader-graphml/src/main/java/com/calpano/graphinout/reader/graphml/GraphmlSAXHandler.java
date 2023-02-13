package com.calpano.graphinout.reader.graphml;


import com.calpano.graphinout.base.gio.GioDocument;
import com.calpano.graphinout.base.gio.GioKey;
import com.calpano.graphinout.base.gio.GioKeyForType;
import com.calpano.graphinout.base.gio.GioWriter;
import com.calpano.graphinout.base.reader.GioReader;
import lombok.extern.slf4j.Slf4j;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Stack;

@Slf4j
class GraphmlSAXHandler extends DefaultHandler {
    private final GioWriter gioWriter;
    private final GioReader.ContentError contentError;

    private GraphmlEntity currentEntity;

    private Stack<GraphmlEntity> stack = new Stack<>();


    public GraphmlSAXHandler(GioWriter gioWriter, GioReader.ContentError contentError) {
        this.gioWriter = gioWriter;
        this.contentError = contentError;
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {

        if (GraphmlConstant.GRAPHML_ELEMENT_NAME.equals(qName)) {
            startGraphmlElement(uri, localName, attributes);
        } else if (GraphmlConstant.DESC_ELEMENT_NAME.equals(qName)) {
            startDescElement(uri, localName, attributes);
        }else if (GraphmlConstant.KEY_ELEMENT_NAME.equals(qName)) {
            startKeyElement(uri, localName, attributes);
        }

    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        if (GraphmlConstant.GRAPHML_ELEMENT_NAME.equals(qName)) {
            endGraphmlElement();
        } else if (GraphmlConstant.DESC_ELEMENT_NAME.equals(qName)) {
            endDescElement();
        }else if (GraphmlConstant.KEY_ELEMENT_NAME.equals(qName)) {
            endKeyElement();
        }
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        currentEntity.addData(new String(ch));
    }

    @Override
    public void warning(SAXParseException e) throws SAXException {
        super.warning(e);
    }

    @Override
    public void error(SAXParseException e) throws SAXException {
        super.error(e);
    }

    @Override
    public void fatalError(SAXParseException e) throws SAXException {
        super.fatalError(e);
    }


    private void startGraphmlElement(String uri, String localName, Attributes attributes) {

        Map<String, String> customAttributes = new LinkedHashMap<>();
        for (int i = 0; i < attributes.getLength(); i++) {
            customAttributes.put(attributes.getType(i), attributes.getValue(i));
        }


        currentEntity = new GioDocumentEntity(GioDocument.builder().customAttributes(customAttributes).build());
    }

    private void endGraphmlElement() {

    }

    private void startDescElement(String uri, String localName, Attributes attributes) {
        stack.push(currentEntity);

        currentEntity = new GioDescriptionEntity();
    }

    private void endDescElement() {
        stack.peek().addEntity(currentEntity);
        currentEntity = stack.pop();
    }
    private void startKeyElement(String uri, String localName, Attributes attributes) {
        stack.push(currentEntity);
        GioKey.GioKeyBuilder builder = GioKey.builder();
        Map<String, String> customAttributes = new LinkedHashMap<>();
        int attributesLength=attributes.getLength();
        for (int i = 0; i <attributesLength; i++) {
            switch(attributes.getType(i)){
                case "id": builder.id( attributes.getValue(i));break;
                case "for":  builder.forType(GioKeyForType.valueOf(attributes.getValue(i)));break;
                default:
                    customAttributes.put(attributes.getType(i), attributes.getValue(i));
            }

        }
        builder.customAttributes(customAttributes);
        currentEntity = new GioKeyEntity(builder.build());
    }
    private void  endKeyElement() {
        stack.peek().addEntity(currentEntity);
        currentEntity = stack.pop();
    }

    public GraphmlEntity getCurrentEntity() {
        return currentEntity;
    }



}
