package com.calpano.graphinout.reader.graphml;


import com.calpano.graphinout.base.gio.GioData;
import com.calpano.graphinout.base.gio.GioDocument;
import com.calpano.graphinout.base.gio.GioEdge;
import com.calpano.graphinout.base.gio.GioEndpoint;
import com.calpano.graphinout.base.gio.GioEndpointDirecton;
import com.calpano.graphinout.base.gio.GioGraph;
import com.calpano.graphinout.base.gio.GioKey;
import com.calpano.graphinout.base.gio.GioKeyForType;
import com.calpano.graphinout.base.gio.GioNode;
import com.calpano.graphinout.base.gio.GioWriter;
import com.calpano.graphinout.base.reader.ContentError;
import lombok.extern.slf4j.Slf4j;
import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

import javax.annotation.Nullable;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Stack;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@Slf4j
class GraphmlSAXHandler extends DefaultHandler {
    private final static String GRAPHML_STANDARD_NAME_SPACE = "http://graphml.graphdrawing.org/xmlns";
    private final GioWriter gioWriter;
    private final Consumer<ContentError> errorConsumer;
    private boolean structuralAssertionsEnabled = true;
    /**
     * this stack always represents (A) all currently open Gio-elements, (B) the parents of all currently open
     * Gio-elements, (C) ... ?
     * TODO answer this and delete wrong options :-)
     */
    private Stack<GraphmlEntity> openEntities = new Stack<>();
    private Locator locator;

    public GraphmlSAXHandler(GioWriter gioWriter, Consumer<ContentError> errorConsumer) {
        this.gioWriter = gioWriter;
        this.errorConsumer = errorConsumer;
    }

    private static String tagName(String uri, String localName, String qName) {
        String tagName = localName;
        if (!GRAPHML_STANDARD_NAME_SPACE.equals(uri) || uri.isEmpty()) tagName = qName;
        else if (tagName.isEmpty()) {
            tagName = qName;
        }
        if (localName.isEmpty()) {
            tagName = qName;
        }
        return tagName;
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        log.trace("characters [{}].", new String(ch, start, length));
        if (openEntities.isEmpty()) throw new IllegalStateException("No open element to add characters to");
        openEntities.peek().addData(new String(ch, start, length));
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        log.trace("endElement [{}].", qName);
        String tagName = tagName(uri, localName, qName);
        try {
            switch (tagName) {
                case GraphmlConstant.GRAPHML_ELEMENT_NAME:
                    endGraphmlElement();
                    break;

                case GraphmlConstant.DESC_ELEMENT_NAME:
                    endDescElement();
                    break;

                case GraphmlConstant.KEY_ELEMENT_NAME:
                    endKeyElement();
                    break;

                case GraphmlConstant.DATA_ELEMENT_NAME:
                    endDataElement();
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
                case GraphmlConstant.LOCATOR_ELEMENT_NAME:
                    endLocatorElement();
                    break;
                default:
                    //TODO Does it need to log?
                    //TODO Dose have to control qName and uri?
                    createEndXMlElement(qName);

            }
        } catch (Exception e) {
            throw buildException(e);
        }
    }

    @Override
    public void error(SAXParseException e) throws SAXException {
        super.error(e);
    }

    @Override
    public void fatalError(SAXParseException e) throws SAXException {
        super.fatalError(e);
    }

    public @Nullable GraphmlEntity getCurrentEntity() {
        if (openEntities.isEmpty()) return null;
        else return openEntities.peek();
    }

    @Override
    public void setDocumentLocator(Locator locator) {
        this.locator = locator;
    }

    public void setStructuralAssertionsEnabled(boolean structuralAssertionsEnabled) {
        this.structuralAssertionsEnabled = structuralAssertionsEnabled;
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        log.trace("startElement [{}].", qName);
        String tagName = tagName(uri, localName, qName);


        // validate URI once, should be "http://graphml.graphdrawing.org/xmlns"; all other URIs: verbatim data, no interpretation, still resolve URI + localName
        /// TODO warn about wrong URI
        try {
            switch (tagName) {
                case GraphmlConstant.GRAPHML_ELEMENT_NAME:
                    startGraphmlElement(attributes);
                    break;

                case GraphmlConstant.DESC_ELEMENT_NAME:
                    startDescElement(attributes);
                    break;

                case GraphmlConstant.KEY_ELEMENT_NAME:
                    startKeyElement(attributes);
                    break;

                case GraphmlConstant.DATA_ELEMENT_NAME:
                    startDataElement(attributes);
                    break;

                case GraphmlConstant.GRAPH_ELEMENT_NAME:
                    startGraphElement(attributes);
                    break;
                case GraphmlConstant.NODE_ELEMENT_NAME:
                    startNodeElement(attributes);
                    break;
                case GraphmlConstant.EDGE_ELEMENT_NAME:
                    startEdgeElement(attributes);
                    break;
                case GraphmlConstant.LOCATOR_ELEMENT_NAME:
                    startLocatorElement(attributes);
                    break;
                default:
                    //TODO Does it need to log?
                    //TODO Dose have to control qName and uri?
                    createStartXMlElement(qName, attributes);

            }
        } catch (Exception e) {
            throw buildException(e);
        }

    }

    @Override
    public void warning(SAXParseException e) throws SAXException {
        super.warning(e);
    }

    private void assertCurrent(String location, @Nullable Class<?>... expectedCurrentEntities) {
        if (!structuralAssertionsEnabled) return;
        if (expectedCurrentEntities != null && expectedCurrentEntities.length > 0) {
            if (openEntities.isEmpty()) {
                throw new IllegalStateException("No current element at '" + location + "'. Expected current=" + Arrays.asList(expectedCurrentEntities).stream().map(Class::getSimpleName).collect(Collectors.joining(", ")));
            } else {
                if (!Arrays.asList(expectedCurrentEntities).stream().anyMatch(c -> c.equals(openEntities.peek().getClass()))) {
                    throw new IllegalStateException("Unexpected current element at '" + location + "': " + openEntities.peek().getClass().getSimpleName() + ". Expected current=" + Arrays.asList(expectedCurrentEntities).stream().map(Class::getSimpleName).collect(Collectors.joining(", ")));
                }
            }
        }
    }

    private RuntimeException buildException(Exception e) {
        //TODO manage Exception
        String location = locator == null ? "N/A" : locator.getLineNumber() + ":" + locator.getColumnNumber();
        return new RuntimeException("While parsing " + location + "\n" + e.getMessage(), e);
    }

    private void createEndXMlElement(String name) throws SAXException {
        StringBuilder builder = new StringBuilder();
        builder.append("</");
        builder.append(name);
        builder.append('>');

        getCurrentEntity().addData(builder.toString());
        //    characters(builder.toString().toCharArray(), 0, builder.length());
    }

    private void createStartXMlElement(String name, Attributes attributes) throws SAXException {
        StringBuilder builder = new StringBuilder();
        builder.append("<");
        builder.append(name);
        int attributesLength = attributes.getLength();
        for (int i = 0; i < attributesLength; i++) {
            builder.append(" " + attributes.getQName(i) + "=\"" + attributes.getValue(i) + "\"");
        }

        builder.append('>');
        // characters(builder.toString().toCharArray(), 0, builder.length());
        getCurrentEntity().addData(builder.toString());
    }

    private void endDataElement() throws IOException {
        assertCurrent("endData", GioDataEntity.class);
        gioWriter.data(((GioDataEntity) openEntities.pop()).getEntity());
    }

    private void endDescElement() throws IOException {
        assertCurrent("endDesc", GioDocumentEntity.class, GioGraphEntity.class, GioNodeEntity.class, GioEdgeEntity.class, GioKeyEntity.class, GioPortEntity.class);
        GioDescriptionEntity gioDescriptionEntity = (GioDescriptionEntity) openEntities.pop();
        getCurrentEntity().addEntity(gioDescriptionEntity);
        // TODO fire parent element.start to GioWriter
    }

    private void endEdgeElement() throws IOException {
        assertCurrent("endEdge", GioEdgeEntity.class);
        GioEdgeEntity gioEdgeEntity = (GioEdgeEntity) openEntities.pop();
        gioWriter.startEdge(gioEdgeEntity.getEntity());
        gioWriter.endEdge();
    }


    private void endGraphElement() throws IOException {
        assertCurrent("endGraph", GioGraphEntity.class);
        GioGraphEntity gioGraphEntity = (GioGraphEntity) openEntities.pop();
        sendStartGraphMaybe(gioGraphEntity);
        gioWriter.endGraph(gioGraphEntity.url);
    }

    private void endGraphmlElement() throws IOException {
        assertCurrent("endGraphML", GioDocumentEntity.class);
        sendStartGraphmlDocumentMaybe((GioDocumentEntity) openEntities.peek());
        gioWriter.endDocument();
        openEntities.pop();
    }

    private void endKeyElement() throws IOException {
        assertCurrent("endKey", GioKeyEntity.class);
        GioKey gioKey = ((GioKeyEntity) openEntities.pop()).getEntity();
        gioWriter.key(gioKey);
    }

    private void endLocatorElement() throws IOException {
        assertCurrent("endLocator", GioGraphEntity.class, GioNodeEntity.class);
    }

    private void endNodeElement() throws IOException {
        assertCurrent("endNode", GioNodeEntity.class);
        GioNodeEntity gioNodeEntity = (GioNodeEntity) openEntities.pop();
        GioNode gioNode = gioNodeEntity.getEntity();
        gioWriter.startNode(gioNode);
        gioWriter.endNode(gioNodeEntity.url);
    }

    /**
     * Send graph to gioWriter, if not already sent
     * <p>
     * There are several possible XML event flows, each having their own time point when we can be sure we collected all
     * data for the gioWriter.startGraph (i.e. the description, if there was any)
     *
     * <li>startGraph,startDesc,endDesc,endGraph(=>gioWriter.startGraph) </li>
     * <li>startGraph,startNode(=>gioWriter.startGraph),endNode,endGraph </li>
     * <li>startGraph,startEdge(=>gioWriter.startGraph),endEdge,endGraph </li>
     * <li>startGraph,startDesc,endDesc,startNode(=>gioWriter.startGraph),endNode,endGraph </li>
     * <li>startGraph,startDesc,endDesc,startEdge(=>gioWriter.startGraph),endEdge,endGraph </li>
     *
     * @param gioGraphEntity
     * @throws IOException
     */
    private void sendStartGraphMaybe(GioGraphEntity gioGraphEntity) throws IOException {
        if (!gioGraphEntity.isSent()) {
            gioGraphEntity.markAsSent();
            gioWriter.startGraph(gioGraphEntity.getEntity());
        }
    }

    private void sendStartGraphmlDocumentMaybe(GioDocumentEntity gioDocumentEntity) throws IOException {
        if (!gioDocumentEntity.isSent()) {
            gioDocumentEntity.markAsSent();
            gioWriter.startDocument(gioDocumentEntity.getEntity());
        }
    }


    private void startDataElement(Attributes attributes) throws IOException {
        assertCurrent("startData", GioDocumentEntity.class, GioGraphEntity.class, GioNodeEntity.class, GioEdgeEntity.class, GioPortEntity.class);
        if (openEntities.peek() instanceof GioDocumentEntity) {
            sendStartGraphmlDocumentMaybe((GioDocumentEntity) openEntities.peek());
        }
        GioData.GioDataBuilder builder = GioData.builder();
        Map<String, String> customAttributes = new LinkedHashMap<>();
        int attributesLength = attributes.getLength();
        for (int i = 0; i < attributesLength; i++) {
            switch (attributes.getQName(i)) {
                case "id":
                    builder.id(attributes.getValue(i));
                    break;
                case "key":
                    builder.key(attributes.getValue(i));
                    break;
                default:
                    customAttributes.put(attributes.getQName(i), attributes.getValue(i));
            }

        }
        GioDataEntity gioDataEntity = new GioDataEntity(builder.build());
        openEntities.push(gioDataEntity);
    }

    private void startDescElement(Attributes attributes) {
        openEntities.push(new GioDescriptionEntity());
    }

    private void startEdgeElement(Attributes attributes) throws IOException {
        assertCurrent("startEdge", GioGraphEntity.class);
        GioGraphEntity gioGraphEntity = (GioGraphEntity) openEntities.peek();
        sendStartGraphMaybe(gioGraphEntity);

        GioEdge.GioEdgeBuilder builder = GioEdge.builder();
        Map<String, String> customAttributes = new LinkedHashMap<>();
        int attributesLength = attributes.getLength();
        for (int i = 0; i < attributesLength; i++) {
            switch (attributes.getQName(i)) {
                case "id":
                    builder.id(attributes.getValue(i));
                    break;
                case "source":
                    builder.endpoint(GioEndpoint.builder().type(GioEndpointDirecton.In).node(attributes.getValue(i)).build());
                    break;
                case "target":
                    builder.endpoint(GioEndpoint.builder().type(GioEndpointDirecton.Out).node(attributes.getValue(i)).build());
                    break;
                default:
                    customAttributes.put(attributes.getQName(i), attributes.getValue(i));
            }
        }
        openEntities.push(new GioEdgeEntity(builder.build()));
    }

    private void startGraphElement(Attributes attributes) throws IOException {
        assertCurrent("startGraph", GioDocumentEntity.class, GioNodeEntity.class);
        if (openEntities.peek() instanceof GioDocumentEntity) {
            sendStartGraphmlDocumentMaybe((GioDocumentEntity) openEntities.peek());
        }
        GioGraph.GioGraphBuilder builder = GioGraph.builder();
        Map<String, String> customAttributes = new LinkedHashMap<>();
        int attributesLength = attributes.getLength();
        for (int i = 0; i < attributesLength; i++) {
            switch (attributes.getQName(i)) {
                case "id":
                    builder.id(attributes.getValue(i));
                    break;
                case "edgedefault":
                    builder.edgedefaultDirected(Boolean.valueOf(attributes.getValue(i)));
                    break;
                default:
                    customAttributes.put(attributes.getQName(i), attributes.getValue(i));
            }

        }
        GioGraphEntity gioGraphEntity = new GioGraphEntity(builder.build());
        openEntities.push(gioGraphEntity);
    }

    private void startGraphmlElement(Attributes attributes) {
        Map<String, String> customAttributes = new LinkedHashMap<>();
        for (int i = 0; i < attributes.getLength(); i++) {
            customAttributes.put(attributes.getQName(i), attributes.getValue(i));
        }
        GioDocumentEntity gioDocumentEntity = new GioDocumentEntity(GioDocument.builder().customAttributes(customAttributes).build());
        openEntities.push(gioDocumentEntity);
    }

    private void startKeyElement(Attributes attributes) throws IOException {
        assertCurrent("startKey", GioDocumentEntity.class);
        sendStartGraphmlDocumentMaybe((GioDocumentEntity) openEntities.peek());
        GioKey.GioKeyBuilder builder = GioKey.builder();
        Map<String, String> customAttributes = new LinkedHashMap<>();
        int attributesLength = attributes.getLength();
        boolean isForDefined = false;
        for (int i = 0; i < attributesLength; i++) {
            switch (attributes.getQName(i)) {
                case "id":
                    builder.id(attributes.getValue(i));
                    break;
                case "for":
                    builder.forType(GioKeyForType.keyForType(attributes.getValue(i).toLowerCase()));
                    isForDefined = true;
                    break;
                default:
                    customAttributes.put(attributes.getQName(i), attributes.getValue(i));
            }
        }
        if (!isForDefined) {
            builder.forType(GioKeyForType.All);
        }
        builder.customAttributes(customAttributes);
        GioKeyEntity gioKeyEntity = new GioKeyEntity(builder.build());
        openEntities.push(gioKeyEntity);
    }

    private void startLocatorElement(Attributes attributes) throws IOException {
        assertCurrent("startLocator", GioGraphEntity.class, GioNodeEntity.class);
        //TODO locator customAttributes doese need to process that
        URL url = null;
        Map<String, String> customAttributes = new LinkedHashMap<>();
        int attributesLength = attributes.getLength();
        for (int i = 0; i < attributesLength; i++) {
            switch (attributes.getQName(i)) {
                case "xlink:href":
                    url = new URL(attributes.getValue(i));
                    break;
                default:
                    customAttributes.put(attributes.getQName(i), attributes.getValue(i));
            }

        }
        // NOTE: we are throwing away custom attributes on <locator>-element
        GraphmlEntity entity = openEntities.peek();
        if (entity instanceof GioGraphEntity) {
            GioGraphEntity e = (GioGraphEntity) entity;
            e.url = url;
        } else {
            GioNodeEntity e = (GioNodeEntity) entity;
            e.url = url;
        }
    }

    private void startNodeElement(Attributes attributes) throws IOException {
        assertCurrent("startNode", GioGraphEntity.class);
        GioGraphEntity gioGraphEntity = (GioGraphEntity) openEntities.peek();
        sendStartGraphMaybe(gioGraphEntity);
        GioNode.GioNodeBuilder builder = GioNode.builder();
        Map<String, String> customAttributes = new LinkedHashMap<>();
        int attributesLength = attributes.getLength();
        for (int i = 0; i < attributesLength; i++) {
            switch (attributes.getQName(i)) {
                case "id":
                    builder.id(attributes.getValue(i));
                    break;
                default:
                    customAttributes.put(attributes.getQName(i), attributes.getValue(i));
            }

        }
        openEntities.push(new GioNodeEntity(builder.build()));
    }


}
