package com.calpano.graphinout.reader.graphml;


import com.calpano.graphinout.base.gio.*;
import com.calpano.graphinout.base.reader.GioReader;
import lombok.extern.slf4j.Slf4j;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

import java.io.IOException;
import java.net.URL;
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
        // validate URI once, should be "http://graphml.graphdrawing.org/xmlns"; all other URIs: verbatim data, no interpretation, still resolve URI + localName
        /// TODO warn about wrong URI
        try {
            switch (localName) {
                case GraphmlConstant.GRAPHML_ELEMENT_NAME:
                    startGraphmlElement(uri, localName, attributes);
                    break;

                case GraphmlConstant.DESC_ELEMENT_NAME:
                    startDescElement(uri, localName, attributes);
                    break;

                case GraphmlConstant.KEY_ELEMENT_NAME:
                    startKeyElement(uri, localName, attributes);
                    break;

                case GraphmlConstant.NODE_DATA_ELEMENT_NAME:
                    startNodeDataElement(uri, localName, attributes);
                    break;

                case GraphmlConstant.GRAPH_ELEMENT_NAME:
                    startGraphElement(uri, localName, attributes);
                    break;
                case GraphmlConstant.NODE_ELEMENT_NAME:
                    startNodeElement(uri, localName, attributes);
                    break;
                case GraphmlConstant.EDGE_ELEMENT_NAME:
                    startEdgeElement(uri, localName, attributes);
                    break;
                default:
                    //TODO Does it need to log?
                    //TODO Dose have to control qName and uri?
                    createStartXMlElement(localName, attributes);

            }
        } catch (Exception e) {
            //TODO manage Exception
        }


    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        try {
            switch (localName) {
                case GraphmlConstant.GRAPHML_ELEMENT_NAME:
                    endGraphmlElement();
                    break;

                case GraphmlConstant.DESC_ELEMENT_NAME:
                    endDescElement();
                    break;

                case GraphmlConstant.KEY_ELEMENT_NAME:
                    endKeyElement();
                    break;

                case GraphmlConstant.NODE_DATA_ELEMENT_NAME:
                    endNodeDataElement();
                    break;

                case GraphmlConstant.GRAPH_ELEMENT_NAME:
                    endGraphElement();
                    break;
                case GraphmlConstant.NODE_ELEMENT_NAME:
                    endNodeElement();
                    break;
                case GraphmlConstant.EDGE_ELEMENT_NAME:
                    endEdgeElement();
                    break;
                default:
                    //TODO Does it need to log?
                    //TODO Dose have to control qName and uri?
                    createEndXMlElement(localName);

            }
        } catch (Exception e) {
            //TODO manage Exception
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

    private void endGraphmlElement() throws IOException {
        if (!stack.isEmpty() || (currentEntity != null && !(currentEntity instanceof GioDocumentEntity)))
            throw new IOException("Invalid Structure format.");
        else if (currentEntity != null && (currentEntity instanceof GioDocumentEntity g))
            gioWriter.startDocument(g.getEntity());
        currentEntity = null;
        gioWriter.endDocument();

    }

    private void startDescElement(String uri, String localName, Attributes attributes) {
        stack.push(currentEntity);
        currentEntity = new GioDescriptionEntity();
    }

    private void endDescElement() throws IOException {
        if (stack.isEmpty()) throw new IOException("Invalid Structure format.");
        stack.peek().addEntity(currentEntity);
        currentEntity = stack.pop();
    }

    private void startKeyElement(String uri, String localName, Attributes attributes) throws IOException {
        if (currentEntity == null || !(currentEntity instanceof GioDocumentEntity))
            throw new IOException("Invalid Structure format.");
        stack.push(currentEntity);
        GioKey.GioKeyBuilder builder = GioKey.builder();
        Map<String, String> customAttributes = new LinkedHashMap<>();
        int attributesLength = attributes.getLength();
        for (int i = 0; i < attributesLength; i++) {
            switch (attributes.getType(i)) {
                case "id":
                    builder.id(attributes.getValue(i));
                    break;
                case "for":
                    builder.forType(GioKeyForType.valueOf(attributes.getValue(i)));
                    break;
                default:
                    customAttributes.put(attributes.getType(i), attributes.getValue(i));
            }

        }
        builder.customAttributes(customAttributes);
        currentEntity = new GioKeyEntity(builder.build());
    }

    private void endKeyElement() throws IOException {
        if (stack.isEmpty() || !(stack.peek() instanceof GioDocumentEntity))
            throw new IOException("Invalid Structure format.");

        stack.peek().addEntity(currentEntity);
        currentEntity = stack.pop();
    }

    private void startNodeDataElement(String uri, String localName, Attributes attributes) throws IOException {
        if (currentEntity == null) throw new IOException("Invalid Structure format.");
        stack.push(currentEntity);
        GioData.GioDataBuilder builder = GioData.builder();
        Map<String, String> customAttributes = new LinkedHashMap<>();
        int attributesLength = attributes.getLength();
        for (int i = 0; i < attributesLength; i++) {
            switch (attributes.getType(i)) {
                case "id":
                    builder.id(attributes.getValue(i));
                    break;
                case "key":
                    builder.key(attributes.getValue(i));
                    break;
                default:
                    customAttributes.put(attributes.getType(i), attributes.getValue(i));
            }

        }
        currentEntity = new GioDataEntity(builder.build());
    }

    private void endNodeDataElement() throws IOException {
        if (stack.isEmpty()) throw new IOException("Invalid Structure format.");
        stack.peek().addEntity(currentEntity);
        currentEntity = stack.pop();
    }


    private void startGraphElement(String uri, String localName, Attributes attributes) throws IOException {

        if (currentEntity != null && currentEntity.getEntity() instanceof GioDocument g) {
            gioWriter.startDocument(g);
        } else if (currentEntity != null) stack.push(currentEntity);

        GioGraph.GioGraphBuilder builder = GioGraph.builder();
        Map<String, String> customAttributes = new LinkedHashMap<>();
        int attributesLength = attributes.getLength();
        for (int i = 0; i < attributesLength; i++) {
            switch (attributes.getType(i)) {
                case "id":
                    builder.id(attributes.getValue(i));
                    break;
                case "edgedefault":
                    builder.edgedefault(Boolean.valueOf(attributes.getValue(i)));
                    break;
                default:
                    customAttributes.put(attributes.getType(i), attributes.getValue(i));
            }

        }
        currentEntity = new GioGraphEntity(builder.build());
    }

    private void endGraphElement() throws IOException {

        if (currentEntity != null) {
            if (currentEntity.getEntity() instanceof GioGraph g) {
                gioWriter.startGraph(g);
                gioWriter.endGraph(null);
            } else if (currentEntity.getEntity() instanceof URL g) {
                gioWriter.endGraph(g);
            } else {
                throw new IOException("Invalid Structure format.");
            }

        } else {
            gioWriter.endGraph(null);
        }
        currentEntity = null;

    }


    private void startNodeElement(String uri, String localName, Attributes attributes) throws IOException {

        if (currentEntity != null && currentEntity.getEntity() instanceof GioGraph g) {
            gioWriter.startGraph(g);
        } else if (currentEntity != null) stack.push(currentEntity);

        GioNode.GioNodeBuilder builder = GioNode.builder();
        Map<String, String> customAttributes = new LinkedHashMap<>();
        int attributesLength = attributes.getLength();
        for (int i = 0; i < attributesLength; i++) {
            switch (attributes.getType(i)) {
                case "id":
                    builder.id(attributes.getValue(i));
                    break;
                default:
                    customAttributes.put(attributes.getType(i), attributes.getValue(i));
            }

        }
        currentEntity = new GioNodeEntity(builder.build());
    }

    private void endNodeElement() throws IOException {
     if (currentEntity != null) {
            if (currentEntity.getEntity() instanceof GioNode g) {
                gioWriter.startNode(g);
                gioWriter.endNode(null);
            } else if (currentEntity.getEntity() instanceof URL g) {
                gioWriter.endNode(g);
            }
            currentEntity = null;
        } else {
         gioWriter.endNode(null);
     }

    }

    private void startEdgeElement(String uri, String localName, Attributes attributes) throws IOException {

        if (currentEntity != null && currentEntity.getEntity() instanceof GioGraph g) {
            gioWriter.startGraph(g);
        } else if (currentEntity != null) stack.push(currentEntity);

        GioEdge.GioEdgeBuilder builder = GioEdge.builder();
        Map<String, String> customAttributes = new LinkedHashMap<>();
        int attributesLength = attributes.getLength();
        for (int i = 0; i < attributesLength; i++) {
            switch (attributes.getType(i)) {
                case "id":
                    builder.id(attributes.getValue(i));
                    break;
                default:
                    customAttributes.put(attributes.getType(i), attributes.getValue(i));
            }

        }
        currentEntity = new GioEdgeEntity(builder.build());
    }

    private void endEdgeElement() throws IOException {
        if (currentEntity != null) {
            if (currentEntity.getEntity() instanceof GioEdge g) {
                gioWriter.startEdge(g);
                gioWriter.endEdge(null);
            } else if (currentEntity.getEntity() instanceof URL g) {
                gioWriter.endNode(g);
            }
            currentEntity = null;
        } else {
            gioWriter.endNode(null);
        }

    }
    public GraphmlEntity getCurrentEntity() {
        return currentEntity;
    }

    private void createStartXMlElement(String name, Attributes attributes) throws SAXException {
        StringBuilder builder = new StringBuilder();
        builder.append("<");
        builder.append(name);
        int attributesLength = attributes.getLength();
        for (int i = 0; i < attributesLength; i++) {
            builder.append(" " + attributes.getType(i) + "=\"" + attributes.getValue(i) + "\"");
        }

        builder.append('>');
        characters(builder.toString().toCharArray(), 0, builder.length());
    }

    private void createEndXMlElement(String name) throws SAXException {
        StringBuilder builder = new StringBuilder();
        builder.append("</");
        builder.append(name);
        builder.append('>');
        characters(builder.toString().toCharArray(), 0, builder.length());
    }


}
