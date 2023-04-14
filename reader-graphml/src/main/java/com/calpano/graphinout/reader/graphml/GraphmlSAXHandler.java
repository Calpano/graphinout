package com.calpano.graphinout.reader.graphml;


import com.calpano.graphinout.base.gio.GioData;
import com.calpano.graphinout.base.gio.GioDocument;
import com.calpano.graphinout.base.gio.GioEndpoint;
import com.calpano.graphinout.base.gio.GioEndpointDirection;
import com.calpano.graphinout.base.gio.GioGraph;
import com.calpano.graphinout.base.gio.GioKey;
import com.calpano.graphinout.base.gio.GioKeyForType;
import com.calpano.graphinout.base.gio.GioNode;
import com.calpano.graphinout.base.gio.GioPort;
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
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@Slf4j
class GraphmlSAXHandler extends DefaultHandler {
    private static final String GRAPHML_STANDARD_NAME_SPACE = "http://graphml.graphdrawing.org/xmlns";
    private final GioWriter gioWriter;
    private final Consumer<ContentError> errorConsumer;
    /**
     * This stack represents all currently open GioWriter-entites, collecting more data before they are ready to be sent
     * to GioWriter
     */
    private final Deque<GraphmlEntity<?>> openEntities = new LinkedList<>();
    private boolean structuralAssertionsEnabled = true;
    private Locator locator;
    private Map<String, String> namespaces = new HashMap<>();

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
        String s = new String(ch, start, length);
        log.trace("characters [{}].", s);
        if (openEntities.isEmpty()) {
            if (s.trim().length() == 0) {
                // ignore whitespace
            } else {
                buildException(ContentError.ErrorLevel.Warn, "Unexpected characters '" + s + "' [No open element to add characters to.]");
            }
        } else {
            try {
                openEntities.peek().addCharacters(s);
            } catch (UnsupportedOperationException e) {
                // characters could not be handled
                buildException(ContentError.ErrorLevel.Warn, "Unexpected characters '" + s + "' [Element '" + openEntities.peek().getName() +
                        "' does not allow characters.]");
            }
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        log.debug("XML </{}>. Stack before: {}", qName, stackAsString());
        String tagName = tagName(uri, localName, qName);
        try {
            switch (tagName) {
                case GraphmlElement.DATA -> endDataElement();
                case GraphmlElement.DEFAULT -> endDefaultElement();
                case GraphmlElement.DESC -> endDescElement();
                case GraphmlElement.EDGE -> endEdgeElement();
                case GraphmlElement.ENDPOINT -> endEndpointElement();
                case GraphmlElement.GRAPH -> endGraphElement();
                case GraphmlElement.GRAPHML -> endGraphmlElement();
                case GraphmlElement.HYPER_EDGE -> endHyperedgeElement();
                case GraphmlElement.KEY -> endKeyElement();
                case GraphmlElement.LOCATOR -> endLocatorElement();
                case GraphmlElement.NODE -> endNodeElement();
                case GraphmlElement.PORT -> endPortElement();
                default -> {
                    if (openEntities.peek() != null && (openEntities.peek() instanceof GioDefaultEntity || openEntities.peek() instanceof GioDataEntity)) {
                        // we accept any element and forward
                        createEndXMlElement(qName);
                    } else {
                        // we warn and ignore
                        buildException(ContentError.ErrorLevel.Warn, String.format("The Element </%s> not acceptable tag for Graphml.", qName));
                    }
                }
            }
        } catch (Exception e) {
            throw buildException(ContentError.ErrorLevel.Error, e);
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
        log.debug("XML <{}>.  Stack before: {}.", qName, stackAsString());
        String tagName = tagName(uri, localName, qName);
        try {
            switch (tagName) {
                case GraphmlElement.DATA -> startDataElement(attributes);
                case GraphmlElement.DEFAULT -> startDefaultElement(attributes);
                case GraphmlElement.DESC -> startDescElement(attributes);
                case GraphmlElement.EDGE -> startEdgeElement(attributes);
                case GraphmlElement.ENDPOINT -> startEndpointElement(attributes);
                case GraphmlElement.GRAPH -> startGraphElement(attributes);
                case GraphmlElement.GRAPHML -> startGraphmlElement(attributes);
                case GraphmlElement.HYPER_EDGE -> startHyperedgeElement(attributes);
                case GraphmlElement.KEY -> startKeyElement(attributes);
                case GraphmlElement.LOCATOR -> startLocatorElement(attributes);
                case GraphmlElement.NODE -> startNodeElement(attributes);
                case GraphmlElement.PORT -> startPortElement(attributes);
                default -> {
                    GraphmlEntity<?> entity = openEntities.peek();
                    if (entity instanceof GioDataEntity || entity instanceof GioDefaultEntity) {
                        // parse generic xml
                        createStartXMlElement(qName, attributes);
                    } else {
                        // we warn and ignore in valid tags
                        buildException(ContentError.ErrorLevel.Warn, String.format("The Element <%s> not acceptable tag for Graphml.", qName));
                    }
                }
            }
        } catch (Exception e) {
            throw buildException(ContentError.ErrorLevel.Error, e);
        }
    }


    @Override
    public void startPrefixMapping(String prefix, String uri) throws SAXException {
        super.startPrefixMapping(prefix, uri);
        if (!GRAPHML_STANDARD_NAME_SPACE.equals(uri)) namespaces.put(prefix, uri);

    }

    @Override
    public void warning(SAXParseException e) throws SAXException {
        super.warning(e);
    }

    /**
     * Just looks at the stack
     */
    private void assertCurrent(String location, @Nullable Class<?>... expectedCurrentEntities) {
        if (!structuralAssertionsEnabled) return;
        if (expectedCurrentEntities != null && expectedCurrentEntities.length > 0) {
            if (openEntities.isEmpty()) {
                throw new IllegalStateException("No current element at '" + location + "'. Expected current=" + Arrays.stream(expectedCurrentEntities).map(Class::getSimpleName).collect(Collectors.joining(", ")));
            } else {
                if (!Arrays.asList(expectedCurrentEntities).stream().anyMatch(c -> c.equals(openEntities.peek().getClass()))) {
                    throw new IllegalStateException("Unexpected current element at '" + location + "': " + openEntities.peek().getClass().getSimpleName() + ". Expected current=" + Arrays.asList(expectedCurrentEntities).stream().map(Class::getSimpleName).collect(Collectors.joining(", ")));
                }
            }
        }
    }

    private @Nullable RuntimeException buildException(ContentError.ErrorLevel errorLevel, String msg) {
        ContentError contentError = new ContentError(errorLevel, msg,
                locator == null ? null :
                        new ContentError.Location(locator.getLineNumber(), locator.getColumnNumber()));
        if (errorConsumer != null) {
            errorConsumer.accept(contentError);
        }
        if (errorLevel == ContentError.ErrorLevel.Error) {
            String location = locator == null ? "N/A" : locator.getLineNumber() + ":" + locator.getColumnNumber();
            return new RuntimeException("While parsing " + location + "\n" + "Stack: " + stackAsString() + "\n" + "Message: " + msg);
        } else {
            log.warn("ContentError: " + contentError);
            return null;
        }
    }


    private @Nullable RuntimeException buildException(ContentError.ErrorLevel errorLevel, Exception e) {
        ContentError contentError = new ContentError(errorLevel, e.getMessage(),
                locator == null ? null :
                        new ContentError.Location(locator.getLineNumber(), locator.getColumnNumber()));
        if (errorConsumer != null) {
            errorConsumer.accept(contentError);
        }
        String location = locator == null ? "N/A" : locator.getLineNumber() + ":" + locator.getColumnNumber();
        if (errorLevel == ContentError.ErrorLevel.Error) {
            return new RuntimeException("While parsing " + location + "\n" + "Stack: " + stackAsString() + "\n" + "Message: " + e.getMessage(), e);
        } else {
            log.warn("ContentError: " + contentError, e);
            return null;
        }
    }

    private void createEndXMlElement(String name) throws SAXException {
        String builder = "</" + name + '>';
        getCurrentEntity().addCharacters(builder);
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
        getCurrentEntity().addCharacters(builder.toString());
    }

    private void createWarningLog(String message) {
        this.errorConsumer.accept(new ContentError(ContentError.ErrorLevel.Warn, message, locator == null ? null : new ContentError.Location(locator.getLineNumber(), locator.getColumnNumber())));

    }

    private void endDataElement() throws IOException {
        assertCurrent("endData", GioDataEntity.class);
        gioWriter.data((pop(GioDataEntity.class)).getEntity());
    }

    private void endDefaultElement() {
        assertCurrent("endDefault", GioDefaultEntity.class);
        GioDefaultEntity gioDefaultEntity = pop(GioDefaultEntity.class);
        peek(GioKeyEntity.class).addEntity(gioDefaultEntity);
    }

    private void endDescElement() throws IOException {
        assertCurrent("endDesc", GioDescriptionEntity.class);
        GioDescriptionEntity gioDescriptionEntity = pop(GioDescriptionEntity.class);
        getCurrentEntity().addEntity(gioDescriptionEntity);
        // TODO fire parent element.start to GioWriter
    }

    private void endEdgeElement() throws IOException {
        assertCurrent("endEdge", GioEdgeEntity.class);
        sendStartThisOrParentMaybe(GraphmlElement.EDGE);
        gioWriter.endEdge();
        pop(GioEdgeEntity.class);
    }

    private void endEndpointElement() {
        assertCurrent("startEndpoint", GioEdgeEntity.class);
    }

    private void endGraphElement() throws IOException {
        assertCurrent("endGraph", GioGraphEntity.class);
        sendStartThisOrParentMaybe(GraphmlElement.GRAPH);
        @Nullable URL url = peekOptional(GioLocatorEntity.class).map(GioLocatorEntity::getEntity).orElse(null);
        if (url != null)
            pop(GioLocatorEntity.class);
        gioWriter.endGraph(url);
        pop(GioGraphEntity.class);
    }

    private void endGraphmlElement() throws IOException {
        assertCurrent("endGraphML", GioDocumentEntity.class);
        sendStartThisOrParentMaybe(GraphmlElement.GRAPHML);
        gioWriter.endDocument();
        pop(GioDocumentEntity.class);
    }

    private void endHyperedgeElement() throws IOException {
        assertCurrent("endHyperedge", GioEdgeEntity.class);
        sendStartThisOrParentMaybe(GraphmlElement.HYPER_EDGE);
        gioWriter.endEdge();
        pop(GioEdgeEntity.class);
    }

    private void endKeyElement() throws IOException {
        assertCurrent("endKey", GioKeyEntity.class);
        GioKeyEntity gioKeyEntity = pop(GioKeyEntity.class);
        peek(GioDocumentEntity.class).addEntity(gioKeyEntity);
    }

    private void endLocatorElement() throws IOException {
        assertCurrent("endLocator", GioGraphEntity.class, GioNodeEntity.class);
    }

    private void endNodeElement() throws IOException {
        assertCurrent("endNode", GioNodeEntity.class);
        sendStartThisOrParentMaybe(GraphmlElement.NODE);
        @Nullable URL url = peekOptional(GioLocatorEntity.class).map(GioLocatorEntity::getEntity).orElse(null);
        if (url != null)
            pop(GioLocatorEntity.class);
        gioWriter.endNode(url);
        pop(GioNodeEntity.class);
    }

    private void endPortElement() throws IOException {
        assertCurrent("endPort", GioPortEntity.class, GioNodeEntity.class);
        sendStartThisOrParentMaybe(GraphmlElement.PORT);
        pop(GioPortEntity.class);
        gioWriter.endPort();
    }

    private <T extends GraphmlEntity<?>> T peek(Class<T> entity) {
        return (T) openEntities.peek();
    }

    /**
     * The optional only contains something if the top of the stack is not empty and has the requested type
     *
     * @param clazz
     * @param <T>
     * @return
     */
    private <T extends GraphmlEntity<?>> Optional<T> peekOptional(Class<T> clazz) {
        if (openEntities.isEmpty()) return Optional.empty();
        GraphmlEntity<?> element = openEntities.peek();
        if (element.getClass().equals(clazz)) {
            return Optional.of((T) element);
        }
        return Optional.empty();
    }

    private <T extends GraphmlEntity<?>> T pop(Class<T> clazz) {
        T element = (T) openEntities.pop();
        assert element.getClass().equals(clazz) : "Expected to pop " + clazz.getSimpleName() + " but got " + element.getClass().getSimpleName();
        return element;
    }

    private <T extends GraphmlEntity<?>> void push(T entity) {
        openEntities.push(entity);
    }

    private void sendStart(String name, GraphmlEntity<?> entity) throws IOException {
        switch (name) {
            case GraphmlElement.GRAPHML -> gioWriter.startDocument((GioDocument) entity.getEntity());
            case GraphmlElement.GRAPH -> gioWriter.startGraph((GioGraph) entity.getEntity());
            case GraphmlElement.NODE -> gioWriter.startNode((GioNode) entity.getEntity());
            case GraphmlElement.PORT -> gioWriter.startPort((GioPort) entity.getEntity());
            case GraphmlElement.EDGE, GraphmlElement.HYPER_EDGE ->
                    gioWriter.startEdge(((GioEdgeEntity) entity).buildEdge());
            default -> throw new AssertionError("Element " + name + " should have been sent");
        }
    }

    /**
     * Send start of parent element, if not already sent.
     * <p>
     * Many elements in GioWriter require additional data. E.g. the description ("&lt;desc&gt;...&lt;/desc&gt;") of a
     * node. Hence when reading start-element('node') from XML we cannot yet emit GioWriter.startNode. We need to await
     * the description. But, as soon as (A) an element starts which is another GioWriter call, or (B) the node ends, we
     * can emit the GioWriter.start.
     * <p>
     * In {@link Graphml#isXmlChildElementWithIndependentGioWriterCall(String, String)} we can query this.
     *
     * @param currentElement about to start, not yet on the stack
     * @throws IOException
     */
    private void sendStartThisOrParentMaybe(String currentElement) throws IOException {
        GraphmlEntity<?> openEntity = openEntities.peek();

        // parent not yet sent AND current element signals end of parent?
        if (!openEntity.isSent() && Graphml.isXmlChildElementWithIndependentGioWriterCall(openEntity.getName(), currentElement)) {
            sendStart(openEntity.getName(), openEntity);
            openEntity.markAsSent();
            assert openEntities.stream().allMatch(GraphmlEntity::isSent);
        }

        // we are closing X, so its really time to signal start of X
        if (!openEntity.isSent() && openEntity.resultsInGraphmlElement(currentElement)) {
            sendStart(openEntity.getName(), openEntity);
            openEntity.markAsSent();
            assert openEntities.stream().allMatch(GraphmlEntity::isSent);
        }

    }

    private String stackAsString() {
        if (openEntities.isEmpty()) return "-empty-";
        List<String> list = openEntities.stream().map(graphmlEntity -> graphmlEntity.getName() + (graphmlEntity.isSent() ? "" : "[PENDING]")).map(s -> "<" + s + ">").collect(Collectors.toList());
        Collections.reverse(list);
        return String.join("|", list);
    }

    private void startDataElement(Attributes attributes) throws IOException {
        assertCurrent("startData", GioDocumentEntity.class, GioGraphEntity.class, GioNodeEntity.class, GioEdgeEntity.class, GioPortEntity.class);
        sendStartThisOrParentMaybe(GraphmlElement.DATA);
        GioData.GioDataBuilder builder = GioData.builder();
        Map<String, String> customAttributes = new LinkedHashMap<>();
        int attributesLength = attributes.getLength();
        for (int i = 0; i < attributesLength; i++) {
            switch (attributes.getQName(i)) {
                case "id" -> builder.id(attributes.getValue(i));
                case "key" -> builder.key(attributes.getValue(i));
                default -> customAttributes.put(attributes.getQName(i), attributes.getValue(i));
            }

        }
        GioDataEntity gioDataEntity = new GioDataEntity(builder.build());
        push(gioDataEntity);
    }

    private void startDefaultElement(Attributes attributes) {
        push(new GioDefaultEntity());
    }

    private void startDescElement(Attributes attributes) {
        push(new GioDescriptionEntity());
    }

    private void startEdgeElement(Attributes attributes) throws IOException {
        assertCurrent("startEdge", GioGraphEntity.class);
        sendStartThisOrParentMaybe(GraphmlElement.EDGE);
        GioEdgeEntity gioEdgeEntity = new GioEdgeEntity();
        Map<String, String> customAttributes = new LinkedHashMap<>();
        int attributesLength = attributes.getLength();
        for (int i = 0; i < attributesLength; i++) {
            switch (attributes.getQName(i)) {
                case "id" -> gioEdgeEntity.id = attributes.getValue(i);
                case "source" ->
                        gioEdgeEntity.addEndpoint(GioEndpoint.builder().type(GioEndpointDirection.In).node(attributes.getValue(i)).build());
                case "target" ->
                        gioEdgeEntity.addEndpoint(GioEndpoint.builder().type(GioEndpointDirection.Out).node(attributes.getValue(i)).build());
                default -> customAttributes.put(attributes.getQName(i), attributes.getValue(i));
            }
        }
        push(gioEdgeEntity);
    }

    private void startEndpointElement(Attributes attributes) {
        assertCurrent("startEndpoint", GioEdgeEntity.class);

        GioEndpoint.GioEndpointBuilder b = GioEndpoint.builder();
        Map<String, String> customAttributes = new LinkedHashMap<>();
        for (int i = 0; i < attributes.getLength(); i++) {
            switch (attributes.getQName(i)) {
                case "id" -> b.id(attributes.getValue(i));
                case "type" -> b.type(GioEndpointDirection.of(attributes.getValue(i)));
                case "node" -> b.node(attributes.getValue(i));
                // optional
                case "port" -> b.port(attributes.getValue(i));
                default -> customAttributes.put(attributes.getQName(i), attributes.getValue(i));
            }
        }
        peek(GioEdgeEntity.class).addEndpoint(b.build());
    }

    private void startGraphElement(Attributes attributes) throws IOException {
        assertCurrent("startGraph", GioDocumentEntity.class, GioNodeEntity.class);
        sendStartThisOrParentMaybe(GraphmlElement.GRAPH);
        GioGraph.GioGraphBuilder builder = GioGraph.builder();
        Map<String, String> customAttributes = new LinkedHashMap<>();
        int attributesLength = attributes.getLength();
        for (int i = 0; i < attributesLength; i++) {
            switch (attributes.getQName(i)) {
                case "id" -> builder.id(attributes.getValue(i));
                case "edgedefault" -> builder.edgedefaultDirected(Boolean.valueOf(attributes.getValue(i)));
                default -> customAttributes.put(attributes.getQName(i), attributes.getValue(i));
            }
        }
        GioGraphEntity gioGraphEntity = new GioGraphEntity(builder.build());
        push(gioGraphEntity);
    }

    private void startGraphmlElement(Attributes attributes) {
        Map<String, String> customAttributes = new LinkedHashMap<>();
        namespaces.forEach((k, v) -> customAttributes.put("xmlns:" + k, v));
        for (int i = 0; i < attributes.getLength(); i++) {
            customAttributes.put(attributes.getQName(i), attributes.getValue(i));
        }
        namespaces.clear();
        GioDocumentEntity gioDocumentEntity = new GioDocumentEntity(GioDocument.builder().customAttributes(customAttributes).build());
        push(gioDocumentEntity);
    }

    private void startHyperedgeElement(Attributes attributes) throws IOException {
        assertCurrent("startHyperedge", GioGraphEntity.class);
        sendStartThisOrParentMaybe(GraphmlElement.HYPER_EDGE);
        GioEdgeEntity gioEdgeEntity = new GioEdgeEntity();
        Map<String, String> customAttributes = new LinkedHashMap<>();
        int attributesLength = attributes.getLength();
        for (int i = 0; i < attributesLength; i++) {
            switch (attributes.getQName(i)) {
                case "id" -> gioEdgeEntity.id = attributes.getValue(i);
                default -> customAttributes.put(attributes.getQName(i), attributes.getValue(i));
            }
        }
        push(gioEdgeEntity);
    }

    private void startKeyElement(Attributes attributes) throws IOException {
        assertCurrent("startKey", GioDocumentEntity.class);
        GioKey.GioKeyBuilder builder = GioKey.builder();
        Map<String, String> customAttributes = new LinkedHashMap<>();
        int attributesLength = attributes.getLength();
        boolean isForDefined = false;
        for (int i = 0; i < attributesLength; i++) {
            switch (attributes.getQName(i)) {
                case "id" -> builder.id(attributes.getValue(i));
                case "for" -> {
                    builder.forType(GioKeyForType.keyForType(attributes.getValue(i)));
                    isForDefined = true;
                }
                default -> customAttributes.put(attributes.getQName(i), attributes.getValue(i));
            }
        }
        if (!isForDefined) {
            builder.forType(GioKeyForType.All);
        }
        builder.customAttributes(customAttributes);
        GioKeyEntity gioKeyEntity = new GioKeyEntity(builder.build());
        push(gioKeyEntity);
    }

    private void startLocatorElement(Attributes attributes) throws IOException {
        assertCurrent("startLocator", GioGraphEntity.class, GioNodeEntity.class);
        //TODO locator customAttributes doese need to process that
        URL url = null;
        Map<String, String> customAttributes = new LinkedHashMap<>();
        int attributesLength = attributes.getLength();
        for (int i = 0; i < attributesLength; i++) {
            switch (attributes.getQName(i)) {
                case "xlink:href" -> url = new URL(attributes.getValue(i));
                default -> customAttributes.put(attributes.getQName(i), attributes.getValue(i));
            }

        }
        // NOTE: we are throwing away custom attributes on <locator>-element
        GioLocatorEntity gioLocatorEntity = new GioLocatorEntity(url);
        push(gioLocatorEntity);
    }

    private void startNodeElement(Attributes attributes) throws IOException {
        assertCurrent("startNode", GioGraphEntity.class);
        sendStartThisOrParentMaybe(GraphmlElement.NODE);
        GioNode.GioNodeBuilder builder = GioNode.builder();
        Map<String, String> customAttributes = new LinkedHashMap<>();
        int attributesLength = attributes.getLength();
        for (int i = 0; i < attributesLength; i++) {
            switch (attributes.getQName(i)) {
                case "id" -> builder.id(attributes.getValue(i));
                default -> customAttributes.put(attributes.getQName(i), attributes.getValue(i));
            }

        }
        push(new GioNodeEntity(builder.build()));
    }

    private void startPortElement(Attributes attributes) throws IOException {
        assertCurrent("startPort", GioNodeEntity.class, GioPortEntity.class);
        sendStartThisOrParentMaybe(GraphmlElement.PORT);
        GioPort.GioPortBuilder b = GioPort.builder();

        Map<String, String> customAttributes = new LinkedHashMap<>();
        for (int i = 0; i < attributes.getLength(); i++) {
            switch (attributes.getQName(i)) {
                case "name" -> b.name(attributes.getValue(i));
                default -> customAttributes.put(attributes.getQName(i), attributes.getValue(i));
            }
        }
        GioPort gioPort = b.build();
        GioPortEntity gioPortEntity = new GioPortEntity(gioPort);

        openEntities.push(gioPortEntity);
    }
}
