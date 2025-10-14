package com.calpano.graphinout.reader.graphml;

import com.calpano.graphinout.base.graphml.Graphml;
import com.calpano.graphinout.base.graphml.GraphmlDataType;
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
import com.calpano.graphinout.base.graphml.builder.GraphmlElementBuilder;
import com.calpano.graphinout.base.graphml.builder.GraphmlEndpointBuilder;
import com.calpano.graphinout.base.graphml.builder.GraphmlGraphBuilder;
import com.calpano.graphinout.base.graphml.builder.GraphmlHyperEdgeBuilder;
import com.calpano.graphinout.base.graphml.builder.GraphmlKeyBuilder;
import com.calpano.graphinout.base.graphml.builder.GraphmlLocatorBuilder;
import com.calpano.graphinout.base.graphml.builder.GraphmlNodeBuilder;
import com.calpano.graphinout.base.graphml.builder.GraphmlPortBuilder;
import com.calpano.graphinout.base.graphml.impl.GraphmlDescription;
import com.calpano.graphinout.base.graphml.impl.GraphmlEndpoint;
import com.calpano.graphinout.foundation.xml.CharactersKind;
import com.calpano.graphinout.foundation.xml.IXmlName;
import com.calpano.graphinout.foundation.xml.XML;
import com.calpano.graphinout.foundation.xml.XML.XmlSpace;
import com.calpano.graphinout.foundation.xml.Xml2DocumentFragmentWriter;
import com.calpano.graphinout.foundation.xml.XmlFragmentString;
import com.calpano.graphinout.foundation.xml.XmlWriter;
import com.calpano.graphinout.foundation.xml.element.XmlDocumentFragment;

import javax.annotation.Nullable;
import java.io.IOException;
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
import static com.calpano.graphinout.foundation.util.Nullables.ifPresentAccept;
import static com.calpano.graphinout.foundation.util.Nullables.mapOrNull;
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
    /** Buffers XML content for {#code <key><default>}, {@code <data>} and {#code <desc>} elements. */
    private final Xml2DocumentFragmentWriter xmlBuffer = new Xml2DocumentFragmentWriter();
    /** downstream writer */
    private final GraphmlWriter graphmlWriter;
    /** also required to detect the end of raw XML */
    private final XmlParseContext elementStack = new XmlParseContext();
    private boolean isBufferingXml = false;

    /**
     * @param graphmlWriter downstream
     */
    public Xml2GraphmlWriter(GraphmlWriter graphmlWriter) {
        this.graphmlWriter = graphmlWriter;
    }

    public void characters(String characters, CharactersKind kind) {
        if (isBufferingXml) {
            // Accumulate character data for elements that need it (like data, desc, default)
            xmlBuffer.characters(characters, kind);
        } else {
            assert characters.trim().isEmpty() : "Not buffering and got '" + characters + "'";
        }
    }

    public void charactersEnd() {
        if (isBufferingXml) {
            xmlBuffer.charactersEnd();
        } else {
            assert elementStack.isInterpretedAsGraphml();
        }
    }

    public void charactersStart() {
        if (isBufferingXml) {
            assert elementStack.isInterpretedAsGenericPCDATA();
            xmlBuffer.charactersStart();
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
    public void elementEnd(String uri, String localName, String qName) throws IOException {
        // even if we are buffering xml, we need to inspect this
        IXmlName xmlName = IXmlName.of(uri, localName, qName);

        if (isBufferingXml && !xmlName.localName().equals(DATA) && !xmlName.localName().equals(DESC) && !xmlName.localName().equals(DEFAULT)) {
            // keep on buffering
            xmlBuffer.elementEnd(xmlName);
            // remove corresponding context, which we must keep to identify the end of raw xml
            elementStack.pop(localName);
            // IMPROVE remove this check???
            XmlElementContext parent = elementStack.peekNullable();
            if (parent == null) {
                throw new IllegalStateException("No parent for element: " + localName);
            }
            return;
        }

        switch (localName) {
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
            default -> throw new IllegalStateException("Unknown element: " + localName);
        }
    }

    @Override
    public void elementStart(String uri, String localName, String qName, Map<String, String> attributes) throws IOException {
        if (isBufferingXml) {
            assert elementStack.isInterpretedAsGenericPCDATA();
            // push to stack and emit to rawXmlBuffer
            IXmlName xmlName = IXmlName.of(uri, localName, qName);
            elementStack.push(xmlName, attributes, true, null, XmlMode.GENERIC_PC_DATA);
            xmlBuffer.elementStart(xmlName, attributes);
            return;
        }

        switch (localName) {
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
            default -> throw new IllegalStateException("Unknown element: '" + localName + "'");
        }
    }

    @Override
    public void lineBreak() throws IOException {
        if (isBufferingXml) {
            xmlBuffer.lineBreak();
        }
        // Line breaks are typically ignored in GraphML processing
    }

    @Override
    public void raw(String rawXml) {
        if (isBufferingXml) {
            assert elementStack.isInterpretedAsGenericPCDATA();
            xmlBuffer.raw(rawXml);
        } else {
            throw new UnsupportedOperationException("no raw XML in GraphML");
        }
    }

    private @Nullable XmlFragmentString bufferedXml(XmlSpace xmlSpace) {
        return mapOrNull(xmlBuffer.resultFragment(xmlSpace), XmlDocumentFragment::toXmlFragmentString);
    }

    private void bufferingXmlEnd() {
        this.isBufferingXml = false;
        xmlBuffer.fragmentEnd();
    }

    private void bufferingXmlStart() {
        this.isBufferingXml = true;
        xmlBuffer.fragmentStart();
    }

    /**
     * Characters already in builder. Emitted stand-alone.
     */
    private void endDataElement() throws IOException {
        bufferingXmlEnd();
        XmlElementContext dataContext = elementStack.pop(GraphmlElements.DATA);
        XmlElementContext parent = elementStack.peek_(GRAPHML, GRAPH, NODE, EDGE, HYPER_EDGE, PORT);
        parent.maybeWriteStartTo(graphmlWriter);

        // apply xmlSpace from surrounding element
        XmlSpace xmlSpace = XmlSpace.fromAttributesValue(dataContext.xmlAttributes.get(XML.XML_SPACE));
        // do we need to report anything in <data> ?
        XmlFragmentString xmlFragmentString = bufferedXml(XmlSpace.default_);
        if (xmlSpace == XmlSpace.preserve || xmlFragmentString != null) {
            GraphmlDataBuilder builder = dataContext.dataBuilder();
            builder.xmlValue(xmlFragmentString);
            if (xmlSpace == XmlSpace.preserve) {
                builder.attribute(XML.XML_SPACE, xmlSpace.xmlAttValue);
            }
            IGraphmlData data = builder.build();
            try {
                graphmlWriter.data(data);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        // reset parse mode back to Graphml
        elementStack.mode(XmlMode.Graphml);
    }

    private void endDefaultElement() {
        bufferingXmlEnd();
        elementStack.pop(DEFAULT);

        GraphmlKeyBuilder keyBuilder = elementStack.peek_().keyBuilder();
        ifPresentAccept(bufferedXml(XmlSpace.default_), xmlFragmentString -> {
            IGraphmlDefault graphmlDefault = IGraphmlDefault.of(xmlFragmentString);
            keyBuilder.defaultValue(graphmlDefault);
        });

        // reset parse mode back to Graphml
        elementStack.mode(XmlMode.Graphml);
    }

    private void endDescElement() throws IOException {
        bufferingXmlEnd();
        XmlElementContext descContext = elementStack.pop(DESC);
        // Set description for the parent element
        XmlElementContext parent = elementStack.peek_();

        ifPresentAccept(bufferedXml(XmlSpace.default_), xmlFragmentString -> {
            GraphmlDescription desc = descContext.descBuilder().xmlValue(xmlFragmentString).build();
            switch (parent.xmlElementName.localName()) {
                case GRAPHML -> parent.documentBuilder().desc(desc);
                case GRAPH -> parent.graphBuilder().desc(desc);
                case NODE -> parent.nodeBuilder().desc(desc);
                case EDGE, HYPER_EDGE -> parent.hyperEdgeBuilder().desc(desc);
                case KEY -> parent.keyBuilder().desc(desc);
            }
        });

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
            GraphmlEndpoint graphmlEndpoint = context.endpointBuilder().build();
            parentContext.hyperEdgeBuilder().addEndpoint(graphmlEndpoint);
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

    // TODO use in elementStart / end to switch buffering xml
    private boolean isContentElement() {
        XmlElementContext context = elementStack.peekNullable();
        if (context == null) return false;
        return switch (context.xmlElementName.localName()) {
            case DATA, DESC, DEFAULT -> true;
            default -> false;
        };
    }

    private void startDataElement(Map<String, String> attributes) {
        GraphmlDataBuilder builder = IGraphmlData.builder();
        builder.attributes(attributes);
        ifAttributeNotNull(attributes, ATTRIBUTE_KEY, builder::key);

        elementStack.push(Graphml.xmlNameOf(GraphmlElements.DATA), attributes, false, builder, XmlMode.GENERIC_PC_DATA);
        bufferingXmlStart();
    }

    private void startDefaultElement(Map<String, String> attributes) {
        GraphmlDefaultBuilder builder = IGraphmlDefault.builder();
        builder.attributes(attributes);
        elementStack.push(Graphml.xmlNameOf(DEFAULT), attributes, false, builder, XmlMode.GENERIC_PC_DATA);
        bufferingXmlStart();
    }

    private void startDescElement(Map<String, String> attributes) {
        GraphmlElementBuilder<?> builder = IGraphmlDescription.builder().attributes(attributes);
        elementStack.push(Graphml.xmlNameOf(DESC), attributes, false, builder, XmlMode.GENERIC_PC_DATA);
        bufferingXmlStart();
    }

    private void startEdgeElement(Map<String, String> attributes) throws IOException {
        elementStack.peek_(GRAPH).maybeWriteStartTo(graphmlWriter);

        GraphmlHyperEdgeBuilder builder = IGraphmlHyperEdge.builder();
        builder.attributes(attributes);
        ifAttributeNotNull(attributes, ATTRIBUTE_ID, builder::id);

        // init from containing graph
        GraphmlGraphBuilder parentGraphElement = elementStack.findParentGraphElement();
        assert parentGraphElement != null : "Found no active parent graph element";
        IGraphmlGraph.EdgeDefault edgeDefault = requireNonNull(parentGraphElement).edgeDefault();
        boolean isDirected = IGraphmlGraph.EdgeDefault.isDirected(edgeDefault);

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

        elementStack.push(Graphml.xmlNameOf(EDGE), attributes, false, builder, XmlMode.Graphml);
        // dont start element, maybe <desc> or <data> comes
    }

    private void startEndpointElement(Map<String, String> attributes) {
        GraphmlEndpointBuilder builder = IGraphmlEndpoint.builder();
        builder.attributes(attributes);
        ifAttributeNotNull(attributes, ATTRIBUTE_ID, builder::id);
        ifAttributeNotNull(attributes, ATTRIBUTE_NODE, builder::node);
        ifAttributeNotNull(attributes, ATTRIBUTE_PORT, builder::port);
        ifAttributeNotNull(attributes, ATTRIBUTE_TYPE, value -> builder.type(GraphmlDirection.getDirection(value)));

        elementStack.push(Graphml.xmlNameOf(ENDPOINT), attributes, false, builder, XmlMode.Graphml);
        // dont start element, maybe <desc> or <data> comes
    }

    private void startGraphElement(Map<String, String> attributes) throws IOException {
        // elementStack.peek_(GRAPH, GRAPHML).maybeWriteStartTo(graphmlWriter);

        GraphmlGraphBuilder builder = IGraphmlGraph.builder();
        builder.attributes(attributes);
        ifAttributeNotNull(attributes, ATTRIBUTE_ID, builder::id);
        ifAttributeNotNull(attributes, ATTRIBUTE_EDGE_DEFAULT, value -> //
                builder.edgeDefault(IGraphmlGraph.EdgeDefault.valueOf(value)));

        elementStack.push(Graphml.xmlNameOf(GRAPH), attributes, false, builder, XmlMode.Graphml);
        // dont start element, maybe <desc> or <key> comes
    }

    private void startGraphmlElement(Map<String, String> attributes) {
        GraphmlDocumentBuilder builder = IGraphmlDocument.builder();
        builder.attributes(attributes);

        elementStack.push(Graphml.xmlNameOf(GRAPHML), attributes, false, builder, XmlMode.Graphml);
        // dont start element, maybe <desc> or <data> comes FIXME or <key>
    }

    private void startHyperEdgeElement(Map<String, String> attributes) throws IOException {
        elementStack.peek_(GRAPH).maybeWriteStartTo(graphmlWriter);

        GraphmlHyperEdgeBuilder builder = IGraphmlHyperEdge.builder();
        builder.attributes(attributes);
        ifAttributeNotNull(attributes, ATTRIBUTE_ID, builder::id);

        elementStack.push(Graphml.xmlNameOf(HYPER_EDGE), attributes, false, builder, XmlMode.Graphml);
        // dont start element, maybe <desc> or <data> comes, or wait for endpoints to be added
    }

    private void startKeyElement(Map<String, String> attributes) throws IOException {
        elementStack.peek_().maybeWriteStartTo(graphmlWriter);

        GraphmlKeyBuilder builder = IGraphmlKey.builder();
        builder.attributes(attributes);
        ifAttributeNotNull(attributes, ATTRIBUTE_ID, builder::id);
        ifAttributeNotNull(attributes, IGraphmlKey.ATTRIBUTE_FOR, value -> builder.forType(GraphmlKeyForType.keyForType(value)));
        ifAttributeNotNull(attributes, IGraphmlKey.ATTRIBUTE_ATTR_NAME, builder::attrName);
        ifAttributeNotNull(attributes, IGraphmlKey.ATTRIBUTE_ATTR_TYPE, str -> builder.attrType(GraphmlDataType.fromGraphmlName(str)));

        elementStack.push(Graphml.xmlNameOf(KEY), attributes, false, builder, XmlMode.Graphml);
        // dont start element, maybe <default> comes
    }

    private void startLocatorElement(Map<String, String> attributes) throws IOException {
        elementStack.peek_().maybeWriteStartTo(graphmlWriter);

        GraphmlLocatorBuilder builder = IGraphmlLocator.builder();
        builder.attributes(attributes);
        elementStack.push(Graphml.xmlNameOf(LOCATOR), attributes, false, builder, XmlMode.Graphml);
        // element is empty, but we wait for end
    }

    private void startNodeElement(Map<String, String> attributes) throws IOException {
        GraphmlNodeBuilder builder = IGraphmlNode.builder();
        builder.attributes(attributes);
        ifAttributeNotNull(attributes, ATTRIBUTE_ID, builder::id);

        elementStack.peek_(GRAPH).maybeWriteStartTo(graphmlWriter);

        elementStack.push(Graphml.xmlNameOf(NODE), attributes, false, builder, XmlMode.Graphml);
        // dont start element, maybe <desc> or <data> comes, or wait for endpoints to be added
    }

    private void startPortElement(Map<String, String> attributes) throws IOException {
        GraphmlPortBuilder builder = IGraphmlPort.builder();
        builder.attributes(attributes);
        ifAttributeNotNull(attributes, ATTRIBUTE_NAME, builder::name);

        XmlElementContext context = elementStack.push(Graphml.xmlNameOf(GraphmlElements.PORT), attributes, false, builder, XmlMode.Graphml);
        context.maybeWriteStartTo(graphmlWriter);
    }

}
