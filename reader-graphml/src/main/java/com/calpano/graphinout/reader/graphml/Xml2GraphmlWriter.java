package com.calpano.graphinout.reader.graphml;

import com.calpano.graphinout.base.graphml.GraphmlDirection;
import com.calpano.graphinout.base.graphml.GraphmlElements;
import com.calpano.graphinout.base.graphml.GraphmlKeyForType;
import com.calpano.graphinout.base.graphml.GraphmlWriter;
import com.calpano.graphinout.base.graphml.IGraphmlData;
import com.calpano.graphinout.base.graphml.IGraphmlDefault;
import com.calpano.graphinout.base.graphml.IGraphmlDescription;
import com.calpano.graphinout.base.graphml.IGraphmlDocument;
import com.calpano.graphinout.base.graphml.IGraphmlEdge;
import com.calpano.graphinout.base.graphml.IGraphmlEndpoint;
import com.calpano.graphinout.base.graphml.IGraphmlGraph;
import com.calpano.graphinout.base.graphml.IGraphmlHyperEdge;
import com.calpano.graphinout.base.graphml.IGraphmlKey;
import com.calpano.graphinout.base.graphml.IGraphmlLocator;
import com.calpano.graphinout.base.graphml.IGraphmlNode;
import com.calpano.graphinout.base.graphml.IGraphmlPort;
import com.calpano.graphinout.base.graphml.builder.GraphmlDataBuilder;
import com.calpano.graphinout.base.graphml.builder.GraphmlDefaultBuilder;
import com.calpano.graphinout.base.graphml.builder.GraphmlDocumentBuilder;
import com.calpano.graphinout.base.graphml.builder.GraphmlEndpointBuilder;
import com.calpano.graphinout.base.graphml.builder.GraphmlGraphBuilder;
import com.calpano.graphinout.base.graphml.builder.GraphmlHyperEdgeBuilder;
import com.calpano.graphinout.base.graphml.builder.GraphmlKeyBuilder;
import com.calpano.graphinout.base.graphml.builder.GraphmlLocatorBuilder;
import com.calpano.graphinout.base.graphml.builder.GraphmlNodeBuilder;
import com.calpano.graphinout.base.graphml.builder.GraphmlPortBuilder;
import com.calpano.graphinout.base.graphml.impl.GraphmlDescription;
import com.calpano.graphinout.foundation.xml.XmlWriter;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static com.calpano.graphinout.base.graphml.GraphmlElements.DATA;
import static com.calpano.graphinout.base.graphml.GraphmlElements.DEFAULT;
import static com.calpano.graphinout.base.graphml.GraphmlElements.DESC;
import static com.calpano.graphinout.base.graphml.GraphmlElements.EDGE;
import static com.calpano.graphinout.base.graphml.GraphmlElements.ENDPOINT;
import static com.calpano.graphinout.base.graphml.GraphmlElements.GRAPH;
import static com.calpano.graphinout.base.graphml.GraphmlElements.GRAPHML;
import static com.calpano.graphinout.base.graphml.GraphmlElements.HYPER_EDGE;
import static com.calpano.graphinout.base.graphml.GraphmlElements.KEY;
import static com.calpano.graphinout.base.graphml.GraphmlElements.LOCATOR;
import static com.calpano.graphinout.base.graphml.GraphmlElements.NODE;
import static com.calpano.graphinout.base.graphml.GraphmlElements.PORT;
import static com.calpano.graphinout.base.graphml.IGraphmlData.ATTRIBUTE_KEY;
import static com.calpano.graphinout.base.graphml.IGraphmlElementWithId.ATTRIBUTE_ID;
import static com.calpano.graphinout.base.graphml.IGraphmlEndpoint.ATTRIBUTE_NODE;
import static com.calpano.graphinout.base.graphml.IGraphmlEndpoint.ATTRIBUTE_PORT;
import static com.calpano.graphinout.base.graphml.IGraphmlEndpoint.ATTRIBUTE_TYPE;
import static com.calpano.graphinout.base.graphml.IGraphmlGraph.ATTRIBUTE_EDGE_DEFAULT;
import static com.calpano.graphinout.base.graphml.IGraphmlPort.ATTRIBUTE_NAME;
import static com.calpano.graphinout.foundation.xml.XmlTool.ifAttributeNotNull;
import static java.util.Objects.requireNonNull;

/**
 * Interprets incoming XML calls. Buffers them. Emits GraphML calls to downstream.
 * <p>
 * Concerns: Parsing GraphML XML vs. generic XML (as it occurs in KEY-DEFAULT or DATA elements). This is handled via
 * {@link XmlMode} and {@link XmlElementContext#isRawXml}.
 */
public class Xml2GraphmlWriter implements XmlWriter {

    private final Map<String, Map<GraphmlKeyForType, IGraphmlKey>> dataId_for_key = new HashMap<>();
    /** Buffers plain text and XML content */
    private final CharacterBuffer characterBuffer = new CharacterBuffer();

    /** downstream writer */
    private final GraphmlWriter graphmlWriter;
    private final XmlParseContext elementStack = new XmlParseContext();

    /**
     * @param graphmlWriter downstream
     */
    public Xml2GraphmlWriter(GraphmlWriter graphmlWriter) {
        this.graphmlWriter = graphmlWriter;
    }

    @Override
    public void cdataEnd() throws IOException {
        if (elementStack.isInterpretedAsPCDATA()) {
            characterBuffer.cdataEnd();
        } else {
            // TODO CDATA handling - treat as character data
            throw new UnsupportedOperationException("not implemented yet");
        }
    }

    @Override
    public void cdataStart() throws IOException {
        if (elementStack.isInterpretedAsPCDATA()) {
            characterBuffer.cdataStart();
        } else {
            throw new UnsupportedOperationException("not implemented yet");
        }
    }

    @Override
    public void characterData(String characterData, boolean isInCdata) throws IOException {
        if (elementStack.isInterpretedAsPCDATA()) {
            characterBuffer.characterData(characterData, false);
        } else if (isContentElement()) {
            // Accumulate character data for elements that need it (like data, desc, default)
            characterBuffer.append(characterData);
        }
    }

    public void characterDataEnd(boolean isInCdata) throws IOException {
        if (characterBuffer.isEmpty()) return;
        if (elementStack.isInterpretedAsGraphml()) {
            XmlElementContext context = elementStack.peek_();
            switch (context.xmlElementName) {
                case DATA -> context.dataBuilder().value(characterBuffer.getStringAndReset());
                case DESC -> context.descBuilder().value(characterBuffer.getStringAndReset());
                case DEFAULT -> context.defaultBuilder().value(characterBuffer.getStringAndReset());
                default -> throw new IllegalStateException("Unknown element: " + context.xmlElementName);
            }
        } else {
            characterBuffer.characterDataEnd(isInCdata);
        }
    }

    public void characterDataStart(boolean isInCdata) throws IOException {
        if (elementStack.isInterpretedAsPCDATA()) {
            characterBuffer.characterDataStart(isInCdata);
        }
    }

    @Override
    public void documentEnd() {
        // we write document end on </graphml>
    }

    @Override
    public void documentStart() {
        // Document will be started when we encounter the graphml element
    }

    @Override
    public void elementEnd(String name) throws IOException {
        switch (name) {
            case GRAPHML -> endGraphmlElement();
            case GRAPH -> endGraphElement();
            case NODE -> endNodeElement();
            case EDGE -> endEdgeElement();
            case HYPER_EDGE -> endHyperEdgeElement();
            case PORT -> endPortElement();
            case KEY -> endKeyElement();
            case DATA -> endDataElement();
            case DESC -> endDescElement();
            case DEFAULT -> endDefaultElement();
            case LOCATOR -> endLocatorElement();
            case ENDPOINT -> endEndpointElement();
            default -> {
                if (elementStack.isInterpretedAsGraphml()) {
                    throw new IllegalStateException("Unknown element: " + name);
                } else {
                    // treat as raw XML
                    characterBuffer.elementEnd(name);
                    // remove corresponding context
                    elementStack.pop(name);

                    XmlElementContext parent = elementStack.peekNullable();
                    if (parent != null) {
                        if (!parent.isRawXml && parent.xmlElementName.equals(DATA)) {
                            parent.dataBuilder().containsRawXml(true);
                        }
                    } else {
                        throw new IllegalStateException("No parent for element: " + name);
                    }
                }
            }
        }
    }

    @Override
    public void elementStart(String name, Map<String, String> attributes) throws IOException {
        switch (name) {
            case GRAPHML -> startGraphmlElement(attributes);
            case GRAPH -> startGraphElement(attributes);
            case NODE -> startNodeElement(attributes);
            case EDGE -> startEdgeElement(attributes);
            case HYPER_EDGE -> startHyperEdgeElement(attributes);
            case PORT -> startPortElement(attributes);
            case KEY -> startKeyElement(attributes);
            case DATA -> startDataElement(attributes);
            case DESC -> startDescElement(attributes);
            case DEFAULT -> startDefaultElement(attributes);
            case LOCATOR -> startLocatorElement(attributes);
            case ENDPOINT -> startEndpointElement(attributes);
            default -> {
                if (elementStack.isInterpretedAsPCDATA()) {
                    // push to stack and emit to rawXmlBuffer
                    elementStack.push(name, attributes, true, null, XmlMode.PCDATA);
                    characterBuffer.elementStart(name, attributes);
                } else {
                    throw new IllegalStateException("Unknown element: '" + name + "'");
                }
            }
        }
    }

    @Override
    public void lineBreak() {
        // Line breaks are typically ignored in GraphML processing
    }

    // FIXME use it
    public IGraphmlKey lookupKey(String id, GraphmlKeyForType forType) {
        Map<GraphmlKeyForType, IGraphmlKey> subMap = dataId_for_key.getOrDefault(id, Collections.emptyMap());
        return subMap.getOrDefault(
                // 1) look up exact forType
                forType,
                // 2) look up ALL forType
                subMap.get(GraphmlKeyForType.All));
    }

    @Override
    public void raw(String rawXml) throws IOException {
        if (elementStack.isInterpretedAsPCDATA()) {
            characterBuffer.raw(rawXml);
        } else {
            throw new UnsupportedOperationException("no raw XML in GraphML");
        }
    }

    /**
     * Characters already in builder. Emitted stand-alone.
     */
    private void endDataElement() throws IOException {
        XmlElementContext dataContext = elementStack.pop(GraphmlElements.DATA);
        XmlElementContext parent = elementStack.peek_(GRAPHML, GRAPH, NODE, EDGE, HYPER_EDGE, PORT);
        parent.maybeWriteStartTo(graphmlWriter);

        GraphmlDataBuilder builder = dataContext.dataBuilder();

        String dataValue = characterBuffer.getStringAndReset();
        builder.value(dataValue);

        IGraphmlData data = builder.build();
        graphmlWriter.data(data);

        // reset parse mode back to Graphml
        elementStack.mode(XmlMode.Graphml);
    }

    private void endDefaultElement() {
        elementStack.pop(DEFAULT);

        String defaultValue = characterBuffer.getStringAndReset();
        IGraphmlDefault value = IGraphmlDefault.builder().value(defaultValue).build();
        XmlElementContext parentContext = elementStack.peek_();
        GraphmlKeyBuilder builder = parentContext.keyBuilder();
        builder.defaultValue(value);
        elementStack.mode(XmlMode.Graphml);
    }

    private void endDescElement() throws IOException {
        elementStack.pop(DESC);
        // Set description for the parent element
        XmlElementContext parent = elementStack.peek_();
        String descValue = characterBuffer.getStringAndReset();
        GraphmlDescription desc = IGraphmlDescription.builder().value(descValue).build();
        switch (parent.xmlElementName) {
            case GRAPHML -> parent.documentBuilder().desc(desc);
            case GRAPH -> parent.graphBuilder().desc(desc);
            case NODE -> parent.nodeBuilder().desc(desc);
            case EDGE, HYPER_EDGE -> parent.hyperEdgeBuilder().desc(desc);
            case KEY -> parent.keyBuilder().desc(desc);
        }
        parent.maybeWriteStartTo(graphmlWriter);
        // reset parse mode back to Graphml
        elementStack.mode(XmlMode.Graphml);
    }

    private void endEdgeElement() throws IOException {
        XmlElementContext context = elementStack.pop(EDGE);
        context.writeEndTo(graphmlWriter);
    }

    private void endEndpointElement() {
        XmlElementContext context = elementStack.pop(GraphmlElements.ENDPOINT);
        // Add the endpoint to the parent hyperedge builder
        if (!elementStack.isEmpty() && context.endpointBuilder() != null) {
            XmlElementContext parentContext = elementStack.peek_();
            assert parentContext.hyperEdgeBuilder() != null;
            parentContext.hyperEdgeBuilder().addEndpoint(context.endpointBuilder().build());
        }
        // TODO else: parse warning
    }

    private void endGraphElement() throws IOException {
        elementStack.pop(GRAPH).writeEndTo(graphmlWriter);
    }

    private void endGraphmlElement() throws IOException {
        elementStack.pop(GRAPHML).writeEndTo(graphmlWriter);
    }

    private void endHyperEdgeElement() throws IOException {
        XmlElementContext context = elementStack.pop(GraphmlElements.HYPER_EDGE);
        context.writeEndTo(graphmlWriter);
    }

    private void endKeyElement() throws IOException {
        XmlElementContext context = elementStack.pop(GraphmlElements.KEY);
        XmlElementContext parentContext = elementStack.peek_();
        parentContext.maybeWriteStartTo(graphmlWriter);

        IGraphmlKey key = context.keyBuilder().build();
        context.maybeWriteStartTo(graphmlWriter);

        indexKey(key.id(), key.forType(), key);
    }

    private void endLocatorElement() {
        GraphmlLocatorBuilder locatorBuilder = elementStack.pop(GraphmlElements.LOCATOR).locatorBuilder();
        IGraphmlLocator locator = locatorBuilder.build();
        elementStack.peek_().builderWithLocatorSupport().locator(locator);
    }

    private void endNodeElement() throws IOException {
        XmlElementContext context = elementStack.pop(NODE);
        context.writeEndTo(graphmlWriter);
    }

    private void endPortElement() throws IOException {
        XmlElementContext context = elementStack.pop(PORT);
        context.writeEndTo(graphmlWriter);
    }


    private void indexKey(String id, GraphmlKeyForType forType, IGraphmlKey key) {
        dataId_for_key.computeIfAbsent(id, k -> new HashMap<>()).put(forType, key);
    }

    private boolean isContentElement() {
        XmlElementContext context = elementStack.peekNullable();
        if (context == null) return false;
        return switch (context.xmlElementName) {
            case DATA, DESC, DEFAULT -> true;
            default -> false;
        };
    }

    private void startDataElement(Map<String, String> attributes) {


        GraphmlDataBuilder builder = IGraphmlData.builder();
        builder.attributes(attributes);
        ifAttributeNotNull(attributes, ATTRIBUTE_KEY, builder::key);

        elementStack.push(DATA, attributes, false, builder, XmlMode.PCDATA);
    }

    private void startDefaultElement(Map<String, String> attributes) {
        GraphmlDefaultBuilder builder = IGraphmlDefault.builder();
        builder.attributes(attributes);
        elementStack.push(DEFAULT, attributes, false, builder, XmlMode.PCDATA);
    }

    private void startDescElement(Map<String, String> attributes) {
        elementStack.push(DESC, attributes, false, IGraphmlDescription.builder().attributes(attributes), XmlMode.PCDATA);
    }

    private void startEdgeElement(Map<String, String> attributes) throws IOException {
        elementStack.peek_(GRAPH).maybeWriteStartTo(graphmlWriter);

        GraphmlHyperEdgeBuilder builder = IGraphmlHyperEdge.builder();
        builder.attributes(attributes);
        ifAttributeNotNull(attributes, ATTRIBUTE_ID, builder::id);

        // init from containing graph
        boolean isDirected = IGraphmlGraph.EdgeDefault.isDirected(requireNonNull(elementStack.findParentGraphElement()).edgeDefault());

        // maybe overwrite from edge attributes
        @Nullable String b = attributes.get(IGraphmlEdge.ATTRIBUTE_DIRECTED);
        if (b != null) {
            isDirected = Boolean.parseBoolean(b);
        }

        GraphmlDirection sourceDir = isDirected ? GraphmlDirection.In : GraphmlDirection.Undirected;
        builder.addEndpoint(IGraphmlEndpoint.builder() //
                .node(attributes.get(IGraphmlEdge.ATTRIBUTE_SOURCE)) //
                .port(attributes.get(IGraphmlEdge.ATTRIBUTE_SOURCE_PORT)) //
                .type(sourceDir) //
                .build());
        GraphmlDirection targetDir = isDirected ? GraphmlDirection.Out : GraphmlDirection.Undirected;
        builder.addEndpoint(IGraphmlEndpoint.builder() //
                .node(attributes.get(IGraphmlEdge.ATTRIBUTE_TARGET)) //
                .port(attributes.get(IGraphmlEdge.ATTRIBUTE_TARGET_PORT)) //
                .type(targetDir) //
                .build());

        elementStack.push(EDGE, attributes, false, builder, XmlMode.Graphml);
        // dont start element, maybe <desc> or <data> comes
    }

    private void startEndpointElement(Map<String, String> attributes) {
        GraphmlEndpointBuilder builder = IGraphmlEndpoint.builder();
        builder.attributes(attributes);
        ifAttributeNotNull(attributes, ATTRIBUTE_ID, builder::id);
        ifAttributeNotNull(attributes, ATTRIBUTE_NODE, builder::node);
        ifAttributeNotNull(attributes, ATTRIBUTE_PORT, builder::port);
        ifAttributeNotNull(attributes, ATTRIBUTE_TYPE, value -> builder.type(GraphmlDirection.getDirection(value)));

        elementStack.push(ENDPOINT, attributes, false, builder, XmlMode.Graphml);
        // dont start element, maybe <desc> or <data> comes
    }

    private void startGraphElement(Map<String, String> attributes) throws IOException {
        // elementStack.peek_(GRAPH, GRAPHML).maybeWriteStartTo(graphmlWriter);

        GraphmlGraphBuilder builder = IGraphmlGraph.builder();
        builder.attributes(attributes);
        ifAttributeNotNull(attributes, ATTRIBUTE_ID, builder::id);
        ifAttributeNotNull(attributes, ATTRIBUTE_EDGE_DEFAULT, value -> //
                builder.edgeDefault(IGraphmlGraph.EdgeDefault.valueOf(value)));

        elementStack.push(GRAPH, attributes, false, builder, XmlMode.Graphml);
        // dont start element, maybe <desc> or <key> comes
    }

    private void startGraphmlElement(Map<String, String> attributes) {
        GraphmlDocumentBuilder builder = IGraphmlDocument.builder();
        builder.attributes(attributes);

        elementStack.push(GRAPHML, attributes, false, builder, XmlMode.Graphml);
        // dont start element, maybe <desc> or <data> comes FIXME or <key>
    }

    private void startHyperEdgeElement(Map<String, String> attributes) throws IOException {
        elementStack.peek_(GRAPH).maybeWriteStartTo(graphmlWriter);

        GraphmlHyperEdgeBuilder builder = IGraphmlHyperEdge.builder();
        builder.attributes(attributes);
        ifAttributeNotNull(attributes, ATTRIBUTE_ID, builder::id);

        elementStack.push(HYPER_EDGE, attributes, false, builder, XmlMode.Graphml);
        // dont start element, maybe <desc> or <data> comes, or wait for endpoints to be added
    }

    private void startKeyElement(Map<String, String> attributes) throws IOException {
        elementStack.peek_().maybeWriteStartTo(graphmlWriter);

        GraphmlKeyBuilder builder = IGraphmlKey.builder();
        builder.attributes(attributes);
        ifAttributeNotNull(attributes, ATTRIBUTE_ID, builder::id);
        ifAttributeNotNull(attributes, IGraphmlKey.ATTRIBUTE_FOR, value -> builder.forType(GraphmlKeyForType.keyForType(value)));
        ifAttributeNotNull(attributes, IGraphmlKey.ATTRIBUTE_ATTR_NAME, builder::attrName);
        ifAttributeNotNull(attributes, IGraphmlKey.ATTRIBUTE_ATTR_TYPE, builder::attrType);

        elementStack.push(KEY, attributes, false, builder, XmlMode.Graphml);
        // dont start element, maybe <default> comes
    }

    private void startLocatorElement(Map<String, String> attributes) throws IOException {
        elementStack.peek_().maybeWriteStartTo(graphmlWriter);

        GraphmlLocatorBuilder builder = IGraphmlLocator.builder();
        builder.attributes(attributes);
        elementStack.push(LOCATOR, attributes, false, builder, XmlMode.Graphml);
        // element is empty, but we wait for end
    }

    private void startNodeElement(Map<String, String> attributes) throws IOException {
        GraphmlNodeBuilder builder = IGraphmlNode.builder();
        builder.attributes(attributes);
        ifAttributeNotNull(attributes, ATTRIBUTE_ID, builder::id);

        elementStack.peek_(GRAPH).maybeWriteStartTo(graphmlWriter);

        elementStack.push(NODE, attributes, false, builder, XmlMode.Graphml);
        // dont start element, maybe <desc> or <data> comes, or wait for endpoints to be added
    }

    private void startPortElement(Map<String, String> attributes) throws IOException {
        GraphmlPortBuilder builder = IGraphmlPort.builder();
        builder.attributes(attributes);
        ifAttributeNotNull(attributes, ATTRIBUTE_NAME, builder::name);

        XmlElementContext context = elementStack.push(PORT, attributes, false, builder, XmlMode.Graphml);
        context.maybeWriteStartTo(graphmlWriter);
    }

}
